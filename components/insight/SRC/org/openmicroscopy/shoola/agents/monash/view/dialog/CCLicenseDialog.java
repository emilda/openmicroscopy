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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openmicroscopy.shoola.agents.monash.IconManager;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.util.CCLClient;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.data.CCLField;
import org.openmicroscopy.shoola.agents.monash.view.data.CCLFieldEnumValues;
import org.openmicroscopy.shoola.agents.monash.view.data.LicenceBean;
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

	/** Label for commercial */
	private JLabel 					commLabel;

	/** Description for field, commercial */
	private String 					commDescr;

	/** To group commercial field options. */
	private ButtonGroup 			commBtnGroup;

	/** Commercial use options */
	private JRadioButton[] 			commOptRBtn;

	/** Description for commercial use options **/
	private String[] 				commOpDesc;

	/** Label for modification */
	private JLabel 					modLabel;

	/** Description for field, modification */
	private String 					modDescr;

	/** To group modification field buttons. */
	private ButtonGroup 			modfBtnGroup;

	/** List of choices for modification. */
	private JRadioButton[] 			modOptRBtn;

	/** Description for modification use options **/
	private String[] 				modOptDesc;

	/** Label for Jurisdiction */
	private JLabel 					jLabel;

	/** Description for field, modification */
	private String 					jDescr;

	/** The creative commons license */
	private LicenceBean				license;

	/** List of Jurisdiction for the license */
	private JComboBox 				jList;

	/** Client providing creative commons license services */
	private CCLClient 				ccclient;

	/**
	 * Instantiates the dialog to create the Creative Commons License. 
	 * 
	 * @param parent	the parent window
	 * @param title		title of the dialog
	 */
	public CCLicenseDialog(JFrame parent, String title) {
		super(parent, title, "", null);
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
		String C = commBtnGroup.getSelection().getActionCommand();
		String D = modfBtnGroup.getSelection().getActionCommand();
		CCLFieldEnumValues J = (CCLFieldEnumValues)jList.getSelectedItem();

		try {
			String jId = J.getId();
			if (jId.equals(Constants.INTERNATIONAL)) jId = "";
			String licenseParams = 
					"commercial=" + C + "&derivatives=" + D + "&jurisdiction=" + jId;
			String str;
			try {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				str = ccclient.getCCLicense(licenseParams);
			} finally {
				this.setCursor(Cursor.getDefaultCursor());
			}

			license = new LicenceBean();
			license.setCommercial(C);
			license.setDerivatives(D);
			license.setJurisdiction(J.getId());
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
		c.weighty = 0.1;
		c.gridx = 0;
		c.gridy = 0;

		box.add(commLabel, c);
		addDescription(box, commDescr, c);
		addOptions(box, commOptRBtn, commOpDesc, c);

		box.add(modLabel, c);
		addDescription(box, modDescr, c);
		addOptions(box, modOptRBtn, modOptDesc, c);

		box.add(jLabel, c);
		addDescription(box, jDescr, c);
		c.gridy++;
		box.add(jList, c);

		c.gridy++;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END; //bottom of pane
		box.add(Box.createVerticalStrut(5), c);

		return box;
	}

	private void addDescription(JPanel box, String descr,
			GridBagConstraints c) {
		c.gridx++;
		box.add(getInfoLabel(descr), c);
		c.gridx = 0;
	}

	private JLabel getInfoLabel(String descr) {
		JLabel info = new JLabel();
		info.setFont(info.getFont().deriveFont(Font.ITALIC));
		info.setHorizontalAlignment(JLabel.CENTER);
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.INFO);
		//ImageIcon icon = createImageIcon("info.png");
		info.setIcon(icon);
		info.setToolTipText(descr);
		return info;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = CCLicenseDialog.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			return null;
		}
	}

	private void addOptions(JPanel box, JRadioButton[] optionsRBtn,
			String[] opDesc, GridBagConstraints c) {
		c.gridy++;
		for (int i = 0; i < optionsRBtn.length; i++) {
			box.add(optionsRBtn[i], c);
			addDescription(box, opDesc[i], c);
			c.gridy++;
		}
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

		cclWs = PublishAgent.getCCLUrl();

		backButton = new JButton("Back");
		formatButton(backButton, 'B', BACK_TOOLTIP, BACK, this);

		nextButton = new JButton("Next");
		formatButton(nextButton, 'N', NEXT_TOOLTIP, NEXT, this);
		this.getRootPane().setDefaultButton(nextButton);

		try {
			ccclient = new CCLClient(cclWs);
			List<CCLField> fields = ccclient.generateLicenseFields();
			for (CCLField field : fields) {
				String id = field.getId();
				List<CCLFieldEnumValues> options = field.getEnumValues();
				if (id.equals("commercial")) {
					commLabel = new JLabel(field.getLabel());
					commDescr = field.getDescription();
					commOpDesc = new String[options.size()];
					commOptRBtn = setupOptions(options, commOpDesc);
					commBtnGroup = groupOptions(commOptRBtn);
				} else if (id.equals("derivatives")) {
					modLabel = new JLabel(field.getLabel());
					modDescr = field.getDescription();
					modOptDesc = new String[options.size()];
					modOptRBtn = setupOptions(options, modOptDesc);
					modfBtnGroup = groupOptions(modOptRBtn);
				} else if (id.equals("jurisdiction")) {
					jLabel = new JLabel(field.getLabel());
					jDescr = field.getDescription();
					String[] opStrings = new String[options.size()];

					for (int i = 0; i < options.size(); i++) {
						opStrings[i] = options.get(i).getLabel();
					}

					CCLFieldEnumValues[] optionsArray = 
							(CCLFieldEnumValues[]) options.toArray(
									new CCLFieldEnumValues[options.size()]);
					jList = new JComboBox(optionsArray);
					jList.setSelectedIndex(0);
				}
			}
		} catch (Exception e) {
			showErrDialog(this, Constants.ERROR_CCL, e);
		}


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
	 * @param descr 	The description for options
	 * @return			
	 * 		the <code>ButtonGroup</code> grouping the <code>JRadioButton</code>
	 */
	private JRadioButton[] setupOptions(List<CCLFieldEnumValues> options, String[] descr) 
	{
		JRadioButton[] optionsRBtn = new JRadioButton[options.size()];
		for (int i = 0; i < options.size(); i++) {
			CCLFieldEnumValues optionField = options.get(i);
			optionsRBtn[i] = new JRadioButton(optionField.getLabel());
			optionsRBtn[i].setActionCommand(optionField.getId());
			descr[i] = new String(optionField.getDescription());
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
