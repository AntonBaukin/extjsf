package net.java.jsf.extjs.servlet.filters;

/* Java */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/* Java X */

import javax.annotation.PostConstruct;

/* Sprint Framework */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;
import net.java.jsf.extjs.support.LU;


/**
 * Collects all Filters registered as
 * Spring beans via @PickFilter.
 *
 * @author anton.baukin@gmail.com.
 */
@Component
public class FiltersPoint
{
	/* Filters Point */

	public Filter[] getFilters(FilterStage stage)
	{
		synchronized(this.filters)
		{
			//?: {not processed yet}
			if(filters.isEmpty())
				findFilters();

			return filters.get(stage);
		}
	}


	/* protected: filters discovery */

	@PostConstruct
	protected void     findFilters()
	{
		//~: pick up the filters
		Filter[] fs = collectFilters();
		if(fs.length == 0) LU.W(LU.cls(this),
		  "No web filters marked with @PickFilter were found!");

		//~: (stage -> order -> filter) mapping
		Map<FilterStage, Map<Integer, Filter>> filters = new HashMap<>();

		//c: collect the filters
		for(Filter f : fs)
		{
			//~: the annotation
			PickFilter pf = f.getClass().getAnnotation(PickFilter.class);

			//?: {annotation is wrong}
			EX.assertx(pf.order().length != 0);
			EX.assertx(pf.order().length == pf.stage().length);

			//c: process the configured stages
			for(int i = 0;(i < pf.order().length);i++)
			{
				FilterStage s = pf.stage()[i];
				Integer     o = pf.order()[i];

				//~: take the stage-mapping
				Map<Integer, Filter> m = filters.get(s);
				if(m == null) filters.put(s, m = new TreeMap<>());

				//?: {got filter with the same order}
				EX.assertx(!m.containsKey(o), "Filter [", LU.cls(f),
				  "] is registered by the occupied order [",
				  o, "] at the stage ", s, "!");

				m.put(o, f);
			}
		}

		//~: resulting filters
		for(Map.Entry<FilterStage, Map<Integer, Filter>> e : filters.entrySet())
			this.filters.put(e.getKey(),
			  e.getValue().values().stream().toArray(Filter[]::new));
	}

	protected Filter[] collectFilters()
	{
		ArrayList<Filter>   fs = new ArrayList<>();
		Map<String, Object> bs = context.
		  getBeansWithAnnotation(PickFilter.class);

		for(Map.Entry<String, Object> e : bs.entrySet())
		{
			//?: {not a filter}
			EX.assertx(e.getValue() instanceof Filter,
			  "Bean [", e.getKey(), "] is not a Filter!");

			fs.add((Filter) e.getValue());
		}

		return fs.toArray(new Filter[fs.size()]);
	}

	@Autowired
	protected ApplicationContext context;

	/**
	 * The filters for each stage collected in the call-order.
	 */
	protected final Map<FilterStage, Filter[]> filters = new HashMap<>();
}