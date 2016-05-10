package net.java.jsf.extjs.support.cpscan;

/* Spring Framework (internals) */

import org.springframework.core.type.classreading.MetadataReader;


/**
 * Implementation base of filter for classpath scaner.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class CPScanFilterBase
       implements     CPScanFilter
{
	/* protected: filtering support */

	protected boolean isAllowedClass(MetadataReader mr)
	{
		return mr.getClassMetadata().isConcrete();
	}

	protected String  className(MetadataReader mr)
	{
		return mr.getClassMetadata().getClassName();
	}

	protected Class   loadClass(MetadataReader mr)
	{
		try
		{
			return Thread.currentThread().getContextClassLoader().
			  loadClass(className(mr));
		}
		catch(Exception e)
		{
			throw new RuntimeException(String.format(
			  "Error occured while loading scanned class '%s'!",
			  className(mr)), e);
		}
	}
}