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

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
/** 
 * Helper class to unmarshall XML into Objects
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class Unmarshaller {

	private static final String PREFIX = "/response/message/registryObject/";

	/**
	 * Unmarshal the XML into PartyBean object
	 * @param result	the XML file to unmarshall	
	 * @return			the PartyBean object
	 */
	public static PartyBean getPartyBean(String result) 
	{
		if (result == null) return null;
		
		try {
			InputStream is = IOUtils.toInputStream(result, "UTF-8");
			XMLReader reader = new XMLReader(is);
			PartyBean pb = new PartyBean();
			pb.setSelected(true);

			// To get a xml attribute, group
			String expression = PREFIX + "@group";
			pb.setGroupName(compileExpression(reader, expression));

			// To get a child element's value.
			expression = PREFIX + "key";
			pb.setPartyKey(compileExpression(reader, expression));

			// To get originatingSource type.
			expression = PREFIX + "originatingSource/@type";
			pb.setOriginateSourceType(compileExpression(reader, expression));

			// To get originatingSource value.
			expression = PREFIX + "originatingSource";
			pb.setOriginateSourceValue(compileExpression(reader, expression));

			// To get identifier type.
			expression = PREFIX + "party/identifier/@type";
			pb.setIdentifierType(compileExpression(reader, expression));

			// To get identifier value.
			expression = PREFIX + "party/identifier";
			pb.setIdentifierValue(compileExpression(reader, expression));

			// To get person title.
			expression = PREFIX + "party/name/namePart[1]";
			pb.setPersonTitle(compileExpression(reader, expression));

			// To get person Given Name.
			expression = PREFIX + "party/name/namePart[2]";
			pb.setPersonGivenName(compileExpression(reader, expression));

			// To get person Family Name.
			expression = PREFIX + "party/name/namePart[3]";
			pb.setPersonFamilyName(compileExpression(reader, expression));

			// To get address electronic address type.
			expression = PREFIX + "party/location/address/electronic/@type";
			String addrType = compileExpression(reader, expression);
			
			// To get address electronic address value.
			expression = PREFIX + "party/location/address/electronic/value";
			String value = compileExpression(reader, expression);
			
			if (addrType.equals("url")) {
				pb.setUrl(value);
			} else if (addrType.equals("email")) {
				pb.setEmail(value);
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
	private static String compileExpression(XMLReader reader, String expression) {
		String value = (String) reader.read(expression, XPathConstants.STRING);
		System.out.println(expression + " ---> " + value);
		return value;
	}
}
