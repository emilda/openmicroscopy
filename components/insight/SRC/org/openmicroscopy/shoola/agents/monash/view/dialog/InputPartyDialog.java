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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JTextField;

import org.openmicroscopy.shoola.agents.monash.service.MonashSvcReply;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.util.Unmarshaller;
import org.openmicroscopy.shoola.agents.monash.view.AndsPublishModel;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.openmicroscopy.shoola.svc.transport.TransportException;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog to Search Party in the Research Master
 * @see #OPTIONS
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class InputPartyDialog extends MonashDialog {

	/** The default size of the window. */
	private static final Dimension 	PANEL_SIZE = new Dimension(700, 600);
	
	/** Description of the panel. */
	private static final String 	DESCRIPTION = "This page is under construction";

	/** The tooltip of the {@link #backButton}. */
	private static final String		BACK_TOOLTIP = "Go back to previous page.";

	/** The tooltip of the {@link #addButton}. */
	private static final String		ADD_TOOLTIP = "Search for Party in the Research Master.";

	/** Action ID to close the dialog. */
	private static final int		BACK = 0;

	/** Action ID to go to the next page and close the dialog. */
	private static final int		ADD = 1;

	/** Field description of the {@link #titleField}. */
	private static final String MAX_TWENTY = "*(Maximum 20 characters in length)";

	private static final String VALID_EMAIL = "*(A valid email required, e.g. yourname@example.com)";

	private static final String URL_REQD = "*(A valid web URL required, e.g. http://www.sample.com/profile)";

	private static final String GROUP_NAME = "*(Maximum 100 characters in length, e.g. Monash University)";

	private static final String GROUP_URL = "*(A valid web URL required, e.g. http://www.monash.edu.au)";

	private static final String ADD_DETAIL = "*(Maximum 255 characters in length, a physical address)";

	/** Button to close and dispose of the window. */
	private JButton 				backButton;

	/** Button to search for Party in the Research Master. */
	private JButton					addButton;

	/** The field holding the title of the party to add. */
	private JTextField				titleField;
	
	/** The field holding the first name of the party add. */
	private JTextField				fName;
	
	/** The field holding the last name of the party to add. */
	private JTextField				lName;
	
	/** Search if successful sets <code>PartyBean</code>. */
	private PartyBean 				partyBean;

	/**
	 * Instantiates the dialog to Search Party in the Research Master
	 * @see #OPTIONS
	 * @param parent	the parent window
	 * @param title		title of the dialog
	 * @param model		Reference to the model
	 */
	public InputPartyDialog(JFrame parent, String title) {
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
			close();
			break;
		case ADD:
			addParty();
			break;
		}
	}

	private void addParty() {
		setMessage("");
		String title = titleField.getText();
		//StringUtils.isBlank(party); TODO use this.
		if (null == title || Constants.SPACE.equals(title)) {
			setMessage(Constants.ERROR_NULL_FIELD);
		} else {
			// create party bean
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

		//JLabel label = new JLabel(DESCRIPTION);
		//box.add(label, c);

		addField(box, c, "Title", MAX_TWENTY, titleField);
		addField(box, c, "First Name", MAX_TWENTY, fName);
		addField(box, c, "Last Name", MAX_TWENTY, lName);
		addField(box, c, "Email", VALID_EMAIL, lName);
		addField(box, c, "Address", ADD_DETAIL, lName);
		addField(box, c, "Web URL", URL_REQD, lName);
		addField(box, c, "Group Name", GROUP_NAME, lName);
		addField(box, c, "Group Wb Site", GROUP_URL, lName);

		c.gridy++;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END; //bottom of pane
		box.add(Box.createVerticalStrut(5), c);

		return box;
	}

	private void addField(JPanel box, GridBagConstraints c, String fieldTxt,
			String info, JTextField comp) {
		c.gridy++;
		JLabel label = new JLabel(fieldTxt + ":");
		box.add(label, c);
		c.gridy++;
		
		JLabel infoLabel = new JLabel(info);
		Font f = new Font("Serif", Font.PLAIN, 10);
		infoLabel.setFont(f);
		//infoLabel.setFont(infoLabel.getFont().deriveFont(4));
		infoLabel.setForeground(Color.GRAY);
		
		box.add(infoLabel, c);
		c.gridy++;
		box.add(comp, c);
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
		bar.add(addButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}

	/** 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#initComponents()
	 */
	protected void initComponents() {

		setSize(PANEL_SIZE);
		
		partyBean = null;
		
		titleField = new JTextField(20);
		titleField.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		fName = new JTextField(20);
		fName.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		lName = new JTextField(20);
		lName.setBackground(UIUtilities.BACKGROUND_COLOR);

		backButton = new JButton("Back");
		formatButton(backButton, 'B', BACK_TOOLTIP, BACK, this);
		this.getRootPane().setDefaultButton(backButton);

		addButton = new JButton("Next");
		formatButton(addButton, 'A', ADD_TOOLTIP, ADD, this);
		addButton.setEnabled(false);
		

	}

	/**
	 * Return the <code>PartyBean</code> if search was successful. 
	 * Null if user cancels the action, closes the dialog
	 * or no party information found in <code>Research Master</code>.
	 * 
	 * @return the partyBean
	 */
	public PartyBean getPartyBean() {
		return partyBean;
	}
	
	public static void main(String[] args) {
		InputPartyDialog ld = new InputPartyDialog(null, "Manually Input a Researcher Information");
		UIUtilities.centerAndShow(ld);
	}
}
