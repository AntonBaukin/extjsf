package net.java.jsf.extjs.faces.system;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterTask;
import net.java.jsf.extjs.servlet.filters.PickFilter;
import net.java.jsf.extjs.servlet.go.GoPageFilterBase;


/**
 * Forwards the request to xhtml page if such exists.
 * Tries to forward initial go-requests.
 *
 * @author anton.baukin@gmail.com
 */
@Component @PickFilter(order = { 5005 })
public class GoServerFaces extends GoPageFilterBase
{
	/* protected: GoFilterBase interface */

	protected boolean isExactURI(String uri)
	{
		return uri.endsWith(".xhtml");
	}

	protected boolean varForward(FilterTask task, String page)
	{
		return runForward(task, page + ".xhtml");
	}
}