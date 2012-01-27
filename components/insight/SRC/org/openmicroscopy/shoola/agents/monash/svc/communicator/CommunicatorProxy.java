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
package org.openmicroscopy.shoola.agents.monash.svc.communicator;

import java.io.IOException;
import java.util.Map;

import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Activates the {@link Communicator}.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class CommunicatorProxy extends AbstractProxy implements Communicator {

	/**
	 * Creates a new instance.
	 * 
	 * @param channel The communication link.
	 */
	protected CommunicatorProxy(HttpChannel channel) {
		super(channel);
	}

	/**
	 * Implemented as specified by the {@link Communicator} interface.
	 * @see Communicator#searchRM()
	 */
	public Object searchRM(String cookie, Map<String, String> params)
			throws TransportException 
	{
		MessengerRequest out = new MessengerRequest(cookie, params);
		StringBuilder reply = new StringBuilder();
		MessengerReply in = new MessengerReply(reply);
		
		try {
			channel.exchange(out, in);
		} catch (IOException ioe) {
			throw new TransportException(
					"Couldn't communicate with server (I/O error).", ioe);
		}
		return reply;
	}

}
