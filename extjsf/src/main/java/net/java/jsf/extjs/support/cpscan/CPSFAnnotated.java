package net.java.jsf.extjs.support.cpscan;

/* Spring Framework (internals) */

import org.springframework.core.type.classreading.MetadataReader;


/**
 * Allows classes that directly declare
 * the runtime annotation specified.
 *
 * @author anton.baukin@gmail.com
 */
public class CPSFAnnotated extends CPScanFilterBase
{
	/* public: CPSFInstanceOf (bean) interface */

	public Class[] getAnnotations()
	{
		return annotations;
	}

	public void    setAnnotations(Class[] annotations)
	{
		if(annotations == null)  annotations = new Class[0];
		this.annotations = annotations;
	}


	/* public: CPScanFilter interface */

	public boolean isClassOfInterest(MetadataReader mr)
	{
		if(!isAllowedClass(mr))
			return false;

		for(Class acls : getAnnotations())
			if(mr.getAnnotationMetadata().hasAnnotation(acls.getName()))
				return true;
		return false;
	}


	/* private: parameters of the filter */

	private Class[] annotations = new Class[0];
}