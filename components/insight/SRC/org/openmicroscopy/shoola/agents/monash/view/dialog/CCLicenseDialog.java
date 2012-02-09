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
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.parsers.ParserConfigurationException;

import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.util.Unmarshaller;
import org.openmicroscopy.shoola.agents.monash.view.data.LicenceBean;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.xml.sax.SAXException;

/** 
 * Dialog to create the User Defined License. 
 * User Defined License is limited to 4000 characters.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class CCLicenseDialog extends MonashDialog {

	/** The tooltip of the {@link #backButton}. */
	private static final String		BACK_TOOLTIP = 
			"Cancel and go back to previous page.";

	/** The tooltip of the {@link #nextButton}. */
	private static final String		NEXT_TOOLTIP = 
			"Saves the creative commons license and goes to register with RDA screen.";

	/** Action ID to close the dialog. */
	private static final int		BACK = 0;

	/** Action ID to go to the next page and close the dialog. */
	private static final int		NEXT = 1;

	/** Button to close and dispose of the window. */
	private JButton 				backButton;

	/** Button to save and go to next screen. */
	private JButton					nextButton;

	/** URI to Creative Commons Web Services accessible via REST interface. */
	private String 					cclWs;
	/** To group commercial use. */
	private ButtonGroup 			commBtnGroup;

	/** Commercial use allowed - Yes or No **/
	private String 					commercial;

	/** List of choices for commercial use. */
	private JRadioButton[] 			commOptionsRBtn;

	/** To group modification options. */
	private ButtonGroup 			modfcnBtnGroup;

	/** Modifications allowed - Yes, ShareAlike or No **/
	private String 					derivatives;

	/** List of choices for modification. */
	private JRadioButton[] 			modfcnOptionsRBtn;

	/** To group Jurisdiction. */
	private ButtonGroup 			jurdnBtnGroup;

	/** Jurisdiction -  **/
	private String jurisdiction;

	/** The creative commons license */
	private LicenceBean				license;

	/**
	 * Instantiates the dialog to create the Creative Commons License. 
	 * 
	 * @param parent	the parent window
	 * @param title		title of the dialog
	 */
	public CCLicenseDialog(JFrame parent, String title) {
		super(parent, title, "", null);
		cclWs = PublishAgent.getCCLUrl();
		System.out.println("cclUrl: " + cclWs);
	}

	/**
	 * Reacts to click on buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
		case BACK:
			license = null;
			close();
			break;
		case NEXT:
			getCCLicense();
			break;
		}
	}

	private void getCCLicense() {
		commercial = commBtnGroup.getSelection().getActionCommand();
		derivatives = modfcnBtnGroup.getSelection().getActionCommand();
		System.out.println("commercial: " + commercial);
		System.out.println("modification: " + derivatives);
		
		try {
			// TODO setup param from user input
			String licenseParams = "commercial=n&derivatives=y&jurisdiction=au";
			String uri = cclWs  + "/get?" + licenseParams;
			String str  = Unmarshaller.getCCLicense(uri);
			
			license = new LicenceBean();
			license.setCommercial(commercial);
			license.setDerivatives(derivatives);
			//license.setJurisdiction(jurisdiction);
			license.setLicenceContents(str);
			license.setLicenceType(Constants.LICENSE_CCCL_TYPE);
			close();
		} catch (Exception e) {
			showErrDialog(this, Constants.ERROR_CCL, e);
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

		JLabel label = new JLabel("Allow commercial uses of your work?");
		box.add(label, c);
		c.gridy++;

		for (JRadioButton button : commOptionsRBtn) {
			box.add(button, c);
			c.gridy++;
		}

		label = new JLabel("Allow modifications of your work?");
		box.add(label, c);
		c.gridy++;

		for (JRadioButton button : modfcnOptionsRBtn) {
			box.add(button, c);
			c.gridy++;
		}

		label = new JLabel("Jurisdiction of your license?");
		box.add(label, c);

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
		bar.add(nextButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}

	/** 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#initComponents()
	 */
	protected void initComponents() {

		license = null;

		backButton = new JButton("Back");
		formatButton(backButton, 'B', BACK_TOOLTIP, BACK, this);

		nextButton = new JButton("Next");
		formatButton(nextButton, 'N', NEXT_TOOLTIP, NEXT, this);

		//String[] str  = Unmarshaller.getLicenseFields(cclWs);
		
		String[] options = new String[] {"Yes", "No"};
		commOptionsRBtn = setupOptions(options.length, options);
		commBtnGroup = groupOptions(commOptionsRBtn);

		options = new String[] {"Yes", "ShareAlike", "No"};
		modfcnOptionsRBtn = setupOptions(options.length, options);
		modfcnBtnGroup = groupOptions(modfcnOptionsRBtn);
	}

	/**
	 * Groups the buttons passed and returns the group
	 * 
	 * @param buttons the buttons to group
	 * @return	the group for the <code>buttons</code>
	 */
	private ButtonGroup groupOptions(JRadioButton[] buttons) 
	{
		ButtonGroup btnGroup = new ButtonGroup();
		for (JRadioButton button : buttons) {
			btnGroup.add(button);
		}
		return btnGroup;
	}

	/**
	 * Creates and initializes the button group and various options
	 * as <code>JRadioButton</code> for {@link #commBtnGroup}, 
	 * {@link #nextButton} and {@link #nextButton}.
	 * 
	 * @param len		Total number of options
	 * @param options	the possible options
	 * @return			
	 * 		the <code>ButtonGroup</code> grouping the <code>JRadioButton</code>
	 */
	private JRadioButton[] setupOptions(int len, String[] options) 
	{
		JRadioButton[] optionsRBtn = new JRadioButton[len];
		for (int i = 0; i < options.length; i++) {
			optionsRBtn[i] = new JRadioButton(options[i]);
			optionsRBtn[i].setActionCommand(options[i]);
		}
		optionsRBtn[0].setSelected(true);
		return optionsRBtn;
	}

	/**
	 * Return the Creative Commons License 
	 * Null if user cancels the action, closes the dialog
	 * 
	 * @return see above
	 */
	public LicenceBean getLicense() {
		return license;
	}

	public static void main(String[] args) {
		CCLicenseDialog ld = new CCLicenseDialog(null, "Creative Commons License");
		UIUtilities.centerAndShow(ld);
	}
}
