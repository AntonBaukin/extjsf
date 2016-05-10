package net.java.jsf.extjs.model.store;

/* Java */

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/* Spring Framework */

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/* extjsf: models */

import net.java.jsf.extjs.model.ModelBeanBase;
import net.java.jsf.extjs.model.ModelsAccessPoint;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;
import net.java.jsf.extjs.support.SpringPoint;


/**
 * Tests saving, restoring the models
 * in the memory store.
 *
 * @author anton.baukin@gmail.com.
 */
public class TestModelsStore
{
	/**
	 * Configured in test 'spring/models.xml' file.
	 */
	public static final int CACHE = 10;

	@org.junit.Test
	public void testAddReadCache()
	{
		Set<Integer> ids = new LinkedHashSet<>();

		mss.start();

		//~: put all the models in the cache
		for(int id = 1;(id <= CACHE);id++)
		{
			//~: create & save
			Model m = model();
			mss.add(m);

			EX.assertx(id == m.id);
			EX.asserts(m.getModelKey());
			ids.add(id);
		}

		EX.assertx(models.size() == CACHE);

		//~: test the direct instances
		for(Model x : models.values())
		{
			Model m = (Model) mss.read(x.getModelKey());
			EX.assertn(m);
			EX.assertx(m == x);
		}

		//~: prune the first half of the cache
		for(int id = 1 + CACHE;(id <= 3*CACHE/2);id++)
		{
			//~: create & save
			Model m = model();
			mss.add(m);

			EX.assertx(id == m.id);
			EX.asserts(m.getModelKey());
			ids.add(id);

			//~: persist pruned model
			sleep(2);
			mss.runEvents();

			//~: check the pruned index
			Model x = (Model) mss.getLastSaved();
			EX.assertn(x);
			EX.assertx(x.id == id - CACHE);
			EX.assertx(x == models.get(x.id));
			ids.remove(x.id);
		}

		//~: check currently cached models
		for(int id : ids)
		{
			Model x = models.get(id);
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);
			EX.assertx(m == x);
		}

		//~: restore back the models
		for(int i = 1;(i <= CACHE/2);i++)
		{
			Model x = models.get(i);
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);

			//?: {same data in a new model instance}
			EX.assertx(x != m);
			EX.assertx(x.equals(m));
			models.put(m.id, m);

			//~: persist pruned model
			sleep(2);
			mss.runEvents();

