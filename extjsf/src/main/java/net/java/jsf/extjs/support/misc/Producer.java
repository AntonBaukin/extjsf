package net.java.jsf.extjs.support.misc;

/* Java */

import java.util.function.Supplier;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Analog of Supplier that can generate error.
 *
 * @author anton.baukin@gmail.com.
 */
public interface Producer<T> extends Supplier<T>
{
	/* Producer */

	public T  produce()
	  throws Throwable;

	default T get()
	{
		try
		{
			return this.produce();
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}
}