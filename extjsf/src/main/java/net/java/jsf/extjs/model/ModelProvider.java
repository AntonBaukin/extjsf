package net.java.jsf.extjs.model;

/**
 * Model Provider is interface to access
 * Model Beans (or create them on demand)
 * with else method, not from the models
 * storage point.
 *
 * @author anton.baukin@gmail.com
 */
public interface ModelProvider
{
	/* public: ModelProvider interface */

	public ModelBean provideModel();
}