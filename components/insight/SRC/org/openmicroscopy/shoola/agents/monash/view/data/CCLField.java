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
import java.util.ArrayList;
import java.util.List;

/** 
 * Java Bean containing Creative Commons License Fields
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */

public class CCLField implements Serializable {

	private String id;
	private String label;
	private String description;
	private String type;

	/** Contains a list of enum field values for the field */
	private List<CCLFieldEnumValues> enumValues;

	public CCLField() {
		super();
		enumValues = new ArrayList<CCLFieldEnumValues>();
	}

	public CCLField(String id, String label) {
		super();
		this.enumValues = new ArrayList<CCLFieldEnumValues>();
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void addEnumValue(CCLFieldEnumValues enumField) {
		enumValues.add(enumField);
	}

	/**
	 * @return the enumValues
	 */
	public List<CCLFieldEnumValues> getEnumValues() {
		return enumValues;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != id)
			sb.append(id);

		if (null != label)
			sb.append("\n\t" + label);

		if (null != description)
			sb.append("\n\t" + description);

		for (CCLFieldEnumValues evalue : enumValues) {
			sb.append("\n" + evalue.getId() + ": ");
			if (null != evalue.getLabel())
				sb.append(evalue.getLabel() + " ");
			if (null != evalue.getDescription())
				sb.append("\n\t" + evalue.getDescription());
		}

		return sb.toString();
	}
}

