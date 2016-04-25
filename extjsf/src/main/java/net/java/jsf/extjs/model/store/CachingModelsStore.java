package net.java.jsf.extjs.model.store;

/* Java */

import java.util.Map;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelsStore;


/**
 * Models Store with caching abilities.
 *
 * @author anton.baukin@gmail.com.
 */
public interface CachingModelsStore extends ModelsStore
{
	/* Caching Models Store */

	/**
	 * Delegate invokes this method to select Model Entries
	 * in the cache that are to remove from it. Note that
	 * the items still left in the cache till save commit
	 * is invoked, see {@link #commitSaved(Map, boolean)}.
	 *
	 * The integer value of the map is copy of the entry
	 * access counter at the time the entry was copied.
	 */
	public void copyPruned(Map<ModelEntry, Integer> items);

	/**
	 * The same as {@link #copyPruned(Map)}, but returns
	 * all the items of the cache.
	 */
	public void copyAll(Map<ModelEntry, Integer> items);

	/**
	 * Delegate notifiers the cache that the items listed
	 * are saved in the persistent backend and may be removed
	 * from the cache. The Store checks the access counters
	 * given with the values are in the cache: if counters
	 * do match, the entry is removed from the map; else,
	 * the mapped counter is updated, and the entry must
	 * be repeatedly saved.
	 *
	 * Committed items with valid counters must be removed
	 * from the cache when the flag is set. Pruned items
	 * are always removed.
	 */
	public void commitSaved(Map<ModelEntry, Integer> items, boolean remove);


	/* Caching Delegate */

	public static interface CachingDelegate
	       extends          ModelsStoreBase.Delegate
	{
		/* Caching Delegate */

		/**
		 * Invoked when the cache gains it's capacity limits
		 * and must be asked to persist a part of it's data.
		 *
		 * On this call, Store Delegate must plan to save
		 * the entities pruned.
		 *
		 * The call tells the number of items to save.
		 */
		public void overflow(int size);
	}
}