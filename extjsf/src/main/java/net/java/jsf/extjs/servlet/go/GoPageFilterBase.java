package net.java.jsf.extjs.servlet.go;

/* extjsf: filters */

import net.java.jsf.extjs.servlet.filters.FilterBase;
import net.java.jsf.extjs.servlet.filters.FilterTask;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Implementation base for go-filters that redirect
 * to the real pages on the server having the same
 * name base, but different suffices (or prefixes).
 *
 * @author anton.baukin@gmail.com
 */
public abstract class GoPageFilterBase extends FilterBase
{
	/* public: Filter interface */

	public void openFilter(FilterTask task)
	{
		//?: {this is exact include-forward}
		if(isExactRequest(task))
		{
			runExactMatch(task);
			return;
		}

		//?: {this is not a go-request}
		String page = isGoRequest(task);
		if(page == null) return;

		//~: try forward
		tryForward(task, page);
	}


	/* public: GoFilterBase (bean) interface */

	public GoDisper getDisper()
	{
		return disper;
	}

	private GoDisper disper = new GoDisperCached();

	public void setDisper(GoDisper disper)
	{
		this.disper = EX.assertn(disper);
	}


	/* protected: request processing */

	protected boolean isExactRequest(FilterTask task)
	{
		//?: {name matches exactly}
		return isExactURI(task.getRequest().getRequestURI());
	}

	/**
	 * Tells that the URI given matches the target
	 * servlet (faces, pages, etc.) exactly.
	 */
	protected boolean isExactURI(String uri)
	{
		return false;
	}

	/**
	 * See {@link GoDisper#isGoRequest(FilterTask)}
	 * for the result format.
	 */
	protected String  isGoRequest(FilterTask task)
	{
		return getDisper().isGoRequest(task);
	}

	/**
	 * Variates forwarding of not-certain requests.
	 * See {@link #tryForward(FilterTask, String)}.
	 *
	 * Note that here page name doesn't start with '?'.
	 */
	protected boolean varForward(FilterTask task, String page)
	{
		return false;
	}

	/**
	 * Attempts to forward the page given changing
	 * it's name when not certain (starts with '?').
	 * Returns true when actually forwarded.
	 */
	protected boolean tryForward(FilterTask task, String page)
	{
		//?: {got certain page name}
		if(page.charAt(0) != '?')
		{
			if(!runForward(task, page))
				throw new IllegalStateException();

			return true;
		}

		//~: variate the forwarding
		return varForward(task, page.substring(1));
	}

	/**
	 * Tries to forward the page given returning
	 * true on the success. Implementation may
	 * try to forward several pages for no-certain
	 * {@link #isGoRequest(FilterTask)}}.
	 */
	protected boolean runForward(FilterTask task, String page)
	{
		//~: prepare the dispatching request
		GoDispatch request = new GoDispatch(task, page);
		initGoDispatch(request);

		//?: {dispatcher it} break the filter cycle
		if(getDisper().dispatch(request))
		{
			//~: finish go-filtering
			task.doBreak();

			return true;
		}

		return false;
	}

	protected void    runExactMatch(FilterTask task)
	{}

	protected void    initGoDispatch(GoDispatch request)
	{}
}