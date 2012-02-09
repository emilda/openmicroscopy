package org.openmicroscopy.shoola.agents.monash.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openmicroscopy.shoola.svc.proxy.Request;
import org.openmicroscopy.shoola.svc.transport.TransportException;

public class MonashSvcRequest extends Request {

	/** The parameters to set */
	private Map<String, String> 	params;
	
	/** The cookie to set */
	private String 					cookie;

	/** The <code>NameValuePair</code> array containing parameters */
	private NameValuePair[] 		nvp;

	/**
	 * Creates a new instance.
	 * 
	 * @param cookie	The cookie to set.
	 * @param params		The parameters to set.
	 */
	public MonashSvcRequest(String cookie, Map<String, String> params) {
		this.params = params;
		this.cookie = cookie;
		nvp = null;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param cookie	The cookie to set.
	 * @param params		The parameters to set.
	 */
	public MonashSvcRequest(String cookie, NameValuePair[] nvp) {
		this.nvp = nvp;
		this.cookie = cookie;
		params = null;
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
        	request.addParameters(nvp);
        }
		return request;
	}
}
