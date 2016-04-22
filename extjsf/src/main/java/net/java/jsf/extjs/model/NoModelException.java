package net.java.jsf.extjs.model;

/* extjsf: support */

import net.java.jsf.extjs.support.misc.HiddenError;


/**
 * While processing HTTP request, if no model reference
 * provided, or the reference is for obsolete model instance,
 * the view implementation may raise this exception to redirect
 * the request to newly created model reference.
 *
 * This exception is handled by {@link NoModelFilter}.
 *
 * Note that this error must be raised before any printing
 * to the HTTP response, or redirection header may be skipped.
 *
 *
 * @author anton.baukin@gmail.com
 */
public class      NoModelException
       extends    RuntimeException
       implements HiddenError
{
	/* public: constructor */

	public NoModelException(String message)
	{
		super(message);
	}

	public NoModelException(ModelBean model)
	{
		this.model = model;
	}


	/* public: NoModelException interface */

	public ModelBean        getModel()
	{
		return model;
	}

	/**
	 * Optional parameter allowing to set the list of the
	 * model keys to redirect. If set, the model key is not
	 * added automatically.
	 */
	public String           getModelKeys()
	{
		return (modelKeys != null)?(modelKeys):
		  (getModel().getModelKey());
	}

	public NoModelException setModelKeys(String modelKeys)
	{
		this.modelKeys = modelKeys;
		return this;
	}


	/* private: actual model reference */

	private ModelBean model;
	private String    modelKeys;
}