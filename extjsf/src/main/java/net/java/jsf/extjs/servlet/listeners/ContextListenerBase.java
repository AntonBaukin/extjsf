package net.java.jsf.extjs.servlet.listeners;

/* Java Servlet */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Combines listener with self referencing object.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class ContextListenerBase
       implements ServletContextListener
{
	/* Servlet Context Listener */

	public void contextInitialized(ServletContextEvent sce)
	{
		try
		{
			this.event = sce;
			this.init();
		}
		finally
		{
			this.event = null;
		}
	}

	public void contextDestroyed(ServletContextEvent sce)
	{
		try
		{
			this.event = sce;
			this.destroy();
		}
		finally
		{
			this.event = null;
		}
	}


	/* protected: listening */

	protected abstract void       init();

	protected void                destroy()
	{}

	protected ServletContextEvent getEvent()
	{
		return this.event;
	}


	/* private: servlet context event */

	private ServletContextEvent event;
}