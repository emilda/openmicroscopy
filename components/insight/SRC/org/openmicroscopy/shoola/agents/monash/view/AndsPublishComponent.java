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

import org.apache.commons.httpclient.NameValuePair;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.service.MonashServices;
import org.openmicroscopy.shoola.agents.monash.service.MonashSvcReply;
import org.openmicroscopy.shoola.agents.monash.service.ServiceFactory;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.dialog.LicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.MonashDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.PartyDialog;
import org.openmicroscopy.shoola.svc.transport.TransportException;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

public class AndsPublishComponent extends AbstractComponent implements AndsPublish {

	/** The Model sub-component. */
	private AndsPublishModel	model;

	/** The Controller sub-component. */
	private AndsPublishControl	controller;

	/** The View sub-component. */
	private AndsPublishUI       view;

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
		view.setVisible(false);
		view.dispose();
	}

	/**
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#discard()
	 */
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

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setExistingTags()
	 */
	public void setExistingTags() {
		// TODO Auto-generated method stub

	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#getView()
	 */
	public JFrame getView() {
		return view;
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#cancel()
	 */
	public void cancel() {
		if (model.getState() == LOADING_DATA)
			model.cancel();
		close();
	}

	/**
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#moveToFront()
	 */
	public void moveToFront() {
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (!view.isVisible()) view.setVisible(true);
		view.toFront();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#publishData()
	 */
	public void publishData() {
		System.out.println("AndsPublishComponent.publishData()");
		
		String cookie = model.getCookie();
		System.out.println("cookie: " + cookie);
		
		String wsURL = PublishAgent.getMdRegToken();
		System.out.println("partyWS: " + wsURL);
		
		MonashServices mSvc = ServiceFactory.getMonashServices(wsURL, -1);
		NameValuePair[] nvp = model.getMdRegParams();
		try {
			MonashSvcReply reply = mSvc.mdReg(cookie, nvp);
			String errMsg = reply.getErrMsg();
			if (errMsg != null) {
				view.setMessage(errMsg);
			} else {
				model.changeTag();
				view.setMessage(reply.getSuccessMsg());
			}
		} catch (TransportException e) {
			MonashDialog.showErrDialog(null, Constants.ERROR_PARTY_NF, e);
		}
		//close();
	}

	void refreshTree() {
		view.refresh();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#showAddResearcher()
	 */
	public void showAddResearcher() {
		String option = view.showResearcher();
		//firePropertyChange(FINDER_VISIBLE_PROPERTY, oldValue, newValue);
		if (null != option) {
			/** Shows the Search/ Manual add researcher screen 
			 * based on the user choice. */
			if (PartyDialog.PARTY_OPTION_SEARCH.equals(option)) {
				if (!view.searchResearcher()) showAddResearcher();
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
		/*if (result == null) 
			throw new NullPointerException("No data available to register with RDA.");*/

		model.setState(READY);
		model.setDataCollection(result);
		view.setListData(result);
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setDataLoaded()
	 */
	public void setDataLoaded(Collection dataloaded) {
		System.out.println("Load data");
		if(model.getState() == DISCARDED) return;
		model.setState(LOADING_DATA);
		setDataCollection(dataloaded);
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setCookie()
	 */
	public void setCookie(String cookie) {
		System.out.println("setCookie...");
		model.setCookie(cookie);
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#showLicenseMain()
	 */
	public void showLicenseMain() {
		String option = view.showLicenseMain();
		if (null != option) {
			if (LicenseDialog.LICENSE_OPTION_CCL.equals(option)) {
				System.out.println("CCL...");
				if (!view.showCCL()) showLicenseMain();
			} else if (LicenseDialog.LICENSE_OPTION_UDL.equals(option)) {
				System.out.println("UDL...");
				if (!view.showUDL()) showLicenseMain();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setLoginId()
	 */
	public void setLoginId(String loginId) {
		model.setLoginId(loginId);
	}
}
