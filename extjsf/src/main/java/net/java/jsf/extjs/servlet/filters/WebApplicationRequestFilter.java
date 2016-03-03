package net.java.jsf.extjs.servlet.filters;

/* Sprint Framework */

import org.springframework.stereotype.Component;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.RequestPoint;


/**
 * Binds the response with Servlet related components.
 * Related to {@code WebApplicationRequestListener}.
 * The first Filter invoked.
 *
 * @author anton.baukin@gmail.com
 */
@Component
@PickFilter(order = { 0 })
public class WebApplicationRequestFilter extends FilterBase
{
	/* public: Filter interface */

	public void openFilter(FilterTask task)
	{
		//?: {there are no requests in the stack}
		if(RequestPoint.requests() == 0)
			initRequest();

		RequestPoint.getInstance().setRequest(task.getRequest());
		RequestPoint.getInstance().setResponse(task.getResponse());
	}

	public void closeFilter(FilterTask task)
	{
		RequestPoint.getInstance().setResponse(null);
		RequestPoint.getInstance().setRequest(null);

		//?: {there are no requests in the stack}
		if(RequestPoint.requests() == 0)
			freeRequest();
	}


	/* protected: request initialization */

	protected void initRequest()
	{
		freeRequest();
	}

	protected void freeRequest()
	{}
}