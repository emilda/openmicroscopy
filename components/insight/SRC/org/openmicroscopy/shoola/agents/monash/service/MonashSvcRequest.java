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
package org.openmicroscopy.shoola.agents.monash.service;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openmicroscopy.shoola.svc.proxy.Request;
import org.openmicroscopy.shoola.svc.transport.TransportException;
/** 
 * Prepares a request to post to Monash DS
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class MonashSvcRequest extends Request {

	/** The parameters to set */
	private Map<String, String> params;
	
	/** The cookie to set */
	private String 				cookie;

	/** The <code>NameValuePair</code> array containing parameters */
	private NameValuePair[] 	nvp;

	/**
	 * Creates a new instance using String parameters
	 * 
	 * @param cookie	The cookie to set.
	 * @param params	The parameters to set.
	 */
	public MonashSvcRequest(String cookie, Map<String, String> params) {
		this(cookie, null, params);
	}

	/**
	 * Creates a new instance using parameters 
	 * as <code>NameValuePair</code>
	 * 
	 * @param cookie	The cookie to set.
	 * @param nvp		The parameters to set as as <code>NameValuePair</code>
	 */
	public MonashSvcRequest(String cookie, NameValuePair[] nvp) {
		this(cookie, nvp, null);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param cookie	The cookie to set.
	 * @param nvp		The parameters to set as as <code>NameValuePair</code>
	 * @param params	The parameters to set.
	 */
	private MonashSvcRequest(String cookie, NameValuePair[] nvp, Map<String, String> params) {
		this.nvp = nvp;
		this.cookie = cookie;
		this.params = params;
	}
	

	/**
	 * Prepares the <code>method</code> to post.
	 * @see Request#marshal()
	 */
	public HttpMethod marshal() throws TransportException {
		//Create request.
        PostMethod request = new PostMethod();
        //request.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        //		new DefaultHttpMethodRetryHandler(3, false));
        if (null != cookie) 
        	request.setRequestHeader("cookie", cookie);
        
        if (params != null) {
        	for (Entry<String, String> entry : params.entrySet())
        	{
        	    request.addParameter(entry.getKey(), entry.getValue());
        	}
        } else if (nvp != null) {
        	//printNvp();
        	request.addParameters(nvp);
        }
		return request;
	}

	/** helper method to print the <code>NameValuePair</code>  */
	private void printNvp() 
	{
		for (NameValuePair pair : nvp) {
			System.out.println("name: " + 
				pair.getName() + " - " + pair.getValue());
		}
	}
}
