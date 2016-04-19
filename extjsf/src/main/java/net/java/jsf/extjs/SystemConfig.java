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