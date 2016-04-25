package net.java.jsf.extjs.support;

/* Java */

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/* extjsf: support (streams) */

import net.java.jsf.extjs.support.streams.BigDecimalXMLEncoderPersistenceDelegate;
import net.java.jsf.extjs.support.streams.BytesStream;
import net.java.jsf.extjs.support.streams.Streams.NotCloseInput;
import net.java.jsf.extjs.support.streams.Streams.NotCloseOutput;


/**
 * Various utility functions for objects.
 *
 * @author anton.baukin@gmail.com
 */
public class OU
{
	/* Java Bean XML Serialization */

	public static String  obj2xml(Object bean)
	{
		try(BytesStream bs = new BytesStream())
		{
			OU.obj2xml(bs, bean);

			try
			{
				return new String(bs.bytes(), "UTF-8");
			}
			catch(Exception e)
			{
				throw EX.wrap(e);
			}
		}
	}

	/**
	 * Writes the bean object to the stream.
	 * The stream is not closed!
	 */
	public static void    obj2xml(OutputStream os, Object bean)
	{
		try
		{
			XMLEncoder enc = new XMLEncoder(new NotCloseOutput(os));

			enc.setPersistenceDelegate(BigDecimal.class,
			  BigDecimalXMLEncoderPersistenceDelegate.getInstance());

			enc.writeObject(bean);
			enc.close();
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Error occured while XML Encoding Java Bean of class [",
			  LU.cls(bean), "]!");
		}
	}

	public static Object  xml2obj(String xml)
	{
		try
		{
			return (xml == null)?(null):
			  OU.xml2obj(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		}
		catch(Exception e)
		{
			throw EX.wrap(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <O> O   xml2obj(String xml, Class<O> cls)
	{
		Object res = OU.xml2obj(xml);

		if((res != null) && (cls != null) && !cls.isAssignableFrom(res.getClass()))
			throw EX.state("Can't cast XML Decoded instance of class [",
			  LU.cls(res), "] to the required class [", LU.cls(cls), "]!"
			);

		return (O)res;
	}

	/**
	 * Reads a bean object from the stream.
	 * The stream is not closed!
	 */
	public static Object  xml2obj(InputStream is)
	{
		try
		{
			XMLDecoder enc = new XMLDecoder(new NotCloseInput(is));
			Object     res = enc.readObject();

			enc.close();
			return res;
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured while Decoding Java Bean from XML!");
		}
	}

	@SuppressWarnings("unchecked")
	public static <O> O   xml2obj(InputStream is, Class<O> cls)
	{
		Object res = OU.xml2obj(is);

		if((res != null) && (cls != null) && !cls.isAssignableFrom(res.getClass()))
			throw EX.state("Can't cast XML Decoded instance of class [",
			  LU.cls(res), "] to the required class [", LU.cls(cls), "]!"
			);

		return (O)res;
	}


	/* Java Serialization */

	public static byte[]  obj2bytes(Object obj)
	{
		BytesStream bs = new BytesStream();
		bs.setNotClose(true);

		try
		{
			ObjectOutputStream os = new ObjectOutputStream(bs);

			os.writeObject(obj);
			os.close();

			return bs.bytes();
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured while Java-serializing object of class [",
			  LU.cls(obj), "]!");
		}
		finally
		{
			bs.closeAlways();
		}
	}

	public static byte[]  eobj2bytes(boolean cls, boolean gzip, Externalizable obj)
	{
		EX.assertn(obj);

		BytesStream bs = new BytesStream();
		bs.setNotClose(true);

		try
		{
			ObjectOutputStream os = new ObjectOutputStream(
			  (gzip)?(new GZIPOutputStream(bs)):(bs));

			if(cls) IO.cls(os, obj.getClass());
			obj.writeExternal(os);
			os.close();

			return bs.bytes();
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured while externalizing object of class [",
			  LU.cls(obj), "]!");
		}
		finally
		{
			bs.closeAlways();
		}
	}

	public static Object  bytes2obj(byte[] bytes)
	{
		try
		{
			ObjectInputStream is = new ObjectInputStream(
			  new ByteArrayInputStream(bytes));

			return is.readObject();
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occurred while reading Java-serialized object!");
		}
	}

	@SuppressWarnings("unchecked")
	public static <O> O   bytes2obj(byte[] bytes, Class<O> cls)
	{
		Object obj = OU.bytes2obj(bytes);

		if((obj != null) && (cls != null) && !cls.isAssignableFrom(obj.getClass()))
			throw EX.state("Deserialized object of class [",
			  LU.cls(cls), "], but expected [", LU.cls(obj), "]!"
			);

		return (O) obj;
	}

	public static <O> O   bytes2eobj(Class<O> cls, boolean gzip, byte[] bytes)
	{
		try
		{
			//~: create input stream
			InputStream xs = new ByteArrayInputStream(bytes);
			if(gzip) xs = new GZIPInputStream(xs);

			//~: create object stream
			ObjectInputStream is = new ObjectInputStream(xs);

			//~: create new instance
			O o = cls.newInstance();
			if(!(o instanceof Externalizable)) throw EX.state();

			//~: read the data
			((Externalizable)o).readExternal(is);

			return o;
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occurred while reading ",
			  "externalized object of class [", LU.cls(cls), "]!");
		}
	}

	/**
	 * Reads externalized object with class writing option set.
	 */
	@SuppressWarnings("unchecked")
	public static <O> O   bytes2eobj(boolean gzip, byte[] bytes)
	{
		try
		{
			//~: create input stream
			InputStream xs = new ByteArrayInputStream(bytes);
			if(gzip) xs = new GZIPInputStream(xs);

			//~: create object stream
			ObjectInputStream is = new ObjectInputStream(xs);

			//~: read the class
			Class cls = IO.cls(is);
			EX.assertx(Externalizable.class.isAssignableFrom(cls));

			//~: create new instance
			O o = (O) cls.newInstance();

			//~: read the data
			((Externalizable)o).readExternal(is);

			return o;
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occurred while reading ",
			  "externalized object (with class write option set)!");
		}
	}
}