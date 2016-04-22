package net.java.jsf.extjs.model;

/* Java */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * Model bean to select a range of
 * the related data objects.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class DataSelectModelBean
       extends        ModelBeanBase
       implements     DataSelectModel, DataSortModel, DataSearchModel
{
	/* Data Select Model */

	public Integer getDataStart()
	{
		return dsd.getDataStart();
	}

	public void setDataStart(Integer dataStart)
	{
		dsd.setDataStart(dataStart);
	}

	public Integer getDataLimit()
	{
		return dsd.getDataLimit();
	}

	public void setDataLimit(Integer dataLimit)
	{
		dsd.setDataLimit(dataLimit);
	}


	/* Data Sort Model */

	public String[] getSortProps()
	{
		return dsd.getSortProps();
	}

	public void setSortProps(String[] sortProps)
	{
		dsd.setSortProps(sortProps);
	}

	public boolean[] getSortDesc()
	{
		return dsd.getSortDesc();
	}

	public void setSortDesc(boolean[] sortDesc)
	{
		dsd.setSortDesc(sortDesc);
	}

	public int sortSize()
	{
		return dsd.sortSize();
	}

	public void clearSort()
	{
		dsd.clearSort();
	}

	public void addSort(String prop, boolean desc)
	{
		dsd.addSort(prop, desc);
	}

	public String getFirstSortProp()
	{
		String[] sp = getSortProps();
		return ((sp == null) || (sp.length == 0))?(null):(sp[0]);
	}

	public String getFirstSortDir()
	{
		boolean[] sd = getSortDesc();
		return ((sd == null) || (sd.length == 0))?(null):sd[0]?("desc"):("asc");
	}


	/* Data Search Model */

	public String getSearchNames()
	{
		return dsd.getSearchNames();
	}

	public void setSearchNames(String searchNames)
	{
		dsd.setSearchNames(searchNames);
	}

	public String[] searchNames()
	{
		return dsd.searchNames();
	}


	/**
	 * Data selection delegate.
	 */
	private DataSelectDelegate dsd = new DataSelectDelegate();


	/* Serialization */

	public void writeExternal(ObjectOutput o)
	  throws IOException
	{
		super.writeExternal(o);
		dsd.writeExternal(o);
	}

	public void readExternal(ObjectInput i)
	  throws IOException, ClassNotFoundException
	{
		super.readExternal(i);
		dsd.readExternal(i);
	}
}