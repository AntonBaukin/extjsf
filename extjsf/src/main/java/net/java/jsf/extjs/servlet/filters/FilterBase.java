package net.java.jsf.extjs.servlet.filters;

/**
 * Implementation base for Filters.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class FilterBase implements Filter
{
	/* Filter */

	public void openFilter(FilterTask task)
	{}

	public void closeFilter(FilterTask task)
	{}
}