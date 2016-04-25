package net.java.jsf.extjs.model.store;

/* Java */

import java.util.Collection;


/**
 * Strategy to permanently store UI Models.
 *
 * @author anton.baukin@gmail.com.
 */
public interface ModelsBackend
{
	/* Models Backend */

	public void find(ModelEntry e);

	public void store(Collection<ModelEntry> es);

	public void remove(ModelEntry e);

	/**
	 * Remove stored entries with last update time
	 * age before the back time given (milliseconds).
	 */
	public void sweep(long backTime);
}