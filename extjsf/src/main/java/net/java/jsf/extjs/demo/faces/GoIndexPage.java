package net.java.jsf.extjs.demo.faces;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: config */

import net.java.jsf.extjs.SystemConfig;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterTask;
import net.java.jsf.extjs.servlet.filters.PickFilter;
import net.java.jsf.extjs.servlet.go.GoPageFilterBase;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * This Go-filter forwards root requests
 * to '/go/index' (see web.xml) to default
 * welcome.xhtml page.
 *
 * In actual application this snippet may
 * be extended depending on the settings
 * and the user state.
 *
 *
 * @author anton.baukin@gmail.com.
 */
@Component @PickFilter(order = { 4950 })
public class GoIndexPage extends GoPageFilterBase
{
	/* protected: GoFilterBase interface */

	protected boolean isExactRequest(FilterTask task)
	{
		return isNoPage(task) || task.getRequest().
		  getRequestURI().equals(getGoIndexPage(task));
	}

	protected void    runExactMatch(FilterTask task)
	{
		//?: {empty path} redirect permanently
		if(isNoPage(task)) try
		{
			task.getResponse().sendRedirect(
			  task.getRequest().getContextPath() + "/go/index");

			task.doBreak();
			return;
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}

		runForward(task, "/welcome.xhtml");
	}

	protected boolean isNoPage(FilterTask task)
	{
		String  cp = task.getRequest().getContextPath();
		String uri = task.getRequest().getRequestURI();

		if(uri.endsWith("/"))
			uri = uri.substring(0, uri.length() - 1);

		return uri.equals(cp);
	}

	protected String  getGoIndexPage(FilterTask task)
	{
		if(goIndex == null)
		{
			String cp = task.getRequest().getContextPath();
			String go = bean(SystemConfig.class).getGoPagePrefix();

			if(!go.startsWith("/")) go  = "/" + go;
			if(!go.endsWith("/"))   go += "/";

			goIndex = cp + go + "index";
		}

		return goIndex;
	}

	protected volatile String goIndex;
}