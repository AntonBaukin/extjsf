package net.java.jsf.extjs.demo.faces;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterTask;
import net.java.jsf.extjs.servlet.filters.PickFilter;
import net.java.jsf.extjs.servlet.go.GoPageFilterBase;


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

	protected boolean isExactURI(String uri)
	{
		return uri.endsWith("/go/index");
	}

	protected void    runExactMatch(FilterTask task)
	{
		runForward(task, "/welcome.xhtml");
	}

	protected String  isGoRequest(FilterTask task)
	{
		return null;
	}

	protected boolean varForward(FilterTask task, String page)
	{
		return false;
	}
}