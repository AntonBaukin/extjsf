package net.java.jsf.extjs.objects;

/* Java */

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.streams.BytesStream;


/**
 * Injects StringReader as a property.
 * Source of string is the context path
 * resource reference.
 *
 * Encoding must be UTF-8.
 *
 *
 * @author anton.baukin@gmail.com.
 */
public class PropResourceReader implements PropInjector
{
	/* Properties Injector */

	public void injectProp(Map<String, Object> pm)
	{
		EX.assertn(pm);

		//~: find the resource file
		URL url = findResource();

		//~: read it
		try(InputStream is = url.openStream();
		    BytesStream bs = new BytesStream())
		{
			//~: copy the bytes
			bs.write(is);

			//~: build the string
			String text = buildText(bs);

			//~: set the property
			assignProperty(pm, text);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Error while reading resource [", resource, "]!");
		}
	}


	/* Bean */

	public String getProperty()
	{
		return property;
	}

	private String property;

	public void setProperty(String property)
	{
		this.property = property;
	}

	public String getResource()
	{
		return resource;
	}

	private String resource;

	public void setResource(String resource)
	{
		this.resource = resource;
	}


	/* protected: processing */

	protected URL    findResource()
	{
		EX.asserts(resource);
		return EX.assertn(Thread.currentThread().
		  getContextClassLoader().getResource(resource),
		  "Resource [", resource, "] is not found!"
		);
	}

	protected String buildText(BytesStream bs)
	  throws Exception
	{
		return new String(bs.bytes(), "UTF-8");
	}

	protected void   assignProperty(Map<String, Object> pm, String text)
	{
		pm.put(EX.asserts(property), text);
	}
}