package org.openmicroscopy.shoola.agents.monash.svc.communicator;

import org.apache.commons.httpclient.HttpMethod;
import org.openmicroscopy.shoola.svc.proxy.Reply;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

public class MessengerReply extends Reply {

	/** The reply to send to the user. */
	private StringBuilder reply;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param reply	The reply to send to user.
	 */
	MessengerReply(StringBuilder reply)
	{
		this.reply = reply;
	}
	
	/**
	 * Checks the HTTP status.
	 * @see Reply#unmarshal(HttpMethod, HttpChannel)
	 */
	public void unmarshal(HttpMethod response, HttpChannel context) 
		throws TransportException
	{
		if (reply != null)
			reply.append(checkStatusCode(response));
	}
}
