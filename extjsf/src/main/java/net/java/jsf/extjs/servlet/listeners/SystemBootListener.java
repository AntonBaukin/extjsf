package net.java.jsf.extjs.servlet.listeners;

/* Java Servlet */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/* Logging for Java */

import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.web.Log4jWebSupport;
import org.apache.logging.log4j.web.WebLoggerContextUtils;

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
		//~: access logging framework
		this.logLifeCycle = WebLoggerContextUtils.
		  getWebLifeCycle(event.getServletContext());

		//~: start it & register
		this.logLifeCycle.start();
		((Log4jWebSupport) this.logLifeCycle).setLoggerContext();

		//~: register the context
		RequestPoint.getInstance().setContext(event.getServletContext());
	}

	protected LifeCycle logLifeCycle;

	public void contextDestroyed(ServletContextEvent event)
	{
		//~: cleanup the context
		RequestPoint.getInstance().setContext(null);

		//~: start logging framework
		try
		{
			this.logLifeCycle.stop();
		}
		finally
		{
			this.logLifeCycle = null;
		}
	}
}