package net.java.jsf.extjs.support.cpscan;

/* Java */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* Spring Framework (internals) */

import net.java.jsf.extjs.support.EX;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

/* extjsf: support */

import net.java.jsf.extjs.support.SU;


/**
 * This system utility class does scanning the classes
 * in the classpath and provides the resulting set.
 *
 * It adopts Spring implementation, but introduces own
 * class filters {@link CPScanFilter}.
 *
 *
 * @author anton.baukin@gmail.com
 */
public class ClasspathScanner
{
	/* public: ClasspathScanner interface */

	public Set<Class> getClassesSet()
	{
		List<MetadataReader> mrs = loadMetadataFiltered();
		Set<Class>           res = new HashSet<>(mrs.size());
		ClassLoader          cll = Thread.currentThread().getContextClassLoader();
		String               cln = Void.class.getName();

		for(MetadataReader mr : mrs) try
		{
			cln = mr.getClassMetadata().getClassName();
			res.add(cll.loadClass(cln));
		}
		catch(Throwable e)
		{
			throw EX.wrap(e, "Error occured while ",
			  "loading scanned class ", cln, "!");
		}

		return res;
	}

	public Class[]    getClassesArray()
	{
		Set<Class> set = getClassesSet();
		return set.toArray(new Class[set.size()]);
	}


	/* public: ClasspathScanner (bean) interface */

	/**
	 * The array of root packages to scan.
	 */
	public String[] getPackages()
	{
		return packages;
	}

	private String[] packages;

	public void setPackages(String[] packages)
	{
		this.packages = packages;
	}

	/**
	 * Set this string reference to list the root
	 * scan packages with ';' separator.
	 */
	public String getPackageNames()
	{
		return packageNames;
	}

	private String packageNames;

	public void setPackageNames(String packageNames)
	{
		this.packageNames = packageNames;
	}

	public CPScanFilter getFilter()
	{
		return filter;
	}

	private CPScanFilter filter;

	public void setFilter(CPScanFilter filter)
	{
		this.filter = filter;
	}


	/* protected: classpath scanning support */

	protected Set<String> collectRootPackages()
	{
		Set<String> acc = new HashSet<>(7);

		//~: add direct packages list
		if(getPackages() != null)
			acc.addAll(Arrays.asList(getPackages()));

		//~: add the packages referred
		if(getPackageNames() != null)
			acc.addAll(Arrays.asList(
			  SU.s2a(getPackageNames(), ';')));

		Set<String> res = new HashSet<>(acc.size());

		//~: remove the empty strings
		for(String s : acc)
			res.add(SU.s2s(s));
		res.remove(null);

		return res;
	}

	protected Set<String> collectScanURIs()
	{
		Set<String> pks = collectRootPackages();
		Set<String> res = new HashSet<>(pks.size());

		for(String pkg : pks)
			res.add(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
			  pkg.replace('.', '/') + "/**/*.class");

		return res;
	}

	protected List<MetadataReader> loadAllMetadata()
	{
		Set<String>                 uris = collectScanURIs();
		ResourcePatternResolver rpr  = new PathMatchingResourcePatternResolver();
		SimpleMetadataReaderFactory mrf  = new SimpleMetadataReaderFactory();
		Set<Resource>               rcls = new HashSet<>(101);

		//~: collect all the resources found at all the paths
		for(String uri : uris) try
		{
			rcls.addAll(Arrays.asList(rpr.getResources(uri)));
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured while ",
			  "scanning classes in the URI [", uri, "]!");
		}

		List<MetadataReader>        res  = new ArrayList<>(rcls.size());

		//~: load metadata for all that resources & filter them
		for(Resource rcl : rcls) try
		{
			res.add(mrf.getMetadataReader(rcl));
		}
		catch(Exception e)
		{
			throw EX.wrap(e, "Error occured while ",
			  "reading metadata of class at ", rcl.toString(), "!");
		}

		return res;
	}

	protected List<MetadataReader> loadMetadataFiltered()
	{
		//?: {there are no filters} no classes must be returned
		if(getFilter() == null) return new ArrayList<>(0);

		//~: load the metadata
		List<MetadataReader> all = loadAllMetadata();
		List<MetadataReader> res = new ArrayList<>(all.size()/10);

		//~: filter it...
		for(MetadataReader mr : all)
			if(getFilter().isClassOfInterest(mr))
				res.add(mr);

		return res;
	}
}