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
package org.openmicroscopy.shoola.agents.monash.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.openmicroscopy.shoola.agents.monash.IconManager;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.data.LicenceBean;
import org.openmicroscopy.shoola.agents.monash.view.data.MonashData;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.openmicroscopy.shoola.agents.monash.view.dialog.CCLicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.InputPartyDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.LicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.PartyDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.SearchPartyDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.UDLicenseDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;

/** 
 * The {@link AndsPublish}'s View component. After creation
 * this window will display a pane with an explorer on the 
 * left displaying the dataset/ project tagged as 
 * <code>Register with RDA</code> and a details pane on the
 * right with controls to add researcher, license for the 
 * data collection and to register the collection with RDA>
 * 
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since Beta4.4
 */
public class AndsPublishUI extends TopWindow 
	implements ListSelectionListener, ItemListener, FocusListener {

	/** The default title of the window. */
	private static final String TITLE = "Register with RDA";
	
	/** Default height of the window */
	private static final int 	HEIGHT = 350;

	/** Message to display when there is no data collections available to register with RDA */
	private static final String NO_DATA = "No data collections available to register with RDA";

	/** Terms and Conditions */
	private static final String TANDC = "Terms and Conditions:";

	/** Message to display when there is no license selected */
	private static final String NO_LICENSE = "A License has not been selected.";

	/** Terms and Conditions detailed text */
	private static final String TC_TEXT = "You are about to publish or register the above research work outside " + 
			"Monash University to be available to the general public\nvia Internet sites that can harvest this " + 
			"information. Sites include but are not limited to: Research Data Australia and search engines.\n\n" +
			"Before you proceed, please ensure you have selected a licence to associate with your research data and work.\n\n" +
			"By using this system to publish or register your research work you are continuing to agree to adhere to the " +
			"Terms and Conditions\nof use detailed at http://www.monash.edu/eresearch/about/ands-merc.html. Please read " +
			"these Terms and Conditions\ncarefully before registering.";

	/** Reference to the model. */
	private AndsPublishModel	model;

	/** Reference to the control. */
	private AndsPublishControl	controller;

	/** The controls bar. */
	private JComponent 			controlsBar;

	/** The component indicating to refresh the containers view.*/
	private JXLabel 			messageLabel;
	
	/** Label displaying license not selected.*/
	private JXLabel 			noLicense;

	/** The field hosting the collection title name. */
	private JXLabel				titleLabel;

	/** The field hosting the login name. */
	private JTextArea			descriptionTxt;	// JTextField

	/** Register with RDA */
	private JButton 			publishButton;

	/** Add License. */
	private JButton				licenseButton;

	/** Add Researcher. */
	private JButton				researcherButton;

	/**
	 * Main panel, with list of collections to register with RDA
	 * on the left and selected collection details on the right 
	 */
	private JSplitPane 			splitPane;

	/** Scrollpane to hold {@link #splitPane}. */
	private JScrollPane 			scrollPane;
	
	/** contains details about collection to register with ANDS  */
	private JPanel 				projectPanel;

	/** Placed within {@link #projectPanel} and contains details about party */
	private JPanel 				researcherPanel;
	
	/** Title panel for displaying title as well as other messages in sub-title */
	private TitlePanel 			tp;

	/** The model for the JList {@link #projectList}. */
	private DefaultListModel 	listmodel;

	/** list containing the items to be published */
	private JList 				projectList;

	/** Hashtable containing party information */
	private Hashtable<String, PartyBean>	partyHtable;

	/** Used to sort the nodes.*/
	private ViewerSorter sorter;
	
	protected AndsPublishUI(String title) {
		super(title);
		sorter = new ViewerSorter();
	}

	/**
	 * Creates a new instance. The
	 * {@link #initialize(AndsPublishControl, AndsPublishModel) initialize}
	 * method should be called straight after to link this View to the
	 * Controller.
	 */
	public AndsPublishUI() {
		this(TITLE);
	}

	/**
	 * Links this View to its Controller.
	 * 
	 * @param model 		The Model.
	 * @param controller	The Controller.
	 */
	void initialize(AndsPublishModel model, AndsPublishControl controller)
	{
		this.model = model;
		this.controller = controller;
		partyHtable = new Hashtable<String, PartyBean>();
		initComponents();
		//setJMenuBar(createMenuBar());
		buildGUI();
	}

	/** Initializes the components. */
	private void initComponents()
	{
		messageLabel = new JXLabel();
		messageLabel.setText(NO_DATA);
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

		noLicense = new JXLabel();
		noLicense.setText(NO_LICENSE);
		noLicense.setFont(new Font("Serif", Font.PLAIN, 12));

		controlsBar = buildControls();

		titleLabel = new JXLabel();

		descriptionTxt = new JTextArea(5, 20);
		descriptionTxt.setText("");
		descriptionTxt.setEnabled(true);
		descriptionTxt.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		descriptionTxt.setWrapStyleWord(true);
		descriptionTxt.setBackground(UIUtilities.BACKGROUND_COLOR);
		descriptionTxt.addFocusListener(this);

		researcherButton = new JButton(controller.getAction(AndsPublishControl.PARTY));
		researcherButton.setBackground(UIUtilities.BACKGROUND_COLOR);

		licenseButton = new JButton(controller.getAction(AndsPublishControl.LICENSE));
		licenseButton.setBackground(UIUtilities.BACKGROUND_COLOR);

		researcherPanel = new JPanel();
		researcherPanel.setLayout(new BoxLayout(researcherPanel, BoxLayout.Y_AXIS));
		researcherPanel.setBackground(UIUtilities.BACKGROUND_COLOR);

		setupSplitPane();
	}

	private void setupSplitPane() {
		projectList = new JList();
		projectList.addListSelectionListener(this);
		projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane projListScrollPane = new JScrollPane(projectList);

		projectPanel = buildProjectDetails();
		JScrollPane andsScrollPane = new JScrollPane(projectPanel);

		//Create a split pane with the two scroll panes in it.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				projListScrollPane, projectPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		
		//Provide minimum sizes for the two components in the split pane.
		//projListScrollPane.setMinimumSize(new Dimension(150, HEIGHT));
		//andsScrollPane.setMinimumSize(new Dimension(500, HEIGHT));
		//splitPane.setMinimumSize(new Dimension(650, HEIGHT));
		scrollPane  = new JScrollPane(splitPane);
	}

	/**
	 * Sets the data for the list in the explorer
	 * @param dataCollection	the data collections to register with RDA
	 */
	protected void setListData(Collection<DataObject> dataCollection) {
		clearFields();
		if (listmodel == null) {
			listmodel = new DefaultListModel();
			projectList.setModel(listmodel);
		}
		listmodel.clear();
		List l = sorter.sort(dataCollection);
		Iterator<DataObject> i = l.iterator();
		while (i.hasNext()) {
			listmodel.addElement(new MonashData((DataObject) i.next()));
		}
		
		if (listmodel.size() > 0) {
			projectList.setSelectedIndex(0);
		}
		setComponentControls();
	}

	/** Enable or disables the components in the panel */
	private void setComponentControls() {
		if (listmodel != null & listmodel.size() > 0) {
			descriptionTxt.setEditable(true);
			researcherButton.setEnabled(true);
			licenseButton.setEnabled(true);
			publishButton.setEnabled(model.hasAllData());
		} else {
			clearFields();
		}
	}

	/**
	 * Updates messge, title and description with the 
	 * new selection details
	 * 
	 * @param object	contains collection metadata
	 */
	private void updateLabel(MonashData object) {
		if (null != object) {
			messageLabel.setText(object.getTitle());
			titleLabel.setText(object.getTitle());
			descriptionTxt.setText(object.getDescription());
		}
	}

	/** 
	 * Implemented as specified by the {@link ListSelectionListener} interface.
	 * @see ListSelectionListener#valueChanged()
	 */
	public void valueChanged(ListSelectionEvent e) {
		JList list = (JList)e.getSource();
		int index = list.getSelectedIndex();
		if (-1 != index) {
			MonashData object = (MonashData) list.getModel().getElementAt(index);
			updateLabel(object);
			model.setMetadata(object);
		}
		//updateLabel((String) list.getSelectedValue());
	}

	/**
	 * Build the collection details panel
	 * @return the panel containing collection details.
	 */
	private JPanel buildProjectDetails() 
	{
		JPanel content = new JPanel(new GridBagLayout());
		content.setBorder(BorderFactory.createTitledBorder("Metadata Public Registration"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(15, 5, 0, 5);
		c.gridx = 0;
		c.gridy = 0;

		// Add Collection name but cannot edit.
		JComponent label = EditorUtil.getLabel(Constants.COLLECTION_NAME, false);
		c.weightx = 0.0;  
		content.add(label, c);
		c.gridx++;
		content.add(Box.createHorizontalStrut(5), c); 
		c.gridx++;
		titleLabel.setText("");
		content.add(titleLabel, c);  

		// Add Collection description.
		c.gridx = 0;
		c.gridy++;
		label = EditorUtil.getLabel(Constants.COLLECTION_DESCRIPTION, false);
		content.add(label, c);
		c.gridx++;
		content.add(Box.createHorizontalStrut(5), c); 
		c.gridx++;
		//content.add(descriptionTxt, c);  
		content.add(new JScrollPane(descriptionTxt), c);

		// Add researcher.
		c.gridx = 0;
		c.gridy++;
		label = EditorUtil.getLabel(Constants.RESEARCHER, false);
		content.add(label, c);
		c.gridx++;
		content.add(Box.createHorizontalStrut(5), c); 
		c.gridx++;
		content.add(researcherButton, c);  

		// add panel containing party added
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.weightx = 0.0;  
		content.add(researcherPanel, c);

		// Add License.
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		label = EditorUtil.getLabel(Constants.LICENSE, false);  
		content.add(label, c);
		c.gridx++;
		content.add(Box.createHorizontalStrut(5), c); 
		c.gridx++;
		content.add(licenseButton, c);

		// Add no license label
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.insets = new Insets(0, 5, 15, 5);
		content.add(noLicense, c);

		// Add t and c
		c.gridx = 0;
		c.gridy++;
		label = EditorUtil.getLabel(TANDC, false);
		label.setFont(new Font("Serif", Font.BOLD, 16));
		content.add(label, c);

		JTextArea tc = new JTextArea(TC_TEXT);
		tc.setRows(10);
		//remove reference to editor agent
		tc.setFont(new Font("SansSerif", Font.PLAIN, 11));
		
		c.insets = new Insets(0, 20, 15, 5);
		c.gridx = 0;
		c.gridy++;
		content.add(tc, c);
		
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END; //bottom of pane
		content.add(Box.createVerticalStrut(5));

		return content;
	}

	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.add(new JButton(controller.getAction(AndsPublishControl.EXIT)));
		p.add(new JButton(controller.getAction(AndsPublishControl.REFRESH)));
		publishButton = new JButton(controller.getAction(AndsPublishControl.PUBLISH));
		p.add(publishButton);
		p.add(Box.createHorizontalStrut(5));
		return UIUtilities.buildComponentPanelRight(p);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));

		IconManager icons = IconManager.getInstance();
		tp = new TitlePanel(TITLE, "", 
				"Register the following collection with RDA", 
				icons.getIcon(IconManager.LOGO_MU));
		JXPanel p = new JXPanel();
		JXPanel lp = new JXPanel();
		lp.setLayout(new FlowLayout(FlowLayout.LEFT));
		lp.add(messageLabel);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(tp);
		p.add(lp);
		p.setBackgroundPainter(tp.getBackgroundPainter());
		lp.setBackgroundPainter(tp.getBackgroundPainter());
		container.add(p, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);	// splitPane
		container.add(controlsBar, BorderLayout.SOUTH);
	}

	/**
	 * Shows the Add Researcher Dialog
	 * @return Selected choice of how to add Party @see PartyDialog#OPTIONS
	 */
	protected String showResearcher() 
	{
		PartyDialog pd = new PartyDialog(this, "Adding Researcher Options");
		UIUtilities.centerAndShow(pd);
		String option = pd.getSelectedOption();
		return option;
	}

	/**
	 * Show the search researcher dialog. On successful search
	 * the <code>PartyBean</code> is added into the
	 * {@link AndsPublishModel#partyList}.
	 * @return 	<code>false</code> goes back to add researcher options page<br>
	 * 			<code>true</code> goes back to RDA main screen
	 */
	protected boolean searchResearcher() 
	{
		SearchPartyDialog spd = 
				new SearchPartyDialog(this, "Adding Researcher Options", model);
		UIUtilities.centerAndShow(spd);
		PartyBean pb = spd.getPartyBean();
		if (null != pb) {
			String key = pb.getPartyKey();
			model.addParty(key, pb);
			addPartyCheckBox(key, pb);
			setComponentControls();
			return true;
		} else {
			return false;
		}
	}
	
	/** Shows the manual input researcher screen */
	public void manualResearcher() {
		InputPartyDialog ipd = 
				new InputPartyDialog(this, "Manually Input a Researcher Infoon", model);
		UIUtilities.centerAndShow(ipd);
		/*PartyBean pb = ipd.getPartyBean();
		String key = pb.getPartyKey();
		model.addParty(key, pb);
		addPartyCheckBox(key, pb);
		setComponentControls();*/
	}

	/**
	 * Adds the checkbox and place the name of researcher 
	 * associated with the collection next to it 
	 * @param key	the party key
	 * @param pb	the <code>PartyBean</code> object
	 */
	protected void addPartyCheckBox(String key, PartyBean pb) {

		JCheckBox rcb = new JCheckBox(pb.toString(), true);
		rcb.setBackground(UIUtilities.BACKGROUND_COLOR);
		rcb.setEnabled(true);
		rcb.setName(key);
		rcb.addItemListener(this);

		partyHtable.put(rcb.getName(), pb);	// get PartyBean

		researcherPanel.add(rcb);
		researcherPanel.add(Box.createVerticalStrut(5));
		researcherPanel.revalidate();
		int ht = this.getHeight() + 20;
		this.setSize(new Dimension(this.getWidth(), ht));
	}

	/**
	 * @see  ItemListener.itemStateChanged()
	 */
	public void itemStateChanged(ItemEvent ie) {
		JCheckBox cb = (JCheckBox) ie.getItem();
		int state = ie.getStateChange();

		if (state == ItemEvent.SELECTED) {
			String key = cb.getName();
			PartyBean pb = (PartyBean)partyHtable.get(key);
			model.addParty(key, pb);
		} else {
			model.removeParty(cb.getName());
		}
		setComponentControls();
	}

	private void clearFields() {
		messageLabel.setText(NO_DATA);
		titleLabel.setText("");
		descriptionTxt.setText("");
		descriptionTxt.setEditable(false);
		researcherButton.setEnabled(false);
		licenseButton.setEnabled(false);
		publishButton.setEnabled(false);
	}
	
	protected void clearData() {
		model.setLicense(null);
		model.clearParty();
		noLicense.setText("");
		partyHtable.clear();
		researcherPanel.removeAll();
		//researcherPanel.updateUI();
	}

	/**
	 * Shows the license main screen
	 * @return Selected license choice @see LicenseDialog#OPTIONS
	 */
	protected String showLicenseMain() {
		LicenseDialog ld = new LicenseDialog(this, "Select License");
		UIUtilities.centerAndShow(ld);
		String option = ld.getSelectedOption();
		return option;
	}

	/**
	 * Shows the user defined license screen. Upon
	 * successful return set the license in model.
	 * 
	 * @return 	<code>false</code> goes back to add researcher options page<br>
	 * 			<code>true</code> goes back to RDA main screen
	 */
	protected boolean showUDL() {
		UDLicenseDialog ld = 
				new UDLicenseDialog(this, "Define Your Own License");
		UIUtilities.centerAndShow(ld);
		LicenceBean udl = ld.getLicense();
		if (udl != null) {
			model.setLicense(udl);
			noLicense.setText(udl.toString());
			setComponentControls();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Shows the creative commons license screen. Upon
	 * successful return set the license in model.
	 * 
	 * @return 	<code>false</code> goes back to add researcher options page<br>
	 * 			<code>true</code> goes back to RDA main screen
	 */
	protected boolean showCCL() 
	{
		CCLicenseDialog ld = 
				new CCLicenseDialog(this, "Creative Commons License");
		UIUtilities.centerAndShow(ld);
		LicenceBean ccl = ld.getLicense();
		if (ccl != null) {
			model.setLicense(ccl);
			noLicense.setText(ccl.toString());
			setComponentControls();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sets the sub-title in the title panel
	 * @param message	the message to set
	 */
	protected void setMessage(String message) {
		tp.setTextHeader(message);
	}

	/** 
	 * Nothing done when focus gained.
	 * Implemented as specified by the {@link FocusListener} interface.
	 * @see FocusListener#focusGained()
	 */
	public void focusGained(FocusEvent arg0) {}

	/** 
	 * On focus lost, the metadata in the model is updated with new value.
	 * Implemented as specified by the {@link FocusListener} interface.
	 * @see FocusListener#focusLost()
	 */
	public void focusLost(FocusEvent fe) {
		String description = descriptionTxt.getText();
		model.getMetadata().setDescription(description);
	}

	/**
	 * Removes the registered collection from the explorer
	 */
	public void removeListItem() {
		listmodel.remove(projectList.getSelectedIndex());
		if (listmodel.size() > 0) {
			projectList.setSelectedIndex(0);
		}
		setComponentControls();
	}
}
