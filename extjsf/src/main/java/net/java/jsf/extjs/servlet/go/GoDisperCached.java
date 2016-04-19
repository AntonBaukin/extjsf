package net.java.jsf.extjs.servlet.go;

/* Java */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterTask;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Go-Dispatcher for one go-filter instance
 * that caches whether the page was successfully
 * dispatched the request.
 *
 * @author anton.baukin@gmail.com
 */
public class GoDisperCached extends GoDisperBase
{
	/* public: Go-Dispatcher interface */

	public String  isGoRequest(FilterTask task)
	{
		String res = super.isGoRequest(task);

		//?: {not a go-request}
		if(res == null) return null;

		//?: {not certain} lookup
		if(res.charAt(0) == '?')
		{
			String x = get(task);

			//?: {not known}
			if(x == null) return res;

			//?: {not that}
			if(x.isEmpty())
				return null;

			return x;
		}

		return res;
	}

	public boolean dispatch(GoDispatch request)
	{
		//~: do dispatch
		boolean res = super.dispatch(request);

		//~: cache
		put(request.task, (res)?(request.page):(null));

		return res;
	}


	/* public: Go-Dispatcher Cached (bean) interface */

	public int  getSize()
	{
		return size;
	}

	private int size = 101;

	public void setSize(int size)
	{
		EX.assertx(size > 0);
		this.size = size;
	}


	/* protected: caching */

	protected String get(FilterTask task)
	{
		String uri = task.getRequest().getRequestURI();
		Object   x = cache.get(uri);

		//?: {not found}
		if(x == null) return null;

		//?: {not that}
		if(Boolean.FALSE.equals(x))
			return "";

		//!: found
		return (String)x;
	}

	/**
	 * Maps request URI to the page string or false.
	 */
	protected final ConcurrentMap<String, Object> cache =
	  new ConcurrentHashMap<>();

	protected void   put(FilterTask task, String page)
	{
		String uri = task.getRequest().getRequestURI();

		//~: put into the cache
		cache.put(uri, (page != null)?(page):Boolean.FALSE);

		//?: {cache is too big} remove something
		if(cache.size() > getSize())
			cache.clear();
	}
}