package net.java.jsf.extjs.model.store;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;


/**
 * Strategy generating keys for Model Beans.
 *
 * @author anton.baukin@gmail.com
 */
public interface ModelKeysGen
{
	/* Model Keys Generator */

	public String genModelKey(ModelBean bean);
}