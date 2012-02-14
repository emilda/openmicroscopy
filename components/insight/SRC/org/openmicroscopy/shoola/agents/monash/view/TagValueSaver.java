package org.openmicroscopy.shoola.agents.monash.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.agents.monash.PublishLoader;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.DataObject;

public class TagValueSaver extends PublishLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle	handle;
	private Collection<DataObject> data;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param dataObject 
	 */
	public TagValueSaver(AndsPublish viewer, DataObject object)
	{
		super(viewer);
		this.data = new ArrayList<DataObject>();
		data.add(object);
	}

	/** 
	 * Loads the tags. 
	 * @see DataImporterLoader#cancel()
	 */
	public void load()
	{
		handle = mhView.saveData(data, null, null, null, -1, this);
	}

	/** 
	 * Cancels the data loading. 
	 * @see DataImporterLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/**
	 * Feeds the result back to the viewer.
	 * @see DataImporterLoader#handleResult(Object)
	 */
	public void handleResult(Object result) 
	{
		if (viewer.getState() == AndsPublish.DISCARDED) return; //Async cancel.
		viewer.onDataSave((List) result);
	} 

	/**
	 * Notifies the user that an error has occurred and discards the 
	 * {@link #viewer}.
	 * @see DSCallAdapter#handleException(Throwable) 
	 */
	public void handleException(Throwable exc) 
	{
		String s = "Updating Tag Failure: ";
		LogMessage msg = new LogMessage();
		msg.print(s);
		msg.print(exc);
		registry.getLogger().error(this, msg);
		registry.getUserNotifier().notifyError("Updating Tag Failure", 
				s, exc);
		viewer.cancel();
	}

}
