package net.java.jsf.extjs.model;

/* Java Servlet */

import javax.servlet.http.Cookie;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.filters.FilterBase;
import net.java.jsf.extjs.servlet.filters.FilterTask;
import net.java.jsf.extjs.servlet.filters.PickFilter;
import static net.java.jsf.extjs.servlet.RequestPoint.request;
import static net.java.jsf.extjs.servlet.filters.FilterStage.FORWARD;

/* extjsf: faces */

import net.java.jsf.extjs.faces.Functions;
import net.java.jsf.extjs.faces.ModelViewBase;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.SU;


/**
 * Catches {@link NoModelException} and redirects the request
 * to the actual model if such is defined in the exception.
 *
 * WARNING! As HTTP specifies, only GET requests may be
 * redirected via 302 response! 307 response is not
 * supported transparently for a user.
 *
 *
 * @author anton.baukin@gmail.com
 */
@Component
@PickFilter(order = { 200 }, stage = { FORWARD })
public class NoModelFilter extends FilterBase
{
	/* public: Filter interface */

	public void closeFilter(FilterTask task)
	{
		NoModelException nomoe = EX.search(
		  task.getError(), NoModelException.class);

		//?: {got no model exception} do redirect
		if(nomoe != null) try
		{
			redirectOnNoModel(task, nomoe);
		}
		catch(Exception e)
		{
			task.setError(e);
		}
	}


	/* protected: no model redirecting */

	protected void   redirectOnNoModel(FilterTask task, NoModelException nomoe)
	  throws Exception
	{
		Throwable error = task.getError();

		//?: {can't send redirect}
		if((nomoe.getModel() == null) || task.getResponse().isCommitted())
			return;

		//?: {not a GET request} 302 redirect is forbidden!
		if(!"GET".equalsIgnoreCase(request(0).getMethod()))
			return;

		//~: do the redirect
		setResponseRedirect(task, nomoe);

		//!: clear the error
		if(task.getError() == error)
			task.setError(null);
	}

	protected void   setResponseRedirect(FilterTask task, NoModelException nomoe)
	  throws Exception
	{
		//HINT: we add session cookie manually to always be sure...
		Cookie cookie = new Cookie("JSESSIONID",
		  request(0).getSession().getId());

		cookie.setSecure(request(0).isSecure());
		cookie.setPath(request(0).getContextPath());
		task.getResponse().addCookie(cookie);


		//!: do send the redirect
		task.getResponse().sendRedirect(
		  task.getResponse().encodeURL(
		    createRedirectURL(task, nomoe)));
	}

	protected String createRedirectURL(FilterTask task, NoModelException nomoe)
	{
		String cp  = request(0).getContextPath();
		String uri = request(0).getRequestURI();
		String url = Functions.absoluteURL(uri.substring(cp.length()));
		String qs = SU.sXs(request(0).getQueryString());
		String mp  = ModelViewBase.MODEL_PARAM + '=';

		//~: add request URI as it is
		StringBuilder res = new StringBuilder(
		  url.length() + qs.length() + 64);
		res.append(url).append('?');

		//~: detect the model reference in the request
		int mi  = qs.indexOf(mp);

		//?: {has no model parameter in query string} add it
		if(mi == -1)
		{
			res.append(mp).append(nomoe.getModelKeys());
			if(qs.length() != 0)
				res.append('&').append(qs);
		}
		//!: replace the model parameter in the query string
		else
		{
			int ai  = qs.indexOf('&', mi);
			if(ai == -1) ai = qs.length();

			res.append(qs.substring(0, mi + mp.length()));
			res.append(nomoe.getModelKeys());
			res.append(qs.substring(ai));
		}

		return res.toString();
	}
}