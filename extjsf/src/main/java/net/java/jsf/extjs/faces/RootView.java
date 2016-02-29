package net.java.jsf.extjs.faces;

/* Spring Framework */

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/* extjsf: support */

import net.java.jsf.extjs.support.SU;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Faces view created for each incoming
 * request and shared between all pages
 * involved in it's processing.
 *
 * @author anton.baukin@gmail.com
 */
@Component @Scope("request")
public class RootView extends ViewWithModes
{
	/* Constants */

	/**
	 * Parameter that names the domain (scope
	 * of names) of the ExtJSF Binds. It is
	 * global for a request and not changed.
	 */
	public static final String PARAM_DOMAIN   =
	  "extjs_domain";


	/* Access Shared View State */

	public String    getExtjsDomainParam()
	{
		return PARAM_DOMAIN;
	}

	public String    getExtjsDomain()
	{
		return (extjsDomain != null)?(extjsDomain):
		  (extjsDomain = obtainExtjsDomain());
	}

	private String extjsDomain;

	public Long      getEntityKey()
	{
		String p = getParam(getEntityParam());
		return (p == null)?(null):Long.parseLong(p);
	}


	/* protected: view support interface */

	protected String genNewViewId()
	{
		return bean(GenViewId.class).genViewId();
	}

	protected String obtainExtjsDomain()
	{
		return SU.sXs(obtainExtjsDomainFromRequest());
	}

	protected String obtainExtjsDomainFromRequest()
	{
		return getParam(getExtjsDomainParam());
	}
}