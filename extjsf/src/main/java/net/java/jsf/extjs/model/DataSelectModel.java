package net.java.jsf.extjs.model;

/**
 * Implement this interface in model beans to note
 * that model supports selection of data objects
 * with given limit and start position.
 *
 * @author anton.baukin@gmail.com
 */
public interface DataSelectModel
{
	/* Data Select Model */

	public Integer  getDataStart();

	public void     setDataStart(Integer start);

	public Integer  getDataLimit();

	public void     setDataLimit(Integer start);
}