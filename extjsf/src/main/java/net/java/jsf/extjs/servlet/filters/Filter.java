package net.java.jsf.extjs.servlet.filters;

/**
 * Represents an analogue of Servlet Filter.
 *
 * But there is one major difference: while
 * a Servlet Filter must invoke the chain of
 * filters to continue filtering, thus creating
 * a stack of filters call, this filter may
 * either return immediately, or continue
 * the filters chain recursively.
 *
 * A Filter must be thread-safe (reentable).
 *
 *
 * @author anton.baukin@gmail.com
 */
public interface Filter
{
	/* Filter */

	/**
	 * Does the main work of the filter. If the filter returns
	 * without invoking {@link FilterTask#continueCycle()} the
	 * cycle of the filters invocation is continued without
	 * nesting the filters calls in the stack. In this case all
	 * cleanup of the filtering state must be done in
	 * {@link #closeFilter(FilterTask)}.
	 *
	 * If the filter needs to create a nested call, it may invoke
	 * (also, indirectly) {@link FilterTask#continueCycle()} to
	 * continue the cycle. Transaction scopes are created in this way.
	 *
	 * It is possible to break the cycle of filtering: call
	 * {@link FilterTask#doBreak()}. The breaking may not be
	 * further cancelled.
	 */
	public void openFilter(FilterTask task);

	/**
	 * Frees the filtering context installed in
	 * {@link #openFilter(FilterTask)}. This method is
	 * invoked in the opposite order of invoking
	 * {@link #openFilter(FilterTask)} independently
	 * of the type of invocation: plain or recursive.
	 *
	 * The breaking of the cycle has no effect here,
	 * but the method may process (or alter) the error.
	 */
	public void closeFilter(FilterTask task);
}