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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;

import pojos.ExperimenterData;
/** 
 * Defines the interface provided by the <code>Register with RDA</code> component. 
 * The Viewer provides a top-level window hosting UI components to interact 
 * with the instance.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class AndsPublishFactory implements ChangeListener {
	
	/** The sole instance. */
    private static final AndsPublishFactory  singleton = new AndsPublishFactory();

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
        singleton.viewer = singleton.getViewer(model);
        PublishAgent.setMonashOmeroDS();
		System.out.println("Cookie.........done");
		return singleton.viewer;
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
		return comp;
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

    /**
	 * Returns a {@link AndsPublish}.
	 *  
	 * @return See above.
	 */
	public static AndsPublish getViewer() {
		return singleton.viewer;
	}

	public static void setCookie(UserCredentials uc) {
    	System.out.println("AndsPublishFactory setCookie");
		if (singleton.viewer != null) {
			singleton.viewer.setCookie(monashAuth(uc));
		}
	}
	
	/**
     * 
     * Authenticates to Monash DS. 
     * TODO Re-work on this method to use <code>Communicator</code> instead of <code>HttpClient</code>
     * @param uc
     */
    private static String monashAuth(UserCredentials uc) { 
    	System.out.println("monashLogin: " + uc.toString());
		BufferedReader reader = null;

		HttpClient httpClient = new HttpClient(); 
		httpClient.getParams().setParameter("http.useragent", "Test Client");
		//httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY); 

		String loginToken = PublishAgent.getLoginToken();
		System.out.println("loginToken: " + loginToken);
		
		PostMethod postMethod = new PostMethod(loginToken); 
		
		// Provide custom retry handler is necessary
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		NameValuePair[] data = { 
				new NameValuePair("userName", uc.getUserName()), 
				new NameValuePair("password", uc.getPassword()) }; 
		postMethod.setRequestBody(data); 
		try { 
			int statusCode = httpClient.executeMethod(postMethod); 
			System.out.println("login statusCode: " + statusCode);
			StringBuffer response = new StringBuffer(); 
			reader = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));	//get response from server

			String line; 
			while ((line = reader.readLine()) != null) { 
				response.append(line).append(System.getProperty("line.separator")); 
			} 
			System.out.println("login:" + response); 
			reader.close(); 

			Cookie[] cookies = httpClient.getState().getCookies();//get cookie
			String tmpcookies = ""; 
			for(Cookie c:cookies){ 
				tmpcookies = tmpcookies + c.toString();	//+";"; 
				System.out.println(c); 
			} 
			return tmpcookies;
			
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			/*LogMessage msg = new LogMessage();
            msg.println("Fatal protocol violation while connecting to Monash OmeroMD.");
            msg.println("Reason: " + e.getMessage());
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);*/
			//e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			//e.printStackTrace();
			/*LogMessage msg = new LogMessage();
            msg.println("Failed to connect to Monash OmeroMD.");
            msg.println("Reason: " + e.getMessage());
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);*/
		} finally {
			// Release the connection.
			postMethod.releaseConnection(); 
			if(reader != null) try { reader.close(); } catch (Exception fe) {}
		}
		return null;  
	} 

}
