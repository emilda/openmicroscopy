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
		if (reader == null ) initReader();
		String value = compileExpression(RESPONSE_STATUS);
		if (value.equals(RESPONSE_ERROR)) {
			return compileExpression(RESPONSE_MESSAGE);
		}
		return null;
	}

	/** Returns the success message if any from the service reply XML */
	public String getSuccessMsg() {
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
