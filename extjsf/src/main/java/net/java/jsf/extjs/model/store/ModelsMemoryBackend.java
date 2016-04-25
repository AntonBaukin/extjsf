package net.java.jsf.extjs.model.store;

/* Java */

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Models backend for test purposes.
 *
 * @author anton.baukin@gmail.com.
 */
public class ModelsMemoryBackend extends ModelsBackendBase
{
	/* Models Backend */

	public void find(ModelEntry e)
	{
		Save s; if((s = store.get(e.key)) != null)
		{
			e.bean = restore(s.bytes);
		}
	}

	public void store(Collection<ModelEntry> es)
	{
		for(ModelEntry e : es)
			store.put(e.key, new Save(e, store(e.bean)));
	}

	public void remove(ModelEntry e)
	{
		store.remove(e.key);
	}

	public void sweep(long backTime)
	{
		Iterator<Save> i = store.values().iterator();
		long           ts = System.currentTimeMillis() - backTime;

		while(i.hasNext())
			if(i.next().time < ts)
				i.remove();
	}

	protected final Map<String, Save> store =
	  new ConcurrentHashMap<>(101);

	/**
	 * Analogue of database record.
	 */
	public static class Save
	{
		long   time;
		byte[] bytes;

		public Save(ModelEntry e, byte[] bytes)
		{
			this.time  = System.currentTimeMillis();
			this.bytes = bytes;
		}
	}
}