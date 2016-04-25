package net.java.jsf.extjs.model.store;

/* Java */

import java.util.UUID;

/* extjsf: model */

import net.java.jsf.extjs.model.ModelBean;


/**
 * Model keys generator creating random keys
 * with standard UUIDs generator.
 *
 * @author anton.baukin@gmail.com
 */
public class ModelKeysGenUUID extends ModelKeysGenBase
{
	/* protected: Model Keys Generator Base */

	protected String nextModelKey(ModelBean bean)
	{
		return UUID.randomUUID().toString().toUpperCase();
	}
}