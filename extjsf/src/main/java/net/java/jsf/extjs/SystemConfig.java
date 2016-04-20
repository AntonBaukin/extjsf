package net.java.jsf.extjs;

/* Spring Framework */

import org.springframework.stereotype.Component;


/**
 * Collection of parameters of various subsystems.
 *
 * @author anton.baukin@gmail.com.
 */
@Component
public class SystemConfig
{
	/* General Settings */

	public boolean isDebug()
	{
		return debug;
	}

	private boolean debug = true;

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}


	/* Servlets and Go-Filters */

	public String getGoPagePrefix()
	{
		return goPagePrefix;
	}

	private String goPagePrefix = "/go/";

	public void setGoPagePrefix(String goPagePrefix)
	{
		this.goPagePrefix = goPagePrefix;
	}
}