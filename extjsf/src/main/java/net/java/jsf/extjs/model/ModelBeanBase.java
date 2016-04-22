package net.java.jsf.extjs.model;

/* Java */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/* Java XML Binding */

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.IO;


/**
 * Implementation base for model beans.
 *
 * @author anton.baukin@gmail.com
 */
@XmlType(propOrder = { "modelKey", "domain" })
public abstract class ModelBeanBase implements ModelBean
{
	/* Model Bean (main) */

	@XmlElement(name = "model-key")
	public String  getModelKey()
	{
		return modelKey;
	}

	private String modelKey;

	public void    setModelKey(String key)
	{
		this.modelKey = key;
	}


	/* Model Bean (data access) */

	@SuppressWarnings("unchecked")
	public ModelData modelData()
	{
		if(dataClass == null)
			return null;

		try
		{
			Class     cls = this.getClass();
			ModelData res = null;

			while((res == null) && (cls != null))
			{
				//?: {has a constructor of that type}
				res = EX.result(NoSuchMethodException.class, () ->
				  dataClass.getConstructor(cls).newInstance(this));

				//~: lookup in the interfaces
				for(Class ifs : cls.getInterfaces())
					if(res != null)
						break;
					else
						res = EX.result(NoSuchMethodException.class, () ->
						  dataClass.getConstructor(ifs).newInstance(this));
			}

			//~: not found any specific constructor, invoke the default
			if(res == null) try
			{
				res = dataClass.getConstructor().newInstance();
			}
			catch(NoSuchMethodException e)
			{
				//!: unable to create an instance
				throw EX.state("No constructor available!");
			}

			return res;
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Unable to create Data Model of class [",
			  dataClass.toString(), "]!");
		}
	}

	public void      setDataClass(Class<? extends ModelData> dataClass)
	{
		this.dataClass = dataClass;
	}

	private Class<? extends ModelData> dataClass;


	/* Serialization */

	public void writeExternal(ObjectOutput o)
	  throws IOException
	{
		IO.str(o, modelKey);
		IO.cls(o, dataClass);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		modelKey  = IO.str(i);
		dataClass = IO.cls(i);
		EX.assertx((dataClass == null) ||
		  ModelData.class.isAssignableFrom(dataClass));
	}
}