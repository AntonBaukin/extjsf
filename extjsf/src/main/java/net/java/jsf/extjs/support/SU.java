package net.java.jsf.extjs.support;

/* Java */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * Strings helper functions.
 *
 * @author anton.baukin@gmail.com
 */
public class SU
{
	/* Simplifications */

	/**
	 * Breaks string into words. The words
	 * are separated by any character except
	 * a letter, a digit, '-', ':', '/', '\'.
	 *
	 * Side whitespaces of the words are ignored,
	 * empty strings are removed.
	 */
	public static String[] s2aw(String s)
	{
		if(s == null) return new String[0];

		List<String> a = new ArrayList<>(4);
		boolean      w = true, f;
		int          l, i, j, b = 0;

		for(i = 0, l = s.length();(i < l);i++)
		{
			char c = s.charAt(i);

			//?: {is a separator}
			f = !Character.isLetterOrDigit(c) &&
			  (c != '-' & c != ':' & c != '/' & c != '\\');

			//?: {in a word & break}
			if(w && f)
			{
				if(b != i)
					a.add(s.substring(b, i));
				w = false;
			}
			//?: {out of a word & enter}
			else if(!w && !f)
			{
				b = i;
				w = true;
			}
		}

		//?: {last word}
		if(w && b != i)
			a.add(s.substring(b, i));

		return a.toArray(new String[a.size()]);
	}

	/**
	 * Splits the string by the separators given.
	 * Note that whitespaces are not trimmed here.
	 */
	public static String[] s2a(String s, char... p)
	{
		if(s == null) return new String[0];
		EX.assertx(p.length != 0);

		List<String> a = new ArrayList<>(4);
		boolean      w = true, f;
		int          l, i, j, b = 0;

		for(i = 0, l = s.length();(i < l);i++)
		{
			char c = s.charAt(i);

			//?: {is a separator}
			for(f = false, j = 0;(j < p.length);j++)
				if(c == p[j]) { f = true; break; }

			//?: {in a word & break}
			if(w && f)
			{
				if(b != i)
					a.add(s.substring(b, i));
				w = false;
			}
			//?: {out of a word & enter}
			else if(!w && !f)
				{ b = i; w = true; }
		}

		//?: {last word}
		if(w && b != i)
			a.add(s.substring(b, i));

		return a.toArray(new String[a.size()]);
	}

	/**
	 * Checks whether the string is {@code null}
	 * or has only whitespaces: if so, returns
	 * {@code null}.
	 *
	 * Otherwise returns the string trimmed. 
	 */
	public static String   s2s(String s)
	{
		return (s == null)?(null):
		  ((s = s.trim()).length() != 0)?(s):(null);
	}

	public static String   s2s(CharSequence s)
	{
		return (s == null)?(null):(s2s(s.toString()));
	}

	public static String   sXs(String s)
	{
		return (s != null)?(s):("");
	}

	@SuppressWarnings("unchecked")
	public static int      sXl(Object... objs)
	{
		int size = 0;

		if(objs != null) for(Object s : objs)
			if(s instanceof CharSequence)
				size += ((CharSequence)s).length();
			else if(s instanceof Object[])
				for(Object x : (Object[])s)
					size += sXl(x);
			else if(s instanceof Collection)
				for(Object x : (Collection)s)
					size += sXl(x);

		return size;
	}

	/**
	 * Returns an array of not-whitespace-strings
	 * collected from the array provided.
	 *
	 * Omits {@code null} strings. Returns always
	 * not {@code null}!
	 *
	 * A copy of the original array is always created.
	 */
	public static String[] a2a(String[] a)
	{
		if(a == null) return new String[0];

		String[] r;
		int      i = 0, l = 0;

		for(String s : a)
			if(!sXe(s)) l++;

		r = new String[l];
		for(String s : a)
			if(!sXe(s)) r[i++] = s2s(s);

		return r;
	}


	/* Comparisons */

	/**
	 * Returns {@code true} if the string is {@code null},
	 * empty, or contains only whitespaces.
	 */
	public static boolean  sXe(CharSequence s)
	{
		int l; if((s == null) || ((l = s.length()) == 0))
			return true;

		for(int i = 0;(i < l);i++)
			if(!Character.isWhitespace(s.charAt(i)))
				return false;
		return true;
	}


	/* Escape Routines */

