/*
 * org.openmicroscopy.shoola.svc.transport.BasicChannel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.monash.service;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
/** 
 * Creates a basic <code>HttpChannel</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * <br>
 * Modified by  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class MonashHttpChannel extends HttpChannel
{

	/** The default value for the time out. */
	static final int    	DEF_CONN_TIMEOUT = 10000;

	/** The server's URL. */
	private final URI       serverURL;

	/** The requested path. */
	private final String    requestPath;

	/** The time before being disconnected. */
	private final int       connTimeout;

	/** The communication channel */
	private HttpClient 		channel;

	/**
	 * Creates a new instance.
	 * 
	 * @param url			The server's URL.
	 * @param connTimeout	The time before being disconnected.
	 * @throws IllegalArgumentException If the specified URL is not valid.
	 */
	MonashHttpChannel(String url, int connTimeout) throws IllegalArgumentException
	{
		try {
			serverURL = new URI(url, true);
			requestPath = serverURL.getPath();
		} catch (URIException e) {
			throw new IllegalArgumentException("Invalid URL to Server: " + url + ".");
		}
		this.connTimeout = (connTimeout < 0 ? DEF_CONN_TIMEOUT : connTimeout);
		setCommunicationLink();
	}

	private void setCommunicationLink() {
		HostConfiguration cfg = new HostConfiguration();
		cfg.setHost(serverURL);
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null && proxyPort != null) {
			int port = Integer.parseInt(proxyPort);
			cfg.setProxy(proxyHost, port);
		}

		channel = new HttpClient();
		channel.setHostConfiguration(cfg);
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(connTimeout);
		channel.setParams(params);
	}

	/**
	 * Creates a <code>HttpClient</code> to communicate.
	 * @see HttpChannel#getCommunicationLink()
	 */
	protected HttpClient getCommunicationLink()
	{
		return channel;
	}

	/**
	 * Returns the requested path.
	 * @see HttpChannel#getRequestPath()
	 */
	protected String getRequestPath() { return requestPath; }

}
