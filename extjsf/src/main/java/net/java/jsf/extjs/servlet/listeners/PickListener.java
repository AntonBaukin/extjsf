package net.java.jsf.extjs.servlet.listeners;

/* Java */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a Servlet Context Listener
 * to be added to the application start-stop sequence.
 *
 * @author anton.baukin@gmail.com.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PickListener
{
	/**
	 * Tells the global order of start procedure.
	 * Stop calls are done in the reversed list.
	 */
	int order();
}