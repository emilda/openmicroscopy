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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.WindowConstants;

import org.openmicroscopy.shoola.agents.monash.action.ExitAction;
import org.openmicroscopy.shoola.agents.monash.action.LicenseAction;
import org.openmicroscopy.shoola.agents.monash.action.MonashAction;
import org.openmicroscopy.shoola.agents.monash.action.PartyAction;
import org.openmicroscopy.shoola.agents.monash.action.PublishAction;
import org.openmicroscopy.shoola.agents.monash.action.RefreshAction;

/** 
 * The controller component in the <code>AndsPublish</code> MVC triad.
 * All the main actions in <code>PublishAgent</code> is controlled here.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class AndsPublishControl implements PropertyChangeListener {
	
	/** Action ID indicating to exit the application. */
	static final Integer EXIT = 0;
	
	/** Action ID indicating to register metadata with RDS. */
	static final Integer PUBLISH = 1;
	
	/** Action ID indicating to add Researcher. */
	static final Integer PARTY = 2;
	
	/** Action ID indicating to add License. */
	static final Integer LICENSE = 3;
	
	/** Action ID indicating to refresh the view. */
	static final Integer REFRESH = 4;
	
	/** 
	 * Reference to the {@link AndsPublish} component, which, in this context,
	 * is regarded as the Model.
	 */
	private AndsPublish		model;
	
	/** Reference to the View. */
	private AndsPublishUI	view;
	
	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, MonashAction>	actionsMap;

	/**
	 * Creates a new instance.
	 * The {@link #initialize(AndsPublishUI) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link AndsPublish} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	public AndsPublishControl(AndsPublish model) {
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
	}

	/**
	 * Initialize the controller. Links the view with the controller.
	 * @param view
	 */
	public void initialize(AndsPublishUI view) {
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		createActions();
		attachListeners();
	}
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap = new HashMap<Integer, MonashAction>();
		actionsMap.put(PUBLISH, new PublishAction(model));
		actionsMap.put(LICENSE, new LicenseAction(model));
		actionsMap.put(PARTY, new PartyAction(model));
		actionsMap.put(EXIT, new ExitAction(model));
		actionsMap.put(REFRESH, new RefreshAction(model));
	}

	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	MonashAction getAction(Integer id) { return actionsMap.get(id); }
	
	/** Attaches listener to the window listener. */
	private void attachListeners()
	{
		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { model.close(); }
		});
	}

	/**
	 * Implemented as specified by {@link PropertyChangeListener}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String name = evt.getPropertyName();
		/*if (AndsPublishDialog.CANCEL_SELECTION_PROPERTY.equals(name)) {
			System.out.println("Cancel Property received");
			model.close();
		} else if (AndsPublishDialog.PUBLISH_PROPERTY.equals(name)) {
			System.out.println("CancelAll Property received");
			model.publishData(null);
		}*/
	}
}
