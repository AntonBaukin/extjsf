package net.java.jsf.extjs.model.store;

/* Java */

import java.util.List;

/* extjsf: models */

import net.java.jsf.extjs.model.ModelBean;

/* extjsf: support */

import net.java.jsf.extjs.support.EX;


/**
 * Models Store for test purposes.
 *
 * @author anton.baukin@gmail.com.
 */
public class ModelsServiceMock extends ModelsStoreService
{
	/* Test Support */

	public ModelBean getLastSaved()
	{
		if(!(backend instanceof ModelsBackendMock))
			throw EX.ass();

		List<ModelEntry> ls =
		  ((ModelsBackendMock)backend).lastSaved;

		if((ls == null) || ls.isEmpty())
			return null;

		return ls.get(ls.size() - 1).bean;
	}

	public void      runEvents()
	{
		super.runEvents();
	}

	public void      setBackendFile(String path)
	{
		if(!(backend instanceof ModelsBackendMock))
			throw EX.ass();

		((ModelsBackendMock) backend).setFile(path);
	}

	public void      synchNow()
	{
		ModelsStoreEvent x = new ModelsStoreEvent();

		x.setEventTime(System.currentTimeMillis());
		x.setSynch(true);
		synchEvent.set(x);

		runEvents();
	}

	public void      dumpBackend()
	{
		((ModelsBackendMock) backend).dump();
	}


	/* protected: internals */

	/**
	 * Delay [1; 10] ms of start sweep.
	 */
	protected long startSweepDelay()
	{
		return 1L + System.currentTimeMillis() % 10L;
	}

	protected void delegateSave(ModelEntry e)
	{
		super.delegateSave(e);
	}

	private ModelEntry lastSaved;
}
