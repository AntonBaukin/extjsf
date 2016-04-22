package net.java.jsf.extjs.model;

/**
 * Denotes a model that searches the entities
 * by the strings array.
 *
 * @author anton.baukin@gmail.com.
 */
public interface DataSearchModel
{
	/* Data Search Model */

	/**
	 * The search names as they are written
	 * in the search field.
	 */
	public String   getSearchNames();

	public void     setSearchNames(String searchNames);

	/**
	 * Returns array of the names to search based
	 * on split the whole input string of the names.
	 */
	public String[] searchNames();
}