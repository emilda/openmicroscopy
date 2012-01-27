package org.openmicroscopy.shoola.agents.monash.svc.communicator;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openmicroscopy.shoola.svc.proxy.Request;
import org.openmicroscopy.shoola.svc.transport.TransportException;

public class MessengerRequest extends Request {

	private Map<String, String> info;
	private String cookie;

	public MessengerRequest(String cookie, Map<String, String> info) {
		this.info = info;
		this.cookie = cookie;
	}

	@Override
	public HttpMethod marshal() throws TransportException {
		//Create request.
        PostMethod request = new PostMethod();
        request.setRequestHeader("cookie", cookie);
        //Marshal.
        //if (email != null) request.addParameter(EMAIL, email);
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