			//~: check the pruned index
			x = (Model) mss.getLastSaved();
			EX.assertn(x);
			EX.assertx(x.id == i + CACHE/2);
			EX.assertx(x == models.get(x.id));
			EX.assertx(x.id == ids.iterator().next());
			ids.remove(x.id);
		}

		//~: check currently cached models
		for(int id : ids)
		{
			Model x = models.get(id);
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);
			EX.assertx(m == x);
		}

		mss.destroy();
	}

	@org.junit.Test
	public void testShutdown()
	  throws Exception
	{
		final int N = CACHE + 1 +
		  (int)(System.currentTimeMillis() % 1000);

		//=: temporary file of the backend
		File file = File.createTempFile("models.", ".store");
		mss.setBackendFile(file.getAbsolutePath());

		//~: start the store
		mss.start();

		//~: save all the models
		for(int id = 1;(id <= N);id++)
		{
			//~: create & save
			Model m = model();
			mss.add(m);

			EX.assertx(id == m.id);
			EX.asserts(m.getModelKey());
		}

		//~: destroy the store
		mss.destroy();

		//?: {file written}
		EX.assertx(file.length() > 64);

		//~: restart the store
		mss.start();

		//~: check all the models were saved
		for(int id = 1;(id <= N);id++)
		{
			Model x = EX.assertn(models.get(id));
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);
			EX.assertx(x.equals(m));
			EX.assertx(x != m);
		}

		//~: destroy & remove the test file
		mss.destroy();
		EX.assertx(file.delete());
	}

	@org.junit.Test
	public void testSweep()
	{
		final int N = CACHE + 1 +
		  (int)(System.currentTimeMillis() % 100);

		//=: 1-second sweep timeout
		mss.setSweepTimeout(1000);

		//~: start the store
		mss.start();

		//~: save all the models
		LU.I(LU.cls(this), "testing sweep with ", N, " items");
		for(int id = 1;(id <= N);id++)
		{
			//~: create & save
			Model m = model();
			mss.add(m);

			EX.assertx(id == m.id);
			EX.asserts(m.getModelKey());
		}

		//~: prune [1, N] from the cache
		for(int i = 1;(i <= CACHE);i++)
			mss.add(model());
		mss.synchNow();

		//~: wait half the sweep time
		sleep(500);

		//~: touch all even items
		LU.I(LU.cls(this), "touched at ", java.time.Instant.now());
		for(int id = 2;(id <= N);id += 2)
		{
			Model x = EX.assertn(models.get(id));
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);
			EX.assertx(x.equals(m));
			EX.assertx(x != m);
		}

		//~: prune [1, N] from the cache
		for(int i = 1;(i <= CACHE);i++)
			mss.add(model());
		mss.synchNow();

		//~: wait second half to make odd items become obsolete
		sleep(510);

		//~: run sweep
		mss.runEvents();

		//~: manual check
		//mss.dumpBackend();

		//~: check even are still there
		for(int id = 2;(id <= N);id += 2)
		{
			Model x = EX.assertn(models.get(id));
			Model m = (Model) mss.read(x.getModelKey());

			EX.assertn(m);
			EX.assertx(x.equals(m));
		}

		//~: check odd are permanently removed
		for(int id = 1;(id <= N);id += 2)
		{
			Model x = EX.assertn(models.get(id));
			Model m = (Model) mss.read(x.getModelKey());
			EX.assertx(m == null);
		}
	}

	/**
	 * Service (bean) to test.
	 */
	private ModelsServiceMock mss;

	private void sleep(long d)
	{
		try
		{
			Thread.sleep(d);
		}
		catch(InterruptedException e)
		{
			throw EX.wrap(e);
		}
	}


	/* Test Model Bean */

	public static class Model extends ModelBeanBase
	{
		public int id;


		/* Object */

		public boolean equals(Object o)
		{
			if(o == this) return true;
			if(!(o instanceof Model)) return false;

			String k = this.getModelKey();
			Model  m = (Model) o;

			return (id != 0) && (id == m.id) &&
			  (k != null) && k.equals(m.getModelKey());
		}


		/* Serialization */

		public void writeExternal(ObjectOutput o)
		  throws IOException
		{
			super.writeExternal(o);
			o.writeInt(id);
		}

		public void readExternal(ObjectInput i)
		  throws IOException, ClassNotFoundException
		{
			super.readExternal(i);
			id = i.readInt();
		}


		/* Object */

		public String toString()
		{
			return String.format("Model %3d", id);
		}
	}

	/**
	 * All models created during a test.
	 */
	private Map<Integer, Model> models = new HashMap<>();

	/**
	 * Test-unique index of model.
	 */
	private int mid = 1;

	private Model model()
	{
		Model m = new Model();

		m.id = mid++;
		models.put(m.id, m);

		return m;
	}


	/* Spring Activation */

	@org.junit.Before
	public void startSpring()
	{
		//~: create test Spring context
		SpringPoint.getInstance().setTestContext(
		  new ClassPathXmlApplicationContext("applicationContext.xml"));

		//~: models store service
		this.mss = EX.assertn((ModelsServiceMock)
		  ModelsAccessPoint.getInstance().getModelAccess());
	}

	@org.junit.After
	public void stopSpring()
	{
		ConfigurableApplicationContext ctx =
		  (ConfigurableApplicationContext)
		    SpringPoint.getInstance().getSpringContext();

		//~: close the context
		SpringPoint.getInstance().setTestContext(null);
		ctx.close();
	}
}