package org.openmicroscopy.shoola.agents.monash.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openmicroscopy.shoola.svc.proxy.Request;
import org.openmicroscopy.shoola.svc.transport.TransportException;

public class MonashSvcRequest extends Request {

	/** The parameters to set */
	private Map<String, String> 	info;
	
	/** The cookie to set */
	private String 					cookie;

	/**
	 * Creates a new instance.
	 * 
	 * @param cookie	The cookie to set.
	 * @param info		The parameters to set.
	 */
	public MonashSvcRequest(String cookie, Map<String, String> info) {
		this.info = info;
		this.cookie = cookie;
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
        //Marshal.
        Entry entry;
        Iterator k = info.entrySet().iterator();
        while (k.hasNext()) {
        	entry = (Entry) k.next();
        	request.addParameter((String) entry.getKey(),
        			 (String) entry.getValue());
		}
		return request;
	}

}
