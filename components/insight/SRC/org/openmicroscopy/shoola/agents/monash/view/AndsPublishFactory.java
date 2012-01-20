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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pojos.ExperimenterData;

public class AndsPublishFactory implements ChangeListener {
	
	/** The sole instance. */
    private static final AndsPublishFactory  singleton = new AndsPublishFactory();

	private static boolean groupchange = false;

    /** The tracked component. */
	private AndsPublish	viewer;

	/** The name of the windows menu. */
	private static final String MENU_NAME = "Register with RDA";
    
    /** Creates a new instance. */
	private AndsPublishFactory() {}
    
    /**
     * Returns a viewer to display.
     * 
     * @param exp The <code>ExperimenterData</code> object.
     * @param userGroupID Group Id of user
     */
    public static AndsPublish getViewer(ExperimenterData exp, long userGroupID) 
    {
    	if (singleton.viewer != null) return singleton.viewer;
        AndsPublishModel model = new AndsPublishModel(exp, userGroupID);
        return singleton.getViewer(model);
    }

    /**
	 * Notifies the viewer that the user's group has successfully be modified
	 * if the passed value is <code>true</code>, unsuccessfully 
	 * if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, <code>false</code>
	 * 					otherwise.
	 */
    public static void onGroupSwitch(boolean success)
    {
    	if (!success) return;
    	groupchange  = true;
    	if (singleton.viewer == null) return;
    	singleton.viewer.refresh();
    }

    public static void onELoadedDataEvent() {
		if (groupchange) {
			groupchange  = false;
			return;
		} else {
			if (singleton.viewer == null) return;
	    	singleton.viewer.refresh();
		}
	}
    
    /**
     * Creates or recycles a viewer component for the specified 
     * <code>model</code>.
     * 
     * @param model The component's Model.
     * @return A {@link AndsPublish} for the specified <code>model</code>.  
     */
    private AndsPublish getViewer(AndsPublishModel model)
    {
    	System.out.println("Creates or recycles a AndsPublish component");
    	
    	if (singleton.viewer != null) return viewer;
    	AndsPublishComponent comp = new AndsPublishComponent(model);
    	comp.initialize();
		model.initialize(comp);
		singleton.viewer = comp;
		return singleton.viewer;
    }

    /**
	 * Implemented as specified by {@link ChangeListener}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
    public void stateChanged(ChangeEvent ce) {
		AndsPublishComponent comp = (AndsPublishComponent) ce.getSource();
		// TODO check for the state and do something
		//if (comp.getState() == TreeViewer.DISCARDED) viewers.remove(comp);
	}
}
