package net.java.jsf.extjs.objects;

/* Java */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/* Java API for XML Binding */

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;
import net.java.jsf.extjs.support.SU;
import net.java.jsf.extjs.support.streams.BytesStream;



/**
 * Wraps Spring OXM Marshaller invocation.
 *
 * @author anton.baukin@gmail.com.
 */
public class XStreamer
{
	/* public: initialization */

	public XStreamer(Class[] classes)
	{
		this(classes, null);
	}

	public XStreamer(Class[] classes, PropInjector[] props)
	{
		String FP = JAXBContext.JAXB_CONTEXT_FACTORY;
		String fp = System.getProperty(FP);
		Class  XP = org.eclipse.persistence.jaxb.JAXBContextFactory.class;

		try
		{
			Map<String, Object> pm = new HashMap<>();

			//?: has property injectors
			if(props != null)
				for(PropInjector pi : props)
					pi.injectProp(pm);

			System.setProperty(FP, XP.getName());
			this.ctx = JAXBContext.newInstance(classes, pm);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Can't create JAXB Context!");
		}
		finally
		{
			if(fp != null)
				System.setProperty(FP, fp);
		}
	}

	protected final JAXBContext ctx;

	public void   setWriteProps(String str)
	{
		String[] ps = SU.a2a(SU.s2a(str, ','));
		EX.assertx((ps.length % 2) == 0);

		for(int i = 0;(i < ps.length);i += 2)
			writeProps.put(ps[i], ps[i + 1]);
	}

	protected final Map<String, String> writeProps = new HashMap<>();

	public void   setReadProps(String str)
	{
		String[] ps = SU.a2a(SU.s2a(str, ','));
		EX.assertx((ps.length % 2) == 0);

		for(int i = 0;(i < ps.length);i += 2)
			readProps.put(ps[i], ps[i + 1]);
	}

	protected final Map<String, String> readProps = new HashMap<>();


	/* Writing Objects */

	public boolean supported(Object object)
	{
		return (object == null) ||
		  (object.getClass().getAnnotation(XmlType.class) != null);
	}

	public void    write(Object object, Result result)
	{
		try
		{
			//~: create the marshaller
			Marshaller m = this.ctx.createMarshaller();
			for(Map.Entry<String, String> e : writeProps.entrySet())
				m.setProperty(e.getKey(), propertyValue(e.getValue()));

			//!: do write
			m.marshal(object, result);
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured when marshalling ",
			  "object of class [", LU.cls(object), "]!"
			);
		}
	}

	public void    write(Object object, Writer stream)
	{
		this.write(object, new StreamResult(stream));
	}

	/**
	 * Write the object to the raw stream given in UTF-8.
	 * Note that the stream is closed (also, in the case of error).
	 */
	public void    write(Object object, OutputStream stream)
	{
		Throwable error  = null;
		boolean   closed = false;

		try
		{
			Writer w = new OutputStreamWriter(stream, "UTF-8");

			//!: write
			this.write(object, new StreamResult(stream));

			//~: close
			w.close();
			closed = true;
		}
		catch(Throwable e)
		{
			error = e;
		}
		finally
		{
			if(!closed) try
			{
				stream.close();
			}
			catch(Throwable e)
			{
				error = e;
			}
		}

		if(error != null)
			throw EX.wrap(error);
	}

	public void    write(Object object, BytesStream stream)
	{
		//~: prevent close on the exit
		boolean notclose = stream.isNotCloseNext();

		try
		{
			//~: write the object
			Writer w = new OutputStreamWriter(stream, "UTF-8");
			this.write(object, w);

			stream.setNotCloseNext(true);
			w.close(); //<-- without the bytes stream

			//~: restore original not-close flag
			stream.setNotCloseNext(notclose);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}

	public byte[]  write(boolean gzip, Object object)
	{
		BytesStream bs = new BytesStream();
		bs.setNotClose(true);

		try
		{
			OutputStream os = (!gzip)?(bs):(new GZIPOutputStream(bs));

			//~: write
			this.write(object, os);

			if(gzip) //<-- close the deflating stream only
				os.close();

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

	public String  write(Object object)
	{
		StringWriter sw = new StringWriter(512);
		this.write(object, sw);
		return sw.toString();
	}


	/* Reading Objects */

	/**
	 * Reads the object from the source given.
	 * For JSON format the exact target class
	 * must be provided! For XML, in general,
	 * instead of setting null class, set it
	 * to {@code Object.class}.
	 */
	@SuppressWarnings("unchecked")
	public <T> T   read(Source source, Class<T> cls)
	{
		EX.assertn(cls);

		try
		{
			//~: create the un-marshaller
			Unmarshaller u = this.ctx.createUnmarshaller();
			for(Map.Entry<String, String> e : readProps.entrySet())
				u.setProperty(e.getKey(), propertyValue(e.getValue()));

			//!: do read
			Object res; if(Object.class.equals(cls))
				res = u.unmarshal(source);
			else
				res = u.unmarshal(source, cls);

			//?: {it's wrapped}
			if(res instanceof JAXBElement)
				res = ((JAXBElement)res).getValue();

			//~: check the result
			EX.assertx( (res == null) || cls.isAssignableFrom(res.getClass()),
			  "JAXB-XML mapped object of class [", LU.cls(res),
			  "] is not of the request class [", LU.cls(cls), "]!"
			);

			return (T)res;
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured when unmarshalling document!");
		}
	}

	public <T> T   read(Reader stream, Class<T> cls)
	{
		return read(new StreamSource(stream), cls);
	}

	public <T> T   read(String str, Class<T> cls)
	{
		return read(new StringReader(str), cls);
	}

	public <T> T   read(InputStream stream, Class<T> cls)
	{
		return read(new StreamSource(stream), cls);
	}

	@SuppressWarnings("unchecked")
	public <T> T   read(boolean gzip, Class<T> cls, byte[] bytes)
	{
		InputStream is = new ByteArrayInputStream(bytes);

		try
		{
			//?: {data are gun-zipped}
			if(gzip) is = new GZIPInputStream(is);

			//~: decode the object
			Object res = read(new StreamSource(is), cls);
			is.close();

			//~: check the result
			EX.assertx( (res == null) || cls.isAssignableFrom(res.getClass()),
			  "JAXB-XML mapped object of class [", LU.cls(res),
			  "] is not of the request class [", LU.cls(cls), "]!"
			);

			return (T) res;
		}
		catch(Exception e)
		{
			throw EX.wrap(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T   clone(T obj)
	{
		if(obj == null)
			return null;

		BytesStream s = new BytesStream();
		s.setNotClose(true);

		try
		{
			//~: write object
			this.write(obj, s);

			//~: read it back
			return (T) read(s.inputStream(), obj.getClass());
		}
		finally
		{
			s.closeAlways();
		}
	}


	/* protected: support routines */

	protected Object propertyValue(String v)
	{
		if("true".equals(v))
			return Boolean.TRUE;
		else if("false".equals(v))
			return Boolean.FALSE;

		return v;
	}
}