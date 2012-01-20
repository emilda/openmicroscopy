/*
 * Copyright (c) 2010-2011, Monash e-Research Centre
 * (Monash University, Australia)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright
 * 	  notice, this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright
 * 	  notice, this list of conditions and the following disclaimer in the
 * 	  documentation and/or other materials provided with the distribution.
 * 	* Neither the name of the Monash University nor the names of its
 * 	  contributors may be used to endorse or promote products derived from
 * 	  this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openmicroscopy.shoola.agents.monash;

import java.util.Collection;

import org.openmicroscopy.shoola.agents.monash.view.AndsPublish;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ProjectData;

/** 
 * Loads Project/ Dataset tagged as <i>Register with RDA</i> for
 * the user in default/ collaborative group asynchronously.
 * Also, this class extends {@link DSCallAdapter} so that it
 * automatically become observers to an asynchronous call.  This 
 * class provides implementations of some of the call-backs to 
 * notify the {@link AndsPublishUI} of the progress and the user 
 * in the case of errors. The <code>handleResult</code> method 
 * feed the {@link AndsPublishUI} back with the results.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class DataCollectionLoader extends PublishLoader {

	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle  		handle;

	/**
	 * Creates a new instance.
	 * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
	 */
	public DataCollectionLoader(AndsPublish viewer)
	{
		super(viewer);
	}
	
	/**
	 * Feeds the result back to the viewer.
	 * @see DSCallAdapter#handleResult(Object)
	 */
	public void handleResult(Object result) {
		if (viewer.getState() == AndsPublish.DISCARDED) return; 
		viewer.setDataCollection((Collection) result);
	}

	/**
	 * Retrieves the data. Fires an asynchronous data loading.
	 */
	public void load() {
		long id = PublishAgent.getUserDetails().getId();
		handle = dmView.loadContainerHierarchy(ProjectData.class, null, false,
				id, -1, this);	
	}

	/** Cancels any ongoing data loading. */
	public void cancel() { handle.cancel(); }
	
}
