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

import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.monash.view.dialog.PartyDialog;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

public class AndsPublishComponent extends AbstractComponent implements AndsPublish {

	/** The Model sub-component. */
	private AndsPublishModel     model;

	/** The Controller sub-component. */
	private AndsPublishControl   controller;

	/** The View sub-component. */
	private AndsPublishUI       view;

	/** Flag indicating that the window has been marked to be closed.*/
	private boolean 		markToclose;

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component.
	 */
	public AndsPublishComponent(AndsPublishModel model) {
		System.out.println("Created a new AndsPublish component");

		if (model == null) throw new NullPointerException("No model."); 
		this.model = model;
		controller = new AndsPublishControl(this);
		view = new AndsPublishUI();
		markToclose = false;
	}

	/** 
	 * Links up the MVC triad. 
	 * 
	 */
	public void initialize() {
		controller.initialize(view);
		view.initialize(model, controller);
	}

	/**
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#activate()
	 */
	@Override
	public void activate() {
		System.out.println("Activating AndsPublish component");
		switch (model.getState()) {
		case NEW:
			System.out.println("model is NEW");
			loadDataCollection();
			view.setOnScreen();
			view.toFront();
			break;
		case READY:
			System.out.println("model is READY");
			view.setOnScreen();
			break;
		case DISCARDED:
			System.out.println("model is DISCARDED");
			return;
		}
	}

	/**
	 * Returns the Model sub-component.
	 * @return  A {@link AndsPublishModel}
	 */
	public AndsPublishModel getModel() {
		return model;
	}

	/**
	 * Sets to <code>true</code> if the component is recycled, 
	 * to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { model.setRecycled(b); }

	/** Refreshes the view on UserGroupSwitched */
	public void refresh() {
		if (model.getState() == DISCARDED) return;
		loadDataCollection();
		//view.refresh();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#close()
	 */
	public void close()
	{
		System.out.println("Close AndsPublish component");
		markToclose = true;
		view.setVisible(false);
		view.dispose();
	}

	@Override
	public void discard() {
		if (model.getState() == READY) {
			view.close();
			model.discard();
		}
	}

	/**
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#getState()
	 */
	public int getState() { return model.getState(); }

	@Override
	public void setExistingTags() {
		// TODO Auto-generated method stub

	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#getView()
	 */
	@Override
	public JFrame getView() {
		return view;
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#cancel()
	 */
	@Override
	public void cancel() {
		if (model.getState() == LOADING_DATA)
			model.cancel();
		close();
	}

	/**
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#moveToFront()
	 */
	@Override
	public void moveToFront() {
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (!view.isVisible()) view.setVisible(true);
		view.toFront();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#retryPublish()
	 */
	@Override
	public void retryPublish() {
		// TODO Auto-generated method stub

	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#publishData(ImportableObject)
	 */
	@Override
	public void publishData(ImportableObject data) {
		System.out.println("AndsPublishComponent.publishData()");

		// TODO Sets the action to publish enabled depending on the authentication to Monash DS
		// Example 
		/*
		 *  protected void onPublishStateChange(boolean state) {
        	setEnabled(state);
        	return;
        }*/
		close();	// TODO implement registration functionality here
	}

	void refreshTree() {
		view.refresh();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#showAddResearcher(boolean)
	 */
	@Override
	public void showAddResearcher(boolean b) {
		String option = view.showResearcher();
		//firePropertyChange(FINDER_VISIBLE_PROPERTY, oldValue, newValue);
		if (null != option) {
			/**
			 * Shows the Search/ Manual add researcher screen based on the user choice.
			 */
			if (PartyDialog.PARTY_OPTION_SEARCH.equals(option)) {
				String party = view.searchResearcher();
				if (null == party) {
					showAddResearcher(true);
				}
			} else if (PartyDialog.PARTY_OPTION_MANUAL.equals(option)) {
				//view.manualResearcher();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#loadDataCollection()
	 */
	public void loadDataCollection() {
		model.setState(LOADING_DATA);
		model.loadDataCollection();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setDataCollection()
	 */
	public void setDataCollection(Collection result) {
		System.out.println("Data collection loaded...");
		int state = model.getState();
		if (state != LOADING_DATA)
			throw new IllegalStateException(
					"This method can only be invoked in the LOADING_DATA state.");
		if (result == null) 
			throw new NullPointerException("No data available to register with RDA.");

		model.setState(READY);
		model.setDataCollection(result);
		view.setDataCollection(result);
	}
}
