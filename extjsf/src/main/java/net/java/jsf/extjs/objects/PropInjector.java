package net.java.jsf.extjs.objects;

/* Java */

import java.util.Map;


/**
 * Injects properties into configuration
 * Map: String -> Object.
 *
 * @author anton.baukin@gmail.com.
 */
public interface PropInjector
{
	/* Properties Injector */

	public void injectProp(Map<String, Object> pm);
}
