package net.java.jsf.extjs.servlet.listeners;

/* Java Servlet */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.RequestPoint;


/**
 * Prepares the system to work.
 *
 * Binds web application context with Servlet
 * and JavaServer Faces support routines.
 *
 *
 * @author anton.baukin@gmail.com
 */
public class      SystemBootListener
       implements ServletContextListener
{
	/* public: ServletContextListener interface */

	public void contextInitialized(ServletContextEvent event)
	{
		RequestPoint.getInstance().setContext(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event)
	{
		RequestPoint.getInstance().setContext(null);
	}
}