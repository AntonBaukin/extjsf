package net.java.jsf.extjs.faces;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.RequestPoint;

/* extjsf: support */

import net.java.jsf.extjs.support.CMP;
import net.java.jsf.extjs.support.SU;


/**
 * Abstraction of a Faces View (back bean).
 *
 * @author anton.baukin@gmail.com.
 */
public abstract class ViewBase
{
	/* Phony Fields */

	public boolean  isPhonyBoolTrue()
	{
		return true;
	}

	public void     setPhonyBoolTrue(boolean phonyBoolTrue)
	{}

	public boolean  isPhonyBoolFalse()
	{
		return false;
	}

	public void     setPhonyBoolFalse(boolean phonyBoolFalse)
	{}

	public String   getPhonyString()
	{
		return "";
	}

	public void     setPhonyString(String s)
	{}

	public String   noAction()
	{
		return null;
	}


	/* Request Access */

	public String   getParam(String name)
	{
		return SU.s2s(RequestPoint.param(name));
	}

	public String[] getParams(String name)
	{
		return RequestPoint.params(name);
	}

	/**
	 * Returns web context related URL (starts with '/')
	 * to the page current request was issued for.
	 * It is already URL-encoded.
	 */
	public String   getRequestURI()
	{
		return RequestPoint.response().encodeURL(
		  RequestPoint.request(0).getRequestURI());
	}
}