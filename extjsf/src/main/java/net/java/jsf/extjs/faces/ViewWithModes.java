package net.java.jsf.extjs.faces;

/* JavaServer Faces */

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import static net.java.jsf.extjs.support.SpringPoint.bean;


/**
 * Provides system infrastructure for the views.
 *
 * @author anton.baukin@gmail.com
 */
public abstract class ViewWithModes extends ViewBase
{
	/* Constants */

	public static final String VIEWID_PARAM = "view";

	public static final String VMODE_PARAM  = "mode";

	/**
	 * Parameter used to refer database entities
	 * by their primary keys.
	 */
	public static final String ENTITY_PARAM = "entity";


	/* View With Modes Interface */

	public String   getId()
	{
		return (this.id != null)?(this.id):
		  (this.id = obtainViewId());
	}

	private String  id;

	public ViewMode getViewMode()
	{
		if(viewMode != null)
			return viewMode;

		viewMode = obtainViewMode();
		if(viewMode == null)
			viewMode = ViewMode.PAGE;
		return viewMode;
	}

	private ViewMode viewMode;

	public String   getViewModeStr()
	{
		return getViewMode().toString().toLowerCase();
	}

	/**
	 * Detects what mode to use to send POST requests
	 * from the current view mode.
	 */
	public ViewMode getViewModePost()
	{
		switch(getViewMode())
		{
			case PAGE: return ViewMode.PAGE_POST;
			case BODY: return ViewMode.BODY_POST;
			default  : return getViewMode();
		}
	}

	public String   getViewModePostStr()
	{
		return getViewModePost().toString().toLowerCase();
	}

	public boolean  isViewModePage()
	{
		return ViewMode.PAGE.equals(getViewMode());
	}

	/**
	 * This view mode is for Ajax request to the components
	 * placed in page mode. This components do receive all
	 * the Faces phases except the render one.
	 *
	 * This method tells {@code true} only in regular page
	 * mode, or in page post mode.
	 */
	public boolean  isViewModePageRendered()
	{
		return isViewModePage() ||
		  isViewModePagePost() && !isFacesRenderPhase();
	}

	public boolean  isViewModePagePost()
	{
		return ViewMode.PAGE_POST.equals(getViewMode());
	}

	public boolean  isViewModeBody()
	{
		return ViewMode.BODY.equals(getViewMode());
	}

	/**
	 * Having the same idea as page mode rendered,
	 * this method supports only body and body post
	 * modes. In body post mode it tell {@code false}
	 * on the render phase.
	 */
	public boolean  isViewModeBodyRendered()
	{
		return isViewModeBody() ||
		  isViewModeBodyPost() && !isFacesRenderPhase();
	}

	public boolean  isViewModeBodyPost()
	{
		return ViewMode.BODY_POST.equals(getViewMode());
	}

	public boolean  isFacesRenderPhase()
	{
		return PhaseId.RENDER_RESPONSE.equals(
		  FacesContext.getCurrentInstance().getCurrentPhaseId());
	}


	/* View With Modes (Parameter Names) */

	public String   getViewIdParam()
	{
		return VIEWID_PARAM;
	}

	public String   getViewModeParam()
	{
		return VMODE_PARAM;
	}

	public String   getEntityParam()
	{
		return ENTITY_PARAM;
	}


	/* protected: view support interface */

	protected String   obtainRequestedViewId()
	{
		return getParam(getViewIdParam());
	}

	protected String   obtainViewId()
	{
		//~: take value from the request
		String id = obtainRequestedViewId();
		return (id != null)?(id):(genNewViewId());
	}

	protected String   genNewViewId()
	{
		//~: ask for the shared id
		return EX.assertn(bean(RootView.class).getId(),
		  "No shared Faces View ID was generated!");
	}

	protected ViewMode obtainViewMode()
	{
		return obtainRequestViewMode();
	}

	protected ViewMode obtainRequestViewMode()
	{
		String vmp = getParam(getViewModeParam());
		if(vmp == null) return null;

		for(ViewMode vm : ViewMode.values())
			if(vm.toString().equalsIgnoreCase(vmp))
				return vm;
		return null;
	}
}