package net.java.jsf.extjs.servlet;

/* Java */

import java.util.Locale;

/* Java Servlet */

import javax.servlet.http.HttpServletResponse;


/**
 * Provides access to a restricted subset of
 * HTTP response object.
 *
 * @author anton.baukin@gmail.com
 */
public class ResponseWrapper
{
	/* public: constructor */

	public ResponseWrapper(HttpServletResponse response)
	{
		this.response = response;
	}


	/* Request Subset */

	public String  encodeURL(String url)
	{
		return getResponse().encodeURL(url);
	}

	public String  encodeRedirectURL(String url)
	{
		return getResponse().encodeRedirectURL(url);
	}

	public boolean containsHeader(String name)
	{
		return getResponse().containsHeader(name);
	}

	public String  getContentType()
	{
		return getResponse().getContentType();
	}

	public String  getCharacterEncoding()
	{
		return getResponse().getCharacterEncoding();
	}

	public Locale  getLocale()
	{
		return getResponse().getLocale();
	}


	/* protected: response access */

	protected final HttpServletResponse getResponse()
	{
		return response;
	}

	private HttpServletResponse response;
}