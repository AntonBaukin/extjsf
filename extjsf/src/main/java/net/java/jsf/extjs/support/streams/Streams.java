package net.java.jsf.extjs.support.streams;

/* Java */

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * Collection of simple tiny streams.
 *
 * @author anton.baukin@gmail.com.
 */
public class Streams
{
	/**
	 * Input stream filter that never
	 * closes the underlie stream till
	 * it is ordered directly.
	 */
	public static class NotCloseInput extends FilterInputStream
	{
		public NotCloseInput(InputStream out)
		{
			super(out);
		}

		public void allowClose()
		{
			if(close == null)
				close = true;
		}

		private Boolean close;

		public void close()
		  throws IOException
		{
			if(Boolean.TRUE.equals(close))
			{
				close = false;
				super.close();
			}
		}
	}

	/**
	 * Output stream filter that never
	 * closes the underlie stream till
	 * it is ordered directly.
	 */
	public static class NotCloseOutput extends FilterOutputStream
	{
		public NotCloseOutput(OutputStream out)
		{
			super(out);
		}

		public void allowClose()
		{
			if(close == null)
				close = true;
		}

		private Boolean close;

		public void close()
		  throws IOException
		{
			if(Boolean.TRUE.equals(close))
			{
				close = false;
				super.close();
			}
		}
	}

	/**
	 * Writer that does nothing.
	 */
	public static class NullWriter extends Writer
	{
		public void write(char[] cbuf, int off, int len)
		{
			int a = 0;
		}

		public void flush()
		{}

		public void close()
		{}
	}

	/**
	 * Reader that has no content.
	 */
	public static class EmptyReader extends Reader
	{
		public int read(char[] cbuf, int off, int len)
		{
			return -1;
		}

		public void close()
		{}
	}

	public static class ReadWrapper extends Reader
	{
		public int read(char[] cbuf, int off, int len)
		  throws IOException
		{
			if(reader == null)
				return -1;
			return reader.read(cbuf, off, len);
		}

		public void close()
		  throws IOException
		{
			if(reader != null) try
			{
				if(close)
					reader.close();
			}
			finally
			{
				reader = null;
			}
		}

		public ReadWrapper setReader(Reader reader)
		{
			this.reader = reader;
			return this;
		}

		protected Reader reader;

		public ReadWrapper setClose(boolean close)
		{
			this.close = close;
			return this;
		}

		protected boolean close = true;
	}

	public static class WriteWrapper extends Writer
	{
		public void write(char[] cbuf, int off, int len)
		  throws IOException
		{
			if(writer != null)
				writer.write(cbuf, off, len);
		}

		public void flush()
		  throws IOException
		{
			if(writer != null)
				writer.flush();
		}

		public void close()
		  throws IOException
		{
			if(writer != null) try
			{
				if(close)
					writer.close();
			}
			finally
			{
				writer = null;
			}
		}

		protected Writer writer;

		public WriteWrapper setWriter(Writer writer)
		{
			this.writer = writer;
			return this;
		}

		public WriteWrapper setClose(boolean close)
		{
			this.close = close;
			return this;
		}

		protected boolean close = true;
	}
}
