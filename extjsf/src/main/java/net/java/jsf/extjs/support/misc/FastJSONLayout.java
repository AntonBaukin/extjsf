package net.java.jsf.extjs.support.misc;

/* Java */

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/* Logging for Java */

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Clear and fast direct printing writer.
 * I depends on no libraries.
 *
 * The JSON format is as follows:
 *
 *  § level  logging level (category);
 *  § ts     ISO timestamp in the format
 *           YYYY-MM-DDThh:mm:ss.mmm±hh:mm
 *  § thread thread name;
 *  § logger short name of the logger;
 *  § msg    log message text, if any;
 *  § class  full class name of the logger;
 *  § mdc    logging MDC, if not empty;
 *  § err    exception, if any.
 *
 *  Context map is a map of string-to-string
 *  properties (without nested objects).
 *
 *  Exception object format is as follows:
 *
 *  § msg    exception text, if any;
 *  § class  exception Java class full name;
 *  § stack  exception stack trace array;
 *  § cause  parent exception, if any.
 *
 *  Stack trace array item is object with fields:
 *
 *  § class  declaring class full name;
 *  § method method name;
 *  § line   line number.
 *
 *
 * @author anton.baukin@gmail.com.
 */
@Plugin(name = "FastJSONLayout", category = Node.CATEGORY,
  elementType = Layout.ELEMENT_TYPE)
public class FastJSONLayout extends AbstractStringLayout
{
	public FastJSONLayout()
	{
		super(Charset.forName("UTF-8"), HEADER, FOOTER);
	}

	@PluginFactory
	public static FastJSONLayout createLayout()
	{
		return new FastJSONLayout();
	}


	/* Layout */

	public String toSerializable(LogEvent e)
	{
		StringBuilder s = new StringBuilder(512);

		s.append(",\n\t{");

		//~: level
		tag(s, "level");
		String l; jss(s, l = String.valueOf(e.getLevel()));
		s.append('"');
		if(l.length() == 4)
			s.append(' ');

		//~: timestamp
		tag(s, "ts");
		timestamp(s, e);
		s.append('"');

		//~: thread name
		tag(s, "thread");
		jss(s, e.getThreadName());
		s.append('"');

		//~: logger name
		String logger = e.getLoggerName();
		if(logger != null)
		{
			String n = logger;
			int    i = logger.lastIndexOf('.');
			if(i != -1) n = logger.substring(i + 1);

			tag(s, "logger");
			jss(s, n);
			s.append('"');
		}

		//~: message
		tag(s, "msg");
		jss(s, e.getMessage().getFormattedMessage());
		s.append('"');

		//~: logger class
		if(logger != null)
		{
			tag(s, "class");
			jss(s, logger);
			s.append('"');
		}

		//~: mapped diagnostic context
		mdc(s, e);

		//~: exception
		err(s, e);

		s.append('}');
		return s.toString();
	}


	/* protected: printing */

	protected void timestamp(StringBuilder s, LogEvent e)
	{
		Calendar c = calendar.getAndSet(null);
		if(c == null) c = Calendar.getInstance();
		c.setTimeInMillis(e.getTimeMillis());

		//-->  YYYY-MM-DDThh:mm:ss.mmm±hh:mm

		//~: year
		lnn(s, 4, c.get(Calendar.YEAR));
		s.append('-');

		//~: month
		lnn(s, 2, c.get(Calendar.MONTH) + 1);
		s.append('-');

		//~: day
		lnn(s, 2, c.get(Calendar.DAY_OF_MONTH));
		s.append('T');

		//~: hour of a day
		lnn(s, 2, c.get(Calendar.HOUR_OF_DAY));
		s.append(':');

		//~: minutes
		lnn(s, 2, c.get(Calendar.MINUTE));
		s.append(':');

		//~: seconds
		lnn(s, 2, c.get(Calendar.SECOND));
		s.append('.');

		//~: milliseconds
		lnn(s, 3, c.get(Calendar.MILLISECOND));

		//~: time zone offset (truncated to minutes)
		int tz = (int)(c.getTimeZone().getRawOffset() / (1000L * 60));
		s.append((tz < 0)?('-'):('+'));
		lnn(s, 2, Math.abs(tz)/60);
		s.append(':');
		lnn(s, 2, Math.abs(tz)%60);

		calendar.compareAndSet(null, c);
	}

