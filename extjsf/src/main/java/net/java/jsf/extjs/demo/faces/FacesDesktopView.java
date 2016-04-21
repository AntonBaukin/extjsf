package net.java.jsf.extjs.demo.faces;

/* JavaServer Faces */

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/* extjsf: faces */

import net.java.jsf.extjs.faces.ViewWithModes;


/**
 * Back bean for desktop.xhtml page.
 *
 * @author anton.baukin@gmail.com.
 */
@ManagedBean @RequestScoped
public class FacesDesktopView extends ViewWithModes
{
	public String  getUserDisplayName()
	{
		return "Smith, John";
	}

	public String  getUserRoleTitle()
	{
		return "Visitor";
	}
}