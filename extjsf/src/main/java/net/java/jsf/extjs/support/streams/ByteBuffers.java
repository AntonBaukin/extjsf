package net.java.jsf.extjs.support.streams;

/* Java */

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Weak buffer of 512 bytes arrays.
 *
 * @author anton.baukin@gmail.com
 */
public final class ByteBuffers
{
	/* ByteBuffer Singleton */

	public static final ByteBuffers INSTANCE =
	  new ByteBuffers();


	/* public: ByteBuffer interface */

	public byte[] get()
	{
		byte[] res = null;

		synchronized(this)
		{
			List<byte[]> list = (pool == null)?(null):(pool.get());

			if((list != null) && !list.isEmpty())
				res = list.remove(list.size() - 1);
		}

		return (res != null)?(res):(new byte[512]);
	}


	public void   free(byte[] buf)
	{
		if(buf == null) throw new IllegalArgumentException();

		synchronized(this)
		{
			List<byte[]> list = (pool == null)?(null):(pool.get());

			if(list == null)
				pool = new WeakReference<>(list = new ArrayList<>(16));

			list.add(buf);
		}
	}

	public void   free(Collection<byte[]> bufs)
	{
		if(bufs == null) throw new IllegalArgumentException();
		for(byte[] buf : bufs)
			if(buf == null) throw new IllegalArgumentException();

		synchronized(this)
		{
			List<byte[]> list = (pool == null)?(null):(pool.get());

			if(list == null)
				pool = new WeakReference<>(list =
				  new ArrayList<>(bufs.size() + 16));

			list.addAll(bufs);
		}
	}


	/* private: the weak pool */

	private volatile WeakReference<List<byte[]>> pool;
}