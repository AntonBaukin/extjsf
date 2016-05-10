package net.java.jsf.extjs.objects;

/* Java */

import java.math.BigDecimal;

/* Java API for XML Binding */

import javax.xml.bind.annotation.XmlType;


/**
 * Item of test data model object.
 *
 * @author anton.baukin@gmail.com.
 */
@XmlType(name = "item", propOrder = {
  "code", "volume"
})
public class SampleItem
{
	public String getCode()
	{
		return code;
	}

	private String code;

	public void setCode(String code)
	{
		this.code = code;
	}

	public BigDecimal getVolume()
	{
		return volume;
	}

	private BigDecimal volume;

	public void setVolume(BigDecimal volume)
	{
		this.volume = volume;
	}


	/* Object */

	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SampleItem that = (SampleItem)o;

		if(code != null?!code.equals(that.code):that.code != null) return false;
		return !(volume != null?!volume.equals(that.volume):that.volume != null);
	}
}