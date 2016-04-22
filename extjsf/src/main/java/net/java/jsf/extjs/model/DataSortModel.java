package net.java.jsf.extjs.model;

/**
 * Defines model of selecting data with
 * sorting on one or more properties.
 *
 * @author anton.baukin@gmail.com
 */
public interface DataSortModel
{
	/* Data Sort Model (bean) */

	public String[]  getSortProps();

	public void      setSortProps(String[] ps);

	public boolean[] getSortDesc();

	public void      setSortDesc(boolean[] sd);


	/* Data Sort Model (support) */

	public int       sortSize();

	public void      clearSort();

	public void      addSort(String prop, boolean desc);
}