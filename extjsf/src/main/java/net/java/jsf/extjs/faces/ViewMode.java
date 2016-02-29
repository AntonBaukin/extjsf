package net.java.jsf.extjs.faces;

/**
 * Defines various request processing and rendering modes.
 *
 * @author anton.baukin@gmail.com
 */
public enum ViewMode
{
	/**
	 * Page view mode is the default one when HTTP GET
	 * request is asking to render regular HTML page.
	 *
	 * This is the default mode.
	 */
	PAGE,

	/**
	 * Variant of the page mode is used to process all the phases,
	 * but the render one. In this mode the page components are
	 * processed regularly, but not displayed.
	 */
	PAGE_POST,

	/**
	 * In body mode only the body part (without body tag)
	 * of the page is displayed. Also no pages shared scripts
	 * are te be loaded.
	 */
	BODY,

	/**
	 * Analogue of the page post mode for the body mode.
	 */
	BODY_POST
}