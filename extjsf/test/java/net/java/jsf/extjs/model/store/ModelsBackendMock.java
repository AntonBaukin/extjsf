package net.java.jsf.extjs.model.store;

/* Java */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/* extjsf: support */

import net.java.jsf.extjs.support.LU;


/**
 * Backend to store models in the memory
 * with test-specific access.
 *
 * @author anton.baukin@gmail.com.
 */
public class ModelsBackendMock extends ModelsMemoryBackend
{
	/* Test Support */

	public List<ModelEntry> lastSaved;


	/* Models Backend */

	public void store(Collection<ModelEntry> es)
	{
		super.store(es);
		this.lastSaved = new ArrayList<>(es);
	}

	public void dump()
	{
		List<String>  l = new ArrayList<>();
		final long    t = System.currentTimeMillis();
		StringBuilder s = new StringBuilder();

		for(Save x : store.values())
		{
			long dt = t - x.time;

			s.delete(0, s.length());
			s.append(restore(x.bytes));
			s.append(" @ ");
			if(dt >= 0) s.append('+');
			s.append(dt);
			l.add(s.toString());
		}

		Collections.sort(l);
		s.delete(0, s.length());
		for(String x : l)
			s.append('\n').append(x);

		LU.I(LU.cls(this), "DUMP", s);
	}
}