package net.java.jsf.extjs.model.store;

/* Java */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;
import net.java.jsf.extjs.model.ModelsStore;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Service that stores UI Model Beans applying
 * in-memory caching and persistence strategies.
 *
 * @author anton.baukin@gmail.com.
 */
public class      ModelsStoreService
       implements ModelsStore, ModelsStoreAccess
{
	/* Models Store Service Singleton */

	public static ModelsStoreService getInstance()
	{
		return bean(ModelsStoreService.class);
	}

	protected ModelsStoreService()
	{
		modelsStore = new LinkedCacheModelsStore(100);
		modelsStore.setDelegate(createDelegate());
	}


	/* Models Store */

	public ModelBean   add(ModelBean bean)
	{
		//?: {has no key} invoke the generator
		if(bean.getModelKey() == null)
		{
			EX.assertn(keysGen);
			bean.setModelKey(keysGen.genModelKey(bean));
		}

		return modelsStore.add(bean);
	}

	public ModelBean   remove(String key)
	{
		return modelsStore.remove(key);
	}

	public ModelBean   read(String key)
	{
		return modelsStore.read(key);
	}


	/* Models Store Access */

	public ModelsStore accessStore()
	{
		return this;
	}


	/* Service */

	public void destroy()
	{
		//~: save all the entities
		if(!saveShutdown())
			saveShutdown(); //<-- try second time
	}


	/* Models Store Service (configuration) */

	public void setModelsStore(ModelsStoreBase modelsStore)
	{
		EX.assertx(modelsStore instanceof CachingModelsStore);
		this.modelsStore = modelsStore;
		modelsStore.setDelegate(createDelegate());
	}

	protected ModelsStoreBase modelsStore;

	public void setKeysGen(ModelKeysGen keysGen)
	{
		this.keysGen = keysGen;
	}

	protected ModelKeysGen keysGen;

	public void setBackend(ModelsBackend backend)
	{
		this.backend = EX.assertn(backend);
	}

	protected ModelsBackend backend;

	/**
	 * Persistent store cleanup timeout
	 * of old-stored records in minutes.
	 * Defaults to 4 hours.
	 */
	public void setSweepTimeout(int sweepTimeout)
	{
		EX.assertx(sweepTimeout > 0);
		this.sweepTimeout = sweepTimeout;
	}

	protected int sweepTimeout = 240;

	/**
	 * Delay to start synchronization of
	 * Model Beans pruned from the cache.
	 * In milliseconds, defaults to 8 seconds.
	 */
	public void setSynchDelay(int synchDelay)
	{
		EX.assertx(synchDelay > 0);
		this.synchDelay = synchDelay;
	}

	protected int synchDelay = 1000 * 8;


	/* protected: events processing */

	protected void    startupPlan()
	{
		ModelsStoreEvent e = new ModelsStoreEvent();

		//~: order to sweep
		e.setSweep(true);

		//~: delay [1; 11) seconds
		delay(e, 1000L + System.currentTimeMillis() % 10000L);

		LU.I(LU.cls(this), ": system is ready, planning periodical sweep task");
	}

	protected void    sweepDelay()
	{
		ModelsStoreEvent e = new ModelsStoreEvent();

		//~: order to sweep
		e.setSweep(true);

		//~: delay (in minutes ÷ 10)
		delay(e, 1000L * 60 / 10 * sweepTimeout);
	}

	protected void    synchDelay()
	{
		ModelsStoreEvent e = new ModelsStoreEvent();

		//~: order to synchronize
		e.setSynch(true);

		//~: delay
		delay(e, synchDelay);
	}

	protected void    synchAgain()
	{
		ModelsStoreEvent e = new ModelsStoreEvent();

		//~: order to synchronize
		e.setSynch(true);

		//~: minimal delay
		delay(e, 100L);
	}

	/**
	 * Replace this method with call to messaging
	 * subsystem instead of saving events as fields.
	 */
	protected void    delay(ModelsStoreEvent e, long dt)
	{
		ModelsStoreEvent  x;
		long             ts = System.currentTimeMillis();

		EX.assertx(dt >= 0L);
		e.setEventTime(ts + dt);

		//?: {execute sweep}
		x = sweepEvent; this.sweepEvent = null;
		if((x != null) && (x.getEventTime() >= ts))
			doExecute(x);

		//?: {execute synch}
		if(synchEvent != x)
		{
			x = synchEvent; this.synchEvent = null;
			if((x != null) && (x.getEventTime() >= ts))
				doExecute(x);
		}

		//?: {set execute sweep}
		if(e.isSweep())
			this.sweepEvent = e;

		//?: {set execute synch}
		if(e.isSynch())
			this.synchEvent = e;
	}

	/**
	 * Replace this fields with event queue.
	 */
	protected ModelsStoreEvent sweepEvent;
	protected ModelsStoreEvent synchEvent;

	protected void    doExecute(ModelsStoreEvent e)
	{
		//?: {sweep}
		if(e.isSweep())
			doSweep();

		//?: {synchronize}
		if(e.isSynch())
			doSynch();
	}

	protected void    doSynch()
	{
		//?: {was not able to lock}
		if(!synchLock.compareAndSet(false, true))
			return;

		try
		{
			Boolean x = savePruned();

			//?: {something is left to save}
			if(Boolean.FALSE.equals(x))
				synchAgain();
		}
		finally
		{
			synchLock.set(false);
		}
	}

	protected void    doSweep()
	{
		if(this.backend != null) try
		{
			LU.I(LU.cls(this), ": executing sweep of UI models persisted");

			//~: do database sweep
			this.backend.sweep(1000L * 60 * sweepTimeout);
		}
		finally
		{
			//~: plan the next sweep
			sweepDelay();
		}
	}

	/**
	 * Saves the pruned instances to the backend
	 * and returns true if all them were actual
	 * during the operation. Returns null when
	 * no pruned entities were found.
	 */
	protected Boolean savePruned()
	{
		Map<ModelEntry, Integer> items =
		  new HashMap<>(101);

		//~: collect the items removed from the cache
		((CachingModelsStore) modelsStore).copyPruned(items);

		//?: {nothing to save}
		if(items.isEmpty())
			return null;

		//~: synchronize them
		backend.store(items.keySet());

		//~: commit the results
		((CachingModelsStore) modelsStore).commitSaved(items, false);

		//?: {has nothing left}
		return items.isEmpty();
	}

	/**
	 * Lock is true when service currently
	 * synchronizes with the backend.
	 */
	protected final AtomicBoolean synchLock =
	  new AtomicBoolean();

	/**
	 * Saves all the entities from the Models Store
	 * and returns true if nothing is left to save.
	 */
	protected boolean saveShutdown()
	{
		Map<ModelEntry, Integer> items =
		  new HashMap<>(1001);

		//~: collect all the items of the store
		((CachingModelsStore) modelsStore).copyAll(items);

		LU.I(LU.cls(this), ": there are [", items.size(),
		  "] model beans to synchronize");

		//?: {nothing to save}
		if(items.isEmpty())
			return true;

		//~: synchronize them
		backend.store(items.keySet());

		//~: commit the results
		((CachingModelsStore) modelsStore).commitSaved(items, true);

		//?: {has nothing left}
		return items.isEmpty();
	}


	/* protected: Delegate */

	protected void delegateFind(ModelEntry e)
	{
		if(backend != null)
			backend.find(e);
	}

	protected void delegateFound(ModelEntry e)
	{
		//...
	}

	protected void delegateRemove(ModelEntry e)
	{
		if(backend != null)
			backend.remove(e);
	}

	protected void delegateSave(ModelEntry e)
	{
		//HINT: in present implementation we do not save the beans
		//  into the database at the moment they are created.

		if(backend == null) return;

		//?: {it is not just created}
		if(e.accessInc.get() != 0)
			backend.store(Collections.singleton(e));
	}

	protected void delegateCreate(ModelEntry e)
	{
		//...
	}

	protected void delegateOverflow(int size)
	{
		synchDelay();
	}

	protected CachingModelsStore.CachingDelegate createDelegate()
	{
		return new CachingModelsStore.CachingDelegate()
		{
			public void find(ModelEntry e)
			{
				delegateFind(e);
			}

			public void found(ModelEntry e)
			{
				delegateFound(e);
			}

			public void remove(ModelEntry e)
			{
				delegateRemove(e);
			}

			public void save(ModelEntry e)
			{
				delegateSave(e);
			}

			public void create(ModelEntry e)
			{
				delegateCreate(e);
			}

			public void overflow(int size)
			{
				delegateOverflow(size);
			}
		};
	}
}