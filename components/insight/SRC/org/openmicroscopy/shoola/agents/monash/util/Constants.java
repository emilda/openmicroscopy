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
/** 
 * Constant values used in the application.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since OME3.0
 */
public class Constants {

	/** Identifies the <code>Collection name</code> field. */
	public static final String COLLECTION_NAME = "Collection Name";
	
	/** Identifies the <code>Collection description</code> field. */
	public static final String COLLECTION_DESCRIPTION = "Collection Description";
	
	/** Identifies the <code>Researcher</code> field. */
	public static final String RESEARCHER = "The Associated Researcher(s): ";
	
	/** Identifies the <code>License</code> field. */
	public static final String LICENSE = "License Required: ";
	
	/** Field to access the login token to Monash DS.  */
	public static final String LOGIN_TOKEN = "/services/monash/loginToken";
	
	/** Field to access the service token for data registration WS in Monash DS.  */
	public static final String MDREG_TOKEN = "/services/monash/mdregToken";
	
	/** Field to access the service token for Party WS in Monash DS.  */
	public static final String PARTY_TOKEN = "/services/monash/partyToken";
	
	/** The REST interface URL to Creative Commons Web Services.  */
	public static final String CCL_URL = "/services/ws/ccl";

	/** Error message when party field is null */
    public static final String ERROR_NULL_FIELD = "Please enter the compulsory field";
    
    /** Error message when party is not found in <code>Research Master</code> */
    public static final String ERROR_PARTY_NF = "Failed to retrieve party information.";

    /** Error message when <code>Monash DS</code> is not available */
	public static final String BACKEND_ERROR = "Monash Data Server Error";

	/** Warning message */
	public static final String WARN_MESSAGE = "Warning Message";
	
	/** Information message */
	public static final String INFO_MESSAGE = "Information";
	
	/** Blank field */
	public static final String SPACE = "";

	/** Creative Commons License type */
	public static final String LICENSE_CCCL_TYPE = "cccl_license";

	/** User Defined License type */
	public static final String LICENSE_USER_DEFINE_TYPE = "user_license";
	
	/** Error retrieving Creative Commons License */
    public static final String ERROR_CCL = "Failed to retrieve Creative Commons License.";

    /** Tag value identifying the collection to register with RDA */
    public static final String REGISTER_RDA_TAG = "Register with RDA";
    
    /** Tag value identifying the collection successfully registered with RDA */
    public static final String SUCCESS_RDA_TAG = "Successfully Registered with RDA";

    /** International License */
	public static final String INTERNATIONAL = "International";
}
