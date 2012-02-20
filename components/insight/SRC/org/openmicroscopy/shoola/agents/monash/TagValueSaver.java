package org.openmicroscopy.shoola.agents.monash;

import java.util.Arrays;
import java.util.List;

import org.openmicroscopy.shoola.agents.monash.PublishLoader;
import org.openmicroscopy.shoola.agents.monash.view.AndsPublish;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.AnnotationData;
import pojos.DataObject;

/** 
 * Updates the tag of the associated to the passed object.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class TagValueSaver extends PublishLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle	handle;
	
	/** The annotation to add to the data object. */
	private List<AnnotationData> toAdd;
	
	/** The annotation to unlink from the data object. */
	private List<AnnotationData> toRemove;
	
	/** The object to update.*/
	private DataObject data;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param data The data object to update.
	 */
	public TagValueSaver(AndsPublish viewer, DataObject data, 
			List<AnnotationData> toAdd, List<AnnotationData> toRemove)
	{
		super(viewer);
		if (data == null)
			throw new IllegalArgumentException("No object specified.");
		this.data = data;
		this.toAdd = toAdd;
		this.toRemove = toRemove;
	}

	/** 
	 * Loads the tags. 
	 * @see DataImporterLoader#cancel()
	 */
	public void load()
	{
		long userID = PublishAgent.getUserDetails().getId();
		handle = mhView.saveData(Arrays.asList(data), toAdd, toRemove, null,
				userID, this);
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
