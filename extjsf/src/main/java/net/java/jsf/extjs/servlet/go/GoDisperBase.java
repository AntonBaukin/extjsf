package net.java.jsf.extjs.servlet.go;

/* Java */

import java.io.File;

/* Java Servlet */

import javax.servlet.RequestDispatcher;

/* extjsf: config */

import net.java.jsf.extjs.SystemConfig;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterTask;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Implementation base for go-dispatchers.
 *
 * @author anton.baukin@gmail.com
 */
public class GoDisperBase implements GoDisper
{
	/* public: Go-Dispatcher interface */

	public String     isGoRequest(FilterTask task)
	{
		String page = task.getRequest().getRequestURI();

		//?: {not a go-request}
		if(!page.startsWith(getGoPrefix(task)))
			return null;

		return preparePage(page, task);
	}

	public boolean    dispatch(GoDispatch request)
	{
		//?: {the page doesn't exist}
		if(!isPageExists(request))
			return false;

		try
		{
			//~: create request dispatcher
			RequestDispatcher d = request.task.getRequest().
			  getRequestDispatcher(request.page);

			if(d == null)
				return false;

			//~: set cache options
			cacheControl(request.task);

			//!: forward
			d.forward(request.task.getRequest(),
			  request.task.getResponse());
		}
		catch(Throwable e)
		{
			throw EX.wrap(EX.xrt(e));
		}

		return true;
	}


	/* protected: dispatching support */

	protected String  getGoPrefix(FilterTask task)
	{
		if(goPrefix == null)
		{
			String p = EX.asserts(bean(SystemConfig.class).
			  getGoPagePrefix());

			if(!p.startsWith("/")) p  = "/" + p;
			if(!p.endsWith("/"))   p += "/";

			goPrefix = task.getRequest().getContextPath() + p;
		}

		return goPrefix;
	}

	private volatile String goPrefix;

	protected String  preparePage(String page, FilterTask task)
	{
		StringBuilder p = new StringBuilder(page.length() + 1);
		String        g = getGoPrefix(task);

		p.append('?').append(page);
		p.delete(1, (g.charAt(0) == '/')?(g.length()):(g.length() + 1));
		if(p.charAt(1) != '/') p.insert(1, '/');

		return p.toString();
	}

	protected boolean isPageExists(GoDispatch request)
	{
		//?: {the callee provided own strategy}
		if(request.exists != null)
			return request.exists.test(request);

		//~: get the path in the file system
		String path = request.task.getRequest().
		  getServletContext().getRealPath(request.page);
		if(path == null) return false;

		//~: check the file exists
		File   file = new File(path);
		return file.exists() && file.isFile();
	}

	protected void    cacheControl(FilterTask task)
	{
		if(!task.getResponse().containsHeader("Cache-Control"))
		{
			task.getResponse().addHeader("Cache-Control", "no-cache, max-age=0");
			task.getResponse().addHeader("Expires", "0");
		}
	}
}