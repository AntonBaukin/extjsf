package net.java.jsf.extjs.model.store;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.SU;


/**
 * Implementation base for a Model Bean keys generator.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class ModelKeysGenBase
       implements     ModelKeysGen
{
	/* Model Keys Generator */

	public String genModelKey(ModelBean bean)
	{
		EX.assertn(bean, "Model Bean instance is undefined!");
		return catModelKey(selectKeyPrefix(bean), nextModelKey(bean));
	}


	/* protected: generation details */

	protected abstract String nextModelKey(ModelBean bean);

	protected String          catModelKey(String p, String k)
	{
		return SU.cats(p, "-", k);
	}

	protected String          selectKeyPrefix(ModelBean bean)
	{
		return bean.getClass().getSimpleName();
	}
}