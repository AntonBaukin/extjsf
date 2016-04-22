package net.java.jsf.extjs.model;

/* Spring Framework */

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/* extjsf: support */

import net.java.jsf.extjs.support.SU;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * While model contains all the parameters of
 * interaction, and model data contains all
 * the state inferred from that parameters,
 * model request single instance stores the
 * runtime-defined request key telling what
 * part of the state data are needed for the
 * pending request.
 *
 *
 * @author anton.baukin@gmail.com
 */
@Component
@Scope("request")
public class ModelRequest
{
	/* Singleton */

	public static ModelRequest getInstance()
	{
		return bean(ModelRequest.class);
	}


	/* Model Request (support) */

	public static boolean isKey(String key)
	{
		if((key = SU.s2s(key)) == null) key = "";
		return getInstance().getKey().equals(key);
	}


	/* Model Request */

	/**
	 * The key of the model request is thread-bound
	 * string value. It is always defined, and empty
	 * string by default.
	 */
	public String getKey()
	{
		return (key == null)?(""):(key);
	}

	private String key;
	
	public void   setKey(String key)
	{
		this.key = SU.s2s(key);
	}
}