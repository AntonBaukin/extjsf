package net.java.jsf.extjs.model;

/* Java */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.IO;


/**
 * Model class that stores a view instance.
 *
 * @author anton.baukin@gmail.com.
 */
public abstract class ViewModelBeanBase extends ModelBeanBase
{
	/* View */

	public Object     getView()
	{
		return view;
	}

	private Object view;

	@SuppressWarnings("unchecked")
	public void       setView(Object view)
	{
		EX.assertx((view == null) ||
		  viewClass().isAssignableFrom(view.getClass()));

		this.view = view;
	}

	public abstract Class viewClass();


	/* Serialization */

	public void       writeExternal(ObjectOutput o)
	  throws IOException
	{
		super.writeExternal(o);

		if(serializeXML(o, null))
			IO.xml(o, getView());
		else
			IO.obj(o, getView());
	}

	@SuppressWarnings("unchecked")
	public void       readExternal(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		super.readExternal(i);

		if(serializeXML(null, i))
			setView(IO.xml(i, viewClass()));
		else
			setView(IO.obj(i, viewClass()));
	}

	protected boolean serializeXML(ObjectOutput o, ObjectInput i)
	{
		return true;
	}
}