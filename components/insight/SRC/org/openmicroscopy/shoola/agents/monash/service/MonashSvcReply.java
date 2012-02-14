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

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.openmicroscopy.shoola.agents.monash.util.XMLReader;
import org.openmicroscopy.shoola.svc.proxy.Reply;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;
/** 
 * Reply from Monash DS
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class MonashSvcReply extends Reply {

	/** service reply contains error message */
	private static final String RESPONSE_ERROR 	= "ERROR";
	
	/** service reply contains success message */
	private static final String RESPONSE_SUCCESS = "SUCCESS";
	
	/** Element that contains the status in service reply XML */
	private static final String RESPONSE_STATUS = "/response/status";
	
	/** Element that contains the message in service reply XML */
	private static final String RESPONSE_MESSAGE = "/response/message";

	/** The reply from Monash DS services. */
	private StringBuilder 		reply;

	/** Reader that uses XPath to read XML file */
	private XMLReader 			reader = null;

	/** Cookies received from Monash DS */
	private Cookie[] 			cookies = null;

	/**
	 * Creates a new instance.
	 * 
	 * @param reply	The reply to send to user.
	 */
	MonashSvcReply(StringBuilder reply)
	{
		if (reply == null) 
			throw new NullPointerException("Cannot instantiate MonashSvcReply using null reply field.");
		this.reply = reply;
	}

	/**
	 * Checks the HTTP status.
	 * @see Reply#unmarshal(HttpMethod, HttpChannel)
	 */
	public void unmarshal(HttpMethod response, HttpChannel channel) 
			throws TransportException {
		if (reply != null) {
			reply.append(checkStatusCode(response));
			setCookie(channel);
		} 
	}

	/**
	 * Set the cookie received from Monash DS
	 * @param channel
	 */
	private void setCookie(HttpChannel channel) {
		MonashHttpChannel bc = (MonashHttpChannel)channel;
		this.cookies = bc.getCommunicationLink().getState().getCookies();
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return reply.toString();
	}

	/** Returns the error message if any from the service reply XML */
	public String getErrMsg() {
		return getResponseMsg(RESPONSE_ERROR);
	}

	/** Returns the success message if any from the service reply XML */
	public String getSuccessMsg() {
		return getResponseMsg(RESPONSE_SUCCESS);
	}
	
	/**
	 * return the value of the element, message in the response.
	 * @param status the value for the element, status
	 * 			two possible values are ERROR and SUCCESS
	 * @return see above
	 */
	private String getResponseMsg(String status) {
		if (reader == null ) initReader();
		String value = compileExpression(RESPONSE_STATUS);
		if (value.equals(status)) {
			return compileExpression(RESPONSE_MESSAGE);
		}
		return null;
	}

	/** Returns the reply XML from Monash DS */
	public String getReply() {
		if (reader == null ) initReader();
		String value = compileExpression(RESPONSE_STATUS);
		if (value.equals(RESPONSE_SUCCESS)) {
			return reply.toString();
		}
		return null;
	}
	
	/** Initialize the reader */
	private void initReader() {
		try {
			InputStream is = IOUtils.toInputStream(reply.toString(), "UTF-8");
			reader = new XMLReader(is);
		} catch (Exception e) {}
	}

	/**
	 * Return the response message from the element mentioned in
	 * <code>expression</code> parameter
	 * @param expression	the XPath expression to search for
	 * @return
	 */
	private String compileExpression(String expression) {
		if (null != reader) {
			return reader.read(expression, XPathConstants.STRING).toString();
		}
		return "";
	}

	/**
	 * @return the cookie from Monash DS
	 */
	public String getCookie() {
		if (cookies == null) return null;
		
		String tmp = ""; 
		for(Cookie c:cookies){ 
			tmp = tmp + c.toString();	//+";"; 
		} 
		System.out.println("Received cookie, " + tmp);
		return tmp;
	}
}
