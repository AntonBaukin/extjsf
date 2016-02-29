package net.java.jsf.extjs.support;

/* Java */

import java.math.BigDecimal;
import java.util.Date;


/**
 * Various comparisons.
 *
 * @author anton.baukin@gmail.com
 */
public class CMP
{
	/* Decimal Values */

	public static boolean grZero(BigDecimal n)
	{
		return (n != null) && (BigDecimal.ZERO.compareTo(n) < 0);
	}

	public static boolean greZero(BigDecimal n)
	{
		return (n != null) && (BigDecimal.ZERO.compareTo(n) <= 0);
	}

	public static boolean eqZero(BigDecimal n)
	{
		return (n != null) && (BigDecimal.ZERO.compareTo(n) == 0);
	}

	public static boolean eq(BigDecimal a, BigDecimal b)
	{
		return ((a == null) && (b == null)) ||
		  ((a != null) && (b != null) && (a.compareTo(b) == 0));
	}

	public static boolean gre(BigDecimal a, BigDecimal b)
	{
		EX.assertn(a);
		EX.assertn(b);
		return (a.compareTo(b) >= 0);
	}

	public static boolean gr(BigDecimal a, BigDecimal b)
	{
		EX.assertn(a);
		EX.assertn(b);
		return (a.compareTo(b) > 0);
	}


	/* Integers */

	public static int     cmp(Integer a, Integer b)
	{
		return (a == null && b == null)?(0):(a == null)?(-1):
		  (b == null)?(+1):Integer.compare(a, b);
	}


	/* General Equality */

	public static boolean eq(Object a, Object b)
	{
		return ((a == null) && (b == null)) ||
		  ((a != null) && a.equals(b));
	}

	public static boolean eq(Date a, Date b)
	{
		return ((a == null) && (b == null)) ||
		  ((a != null) && (a.getTime() == b.getTime()));
	}


	/* Strings Comparison */

	public static int     cmp(String a, String b)
	{
		if((a == null) && (b == null)) return 0;
		if(a == null) return -1; if(b == null) return +1;
		return a.compareTo(b);
	}

	public static int     cmpic(String a, String b)
	{
		if((a == null) && (b == null)) return 0;
		if(a == null) return -1; if(b == null) return +1;
		return a.compareToIgnoreCase(b);
	}
}