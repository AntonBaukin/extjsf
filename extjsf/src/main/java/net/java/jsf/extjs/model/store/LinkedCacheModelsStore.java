package net.java.jsf.extjs.model.store;

/* Java */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Cache applying Hash Map and linked list of the entries.
 * Prunes the last recently used items.
 *
 * @author anton.baukin@gmail.com.
 */
public class LinkedCacheModelsStore
       extends    ModelsStoreBase
       implements CachingModelsStore
{
	/* public: constructor */

	public LinkedCacheModelsStore(int size)
	{
		EX.assertx(size > 0);
		this.size    = size;
		this.entries = new HashMap<String, LinkedEntry>(size);
		this.pruned  = new HashSet<LinkedEntry>(size / 10);
	}

	public final int size;


	/* Models Store Base */

	public void setDelegate(Delegate delegate)
	{
		EX.assertx((delegate == null) ||
		  (delegate instanceof CachingDelegate));

		super.setDelegate(delegate);
	}


	/* Caching Models Store */

	public void copyPruned(Map<ModelEntry, Integer> items)
	{
		synchronized(this)
		{
			for(LinkedEntry e : pruned)
				items.put(e, e.accessInc.intValue());
		}
	}

	public void copyAll(Map<ModelEntry, Integer> items)
	{
		synchronized(this)
		{
			for(LinkedEntry e : entries.values())
				items.put(e, e.accessInc.intValue());
		}
	}

	public void commitSaved(Map<ModelEntry, Integer> items, boolean remove)
	{
		synchronized(this)
		{
			Iterator<Map.Entry<ModelEntry, Integer>> i =
			  items.entrySet().iterator();

			while(i.hasNext())
			{
				Map.Entry<ModelEntry, Integer> p = i.next();

				//~: find the entry
				LinkedEntry e = entries.get(p.getKey().key);

				//?: {found it not} remove from the items
				if(e == null)
				{
					i.remove();
					continue;
				}

				//?: {has not the same counter}
				if(!e.accessInc.compareAndSet(p.getValue(), p.getValue()))
				{
					p.setValue(e.accessInc.get());
					continue;
				}

				i.remove(); //<-- tell the item was saved actual

				//~: remove from the pruned always
				if(pruned.remove(e))
				{
					entries.remove(e.key);
					continue; //<-- need not to unlink
				}

				//?: {do remove from the entries}
				if(remove)
				{
					entries.remove(e.key);
					unlink(e);
				}
			}
		}
	}


	/* protected: entries access */

	protected ModelEntry find(String key)
	{
		synchronized(this)
		{
			//?: {found it not}
			LinkedEntry e = entries.get(key);
			if(e == null) return null;

			//?: {this entry was pruned}
			if(pruned.remove(e))
				placeAsHead(e); //<-- place as the head
			else
				moveToHead(e);  //<-- just move to be head

			return e;
		}
	}

	protected void       remove(ModelEntry x)
	{
		synchronized(this)
		{
			//?: {removed it not}
			LinkedEntry e = entries.remove(x.key);
			EX.assertn(e);

			//?: {this entry was not pruned}
			if(!pruned.remove(e))
				unlink(e); //<-- remove from the list
		}
	}

	protected void       save(ModelEntry e)
	{
		if(!(e instanceof LinkedEntry))
			throw EX.ass("Not a Linked Model Entry!");

		int s, p = 0; synchronized(this)
		{
			//?: {it is not in the cache now}
			EX.assertx(entries.put(e.key, (LinkedEntry)e) == null);

			//~: place it as a head
			placeAsHead((LinkedEntry)e);

			//?: {has overflow}
			if((s = entries.size()) > this.size)
			{
				//~: unlink the tail
				LinkedEntry x = EX.assertn(this.tail);
				unlink(x);

				//~: move it to pruned
				pruned.add(x);
				p = pruned.size();
			}
		}

		//?: {has overflow} notify the delegate
		if((s > this.size) && (delegate != null))
			((CachingDelegate) delegate).overflow(p);
	}

	protected ModelEntry newEntry()
	{
		return new LinkedEntry();
	}


	/* public: Linked Model Entity */

	public static class LinkedEntry extends ModelEntry
	{
		public LinkedEntry prev;
		public LinkedEntry next;
	}


	/* protected: list operations */

	protected void moveToHead(LinkedEntry e)
	{
		EX.assertn(this.head);
		if(head == e) return;

		//~: unlink, then place
		unlink(e);
		placeAsHead(e);
	}

	protected void placeAsHead(LinkedEntry e)
	{
		EX.assertx(e.prev == null);
		EX.assertx(e.next == null);

		//?: {head is undefined}
		if(head == null)
		{
			EX.assertx(tail == null);
			head = tail = e;
		}
		else
		{
			EX.assertx(head.prev == null);
			e.next = head;
			head.prev = e;
			head = e;
		}
	}

	protected void unlink(LinkedEntry e)
	{
		//?: {it is head}
		if(head == e)
			head = e.next;

		//?: {it is tail}
		if(tail == e)
			tail = e.prev;

		//?: {has previous}
		if(e.prev != null)
			e.prev.next = e.next;

		//?: {has next}
		if(e.next != null)
			e.next.prev = e.prev;

		e.prev = e.next = null;
	}


	/* protected: the cache state */

	protected LinkedEntry                    head, tail;
	protected final Map<String, LinkedEntry> entries;
	protected final Set<LinkedEntry>         pruned;
}