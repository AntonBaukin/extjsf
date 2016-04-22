package net.java.jsf.extjs.model;

/**
 * While model bean contains user related input
 * parameters (the UI state), model data bean
 * provides the data loaded from the database.
 *
 * Model data beans are mapped to XML or JSON, may
 * be serialized and transferred, but never stored
 * in the model or their store.
 *
 *
 * @author anton.baukin@gmail.com
 */
public interface ModelData
{
	/* Model Data */

	public ModelBean getModel();
}