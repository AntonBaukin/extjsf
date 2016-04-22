package net.java.jsf.extjs.model;

/**
 * Each UI model element implements this interface.
 * Model is created for distinct user and is stored
 * on the server side in the memory cache and the DB.
 *
 * @author anton.baukin@gmail.com
 */
public interface ModelBean extends java.io.Externalizable
{
	/* Model Bean */

	/**
	 * The key of this model element unique within the container.
	 * Assigned automatically by the model and may not altered.
	 */
	public String    getModelKey();

	public void      setModelKey(String key);

	/**
	 * Returns the data access object derived from
	 * this model bean. Undefined result means that
	 * this model has no data model related.
	 *
	 * Data objects must not be stored in the model!
	 */
	public ModelData modelData();
}