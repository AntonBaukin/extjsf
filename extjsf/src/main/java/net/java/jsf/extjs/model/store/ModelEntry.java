package net.java.jsf.extjs.model.store;

/* Java */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.atomic.AtomicInteger;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.IO;
import net.java.jsf.extjs.support.LU;


/**
 * Record of a model in a store.
 *
 * Extend it with fields to identify a user,
 * it's scope, roles, etc.
 *
 *
 * @author anton.baukin@gmail.com.
 */
public class ModelEntry implements Externalizable
{
	/**
	 * The key of the model bean.
	 * Always assigned, never updated.
	 */
	public String key;

	/**
	 * The Model Bean instance.
	 * Always assigned, never updated.
	 */
	public ModelBean bean;

	/**
	 * The last access time.
	 * Updated on each model read.
	 */
	public volatile long accessTime;

	/**
	 * On create is zero, incremented
	 * on each read access.
	 */
	public final AtomicInteger accessInc =
	  new AtomicInteger();

	/**
	 * This flag is set when the entry was
	 * previously loaded from the store.
	 */
	public boolean loaded;


	/* Object */

	public int     hashCode()
	{
		return key.hashCode();
	}

	public boolean equals(Object o)
	{
		return (this == o) || (o instanceof ModelEntry) &&
		  ((ModelEntry)o).key.equals(this.key);
	}


	/* Serialization */

	public void writeExternal(ObjectOutput o)
	  throws IOException
	{
		//?: {the key not differs}
		EX.assertx(key.equals(bean.getModelKey()));

		//>: model key
		IO.str(o, key);

		//>: access time
		o.writeLong(accessTime);

		//>: access counter
		o.writeInt(accessInc.get());

		//>: model bean class
		IO.cls(o, bean.getClass());

		//>: model bean
		bean.writeExternal(o);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		//<: model key
		key = IO.str(i);

		//<: access time
		accessTime = i.readLong();

		//<: access counter
		accessInc.set(i.readInt());

		//<: model bean class
		Class cls = EX.assertn(IO.cls(i));
		EX.assertx(ModelBean.class.isAssignableFrom(cls));

		//<: model bean
		try
		{
			this.bean = (ModelBean)cls.newInstance();
			this.bean.readExternal(i);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Error occurred while reading Model Bean ",
			  "instance of class [", LU.cls(cls), "]!"
			);
		}
	}
}
