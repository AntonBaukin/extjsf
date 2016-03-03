package net.java.jsf.extjs.servlet.listeners;

/* Java */

import java.util.Map;
import java.util.TreeMap;

/* Java X */

import javax.annotation.PostConstruct;

/* Java Servlet */

import javax.servlet.ServletContextListener;

/* Sprint Framework */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;


/**
 * Collects all {@link ServletContextListener} registered
 * as Spring beans with {@link PickListener} annotation.
 *
 * @author anton.baukin@gmail.com.
 */
@Component
public class ContextListenersPoint
{
	/* Context Listeners Point */

	public ServletContextListener[] getListeners()
	{
		return listeners;
	}


	/* protected: listeners discovery */

	@PostConstruct
	protected void findListeners()
	{
		Map<String, Object> bs = context.
		  getBeansWithAnnotation(PickListener.class);

		//~: no beans found
		if(bs.isEmpty()) LU.E(LU.cls(this),
		  "No web context listeners marked with @PickListener were found!");

		//~: (order -> listener) mapping
		Map<Integer, ServletContextListener> m = new TreeMap<>();

		//c: for each bean
		for(Map.Entry<String, Object> e : bs.entrySet())
		{
			//?: {not a context listener}
			EX.assertx(e.getValue() instanceof ServletContextListener,
			  "Bean [", e.getKey(), "] is not a Servlet Context Listener!");

			//~: pick annotation
			PickListener p = EX.assertn(e.getValue().getClass().
				 getAnnotation(PickListener.class)
			);

			//?: {got listeners with the same order}
			EX.assertx(!m.containsKey(p.order()),
			  "Servlet Context Listener [", LU.cls(e.getValue()),
			  "] is registered by the occupied order [", p.order(), "]!"
			);

			m.put(p.order(), (ServletContextListener)e.getValue());
		}

		//~: ordered listeners
		this.listeners = m.values().toArray(
		  new ServletContextListener[m.size()]);
	}

	@Autowired
	protected ApplicationContext context;

	protected ServletContextListener[] listeners;
}