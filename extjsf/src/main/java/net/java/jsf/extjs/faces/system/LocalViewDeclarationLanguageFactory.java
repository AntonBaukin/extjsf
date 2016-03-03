package net.java.jsf.extjs.faces.system;

/* JavaServer Faces */

import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

/* JavaServer Faces Reference Implementation */

import com.sun.faces.application.view.ViewHandlingStrategyManager;


/**
 * Overwrites the default JSF factory implementation
 * to provide abilities specific for the application.
 *
 * @author anton.baukin@gmail.com
 */
public class   LocalViewDeclarationLanguageFactory
       extends ViewDeclarationLanguageFactory
{
	/* public: ViewDeclarationLanguageFactory interface */

	public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId)
	{
		if(manager == null)
			manager = createViewHandlingStrategyManager();
		return manager.getStrategy(viewId);
	}


	/* protected: implementation internals */

	protected ViewHandlingStrategyManager createViewHandlingStrategyManager()
	{
		return new LocalViewHandlingStrategyManager();
	}


	/* private: strategy manager */

	private volatile ViewHandlingStrategyManager manager;
}