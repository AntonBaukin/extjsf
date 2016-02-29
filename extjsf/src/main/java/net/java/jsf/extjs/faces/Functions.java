package net.java.jsf.extjs.faces;

/* Java */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* extjsf: servlets */

import net.java.jsf.extjs.servlet.RequestPoint;

/* extjsf: support */

import net.java.jsf.extjs.support.CMP;
import net.java.jsf.extjs.support.SU;



/**
 * JavaServer Faces functions.
 *
 * @author anton.baukin@gmail.com
 */
public class Functions
{
	/* public: HTML */

	/**
	 * Wraps the text given into CDATA section.
	 *
	 * If argument is boolean true, opened the
	 * section with. It closes it on false
	 * argument value. Other values are converted
	 * to string and wrapped.
	 */
	public static String cdata(Object s)
	{
		if(s == null)               return "";
		if(Boolean.TRUE.equals(s))  return "<![CDATA[";
		if(Boolean.FALSE.equals(s)) return "]]>";

		return "<![CDATA[" + s.toString() + "]]>";
	}

	public static String escapeHTMLString(Object sobj)
	{
		return SU.escapeXML(sobj);
	}

	public static String trim(Object v)
	{
		if(v instanceof BigDecimal)
			if(CMP.eqZero((BigDecimal)v))
				v = BigDecimal.ZERO;
			else
				v = ((BigDecimal)v).stripTrailingZeros();

		return (v == null)?(""):SU.sXs(SU.s2s(v.toString()));
	}


	/* public: URLs creators */

	public static String encodePath(String path)
	{
		String        cpath = RequestPoint.request().
		  getContextPath();
		StringBuilder res   = new StringBuilder(
		  cpath.length() + path.length() + 1);

		res.append(cpath);
		if((path.length() != 0) && (path.charAt(0) != '/'))
			res.append('/');
		res.append(path);

		return (path.length() == 0)?(res.toString())
		  :(RequestPoint.response().encodeURL(res.toString()));
	}

	public static String absoluteURL(String path)
	{
		return RequestPoint.formAbsoluteURL(path, false);
	}

	public static String goPath(String path)
	{
		String cp = RequestPoint.request(0).getContextPath();

		if(SU.sXe(path))
		{
			path = RequestPoint.request(0).getRequestURI();
			path = path.substring(cp.length());

			if(path.startsWith("/go/"))
				path = path.substring(4);
		}

		if(path.charAt(0) == '/')
			path = path.substring(1);

		return cp + "/go/" + path;
	}


	/* public: JavaScript support */

	/**
	 * Escapes string to place into Java Script
	 * source text. Note that XML entities
	 * are not encoded here, and you must
	 * protected XML text properly with CDATA
	 * sections.
	 */
	public static String escapeJSString(Object sobj)
	{
		return SU.jss(sobj);
	}


	/* public: model views support  */

	public static String genViewId(ViewWithModes v, String name)
	{
		return v.getId() + '-' + name;
	}


	/* public: collections and iterations */

	@SuppressWarnings("unchecked")
	public static List   mapAsList(Map map)
	{
		return ((map == null) || map.isEmpty())
		  ?(Collections.emptyList())
		  :(new ArrayList(map.entrySet()));
	}


	/* public: various data types */

	public static String currency(Object x)
	{
		if(x == null) return "";

		if((x instanceof Number) && !(x instanceof BigDecimal))
			x = x.toString();
		if(!(x instanceof BigDecimal))
			x = new BigDecimal(x.toString());

		//~: strip trailing zeros
		BigDecimal d = (BigDecimal)x;
		if(CMP.eqZero(d))
			d = BigDecimal.ZERO;

		if(d.scale() < 0)
			d = d.setScale(0, BigDecimal.ROUND_UNNECESSARY);

		return d.toString();
	}
}