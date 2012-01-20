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

import org.openmicroscopy.shoola.agents.monash.DataCollectionLoader;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

import pojos.ExperimenterData;

public class AndsPublishModel {

	/** Holds one of the state flags defined by {@link PublishAgent}. */
	private int                 state;

	/** The ID of the root. */
	private long				rootID;

	/** The id of the selected group of the current user. */
	private long				userGroupID;

	/** The currently selected experimenter. */
	private ExperimenterData	experimenter;

	/** Flag indicating if the {@link TreeViewer} is recycled or not. */
	private boolean				recycled;

	/** Reference to the component that embeds this model. */
	protected AndsPublish		component;

	/** 
	 * Loader to load data collection asynchronously or
	 * <code>null</code> depending on the current state. 
	 */
	private DataCollectionLoader dataLoader;

	/** Collection of Project/ Dataset tagged as <i>Register with RDA</i> */
	private Collection dataCollection;

	/**
	 * Creates a new instance and sets the state to {@link AndsPublish#NEW}.
	 */
	protected AndsPublishModel()
	{
		initialize();
	}

	/**
	 * Creates a new instance and sets the state to {@link AndsPublish#NEW}.
	 * 
	 * @param exp			The experimenter this model is for. 
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	public AndsPublishModel(ExperimenterData exp, long userGroupID) {
		System.out.println("AndsPublishModel instantiated");
		initialize();
		this.experimenter = exp;
		setHierarchyRoot(exp.getId(), userGroupID);
	}

	/** Initializes. */
	private void initialize()
	{
		state = AndsPublish.NEW;
		recycled = false;
	}

	/**
	 * Sets the root of the retrieved hierarchies. 
	 * 
	 * @param rootID    	The Id of the root. By default it is the 
	 * 						id of the current user.
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	void setHierarchyRoot(long rootID, long userGroupID)
	{
		this.rootID = rootID;
		this.userGroupID = userGroupID;
	}

	/**
	 * Compares another model to this one to tell if they would result in
	 * having the same display.
	 *  
	 * @param other The other model to compare.
	 * @return <code>true</code> if <code>other</code> would lead to a viewer
	 *          with the same display as the one in which this model belongs;
	 *          <code>false</code> otherwise.
	 */
	public boolean isSameDisplay(AndsPublishModel other) {
		if (other == null) return false;
		return ((other.rootID == rootID) && (other.userGroupID == userGroupID));
	}

	/**
	 * Returns <code>true</code> if the {@link AndsPublish} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRecycled() { return recycled; }

	/**
	 * Sets to <code>true</code> if the {@link AndsPublish} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { recycled = b; }

	/**
	 * Called by the <code>AndsPublish</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(AndsPublish component)
	{ 
		this.component = component; 
	}

	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link AndsPublish} interface.  
	 */
	public int getState() {
		return state;
	}

	/**
	 * Sets the current state.
	 * 
	 * @param state The state to set.
	 */
	void setState(int state) { this.state = state; }

	/**
	 * Sets the object in the {@link AndsPublish#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = AndsPublish.DISCARDED;
	}

	/**
	 * Cancels any ongoing data loading and sets the state 
	 * to {@link AndsPublish#READY} state.
	 */
	void cancel()
	{
		if (dataLoader != null) {
			dataLoader.cancel();
			dataLoader = null;
		}
		state = AndsPublish.READY;
	}

	public ExperimenterData getExperimenter() {
		return experimenter;
	}

	public long getRootID() {
		return rootID;
	}

	public void setRootID(long rootID) {
		this.rootID = rootID;
	}

	public long getUserGroupID() {
		return userGroupID;
	}

	public void setUserGroupID(long userGroupID) {
		this.userGroupID = userGroupID;
	}

	/**
	 * 
	 * @return	the data collections to register with RDA
	 */
	public Collection getDataCollection() {
		System.out.println("model.getDataCollection()");
		return dataCollection;
	}

	/**
	 * Loads the data collections to register with RDA
	 */
	public void loadDataCollection() {
		System.out.println("model.loadDataCollection()");
		dataLoader = new DataCollectionLoader(component);
		dataLoader.load();
	}

	/**
	 * @param dataCollection	the data collections to register with RDA
	 */
	public void setDataCollection(Collection dataCollection) {
		this.dataCollection = dataCollection;
	}
}
