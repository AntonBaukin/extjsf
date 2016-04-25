package net.java.jsf.extjs.model.store;

/**
 * Internal event of Models Store Service.
 *
 * Derive it from events class supporting delayed
 * execution in events drive layer of your system.
 *
 *
 * @author anton.baukin@gmail.com.
 */
public class ModelsStoreEvent
{
	/* Models Store Event */

	public long     getEventTime()
	{
		return eventTime;
	}

	private long    eventTime;

	public void     setEventTime(long eventTime)
	{
		this.eventTime = eventTime;
	}

	/**
	 * This flag tells the service
	 * to cleanup the persistent backend.
	 */
	public boolean  isSweep()
	{
		return sweep;
	}

	private boolean sweep;

	public void     setSweep(boolean sweep)
	{
		this.sweep = sweep;
	}

	/**
	 * This flag tells the service to save Model
	 * Beans removed from the in-memory cache.
	 */
	public boolean  isSynch()
	{
		return synch;
	}

	private boolean synch;

	public void     setSynch(boolean synch)
	{
		this.synch = synch;
	}
}