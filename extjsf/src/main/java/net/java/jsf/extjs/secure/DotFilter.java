package net.java.jsf.extjs.secure;

/* Spring Framework */

import org.springframework.stereotype.Component;

/* extjsf: servlet */

import net.java.jsf.extjs.servlet.filters.FilterBase;
import net.java.jsf.extjs.servlet.filters.FilterStage;
import net.java.jsf.extjs.servlet.filters.FilterTask;
import net.java.jsf.extjs.servlet.filters.PickFilter;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Filter that forbids to access any resource
 * that has '/.' substring in it's path. This
 * follows the convention of the hidden files.
 *
 * @author anton.baukin@gmail.com.
 */
@Component @PickFilter(order = { 10 })
public class DotFilter extends FilterBase
{
	public void openFilter(FilterTask task)
	{
		//?: {not external request}
		if(!FilterStage.REQUEST.equals(task.getFilterStage()))
			return;

		//?: {is dot resource} react as not found
		if(task.getRequest().getRequestURI().contains("/.")) try
		{
			task.getResponse().sendError(404);
		}
		catch(Throwable e)
		{
			throw EX.wrap(e);
		}
		finally
		{
			task.doBreak();
		}
	}
}