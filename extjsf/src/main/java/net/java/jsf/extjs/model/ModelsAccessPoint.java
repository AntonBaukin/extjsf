package net.java.jsf.extjs.model;

/* com.tverts: models (store) */

import net.java.jsf.extjs.model.store.ModelsStoreAccess;

/* com.tverts: support */

import net.java.jsf.extjs.support.EX;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Entry point to access the model attached to the user
 * request to the system. The model is created on demand.
 *
 * @author anton.baukin@gmail.com
 */
public class ModelsAccessPoint
{
	/* ModelAccessPoint Singleton */

	public static ModelsAccessPoint getInstance()
	{
		return bean(ModelsAccessPoint.class);
	}


	/* Models Access Point */

	public static ModelsStore store()
	{
		//?: {has no store access strategy}
		ModelsStoreAccess msa = EX.assertn(
		  getInstance().getModelAccess());

		//?: {access strategy got no store}
		return EX.assertn(msa.accessStore());
	}

	public static ModelBean   read(String key)
	{
		return ModelsAccessPoint.store().read(key);
	}

	@SuppressWarnings("unchecked")
	public static <B extends ModelBean> B
	                          read(String key, Class<B> beanClass)
	{
		ModelBean mb = ModelsAccessPoint.read(key);

		if((mb != null) && (beanClass != null) &&
		   !beanClass.isAssignableFrom(mb.getClass())
		  )
			throw EX.state("Model bean requested by the key [", key,
			  "] is not a class checked [", beanClass.getName(), "]!");

		return (B) mb;
	}


	/* Models Access Point (configuration) */

	public ModelsStoreAccess getModelAccess()
	{
		return modelAccess;
	}

	private ModelsStoreAccess modelAccess;

	public void setModelAccess(ModelsStoreAccess modelAccess)
	{
		this.modelAccess = EX.assertn(modelAccess);
	}
}