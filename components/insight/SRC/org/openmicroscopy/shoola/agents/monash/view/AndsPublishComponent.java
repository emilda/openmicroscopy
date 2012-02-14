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

import java.awt.Cursor;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.httpclient.NameValuePair;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.service.MonashServices;
import org.openmicroscopy.shoola.agents.monash.service.MonashSvcReply;
import org.openmicroscopy.shoola.agents.monash.service.ServiceFactory;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.openmicroscopy.shoola.agents.monash.view.dialog.LicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.MonashDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.PartyDialog;
import org.openmicroscopy.shoola.svc.transport.TransportException;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.DataObject;

/** 
 * Implements the {@link AndsPublish} interface to provide 
 * the functionality required of the {@link PublishAgent}.
 * This class is the component hub and embeds the component's
 * MVC triad. It manages the component's state and delegates 
 * actual functionality to the MVC sub-components.
 * 
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
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
	public AndsPublishComponent(AndsPublishModel model) 
	{
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
	public void activate() 
	{
		switch (model.getState()) {
		case NEW:
			loadParty();
			loadDataCollection();
			view.setOnScreen();
			view.toFront();
			break;
		case READY:
			view.setOnScreen();
			break;
		case DISCARDED:
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
	public void publishData() 
	{
		String cookie = model.getCookie();
		
		String wsURL = PublishAgent.getMdRegToken();
		
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
	}

	void refreshTree() {
		view.refresh();
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#showAddResearcher()
	 */
	public void showAddResearcher() 
	{
		String option = view.showResearcher();
		if (null != option) {
			/** Shows the Search/ Manual add researcher screen 
			 * based on the user choice. */
			if (PartyDialog.PARTY_OPTION_SEARCH.equals(option)) {
				if (!view.searchResearcher()) showAddResearcher();
			} else if (PartyDialog.PARTY_OPTION_MANUAL.equals(option)) {
				view.manualResearcher();
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
	 * @see AndsPublish#loadParty()
	 */
	public void loadParty() {
		PartyBean pb = model.searchParty();
		if (null != pb) {
			String key = pb.getPartyKey();
			model.addParty(key, pb);
			view.addPartyCheckBox(key, pb);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setDataCollection()
	 */
	public void setDataCollection(Collection result) {
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
		if(model.getState() == DISCARDED) return;
		model.setState(LOADING_DATA);
		setDataCollection(dataloaded);
	}

	/** 
	 * Implemented as specified by the {@link AndsPublish} interface.
	 * @see AndsPublish#setCookie()
	 */
	public void setCookie(String cookie) {
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
				if (!view.showCCL()) showLicenseMain();
			} else if (LicenseDialog.LICENSE_OPTION_UDL.equals(option)) {
				if (!view.showUDL()) showLicenseMain();
			}
		}
	}

	@Override
	public void setTags(Collection result) {
		model.setTags(result);
	}

	@Override
	public void onDataSave(List<DataObject> data) {
		if (data == null) {
			model.setState(READY);
			return;
		}
		if (model.getState() == DISCARDED) return;
		DataObject dataObject = null;
		if (data.size() == 1) dataObject = data.get(0);
		if (dataObject != null && model.isSameObject(dataObject)) {
			view.removeListItem();
		} else
		model.setState(READY);
	}
}
