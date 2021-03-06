package net.java.jsf.extjs.support;

/* Java */

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/* extjsf: support (streams) */

import net.java.jsf.extjs.support.streams.BytesStream;


/**
 * Input-output helping functions.
 *
 * @author anton.baukin@gmail.com.
 */
public class IO
{
	/* Serialization Support */

	/**
	 * 64 MBytes. Maximum size of the compressed bytes of
	 * XML text, or any other bynary array.
	 */
	public static final long MAX_OUT_BYTES = 64 * 1024 * 1024;

	public static void   xml(DataOutput d, Object bean)
	{
		try(BytesStream bs = new BytesStream())
		{
			//!: compress the document
			GZIPOutputStream gz = new GZIPOutputStream(bs);
			OU.obj2xml(gz, bean);

			//~: close the compression
			bs.setNotCloseNext(true);
			gz.close();

			//?: {too big}
			EX.assertx(bs.length() <= MAX_OUT_BYTES,
			  "Object as XML Document is too big!");

			//~: write the bytes
			d.writeLong(bs.length());
			d.write(bs.bytes());
		}
		catch(IOException e)
		{
			throw EX.wrap(e);
		}
	}

	public static Object xml(DataInput d)
	{
		return IO.xml(d, null);
	}

	public static <O> O  xml(DataInput d, Class<O> cls)
	{
		try
		{
			//~: bytes number
			long s = d.readLong();
			EX.assertx((s > 0) & (s <= MAX_OUT_BYTES));

			//~: read that bytes
			byte[] b = new byte[(int) s];
			d.readFully(b);

			//~: read the object
			try(GZIPInputStream gz =
			  new GZIPInputStream(new ByteArrayInputStream(b)))
			{
				return OU.xml2obj(gz, cls);
			}
		}
		catch(IOException e)
		{
			throw EX.wrap(e);
		}
	}

	public static void   obj(ObjectOutput o, Object obj)
	{
		EX.assertx((obj == null) || (obj instanceof Serializable));

		try
		{
			o.writeObject(obj);
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occurred while Java-serializing object of class [",
			  LU.cls(obj), "]!");
		}
	}

	public static Object obj(ObjectInput i)
	{
		try
		{
			return i.readObject();
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occurred reading Java-serialized object!");
		}
	}

	@SuppressWarnings("unchecked")
	public static <O> O  obj(ObjectInput i, Class<O> cls)
	{
		Object res = IO.obj(i);

		if((res != null) && (cls != null) && !cls.isAssignableFrom(res.getClass()))
			throw EX.state("Can't cast Java-serialized instance of class [",
			  LU.cls(res), "] to the required class [", LU.cls(cls), "]!"
			);

		return (O)res;
	}

	/**
	 * Writes string as UTF-8 bytes array after the
	 * 4-bytes prefix where the length of the bytes
	 * array is written.
	 *
	 * Undefined strings are also allowed! They have
	 * FF-FF-FF-FF prefix.
	 */
	public static void   str(ObjectOutput o, String s)
	  throws IOException
	{
		if(s == null)
		{
			o.writeInt(0xFFFFFFFF);
			return;
		}

		byte[] buf = s.getBytes("UTF-8");

		o.writeInt(buf.length);
		o.write(buf);
	}

	public static String str(ObjectInput i)
	  throws IOException
	{
		int l = i.readInt();

		//?: {undefined string}
		if(l == 0xFFFFFFFF)
			return null;

		byte[] buf = new byte[l];
		i.readFully(buf);

		return new String(buf, "UTF-8");
	}

	/**
	 * Write the class name to the stream after the
	 * 2-bytes prefix with the length of UTF-8 buffer.
	 *
	 * Undefined strings are also allowed! They have
	 * FF-FF prefix.
	 */
	public static void   cls(ObjectOutput o, Class cls)
	  throws IOException
	{
		if(cls == null)
		{
			o.writeShort(0xFFFF);
			return;
		}

		byte[] buf = cls.getName().getBytes("UTF-8");
		EX.assertx(buf.length < 0xFFFF);

		o.writeShort(buf.length);
		o.write(buf);
	}

	public static Class  cls(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		int l = i.readUnsignedShort();
		EX.assertx(l <= 0xFFFF);

		//?: {undefined string}
		if(l == 0xFFFF)
			return null;

		byte[] buf = new byte[l];
		i.readFully(buf);

		return Thread.currentThread().getContextClassLoader().
		  loadClass(new String(buf, "UTF-8"));
	}

	protected static final byte FALSE = (byte) 1;
	protected static final byte TRUE  = (byte) 2;
	protected static final byte LONG  = (byte) 3;
	protected static final byte INT   = (byte) 4;

	public static void    longer(ObjectOutput o, Long n)
	  throws IOException
	{
		//?: {undefined}
		if(n == null)
			o.writeByte(-LONG);
		else
		{
			o.writeByte(LONG);
			o.writeLong(n);
		}
	}

	public static Long    longer(ObjectInput i)
	  throws IOException
	{
		byte t = i.readByte();

		//?: {undefined}
		if(t == -LONG)
			return null;
		else
		{
			EX.assertx(t == LONG);
			return i.readLong();
		}
	}

	public static void    inter(ObjectOutput o, Integer n)
	  throws IOException
	{
		//?: {undefined}
		if(n == null)
			o.writeByte(-INT);
		else
		{
			o.writeByte(INT);
			o.writeInt(n);
		}
	}

	public static Integer inter(ObjectInput i)
	  throws IOException
	{
		byte t = i.readByte();

		//?: {undefined}
		if(t == -INT)
			return null;
		else
		{
			EX.assertx(t == INT);
			return i.readInt();
		}
	}

	public static void    booler(ObjectOutput o, Boolean x)
	  throws IOException
	{
		//?: {undefined}
		if(x == null)
			o.writeByte(-TRUE);
		else
			o.writeByte(x?(TRUE):(FALSE));
	}

	public static Boolean booler(ObjectInput i)
	  throws IOException
	{
		byte t = i.readByte();

		//?: {undefined}
		if(t == -TRUE)
			return null;
		//?: {true}
		else if(t == TRUE)
			return true;
		//?: {false}
		else if(t == FALSE)
			return false;
		else
			throw EX.state();
	}

	public static void    bytes(ObjectOutput o, byte[] b)
	  throws IOException
	{
		if(b == null)
		{
			o.writeInt(-1);
			return;
		}

		EX.assertx(b.length <= MAX_OUT_BYTES);

		o.writeInt(b.length);
		o.write(b);
	}

	public static byte[]  bytes(ObjectInput i)
	  throws IOException
	{
		byte[] b;
		int    s = i.readInt();

		//?: {undefined}
		if(s == -1)
			return null;

		EX.assertx(s >= 0 && s <= MAX_OUT_BYTES);
		i.readFully(b = new byte[s]);

		return b;
	}
}