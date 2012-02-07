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
package org.openmicroscopy.shoola.agents.monash.view;

import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

public interface AndsPublish extends ObservableComponent {
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 2;
	
	/** Flag to denote the <i>Ready</i> state. */
	public static final int     LOADING_DATA = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;
	
	/** Flag to denote the <i>Publishing</i> state. */
	public static final int     PUBLISHING = 5;

	/**
	 * Starts the ANDS publishing process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * @param set A hierarchy tree containing project and all its descendants
	 * 
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate();
	
	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data publishing is cancelled.
	 */
	public void discard();
	
	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();
	
	/**
	 * Registers the specified files with ANDS. Sets the action to publish enabled depending on the 
	 * authentication to Monash DS
	 * 
	 * @param data The data to register with ANDS.
	 */
	public void publishData(ImportableObject data);

	/**
	 * Sets the Registered tag.
	 * 
	 * @param tags The Registered tag.
	 */
	public void setExistingTags();
	
	/**
	 * Returns the view.
	 * 
	 * @return See above.
	 */
	public JFrame getView();
	
	/** Cancels any ongoing data loading. */
	public void cancel();
	
	/** 
	 * Closes the interface and cancels the registration in the queue if 
	 * selected by user.
	 */
	public void close();

	/**
	 * Moves the window to the front.
	 * @throws IllegalStateException If the current state is not
	 *                               {@link #DISCARDED}.
	 */
	public void moveToFront();
	
	/** Tries to re-register failed registration. */
	public void retryPublish();
	
	/** Refreshes the view on UserGroupSwitched */
	public void refresh();

	/**
	 * Shows the add researcher screen if the passed parameter is <code>true</code>, hides
	 * otherwise.
	 */
	public void showAddResearcher();

	/** Load the data collections to register with RDA */
	public void loadDataCollection();

	/**
	 * Project/ Dataset tagged as <i>Register with RDA</i>
	 * 
	 * @param nodes		collection to register with RDA
	 */
	public void setDataCollection(Collection result);

	/**
	 * Sets the data loaded.
	 * 
	 * @param result The data loaded
	 */
	public void setDataLoaded(Collection result);

	/**
	 * Sets the cookie in model, {@link AndsPublishModel AndsPublishModel} 
	 * @param monashAuth	the cookie from Monash DS
	 */
	public void setCookie(String monashAuth);

	/**
	 * Shows the license main screen
	 */
	public void showLicenseMain();
}
