package net.java.jsf.extjs;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.SpringPoint;


/**
 * Collection of parameters of various subsystems.
 *
 * @author anton.baukin@gmail.com.
 */
@Component
public class SystemConfig
{
	/* Singleton */

	public static SystemConfig getInstance()
	{
		return SpringPoint.bean(SystemConfig.class);
	}


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

	/**
	 * Default UI grid size.
	 */
	public int getGridSize()
	{
		return (gridSize != 0)?(gridSize):(20);
	}

	private int gridSize;

	public void setGridSize(int gridSize)
	{
		EX.assertx(gridSize >= 1);
		this.gridSize = gridSize;
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