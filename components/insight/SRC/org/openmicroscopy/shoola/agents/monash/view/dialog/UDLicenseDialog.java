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
package org.openmicroscopy.shoola.agents.monash.view.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument.Content;

import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.util.DocumentCharacterLimit;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog to create the User Defined License. 
 * User Defined License is limited to 4000 characters.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class UDLicenseDialog extends MonashDialog {

	/** Description of the panel. */
	private static final String 	DESCRIPTION = "4000 character limit";
	
	/** User Defined License characters limit. */
	private static final int 		UDL_CHARAC_LIMIT = 4000;

	/** The tooltip of the {@link #backButton}. */
	private static final String		BACK_TOOLTIP = "Cancel and go back to previous page.";

	/** The tooltip of the {@link #saveButton}. */
	private static final String		SAVE_TOOLTIP = "Saves the user defined license.";

	/** Action ID to close the dialog. */
	private static final int		BACK = 0;

	/** Action ID to go to the next page and close the dialog. */
	private static final int		SAVE = 1;

	/** Button to close and dispose of the window. */
	private JButton 				backButton;

	/** Button to save and go to next screen. */
	private JButton					saveButton;

	/** The field to enter the user defined license. */
	private JTextArea				licenseArea;
	
	/** The user defined license */
	private String					license;

	/** Scrollpane to hold {@link #licenseArea}. */
	private JScrollPane 			scrollPane;

	/**
	 * Instantiates the dialog to create the User Defined License. 
	 * User Defined License is limited to 4000 characters.
	 * 
	 * @param parent	the parent window
	 * @param title		title of the dialog
	 */
	public UDLicenseDialog(JFrame parent, String title) {
		super(parent, title, "", null);
	}
	
	/**
	 * Reacts to click on buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
		case BACK:
			license = null;
			close();
			break;
		case SAVE:
			saveLicense();
			break;
		}
	}

	private void saveLicense() {
		String udl = licenseArea.getText();
		if (null == udl || Constants.SPACE.equals(udl)) {
			setMessage(Constants.ERROR_NULL_FIELD);
		} else {
			license = udl;
			close();
		}
	}
	
	/** 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#buildContentPane()
	 */
	protected JComponent buildContentPane() {
		JPanel box = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0.1;
		c.gridy = 0;

		JLabel label = new JLabel(DESCRIPTION);
		box.add(label, c);

		c.gridy++;
		box.add(scrollPane, c);

		c.gridy++;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END; //bottom of pane
		box.add(Box.createVerticalStrut(5), c);

		return box;
	}

	/**
	 * Builds and lays out the buttons.
	 * 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#buildToolBar()
	 * @return See above.
	 */
	protected JComponent buildToolBar() {
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(backButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(saveButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}

	/** 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#initComponents()
	 */
	protected void initComponents() {

		license = null;
		
		licenseArea = new JTextArea(15, 50);
		licenseArea.setWrapStyleWord(true);
		licenseArea.setLineWrap(true);
		licenseArea.setDocument(new DocumentCharacterLimit(UDL_CHARAC_LIMIT));
		scrollPane  = new JScrollPane(licenseArea);

		backButton = new JButton("Back");
		formatButton(backButton, 'B', BACK_TOOLTIP, BACK, this);

		saveButton = new JButton("Next");
		formatButton(saveButton, 'S', SAVE_TOOLTIP, SAVE, this);
	}

	/**
	 * Return the user defined license 
	 * Null if user cancels the action, closes the dialog
	 * 
	 * @return see above
	 */
	public String getLicense() {
		return license;
	}
	
	public static void main(String[] args) {
		UDLicenseDialog ld = new UDLicenseDialog(null, "Define Your Own License");
		UIUtilities.centerAndShow(ld);
	}
}
