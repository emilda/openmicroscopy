package org.openmicroscopy.shoola.agents.monash.view;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.monash.PublishLoader;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.DataObject;
import pojos.TagAnnotationData;

/** 
 * Loads the available Tags and update the tag value
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TagsLoader extends PublishLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle	handle;
	private DataObject object;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param dataObject 
	 */
	public TagsLoader(AndsPublish viewer, DataObject object)
	{
		super(viewer);
		this.object = object;
	}

	/** 
	 * Loads the tags. 
	 * @see DataImporterLoader#cancel()
	 */
	public void load()
	{
		handle = mhView.loadStructuredData(object.getClass(), object.getId(), this);
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
		viewer.setTags((Collection) result);
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
