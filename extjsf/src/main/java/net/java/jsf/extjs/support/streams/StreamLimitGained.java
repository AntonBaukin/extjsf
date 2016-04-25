package net.java.jsf.extjs.support.streams;

/* Java */

import java.io.IOException;


/**
 * Thrown when {@link BytesStream} gains
 * the limit of the data bytes.
 *
 * @author anton.baukin@gmail.com.
 */
public class StreamLimitGained extends IOException
{
	/* public: constructor */

	public StreamLimitGained(BytesStream s, long limit)
	{
		this.stream = s;
		this.limit  = limit;
	}

	public StreamLimitGained(BytesStream s, long limit, byte[] buf, int offset, int left)
	{
		this.stream = s;
		this.limit  = limit;
		this.buf    = buf;
		this.offset = offset;
		this.left   = left;
	}


	/* public: access interface */

	/**
	 * The stream. Note that it is still opened!
	 */
	public BytesStream  getStream()
	{
		return stream;
	}

	public long         getLimit()
	{
		return limit;
	}

	/**
	 * The input buffer caused the limit gain.
	 */
	public byte[]       getBuffer()
	{
		return buf;
	}

	/**
	 * The offset in the input buffer at the point
	 * where the limit was reached.
	 */
	public int          getOffset()
	{
		return offset;
	}

	/**
	 * The write length left in the input buffer
	 * at the point where the limit was reached.
	 */
	public int          getLeft()
	{
		return left;
	}


	/* private: references */

	private BytesStream stream;
	private long        limit;

	private byte[]      buf;
	private int         offset;
	private int         left;
}
