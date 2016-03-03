package net.java.jsf.extjs.faces.system;

/* JavaServer Faces Reference Implementation */

import com.sun.faces.application.view.FaceletViewHandlingStrategy;
import com.sun.faces.application.view.ViewHandlingStrategy;
import com.sun.faces.application.view.ViewHandlingStrategyManager;


/**
 * This view handling strategies manager
 * overwrites standard strategies to achieve
 * special system goals.
 *
 *
 * @author anton.baukin@gmail.com
 */
public class   LocalViewHandlingStrategyManager
       extends ViewHandlingStrategyManager
{
	/* public: constructor */

	public LocalViewHandlingStrategyManager()
	{
		reinstallStrategies();
	}


	/* protected: internal implementation */

	protected void reinstallStrategies()
	{
		ViewHandlingStrategy[] strs = getViewHandlingStrategies();

		//~: replace facelets strategy
		for(int i = 0;(i < strs.length);i++)
			if(strs[i] instanceof FaceletViewHandlingStrategy)
				strs[i] = new LocalFaceletViewHandlingStrategy();

		setViewHandlingStrategies(strs);
	}
}