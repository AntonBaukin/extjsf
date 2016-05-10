package net.java.jsf.extjs.support.cpscan;

/* Spring Framework (internals) */

import org.springframework.core.type.classreading.MetadataReader;


/**
 * A filter of classpath scanning.
 *
 * @author anton.baukin@gmail.com
 */
public interface CPScanFilter
{
	/* public: CPScanFilter interface */

	public boolean isClassOfInterest(MetadataReader mr);
}