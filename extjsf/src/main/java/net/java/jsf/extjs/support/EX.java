package net.java.jsf.extjs.support;

/* Java */

import java.io.PrintWriter;
import java.util.Collection;

/* extjsf: support */

import net.java.jsf.extjs.support.streams.StringBuilderWriter;


/**
 * Exception handling support.
 *
 * @author anton.baukin@gmail.com
 */
public class EX
{
	/* other functions */

	/**
	 * Finds the text of the exception. Useful
	 * when original exception is wrapped in
	 * exceptions without text.
	 */
	public static String    e2en(Throwable e)
	{
		String r = null;

		while((r == null) && (e != null))
		{
			r = SU.s2s(e.getMessage());
			if(r == null) e = e.getCause();
		}

		return r;
	}

	/**
	 * The same as {@link #e2en(Throwable)},
	 * but searches for localized version.
	 */
	public static String    e2lo(Throwable e)
	{
		String r = null;

		while((r == null) && (e != null))
		{
			r = SU.s2s(e.getLocalizedMessage());
			if(r == null) e = e.getCause();
		}

		return r;
	}


	/* public: unwrapping  */

	/**
	 * Removes the {@link RuntimeException} wrappers
	 * having no own message.
	 */
	public static Throwable xrt(Throwable e)
	{
		while((e != null) && RuntimeException.class.equals(e.getClass()))
			if(e.getCause() == null)
				return e;
			else
			{
				String  a = e.getMessage();
				String  b = e.getCause().getMessage();

				//?: {message is not set}
				boolean x = (a == null);

				//?: {not null -> messages are the same}
				if(!x)  x = CMP.eq(a, b);

				//?: {not -> check as toString()}
				if(!x)  x = CMP.eq(a, e.getCause().toString());

				//?: {remove wrapper}
				if(x) e = e.getCause();
				else  return e;
			}

		return e;
	}

	@SuppressWarnings("unchecked")
	public static <E> E     search(Throwable e, Class<E> eclass)
	{
		while(e != null)
			if(eclass.isAssignableFrom(e.getClass()))
				return (E)e;
			else
				e = e.getCause();

		return null;
	}


	/* error printing */

	public static String    print(Throwable e)
	{
		StringBuilder s = new StringBuilder(512);

		print(e, s);
		return s.toString();
	}

	public static void      print(Throwable e, StringBuilder s)
	{
		String m = e2en(e);

		if(m != null)
			s.append(m).append('\n');

		PrintWriter p = new PrintWriter(new StringBuilderWriter(s), false);

		e.printStackTrace(p);
		p.flush();
	}


	/* assertions */

	public static void   assertx(boolean x, Object... msg)
	{
		if(x == false)
			throw ass(msg);
	}

	public static <T> T  assertn(T x, Object... msg)
	{
		if(x == null)
			throw ass(msg);
		return x;
	}

	public static void   asserte(Collection c, Object... msg)
	{
		if((c == null) || c.isEmpty())
			throw ass(msg);
	}

	public static void   asserte(Object[] a, Object... msg)
	{
		if((a == null) || (a.length == 0))
			throw ass(msg);
	}

	/**
	 * Forbids whitespace-empty strings, either
	 * as strings having side-whitespaces.
	 */
	public static String asserts(String s, Object... msg)
	{
		if((s == null) || (s.length() != s.trim().length()))
			throw ass(msg);
		return s;
	}


	/* exceptions */

	public static AssertionError   ass(Object... msg)
	{
		String s = SU.s2s(SU.cats(msg));
		if(s == null) return new AssertionError();
		return new AssertionError(SU.cats(msg));
	}

	public static RuntimeException state(Object... msg)
	{
		return new IllegalStateException(SU.cats(msg));
	}

	public static RuntimeException state(Throwable cause, Object... msg)
	{
		return new IllegalStateException(SU.cats(msg), cause);
	}

	public static RuntimeException arg(Object... msg)
	{
		return new IllegalArgumentException(SU.cats(msg));
	}

	public static RuntimeException arg(Throwable cause, Object... msg)
	{
		return new IllegalArgumentException(SU.cats(msg), cause);
	}

	public static RuntimeException unop(Object... msg)
	{
		return new UnsupportedOperationException(SU.s2s(SU.cats(msg)));
	}

	public static RuntimeException wrap(Throwable cause, Object... msg)
	{
		String s = SU.s2s(SU.cats(msg));

		//?: {has no own message}
		if(s == null)
			//?: {is runtime itself} do not wrap
			if(cause instanceof RuntimeException)
				return (RuntimeException) cause;
			//~: just take it's message
			else
				s = e2en(cause);

		return new RuntimeException(s, cause);
	}
}