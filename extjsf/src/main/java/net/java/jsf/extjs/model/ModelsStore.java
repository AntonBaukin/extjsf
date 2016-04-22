package net.java.jsf.extjs.model;

/**
 * Model Store is a container of the User Interface Model
 * Beans with in-memory cache and a persistence backend.
 *
 * @author anton.baukin@gmail.com
 */
public interface ModelsStore
{
	/* Models Store */

	/**
	 * Adds the bean instance to the models store.
	 *
	 * If bean has the model key assigned, checks whether
	 * there is a bean with this key already registered.
	 * If so, overwrites it and returns the removed one.
	 *
	 * If model key is not assigned, assigns it.
	 */
	public ModelBean add(ModelBean bean);

	/**
	 * Removes the bean from the Store caches
	 * and from the persistent backend.
	 */
	public ModelBean remove(String key);

	/**
	 * As the find operation returns the bean instance
	 * registered by the key given. The last read time
	 * of the bean is automatically set to the present.
	 */
	public ModelBean read(String key);
}