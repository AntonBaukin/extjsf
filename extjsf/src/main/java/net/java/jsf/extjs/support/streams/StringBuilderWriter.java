package net.java.jsf.extjs.support.streams;

/* Java */

import java.io.Writer;


/**
 * Writer wrapper for the string builder.
 *
 * @author anton.baukin@gmail.com
 */
public final class StringBuilderWriter extends Writer
{
	/* public: constructor */

	public StringBuilderWriter(StringBuilder buf)
	{
		this.buf = buf;
	}

	public StringBuilderWriter(int capacity)
	{
		this(new StringBuilder(capacity));
	}


	/* public: StringBuilderWriter interface */

	public StringBuilder buffer()
	{
		return this.buf;
	}


	/* public: Writer interface */

	public void write(int c)
	{
		buf.append((char)c);
	}

	public void write(char cbuf[], int off, int len)
	{
		if((off < 0) | (off > cbuf.length) | (len < 0) |
		   ((off + len) > cbuf.length) | ((off + len) < 0)
		  )
			throw new IndexOutOfBoundsException();

		if(len == 0)
			return;

		buf.append(cbuf, off, len);
	}

	public void write(String str)
	{
		buf.append(str);
	}

	public void write(String str, int off, int len)
	{
		buf.append(str, off, off + len);
	}

	public void flush()
	{}

	public void close()
	{}

	public StringBuilderWriter append(CharSequence str)
	{
		buf.append((str == null)?("null"):(str));
		return this;
	}

	public StringBuilderWriter append(CharSequence str, int start, int end)
	{
		buf.append((str == null)?("null"):(str), start, end);
		return this;
	}

	public StringBuilderWriter append(char c)
	{
		buf.append(c);
		return this;
	}


	/* private: the string builder */

	private StringBuilder buf;
}