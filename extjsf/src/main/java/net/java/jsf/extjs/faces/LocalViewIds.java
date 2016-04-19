package net.java.jsf.extjs.faces;

/* Java */

import java.util.concurrent.atomic.AtomicLong;

/* Spring Framework */

import org.springframework.stereotype.Component;


/**
 * Strategy to generate View Ids by
 * incrementing local variable.
 *
 * @author anton.baukin@gmail.com.
 */
@Component("genViewId")
public class LocalViewIds implements GenViewId
{
	/* View Id Generator */

	public String genViewId()
	{
		return String.format("view-%x", VIEWID.incrementAndGet());
	}

	protected static final AtomicLong VIEWID = new AtomicLong();
}