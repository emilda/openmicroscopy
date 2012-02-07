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
package org.openmicroscopy.shoola.agents.monash.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
/** 
 * Helper class using XPath APIs to read the XML file.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class XMLReader {

	/** Stream containing the XML document to parse */
	private InputStream xmlStream;

	/** URL containing the XML document to parse */
	private String		xmlUrlStr;
	
	/** the XML document */
	private Document 	xmlDocument;

	/** to navigate in XML documents */
	private XPath 		xPath;

	/**
	 * Creates a new instance.
	 * 
	 * @param xmlStream	the Stream containing the XML document to parse.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public XMLReader(InputStream xmlStream) 
			throws SAXException, IOException, ParserConfigurationException {
		
		this.xmlStream = xmlStream;
		init();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param xmlUrlStr	the String URL containing the XML document to parse.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public XMLReader(String xmlUrlStr) 
			throws SAXException, IOException, ParserConfigurationException {
		
		this.xmlUrlStr = xmlUrlStr;
		init();
	}
	
	/**
	 * Initializes xPath and creates @see Document
	 * from the {@link #xmlStream}.
	 */
	private void init() 
			throws SAXException, IOException, ParserConfigurationException {  
		
		if (null != xmlStream) {
			xmlDocument = DocumentBuilderFactory.
					newInstance().newDocumentBuilder().parse(xmlStream);
		} else if (null != xmlUrlStr) {
			xmlDocument = DocumentBuilderFactory.
					newInstance().newDocumentBuilder().parse(xmlUrlStr.toString());
		}
		xPath =  XPathFactory.newInstance().newXPath();     
	}

	/**
	 * Returns the value of the <code>expression</code> in the format
	 * specified in <code>returnType</code>
	 * @param expression the XPath expression
	 * @param returnType the type of object returned
	 * @return
	 */
	public Object read(String expression, QName returnType) {
		try {
			XPathExpression xPathExpression = xPath.compile(expression);
			return xPathExpression.evaluate(xmlDocument, returnType);
		} catch (XPathExpressionException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
