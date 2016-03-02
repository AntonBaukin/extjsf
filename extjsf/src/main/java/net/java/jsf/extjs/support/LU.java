package net.java.jsf.extjs.support;

/* Java */

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* Logging for Java */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Logging support, utilities and wrappers.
 *
 * @author anton.baukin@gmail.com.
 */
public final class LU
{
	/* public: log wrappers */

	public static void D(String dest, Object... msgs)
	{
		dispatch(0, dest, null, SU.cats(msgs));
	}

	public static void D(String dest, Throwable err, Object... msgs)
	{
		dispatch(0, dest, err, SU.cats(msgs));
	}

	public static void I(String dest, Object... msgs)
	{
		dispatch(1, dest, null, SU.cats(msgs));
	}

	public static void I(String dest, Throwable err, Object... msgs)
	{
		dispatch(1, dest, err, SU.cats(msgs));
	}

	public static void W(String dest, Object... msgs)
	{
		dispatch(2, dest, null, SU.cats(msgs));
	}

	public static void W(String dest, Throwable err, Object... msgs)
	{
		dispatch(2, dest, err, SU.cats(msgs));
	}

	public static void E(String dest, Object... msgs)
	{
		dispatch(3, dest, null, SU.cats(msgs));
	}

	public static void E(String dest, Throwable err, Object... msgs)
	{
		dispatch(3, dest, err, SU.cats(msgs));
	}


	/* public: logging support */

	public static String sig(Object obj)
	{
		return (obj == null)?("null"):SU.cats(
		  obj.getClass().getSimpleName(), '@',
		  Integer.toUnsignedString(System.identityHashCode(obj), 16)
		);
	}

	public static String cls(Object obj)
	{
		return cls((obj == null)?(null):(obj.getClass()));
	}

	public static String cls(Object obj, Class def)
	{
		return cls((obj == null)?(def):(obj.getClass()));
	}

	public static String cls(Class cls)
	{
		return (cls == null)?("undefined"):(cls.getName());
	}


	/* private: logging system */

	private static void dispatch(int level, String dest, Throwable err, String msg)
	{
		Logger log = (dest == null)?(null):(CACHE.get(dest));

		if(log != null) switch(level)
		{
			case 0: //<-- debug
			{
				if(err == null)
					log.debug(msg);
				else
					log.debug(msg, err);
				break;
			}

			case 1: //<-- info
			{
				if(err == null)
					log.info(msg);
				else
					log.info(msg, err);
				break;
			}

			case 2: //<-- warn
			{
				if(err == null)
					log.warn(msg);
				else
					log.warn(msg, err);
				break;
			}

			case 3: //<-- error
			{
				if(err == null)
					log.error(msg);
				else
					log.error(msg, err);
				break;
			}
		}
	}

	private static final LoggersCache CACHE =
	  new LoggersCache();

	private static class LoggersCache
	{
		/* public: constructor */

		public LoggersCache()
		{
			ReentrantReadWriteLock x =
			  new ReentrantReadWriteLock();

			this.readLock  = x.readLock();
			this.writeLock = x.writeLock();
		}


		/* public: LoggersCache interface */

		public Logger get(String dest)
		{
			Logger result = null;

			//~: optimistic: all exists
			try //<-- shared lock section
			{
				readLock.lock();

				if(cache != null)
				{
					Map<String, Logger> map = cache.get();

					if(map != null)
						result = map.get(dest);
				}
			}
			finally
			{
				readLock.unlock();
			}

			//?: {not found} do it in a harder way
			if(result == null) try //<-- exclusive lock section
			{
				writeLock.lock();

				//~: access the weak mapping
				Map<String, Logger> map = (cache == null)?(null):(cache.get());
				if(map == null) cache = new SoftReference<>(map = new HashMap<>(17));

				//~: get the logger
				result = map.get(dest);

				//?: {not found it} create it now
				if((result == null) && !map.containsKey(dest))
					map.put(dest, result = LogManager.getLogger(dest));
			}
			finally
			{
				writeLock.unlock();
			}

			return result;
		}


		/* the locks */

		private final Lock readLock;
		private final Lock writeLock;


		/* loggers cache */

		private SoftReference<Map<String, Logger>> cache;
	}
}