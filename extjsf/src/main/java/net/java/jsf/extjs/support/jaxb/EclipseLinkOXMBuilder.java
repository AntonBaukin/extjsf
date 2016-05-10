package net.java.jsf.extjs.support.jaxb;

/* Java */

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/* extjsf: objects */

import net.java.jsf.extjs.objects.PropResourceReader;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * This is a strategy to build Eclipse Link OXM
 * files for each package found during the
 * auto-discovery.
 *
 * @author anton.baukin@gmail.com.
 */
public class   EclipseLinkOXMBuilder
       extends PropResourceReader
{
	/* Bean */

	public Class[] getClasses()
	{
		return classes;
	}

	private Class[] classes;

	public void setClasses(Class[] classes)
	{
		this.classes = classes;
	}


	/* protected: processing */

	@SuppressWarnings("unchecked")
	protected void assignProperty(Map<String, Object> pm, String text)
	{
		EX.assertn(classes);
		EX.assertx(text.contains("$PACKAGE"));

		//~: find the packages
		HashSet<String> ps = new HashSet<>(7);
		for(Class cls : classes)
			ps.add(cls.getPackage().getName());

		//~: build the sources
		ArrayList<StringReader> srcs = new ArrayList<>();
		for(String p : ps)
		{
			//~: replace package name
			String x = text.replace("$PACKAGE", p);

			//~: add the source
			srcs.add(new StringReader(x));
		}

		//~: put the property
		EX.asserts(getProperty());
		pm.put(getProperty(), srcs);
	}
}