package net.java.jsf.extjs.servlet.filters;

/**
 * A stage of Servlet Filters invocation.
 *
 * @author anton.baukin@gmail.com
 */
public enum FilterStage
{
	/**
	 * The stage of processing the initial HTTP
	 * request issued to the server.
	 */
	REQUEST,

	/**
	 * Internally included resource.
	 */
	INCLUDE,

	/**
	 * Internal forward redirection.
	 */
	FORWARD,

	/**
	 * Error response handling.
	 */
	ERROR
}