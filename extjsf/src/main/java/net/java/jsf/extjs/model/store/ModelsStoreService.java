package net.java.jsf.extjs.model.store;

/* Java */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;
import net.java.jsf.extjs.model.ModelsStore;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;


/**
 * Service that stores UI Model Beans applying
 * in-memory caching and persistence strategies.
 *
 * @author anton.baukin@gmail.com.
 */
public class      ModelsStoreService
       implements ModelsStore, ModelsStoreAccess
{
	public ModelsStoreService()
	{
		modelsStore = new LinkedCacheModelsStore(101);
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

	public void start()
	{
		EX.assertn(this.backend);

		//?: {activate the backend}
		if(this.backend instanceof Runnable)
			((Runnable)this.backend).run();

		//~: plan initial events
		startupPlan();
	}

	public void destroy()
	{
		//~: save all the entities
		if(!saveShutdown())
			saveShutdown(); //<-- try second time

		//?: {close the backend}
		if(this.backend instanceof AutoCloseable) try
		{
			((AutoCloseable) this.backend).close();
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
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
	 * of old-stored records in milliseconds.
	 * Defaults to 4 hours.
	 */
	public void setSweepTimeout(long sweepTimeout)
	{
		EX.assertx(sweepTimeout > 0);
		this.sweepTimeout = sweepTimeout;
	}

	protected long sweepTimeout = 240 * 60 * 1000L;

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
		delay(e, startSweepDelay());
	}

	/**
	 * Delay [1; 11) seconds of start sweep.
	 */
	protected long    startSweepDelay()
	{
		return 1000L + System.currentTimeMillis() % 10000L;
	}

	protected void    sweepDelay()
	{
		ModelsStoreEvent e = new ModelsStoreEvent();

		//~: order to sweep
		e.setSweep(true);

		//~: delay (in minutes ÷ 10)
		delay(e, sweepTimeout);
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
	 * Runs events if they are ready. This method is
	 * not required if a Messaging Subsystem works.
	 */
	protected void    runEvents()
	{
		ModelsStoreEvent  x;
		final long       ts = System.currentTimeMillis();

		//?: {execute sweep}
		x = sweepEvent.getAndSet(null);
		if(x != null)
			if(x.getEventTime() > ts)
				sweepEvent.compareAndSet(null, x);
			else
			{
				//HINT: not to call twice
				synchEvent.compareAndSet(x, null);

				doExecute(x);
			}

		//?: {execute synch}
		x = synchEvent.getAndSet(null);
		if(x != null)
			if(x.getEventTime() > ts)
				synchEvent.compareAndSet(null, x);
			else
				doExecute(x);
	}

	/**
	 * Replace this method with call to Messaging
	 * Subsystem instead of saving events as fields.
	 */
	protected void    delay(ModelsStoreEvent e, long dt)
	{
		ModelsStoreEvent x;

		//=: the time of the event
		EX.assertx(dt >= 0L);
		e.setEventTime(System.currentTimeMillis() + dt);

		if(e.isSweep())
		{
			x = sweepEvent.getAndSet(e);
			LU.I(LU.cls(this), "planned sweep at ",
			  new java.util.Date(e.getEventTime()).toInstant());

			//?: {current sweep is closer}
			if((x != null) && x.before(e))
				sweepEvent.compareAndSet(e, x);
		}

		if(e.isSynch())
		{
			x = synchEvent.getAndSet(e);

			//?: {current synch is closer}
			if((x != null) && x.before(e))
				synchEvent.compareAndSet(e, x);
		}
	}

	/**
	 * Replace this fields with event queue.
	 */
	protected final AtomicReference<ModelsStoreEvent>
	  sweepEvent = new AtomicReference<>();

	protected final AtomicReference<ModelsStoreEvent>
	  synchEvent = new AtomicReference<>();

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
		try
		{
			LU.I(LU.cls(this), "executing sweep of UI models persisted");

			//~: do database sweep
			this.backend.sweep(sweepTimeout);
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

		LU.I(LU.cls(this), "there are [", items.size(),
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
		backend.find(e);

		runEvents();
	}

	protected void delegateFound(ModelEntry e)
	{
		//...
	}

	protected void delegateRemove(ModelEntry e)
	{
		backend.remove(e);

		runEvents();
	}

	protected void delegateSave(ModelEntry e)
	{
		//HINT: in present implementation we do not save the beans
		//  into the database at the moment they are created.

		//?: {it is not just created}
		if(e.accessInc.get() != 0)
			backend.store(Collections.singleton(e));

		runEvents();
	}

	protected void delegateCreate(ModelEntry e)
	{
		//...
	}

	protected void delegateOverflow(int size)
	{
		synchDelay();
		runEvents();
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