	protected final AtomicReference<Calendar> calendar =
	  new AtomicReference<>();

	protected void mdc(StringBuilder s, LogEvent e)
	{
		if((e.getContextMap() == null) || e.getContextMap().isEmpty())
			return;

		tag(s, "mdc", '{');

		//c: print each entry
		boolean first = true;
		for(Map.Entry<String, String> x : e.getContextMap().entrySet())
		{
			if(!first) s.append(", ");
			first = false;

			//~: key
			s.append('"'); jss(s, x.getKey());
			s.append("\": \"");

			//~: value
			jss(s, x.getValue()); s.append('"');
		}

		s.append("}");
	}

	protected void err(StringBuilder s, LogEvent e)
	{
		ThrowableProxy x = e.getThrownProxy();
		if(x == null) return;

		tag(s, "err", '{');
		err(s, x, 0);
		s.append("}");
	}

	protected void err(StringBuilder s, ThrowableProxy x, int level)
	{
		//~: message
		tag(s, "msg");
		jss(s, x.getMessage());
		s.append('"');

		//~: class
		tag(s, "class");
		jss(s, x.getName());
		s.append('"');

		//~: print stack trace
		tag(s, "stack", '[');
		StackTraceElement[] ts = x.getStackTrace();
		for(int i = 0;(i < ts.length);i++)
			err(s, ts[i], i + 1 == ts.length);
		s.append(']');

		//?: {has parent exception}
		x = x.getCauseProxy();
		if(x == null) return;

		tag(s, "cause", '{');
		err(s, x, level + 1);
		s.append("}");
	}

	protected void err(StringBuilder s, StackTraceElement t, boolean last)
	{
		s.append('{');

		//~: class name
		tag(s, "class");
		jss(s, t.getClassName());
		s.append('"');

		//~: method name
		tag(s, "method");
		jss(s, t.getMethodName());
		s.append('"');

		//~: line number
		tag(s, "line");
		s.append(t.getLineNumber());
		s.append('"');

		s.append(last?("}"):("}, "));
	}


	/* static: initialization */

	private static final byte[] HEADER;
	private static final byte[] FOOTER;

	static
	{
		try
		{
			//{"level": "OFF", "msg": "Fast JSON Log4j2 layout is started"}
			HEADER = ("[\n\t{\"level\": \"OFF\", \"msg\": \"Fast JSON Log4j2 " +
			  "layout is started\"}").getBytes(Charset.forName("UTF-8"));
			FOOTER = "\n]".getBytes(Charset.forName("UTF-8"));
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
	}


	/* protected: helpers */

	protected void        tag(StringBuilder b, String n)
	{
		tag(b, n, '"');
	}

	protected void        tag(StringBuilder b, String n, char open)
	{
		char x = b.charAt(b.length() - 1);
		if((x != '{') && (x != '['))
			b.append(", ");

		b.append('"').append(n).append("\": ");
		if(open != '\0')
			b.append(open);
	}

	/**
	 * Escapes string to place into Java Script source text.
	 * Note that XML entities are not encoded here, and you
	 * must protected XML text properly with CDATA sections.
	 */
	protected static void jss(StringBuilder b, String s)
	{
		if((s == null) || s.isEmpty())
			return;

		int l = s.length();
		b.ensureCapacity(b.length() + l);

		for(int i = 0;(i < l);i++)
		{
			char c = s.charAt(i);

			switch(c)
			{
				case '\"':
					b.append('\\').append('"');
					break;

				case '\\':
					b.append('\\').append('\\');
					break;

				case '/':
					b.append('\\').append('/');
					break;

				case '\t':
					b.append('\\').append('t');
					break;

				case '\n':
					b.append('\\').append('n');
					break;

				case '\r':
					b.append('\\').append('r');
					break;

				case '\b':
					b.append('\\').append('b');
					break;

				case '\f':
					b.append('\\').append('f');
					break;

				case '\0':
					b.append('\\').append('0');
					break;

				default  :
					b.append(c);
			}
		}
	}

	protected static void lnn(StringBuilder b, int l, int v)
	{
		String x = Integer.toString(v);

		for(int i = x.length();(i < l);i++)
			b.append('0');
		b.append(x);
	}
}