package net.java.jsf.extjs.faces;

/* Java */

import java.util.Arrays;
import java.util.List;

/* Spring Framework */

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.REQ;

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

	public static final String PARAM_DOMAIN   =
	  "domain";

	/**
	 * Position within the desktop layout panels.
	 * See desktop-panel.xhtml
	 */
	public static final String PARAM_POSITION =
	  "desktop-position";


	/* Access Shared View State */

	public String    getExtjsDomainParam()
	{
		return PARAM_DOMAIN;
	}

	public String    getExtjsPosition()
	{
		return obtainExtjsPositionFromRequest();
	}

	public String    getExtjsPositionParam()
	{
		return PARAM_POSITION;
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

	/**
	 * Returns all additional parameters of the request
	 * as JSON text excluding any of STD_PARAMS.
	 */
	public String    getRequestParams()
	{
		return REQ.jsonRequestParams(STD_PARAMS);
	}

	/**
	 * TODO add ModelViewBase.MODEL_PARAM
	 */
	private static final List<String> STD_PARAMS = Arrays.asList(
	  "_dc", RootView.PARAM_DOMAIN, /* ModelViewBase.MODEL_PARAM, */
	  ViewWithModes.VIEWID_PARAM, ViewWithModes.ENTITY_PARAM
	);


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

	protected String obtainExtjsPositionFromRequest()
	{
		return getParam(getExtjsPositionParam());
	}
}