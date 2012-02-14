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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.agents.monash.view.data.CCLField;
import org.openmicroscopy.shoola.agents.monash.view.data.CCLFieldEnumValues;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** 
 * Helper class to unmarshall XML into Objects
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class Unmarshaller {

	/**
	 * Unmarshal the XML into PartyBean object
	 * @param result	the XML file to unmarshall	
	 * @return			the PartyBean object
	 */
	public static PartyBean getPartyBean(String result) 
	{
		if (result == null) return null;

		try {

			String prefix = "/response/message/registryObject/";

			InputStream is = IOUtils.toInputStream(result, "UTF-8");
			XMLReader reader = new XMLReader(is);
			PartyBean pb = new PartyBean();
			pb.setFromRm("true");
			pb.setRifcsContent(result);

			// To get a xml attribute, group
			String expression = prefix + "@group";
			pb.setGroupName(compileExpression(reader, expression));

			// To get a child element's value.
			expression = prefix + "key";
			pb.setPartyKey(compileExpression(reader, expression));

			// To get originatingSource type.
			expression = prefix + "originatingSource/@type";
			pb.setOriginateSourceType(compileExpression(reader, expression));

			// To get originatingSource value.
			expression = prefix + "originatingSource";
			pb.setOriginateSourceValue(compileExpression(reader, expression));

			// To get identifier type.
			expression = prefix + "party/identifier/@type";
			pb.setIdentifierType(compileExpression(reader, expression));

			// To get identifier value.
			expression = prefix + "party/identifier";
			pb.setIdentifierValue(compileExpression(reader, expression));

			// To get person title.
			expression = prefix + "party/name/namePart[1]";
			pb.setPersonTitle(compileExpression(reader, expression));

			// To get person Given Name.
			expression = prefix + "party/name/namePart[2]";
			pb.setPersonGivenName(compileExpression(reader, expression));

			// To get person Family Name.
			expression = prefix + "party/name/namePart[3]";
			pb.setPersonFamilyName(compileExpression(reader, expression));

			// TODO other addresses?
			
			// To get address electronic address.
			expression = prefix + "party/location/address/electronic";
			NodeList electronic = (NodeList)reader.read(expression, XPathConstants.NODESET);
			for (int i = 0; i < electronic.getLength(); i++) 
			{
				Node field = electronic.item(i);
				String addrType = field.getAttributes().getNamedItem("type").getTextContent();
				String value = field.getTextContent();
				if (addrType.equals("url")) {
					pb.setUrl(value);
				} else if (addrType.equals("email")) {
					pb.setEmail(value);
				}
			}
			return pb;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Compiles the given <code>expression</code> using the reader and
	 * return the result as <code>String</code
	 * 
	 * @param reader	the XML reader @see XMLReader
	 * @param expression the element or attribute in the XML
	 * @return	the value of the <code>expression</code> from the XML as <code>String</code
	 */
	private static String compileExpression(XMLReader reader, String expression) 
	{
		String value = (String) reader.read(expression, XPathConstants.STRING);
		return value;
	}

	/**
	 * Creative Commons License based on the user parameters
	 * 
	 * @param uri		URI to the XML document
	 * @return			the Creative Commons License
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static String getCCLicense(String uri) 
			throws SAXException, IOException, ParserConfigurationException {

		XMLReader reader = new XMLReader(uri);
		String prefix = "/result/";

		// Get URI
		String expression = prefix + "license-uri";
		String luri = compileExpression(reader, expression);

		// Get Html
		expression = prefix + "html";
		String html = compileExpression(reader, expression);
		html = StringUtils.removeEnd(html, ".").trim();

		return html + " (" + luri + ").";
	}

	public static List<CCLField> getLicenseFields(String uri) 
			throws SAXException, IOException, ParserConfigurationException 
	{
		XMLReader reader = new XMLReader(uri);
		NodeList fieldList = (NodeList)reader.read("/licenseclass/field", XPathConstants.NODESET);

		List<CCLField> fields = new ArrayList<CCLField>();

		for (int i = 0; i < fieldList.getLength(); i++) {
			Node field = fieldList.item(i);
			CCLField ccfield = new CCLField();
			String id = field.getAttributes().getNamedItem("id").getTextContent();
			ccfield.setId(id);
			if (field.hasChildNodes()) {
				NodeList fieldElemsList = field.getChildNodes();
				for (int j = 0; j < fieldElemsList.getLength(); j++) 
				{
					Node fieldElem = fieldElemsList.item(j);
					String nodename = fieldElem.getNodeName();
					String nodevalue  = fieldElem.getTextContent();
					if(nodename.equals("label"))
						ccfield.setLabel(nodevalue);
					else if(nodename.equals("description"))
						ccfield.setDescription(nodevalue);
					else if(nodename.equals("type"))
						ccfield.setType(nodevalue);
					else if(nodename.equals("enum")) {
						String enumId = fieldElem.getAttributes().getNamedItem("id").getTextContent();
						CCLFieldEnumValues enumField = new CCLFieldEnumValues();
						if (enumId.equals("")) enumId = Constants.INTERNATIONAL;
						enumField.setId(enumId);
						if (fieldElem.hasChildNodes()) {
							NodeList enumList = fieldElem.getChildNodes();
							for (int k = 0; k < enumList.getLength(); k++) {
								Node enumElem = enumList.item(k);
								String ename = enumElem.getNodeName();
								String evalue  = enumElem.getTextContent();
								if(ename.equals("label"))
									enumField.setLabel(evalue);
								else if(ename.equals("description"))
									enumField.setDescription(evalue);
							}
						}
						ccfield.addEnumValue(enumField);
					}	// nodename is enum
				} // field elements
			}
			fields.add(ccfield);
		} // fields
		
		//System.out.println("----ccfields: " + fields.toString());	// TODO remove
		return fields;
	}
}
