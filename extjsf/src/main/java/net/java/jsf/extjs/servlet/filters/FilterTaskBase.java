package net.java.jsf.extjs.servlet.filters;

/* Java Servlet */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Basic properties of a {@link FilterTask}.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class FilterTaskBase implements FilterTask
{
	/* public: constructor */

	public FilterTaskBase(FilterStage stage)
	{
		this.stage = stage;
	}


	/* Filter Task */

	public FilterStage getFilterStage()
	{
		return stage;
	}

	protected final FilterStage stage;

	public Throwable getError()
	{
		return error;
	}

	protected Throwable error;

	public void setError(Throwable error)
	{
		if((this.error == null) || (error == null))
			this.error = error;
	}

	public boolean isBreaked()
	{
		return breaked;
	}

	private boolean breaked;

	public void doBreak()
	{
		this.breaked = true;
	}


	/* Filter Task (access the request) */

	public HttpServletRequest getRequest()
	{
		return request;
	}

	protected HttpServletRequest request;

	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

	protected HttpServletResponse response;

	public void setResponse(HttpServletResponse response)
	{
		this.response = response;
	}
}