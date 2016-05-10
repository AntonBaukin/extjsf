package net.java.jsf.extjs.objects;

/* Java */

import java.math.BigDecimal;
import java.util.List;

/* Java API for XML Binding */

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Test data model object containing items.
 *
 * @author anton.baukin@gmail.com.
 */
@XmlRootElement(name = "object")
@XmlType(name = "object", propOrder = {
  "id", "name", "total", "best", "items"
})
public class SampleObject
{
	@XmlAttribute(name = "id")
	public String getId()
	{
		return id;
	}

	private String id;

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	private String name;

	public void setName(String name)
	{
		this.name = name;
	}

	public BigDecimal getTotal()
	{
		return total;
	}

	private BigDecimal total;

	public void setTotal(BigDecimal total)
	{
		this.total = total;
	}

	public SampleItem getBest()
	{
		return best;
	}

	private SampleItem best;

	public void setBest(SampleItem best)
	{
		this.best = best;
	}

	@XmlElement(name = "item")
	@XmlElementWrapper(name = "items")
	public List<SampleItem> getItems()
	{
		return items;
	}

	private List<SampleItem> items;

	public void setItems(List<SampleItem> items)
	{
		this.items = items;
	}


	/* Object */

	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SampleObject that = (SampleObject)o;

		if(id != null?!id.equals(that.id):that.id != null) return false;
		if(name != null?!name.equals(that.name):that.name != null) return false;
		if(total != null?!total.equals(that.total):that.total != null) return false;
		if(best != null?!best.equals(that.best):that.best != null) return false;
		return !(items != null?!items.equals(that.items):that.items != null);
	}
}