	/**
	 * Escapes string to place into Java Script
	 * source text. Note that XML entities
	 * are not encoded here, and you must
	 * protected XML text properly with CDATA
	 * sections.
	 */
	public static String  jss(Object sobj)
	{
		if(sobj == null) return "";

		CharSequence  s = (sobj instanceof CharSequence)
		  ?((CharSequence)sobj):(sobj.toString());
		StringBuilder b;
		int           l = s.length();
		int           e = 0;

		//count the number of escaped characters
		for(int i = 0;(i < l);i++)
			if(Arrays.binarySearch(JS_ESC_K, s.charAt(i)) >= 0)
				e++;

		//?: {no escape symbols found} return the original string
		if(e == 0) return s.toString();
		b = new StringBuilder(l + e);

		// encode the string

		for(int i = 0;(i < l);i++)
		{
			char c = s.charAt(i);

			//lookup the replacement
			e = Arrays.binarySearch(JS_ESC_K, c);

			//?: {replacement not found}
			if(e < 0) b.append(c);
			else b.append('\\').append(JS_ESC_V[e]); //<-- escape it
		}

		//~encode the string

		return b.toString();
	}

	private static char[] JS_ESC_K =
	  {'\'', '\"', '\\', '\t', '\n', '\r'};

	private static char[] JS_ESC_V =
	  {'\'', '\"', '\\', 't', 'n', 'r'};

	static
	{
		TreeMap<Character, Character> m =
		  new TreeMap<>();

		for(int i = 0;(i < JS_ESC_K.length);i++)
			m.put(JS_ESC_K[i], JS_ESC_V[i]);

		int i = 0;
		for(Iterator it = m.entrySet().iterator();(it.hasNext());i++)
		{
			Entry e = (Entry)(it.next());
			JS_ESC_K[i] = (Character)e.getKey();
			JS_ESC_V[i] = (Character)e.getValue();
		}
	}

	public static String   escapeXML(Object sobj)
	{
		return (sobj == null)?(null)
		  :(escapeXML(sobj, null));
	}

	public static String   escapeXML(Object sobj, StringBuilder sb)
	{
		if(sobj == null) return null;

		CharSequence s = (sobj instanceof CharSequence)
		  ?((CharSequence)sobj):(sobj.toString());
		int          l = s.length();

		if(sb != null)
			sb.delete(0,sb.length());
		else
			sb = new StringBuilder(l*108/100);

		for(int j = 0;(j < l);j++)
		{
			char c = s.charAt(j);
			int  p = Arrays.binarySearch(XESC_SYMS,c);

			if(p < 0) sb.append(c);
			else      sb.append(XESC_REPL[p]);
		}

		return sb.toString();
	}

	private static char[]   XESC_SYMS = new char[]
	  {'<',    '>',    '\"',     '\'',     '&'};

	private static String[] XESC_REPL = new String[]
	  {"&lt;", "&gt;", "&quot;", "&#39;", "&amp;"};

	static
	{
		char[]   TXESC_SYMS = new char[XESC_SYMS.length];
		String[] TXESC_REPL = new String[XESC_REPL.length];

		//copy symbols & sort them
		System.arraycopy(XESC_SYMS, 0,
		  TXESC_SYMS, 0, XESC_SYMS.length);
		Arrays.sort(TXESC_SYMS);

		//fit replacement strings
		for(int i = 0;(i < XESC_SYMS.length); i++)
			TXESC_REPL[i] = XESC_REPL[
			  indexOf(XESC_SYMS, TXESC_SYMS[i])];

		//set the arrays
		XESC_SYMS = TXESC_SYMS;
		XESC_REPL = TXESC_REPL;
	}

	private static int indexOf(char[] chs, char ch)
	{
		for(int i = 0;(i < chs.length);i++)
			if(chs[i] == ch)
				return i;
		return -1;
	}


	/* Buffering */

	/**
	 * Concatenates the objects previously converted
	 * to strings. Handles {@code null} values just
	 * skipping them.
	 */
	public static StringBuilder cat(Object... objs)
	{
		StringBuilder s = new StringBuilder(sXl(objs));
		scat(s, "", Arrays.asList(objs));
		return s;
	}

	public static String        cats(Object... objs)
	{
		return cat(objs).toString();
	}

	@SuppressWarnings("unchecked")
	public static String        scats(String sep, Object... objs)
	{
		StringBuilder s = new StringBuilder(
		  sep.length() * objs.length + sXl(objs));

		scat(s, sep, Arrays.asList(objs));
		return s.toString();
	}

	public static void          scat(StringBuilder s, String sep, Collection objs)
	{
		_scat(s, s.length(), sep, objs);
	}

	@SuppressWarnings("unchecked")
	private static void         _scat
	  (StringBuilder s, int l, String sep, Collection objs)
	{
		for(Object o : objs) if(o != null)
		{
			if(o instanceof Object[])
				o = Arrays.asList((Object[])o);

			if(o instanceof Collection)
			{
				_scat(s, l, sep, (Collection)o);
				continue;
			}

			if(s.length() != l)
				s.append(sep);
			s.append(o);
		}
	}
}