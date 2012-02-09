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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Implementation of services described in {@link MonashServices}.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class MonashServicesImpl implements MonashServices {

	/** The channel to communicate. */
	protected final MonashHttpChannel channel;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channel The communication link.
	 */
	protected MonashServicesImpl(MonashHttpChannel channel) {
		if (channel == null) throw new NullPointerException("No channel.");
        this.channel = channel;
	}

	/**
	 * Implemented as specified by the {@link MonashServices} interface.
	 * @see MonashServices#searchRM()
	 */
	public MonashSvcReply searchRM(String cookie, Map<String, String> params)
			throws TransportException 
	{
		MonashSvcRequest out = new MonashSvcRequest(cookie, params);
		StringBuilder reply = new StringBuilder();
		MonashSvcReply in = new MonashSvcReply(reply);
		
		try {
			channel.exchange(out, in);
			System.out.println("response from serachRM:" + in.toString()); 
			return in ;
		} catch (IOException ioe) {
			throw new TransportException(
					"Couldn't communicate with server (I/O error).", ioe);
		}
	}

	/**
	 * Implemented as specified by the {@link MonashServices} interface.
	 * @see MonashServices#login()
	 */
	public String login(Map<String, String> params) 
			throws TransportException 
	{
		MonashSvcRequest out = new MonashSvcRequest(null, params);
		StringBuilder reply = new StringBuilder();
		MonashSvcReply in = new MonashSvcReply(reply);
		
		try {
			channel.exchange(out, in);
			System.out.println("response from login:" + in.toString());
			
			return in.getCookie();
			
		} catch (IOException ioe) {
			throw new TransportException(
					"Couldn't communicate with server (I/O error).", ioe);
		}
	}

	/**
	 * Implemented as specified by the {@link MonashServices} interface.
	 * @see MonashServices#mdReg()
	 */
	public MonashSvcReply mdReg(String cookie, NameValuePair[] nvp)
			throws TransportException {

		MonashSvcRequest out = new MonashSvcRequest(cookie, nvp);
		StringBuilder reply = new StringBuilder();
		MonashSvcReply in = new MonashSvcReply(reply);
		
		try {
			channel.exchange(out, in);
			System.out.println("response from mdReg:" + in.toString()); 
			return in ;
		} catch (IOException ioe) {
			throw new TransportException(
					"Couldn't communicate with server (I/O error).", ioe);
		}
	}
}
