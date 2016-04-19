package net.java.jsf.extjs.servlet;

/* Java */

import java.io.Reader;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;

/* Java Servlet */

import javax.servlet.http.HttpServletRequest;

/* extjsf: servlet */

import static net.java.jsf.extjs.servlet.RequestPoint.request;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.SU;


/**
 * HTTP request handling support.
 *
 * @author anton.baukin@gmail.com
 */
public class REQ
{
	/* request properties */

	/**
	 * Tells whether the request comes from the local host.
	 */
	public static boolean isLocalhost(HttpServletRequest r)
	{
		String adr = r.getRemoteAddr();

		return
		  "127.0.0.1".equals(adr)                || //<-- IPv4, a
		  "::1".equals(adr)                      || //<-- IPv6, a
		  "0:0:0:0:0:0:0:1".equals(adr)          || //<-- IPv6, b
		  adr.matches("127(.\\d\\d?\\d?){3,3}");    //<-- IPv4, b
	}

	public static boolean isLocalhost()
	{
		return isLocalhost(request());
	}

	/**
	 * Returns the substring from the request URI where
	 * the context path prefix is removed, and the
	 * characters till the path parameters or the query
	 * parameters is left. The string always starts with '/'.
	 */
	public static String  getRequestPath(HttpServletRequest r)
	{
		String uri = r.getRequestURI();
		if(uri.length() == 0) return "/";

		String ctx = r.getContextPath();

		//?: {this request is to the default context}
		if(ctx.length() == 0)
			return (uri.charAt(0) == '/')?(uri):("/" + uri);

		if((uri.charAt(0) != '/') && (ctx.charAt(0) == '/'))
			ctx = ctx.substring(1);

		if((uri.charAt(0) == '/') && (ctx.charAt(0) != '/'))
			uri = uri.substring(1);

		if(!uri.startsWith(ctx))
			return "/";

		uri = uri.substring(ctx.length());
		if(uri.length() == 0) return "/";

		if(uri.charAt(0) != '/')
			uri = "/" + uri;

		return uri;
	}

	public static String  getRequestPath()
	{
		return getRequestPath(request());
	}

	public static boolean isGunZIPAllowed(HttpServletRequest r)
	{
		return SU.sXs(r.getHeader("Accept-Encoding")).toLowerCase().contains("gzip");
	}

	/**
	 * Adds the parameters to the given URL string
	 * without encoding it.
	 */
	public static String  param(String url, Object... nv)
	{
		StringBuilder x = new StringBuilder(url.length() + 64).append(url);

		//?: {has no '?'}
		if(url.indexOf('?') == -1)
			x.append('?');

		//?: {has parameters}
		if(x.charAt(x.length() - 1) != '?')
			x.append('&');

		//c: for each (name + value) pair
		for(int i = 0;(i < nv.length);i += 2)
		{
			EX.assertx(i + 1 < nv.length);
			if(i != 0) x.append('&');

			//~: parameter name
			EX.assertx(nv[i] instanceof CharSequence);
			x.append(nv[i]);

			//?: {has value}
			String v = (nv[i+1] == null)?(""):(SU.sXs(nv[i+1].toString()));
			if(!v.isEmpty()) x.append('=').append(v);
		}

		return x.toString();
	}

	/**
	 * Decodes URL-encoded parameters of the request body.
	 */
	public static void    decodeBodyParams(Reader r, String enc, Map<String, Object> ps)
	  throws java.io.IOException
	{
		StringBuilder n = new StringBuilder(64);
		StringBuilder v = new StringBuilder(128);
		char[]        b = new char[8];
		int           s = 0; // 0 name, 1 value

		Runnable      x = () -> {

			String nx = SU.s2s(n.toString());
			n.delete(0, n.length());

			String vx = v.toString();
			v.delete(0, v.length());

			if(nx == null) return;
			try
			{
				nx = URLDecoder.decode(nx, enc);
				vx = URLDecoder.decode(vx, enc);
			}
			catch(Throwable e)
			{
				throw EX.wrap(e);
			}

			Object ss = ps.get(nx);
			if(ss == null)
				ps.put(nx, vx);
			else if(ss instanceof String)
			{
				String[] ss2 = new String[2];
				ss2[0] = (String) ss;
				ss2[1] = vx;
				ps.put(nx, ss2);
			}
			else if(ss instanceof String[])
			{
				String[] ss3 = (String[]) ss;
				String[] ss2 = new String[ss3.length + 1];
				System.arraycopy(ss3, 0, ss2, 0, ss3.length);
				ss2[ss2.length - 1] = vx;
				ps.put(nx, ss2);
			}
		};

		while(true)
		{
			//~: fill the buffer
			int bs = r.read(b);
			if(bs <= 0) break;

			for(int i = 0;(i < bs);i++)
			{
				char c = b[i];

				//?: {reading name}
				if(s == 0)
				{
					//?: {=} end name
					if(c == '=')
					{
						s = 1;
						continue;
					}

					//?: {&} abnormal end
					if(c == '&')
					{
						x.run();
						s = 0;
						continue;
					}

					//~: append name
					n.append(c);
				}

				//?: {reading value}
				if(s == 1)
				{
					//?: {&} end value
					if(c == '&')
					{
						x.run();
						s = 0;
						continue;
					}

					//~: append value
					v.append(c);
				}
			}
		}

		//?: {finish the last}
		if(s == 1)
			x.run();
	}

	/**
	 * Returns all parameters of the request as JSON
	 * excluding any of the optional argument.
	 */
	public static String  jsonRequestParams(Collection<String> exclude)
	{
		StringBuilder         s  = new StringBuilder(32);
		Map<String, String[]> pm = request().getParameterMap();

		s.append('{');
		for(Map.Entry<String, String[]> e : pm.entrySet())
		{
			//?: {standard parameter}
			if((exclude != null) && exclude.contains(e.getKey()))
				continue;

			if(s.length() != 1)
				s.append(", ");

			//~: parameter name as the key
			s.append('"').append(SU.jss(e.getKey())).append("\": ");

			//?: {has single value}
			if(e.getValue().length == 1)
				s.append('"').append(SU.jss(e.getValue()[0])).append('"');
			//~: write an array
			else
			{
				s.append('[');
				for(int i = 0;(i < e.getValue().length);i++)
					s.append((i == 0)?("\""):(", \"")).
					  append(SU.jss(e.getValue()[i])).
					  append('"');
				s.append(']');
			}
		}
		s.append('}');

		return s.toString();
	}
}