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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.openmicroscopy.shoola.agents.events.treeviewer.ExperimenterLoadedDataEvent;
import org.openmicroscopy.shoola.agents.monash.action.RegisterAction;
import org.openmicroscopy.shoola.agents.monash.events.PublishEvent;
import org.openmicroscopy.shoola.agents.monash.view.AndsPublish;
import org.openmicroscopy.shoola.agents.monash.view.AndsPublishFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
/** 
 * The ANDS Publish agent. This agent registers collection with RDA.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class PublishAgent implements Agent, AgentEventListener {

	/** Reference to the registry. */
    private static Registry         registry;

    //no-arguments constructor required for initialization
    public PublishAgent() {
    	System.out.println("PublishAgent constructor");
    }
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }

    //invoked before shutting down the application
    public boolean canTerminate() { return true; }

    //not yet implemented: invoked when shutting down the application
    public Map<String, Set> hasDataToSave() { return null; }

    //invoked while shutting down the application
    public void terminate() {}

    public void setContext(Registry ctx)
    {
    	System.out.println("PublishAgent setContext");
        //Reference to the Agent Registry to access services.
        registry = ctx; 

        //register the events the agent listens to e.g. BrowseImage
        EventBus bus = registry.getEventBus();
        bus.register(this, PublishEvent.class);
        bus.register(this, UserGroupSwitched.class);
        //bus.register(this, ReconnectedEvent.class);
        //bus.register(this, BrowserSelectionEvent.class);
        bus.register(this, ExperimenterLoadedDataEvent.class);
        register();
    }

    /** Registers the agent with the tool bar.*/
	private void register()
	{
		TaskBar tb = registry.getTaskBar();
		RegisterAction a = new RegisterAction();
		//register with tool bar
		JButton b = new JButton(a);
		tb.addToToolBar(TaskBar.AGENTS, b);
		//register with File menu
		JMenuItem item = new JMenuItem(a);
		item.setText(RegisterAction.NAME);
		tb.addToMenu(TaskBar.FILE_MENU, item);
	}

	/**
     * Handles the fact that data were loaded.
     * 
     * @param e The event to handle.
     */
	private void handleExperimenterLoadedDataEvent(ExperimenterLoadedDataEvent e) {
		// TODO write code to handle the new data loaded
		System.out.println("ExperimenterLoadedDataEvent fired, write code to handle the new data loaded");
		AndsPublishFactory.onELoadedDataEvent();
	}
	
	/**
     * Handles the {@link UserGroupSwitched} event.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	AndsPublishFactory.onGroupSwitch(evt.isSuccessful());
    }

    /**
     * Handles the {@link PublishEvent} event.
     * 
     * @param evt The event to handle.
     */
	private void handlePublishEvent(PublishEvent evt) {
		System.out.println("Publish Event notification received by PublishAgent");
		
		if (evt == null) return;
		
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return;
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
    	GroupData gp = null;
    	try {
    		gp = exp.getDefaultGroup();
		} catch (Exception e) {
			//No default group
		}
    	long id = -1;
    	if (gp != null) id = gp.getId();
		AndsPublish viewer = AndsPublishFactory.getViewer(exp, id);
        if (viewer != null) viewer.activate();
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * @see Agent#activate(boolean)
	 */
	public void activate(boolean master) {
		System.out.println("PublishAgent activated");
		if (!master) return;
	}

	/**
	 * Helper method returning the current user's details.
	 * 
	 * @return See above.
	 */
	public static ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) registry.lookup(
								LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Helper method returning the current user's Monash DS authentication details.
	 * @return 
	 * 
	 * @return See above.
	 */
	public static String getMonashOmeroDS()
	{ 
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return null;
    	String cookies = null;  // env.getMonashOmeroDS();
		System.out.println("Cookie: " + cookies);
		//UserCredentials uc = (UserCredentials)registry.lookup(LookupNames.USER_CREDENTIALS);
		//LoginConfig cfg = new LoginConfig(registry);
		return cookies;
	}
	
	/**
	 * Helper method returning the current project's Annotations.
	 * 
	 * @return 
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public static Collection getAnnotations(DataObject object) throws DSOutOfServiceException, DSAccessException
	{ 
		OmeroMetadataService os = registry.getMetadataService();
		return os.loadStructuredAnnotations(object.getClass(), object.getId(), -1);
	}
	
	/**
	 * Returns the available user groups.
	 * 
	 * @return See above.
	 */
	public static Set getAvailableUserGroups()
	{
		return (Set) registry.lookup(LookupNames.USER_GROUP_DETAILS);
	}
	
	public AgentSaveInfo getDataToSave() {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(List<Object> instances) {
		// TODO Auto-generated method stub
		
	}
	
	/**
     * Responds to events fired trigger on the bus.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
    	//Process events
        if (e instanceof PublishEvent) {
        	handlePublishEvent((PublishEvent) e);
        } else if (e instanceof UserGroupSwitched) {
        	handleUserGroupSwitched((UserGroupSwitched) e);
    	} else if (e instanceof ExperimenterLoadedDataEvent) {
    		handleExperimenterLoadedDataEvent((ExperimenterLoadedDataEvent) e);
    	}
    }

}
