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

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.openmicroscopy.shoola.agents.monash.view.dialog.UDLicenseDialog;
/** 
 * This document is linked to the {@link UDLicenseDialog#licenseArea}.
 * to limit the characters entered to 4000.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class DocumentCharacterLimit extends PlainDocument {
	
	/** The character limit */
	private int 	limit;

	/**
	 * Creates a new instance. Beeps sound when the character
	 * limit is reached.
	 * @param limit	the character limit
	 */
	public DocumentCharacterLimit(int limit) {
		super();
		this.limit = limit;
	}

	/**
	 * @see PlainDocument#insertString
	 */
	public void insertString(int offset, String  str, AttributeSet attr)
			throws BadLocationException {
		if (str == null) return;

		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		} else {
	        Toolkit.getDefaultToolkit().beep();
	    }
	}
}
