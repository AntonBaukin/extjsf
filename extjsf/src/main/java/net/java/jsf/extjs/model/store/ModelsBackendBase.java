package net.java.jsf.extjs.model.store;

/* Java */

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/* extjsf: models */

import net.java.jsf.extjs.model.ModelBean;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.IO;
import net.java.jsf.extjs.support.streams.BytesStream;


/**
 * Implementation base for a UI Models Backend.
 *
 * @author anton.baukin@gmail.com.
 */
public abstract class ModelsBackendBase
       implements     ModelsBackend
{
	/* Models Backend Base */

	protected byte[]    store(ModelBean mb)
	{
		BytesStream bs = new BytesStream(); try
		{
			ObjectOutputStream o =
			  new ObjectOutputStream(new GZIPOutputStream(bs));

			//>: bean class
			IO.cls(o, mb.getClass());

			//>: bean bytes
			mb.writeExternal(o);

			//~: return the bytes
			bs.setNotCloseNext(true);
			o.close();

			return bs.bytes();
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
		finally
		{
			bs.closeAlways();
		}
	}

	protected ModelBean restore(byte[] bytes)
	{
		try
		{
			ObjectInputStream i = new ObjectInputStream(
			  new GZIPInputStream(new ByteArrayInputStream(bytes)));

			//>: bean class name
			Class c = IO.cls(i);
			EX.assertx(ModelBean.class.isAssignableFrom(c));

			//~: read the data
			ModelBean mb = (ModelBean) c.newInstance();
			mb.readExternal(i);

			return mb;
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}
}