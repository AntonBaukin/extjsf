package net.java.jsf.extjs.support.jaxb;

/* Java */

import java.math.BigDecimal;

/* Java XML Binding */

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Used for JSON bindings. Forces Big Decimal
 * to be converted to string, not number.
 *
 * @author anton.baukin@gmail.com.
 */
public class   BigDecimalAdapter
       extends XmlAdapter<String, BigDecimal>
{
	public static final BigDecimalAdapter INSTANCE =
	  new BigDecimalAdapter();


	/* public: XmlAdapter interface */

	public BigDecimal unmarshal(String s)
	{
		return (s == null)?(null):(new BigDecimal(s));
	}

	public String     marshal(BigDecimal d)
	{
		return (d == null)?(null):(d.toString());
	}
}