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
package org.openmicroscopy.shoola.agents.monash.view.data;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Java Bean containing the details about the data collection to
 * register with RDA
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class MonashData {
	
	/** Identifies the id field. */
	private String 			id = null;
	
	/** Identifies the title field. */
	private String 			title = null;
	
	/** Identifies the description field. */
	private String 			description = null;
	
	/** Identifies the type i.e Project/Dataset. */
	private String 			type = null;
	
	/** DataObject. */
	private DataObject 		dataObject = null;

	public MonashData(DataObject dataObject) {
		this.dataObject = dataObject;
		
		if (dataObject instanceof ProjectData) {
			title = ((ProjectData) dataObject).getName();
			description = ((ProjectData) dataObject).getDescription();
			type = "Project";
		} else if (dataObject instanceof DatasetData) {
			title = ((DatasetData) dataObject).getName();
			description = ((DatasetData) dataObject).getDescription();
			type = "Dataset";
		}
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return dataObject.getId();
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the dataObject
	 */
	public DataObject getDataObject() {
		return dataObject;
	}

	/* 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return title;
	}
}
