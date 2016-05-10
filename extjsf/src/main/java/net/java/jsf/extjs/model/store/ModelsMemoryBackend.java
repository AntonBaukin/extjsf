package net.java.jsf.extjs.model.store;

/* Java */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.IO;
import net.java.jsf.extjs.support.LU;
import net.java.jsf.extjs.support.SU;


/**
 * Models backend for test purposes.
 *
 * @author anton.baukin@gmail.com.
 */
public class      ModelsMemoryBackend
       extends    ModelsBackendBase
       implements Externalizable, Runnable, AutoCloseable
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
		{
			e.accessTime = System.currentTimeMillis();
			store.put(e.key, new Save(e, store(e.bean)));
		}
	}

	public void remove(ModelEntry e)
	{
		store.remove(e.key);
	}

	public void sweep(long backTime)
	{
		final long  ts = System.currentTimeMillis() - backTime;
		Set<String> rm = new HashSet<>();

		//~: collect items to remove
		for(Map.Entry<String, Save> e : store.entrySet())
			if(e.getValue().time < ts)
				rm.add(e.getKey());

		LU.I(LU.cls(this), "sweeped [", rm.size(),
		  "] memory entries of [", store.size(), "]");
		store.keySet().removeAll(rm);
	}

	protected final Map<String, Save> store =
	  new ConcurrentHashMap<>(101);


	/* Serialization */

	public void writeExternal(ObjectOutput o)
	  throws IOException
	{
		this.writeExternal(o, null);
	}

	public void writeExternal(ObjectOutput o, Map<String, Save> saved)
	  throws IOException
	{
		for(Map.Entry<String, Save> e : store.entrySet())
		{
			o.writeByte(0x5A);
			IO.str(o, e.getKey());
			e.getValue().writeExternal(o);

			if(saved != null)
				saved.put(e.getKey(), e.getValue());
		}

		o.writeByte(0x69);
	}

	public void readExternal(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		while(true)
		{
			int b = i.readByte();

			if(b == 0x69)
				break;

			EX.assertx(b == 0x5A);

			String k = IO.str(i);
			Save   s = new Save();
			s.readExternal(i);

			store.put(k, s);
		}
	}


	/* File Persistence */

	public void close()
	{
		if(SU.sXe(file))
			return;

		Map<String, Save> saved = new HashMap<>(store.size());

		try(ObjectOutputStream o = new ObjectOutputStream(
		  new BufferedOutputStream(new FileOutputStream(file))))
		{
			//~: write the store
			this.writeExternal(o, saved);

			//~: and clear it
			this.store.keySet().removeAll(saved.keySet());
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}

	public void run()
	{
		if(SU.sXe(file))
			return;

		File f = new File(file);
		if(!f.exists() || (f.length() == 0L)) return;
		EX.assertx(f.isFile() && f.canRead());

		try(ObjectInputStream o = new ObjectInputStream(
		  new BufferedInputStream(new FileInputStream(f))))
		{
			this.readExternal(o);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}

	public void setFile(String file)
	{
		this.file = file;
	}

	protected String file;


	/* Database Record */

	/**
	 * Analogue of database record.
	 */
	public static class Save implements Externalizable
	{
		long   time;
		byte[] bytes;

		public Save()
		{}

		public Save(ModelEntry e, byte[] bytes)
		{
			this.time  = e.accessTime;
			this.bytes = bytes;
		}


		/* Serialization */

		public void writeExternal(ObjectOutput o)
		  throws IOException
		{
			o.writeLong(time);
			IO.bytes(o, bytes);
		}

		public void readExternal(ObjectInput i)
		  throws IOException, ClassNotFoundException
		{
			time  = i.readLong();
			bytes = IO.bytes(i);
		}
	}
}