/*
 * org.openmicroscopy.shoola.agents.monash.PublishLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.monash;

import org.openmicroscopy.shoola.agents.monash.view.AndsPublish;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
 * Parent of all classes that load data asynchronously for a {@link AndsPublish}.
 * All these classes invoke methods of the {@link DataManagerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call.  This class provides
 * default implementations of some of the call-backs to notify the 
 * {@link AndsPublish} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link AndsPublish} back with the results.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public abstract class PublishLoader extends DSCallAdapter {

	/** Reference to the registry. */
	protected final Registry		registry;
	
	/** The browser this data loader is for. */
	protected final AndsPublish		viewer;

    /** Convenience reference for subclasses. */
    protected final DataManagerView        dmView;
    
	/**
	 * Creates a new instance.
	 */
	public PublishLoader(AndsPublish viewer)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
        this.viewer = viewer;
		registry = PublishAgent.getRegistry();
		dmView = (DataManagerView) 
        registry.getDataServicesView(DataManagerView.class);
	}

	/**
	 * Notifies the user that it wasn't possible to retrieve the data.
	 * @see DSCallAdapter#handleNullResult()
	 */
	public void handleNullResult() {
		handleException(new Exception("No data available to register with RDA."));
	}

	/**
	 * Notifies the user that the data retrieval has been cancelled.
	 * @see DSCallAdapter#handleCancellation()
	 */
	public void handleCancellation() {
		String info = "The ANDS data collection retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        registry.getUserNotifier().notifyInfo("ANDS Data Retrieval Cancellation", info);
        viewer.cancel();
	}

	/**
	 * Notifies the user that an error has occurred and notifies the 
	 * {@link #viewer}.
	 * @see DSCallAdapter#handleException(Throwable)
	 */
	public void handleException(Throwable exc) {
		int state = viewer.getState();
		String s = "Data Retrieval Failure: ";
		LogMessage msg = new LogMessage();
		msg.print("State: " + state);
		msg.print(s);
		msg.print(exc);
		registry.getLogger().error(this, msg);
		if (state != AndsPublish.DISCARDED)
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s, exc);
		viewer.cancel();
	}

    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();

}
