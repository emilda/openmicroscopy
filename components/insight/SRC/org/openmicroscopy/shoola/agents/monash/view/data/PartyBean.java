package org.openmicroscopy.shoola.agents.monash.view.data;
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
import java.io.Serializable;

/**
 * JavaBean class containing Party information
 *
 * @author Simon Yu - Xiaoming.Yu@monash.edu
 * @version 2.0
 * 
 * Modified by Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 */
public class PartyBean implements Serializable {

    private String groupName;

    private String partyKey;

    private String originateSourceType;

    private String originateSourceValue;

    private String identifierType;

    private String identifierValue;

    private String personTitle;

    private String personGivenName;

    private String personFamilyName;

    private String url;

    private String email;

    private String address;

    private String rifcsContent;

    private String fromRm;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPartyKey() {
        return partyKey;
    }

    public void setPartyKey(String partyKey) {
        this.partyKey = partyKey;
    }

    public String getOriginateSourceType() {
        return originateSourceType;
    }

    public void setOriginateSourceType(String originateSourceType) {
        this.originateSourceType = originateSourceType;
    }

    public String getOriginateSourceValue() {
        return originateSourceValue;
    }

    public void setOriginateSourceValue(String originateSourceValue) {
        this.originateSourceValue = originateSourceValue;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getPersonTitle() {
        return personTitle;
    }

    public void setPersonTitle(String personTitle) {
        this.personTitle = personTitle;
    }

    public String getPersonGivenName() {
        return personGivenName;
    }

    public void setPersonGivenName(String personGivenName) {
        this.personGivenName = personGivenName;
    }

    public String getPersonFamilyName() {
        return personFamilyName;
    }

    public void setPersonFamilyName(String personFamilyName) {
        this.personFamilyName = personFamilyName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRifcsContent() {
        return rifcsContent;
    }

    public void setRifcsContent(String rifcsContent) {
        this.rifcsContent = rifcsContent;
    }

    /**
	 * @return the fromRm
	 */
	public String getFromRm() {
		return fromRm;
	}

	/**
	 * @param fromRm the fromRm to set
	 */
	public void setFromRm(String fromRm) {
		this.fromRm = fromRm;
	}

	/** 
	 * Return in the format, Title gName fName - gName <br>
	 * Eg. Ms first last - Monash University 
	 */
    public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != personTitle)
			sb.append(personTitle + " ");
		
		if (null != personGivenName)
			sb.append(personGivenName + " ");
		
		if (null != personFamilyName)
			sb.append(personFamilyName + " ");
		
		sb.append("- ");
		
		if (null != groupName)
			sb.append(groupName);
		
		return sb.toString();
	}
}

