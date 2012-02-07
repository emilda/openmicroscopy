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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomFont;
import org.openmicroscopy.shoola.agents.monash.IconManager;
import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.data.LicenceBean;
import org.openmicroscopy.shoola.agents.monash.view.data.MonashData;
import org.openmicroscopy.shoola.agents.monash.view.data.PartyBean;
import org.openmicroscopy.shoola.agents.monash.view.dialog.CCLicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.LicenseDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.PartyDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.SearchPartyDialog;
import org.openmicroscopy.shoola.agents.monash.view.dialog.UDLicenseDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.TagAnnotationData;

public class AndsPublishUI extends TopWindow 
implements ListSelectionListener, DocumentListener, ItemListener {

	/** The default title of the window. */
	private static final String TITLE = "Register with RDA";
	
	/** Default height of the window */
	private static final int HEIGHT = 700;

	/** Message to display when there is no data collections available to register with RDA */
	private static final String NO_DATA = "No data collections available to register with RDA";

	/** Tag value identifying the collection to register with RDA */
	private static final String RDA_TAG = "Register with RDA";
	
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

	/** The total of projects to register with ANDS. */
	private int 				total;

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

	/** contains details about collection to register with ANDS  */
	private JPanel 				projectPanel;

	/** Placed within {@link #projectPanel} and contains details about party */
	private JPanel 				researcherPanel;

	/** The model for the JList {@link #projectList}. */
	private DefaultListModel 	listmodel;

	/** list containing the items to be published */
	private JList 				projectList;

	/** Hashtable containing party information */
	private Hashtable<String, PartyBean>	partyHtable;

	protected AndsPublishUI(String title) {
		super(title);
	}

	/**
	 * Creates a new instance. The
	 * {@link #initialize(AndsPublishControl, AndsPublishModel) initialize}
	 * method should be called straight after to link this View to the
	 * Controller.
	 */
	public AndsPublishUI() {
		super(TITLE);
		System.out.println("Created AndsPublish UI");
	}

	/**
	 * Links this View to its Controller.
	 * 
	 * @param model 		The Model.
	 * @param controller	The Controller.
	 */
	void initialize(AndsPublishModel model, AndsPublishControl controller)
	{
		System.out.println("initializing AndsPublish UI");
		this.model = model;
		this.controller = controller;
		partyHtable = new Hashtable<String, PartyBean>();
		total = 1;
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
		descriptionTxt.setEnabled(true);
		descriptionTxt.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		descriptionTxt.setWrapStyleWord(true);
		descriptionTxt.setBackground(UIUtilities.BACKGROUND_COLOR);

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
		projListScrollPane.setMinimumSize(new Dimension(150, HEIGHT));
		andsScrollPane.setMinimumSize(new Dimension(750, HEIGHT));
		splitPane.setPreferredSize(new Dimension(900, HEIGHT));
	}

	/**
	 * @param dataCollection	the data collections to register with RDA
	 */
	public void setListData(Collection dataCollection) {
		clearFields();
		listmodel = new DefaultListModel();

		Iterator i = dataCollection.iterator();
		while (i.hasNext()) {
			DataObject object = (DataObject) i.next();
			MonashData listdata = new MonashData(object);
			if (getTagDetails(object)) listmodel.addElement(listdata);
		}
		System.out.println("Setting new list model object");
		projectList.setModel(listmodel);
		if (listmodel.size() > 0) {
			System.out.println("Setting first element");
			publishButton.setEnabled(true);
			projectList.setSelectedIndex(0);
		}
	}

	// TODO remove this method later
	private boolean getTagDetails(DataObject object) {
		System.out.println("DataObject, id: " + object.getId());
		try {
			Collection tags = PublishAgent.getAnnotations(object);
			if (tags == null || tags.size() == 0)
				return false;
			Iterator iterator = tags.iterator();
			AnnotationData data;
			TagAnnotationData tag;
			while (iterator.hasNext()){
				data = (AnnotationData) iterator.next();
				if (data instanceof TagAnnotationData) {
					tag = (TagAnnotationData) data;
					System.out.println("Tags: " + tag.getTagValue() + " ");
					if(RDA_TAG.equals(tag.getTagValue())) {
						return true;
					}
				}

			}
		} catch (DSOutOfServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DSAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void updateLabel(MonashData object) {
		if (null != object) {
			messageLabel.setText(object.getTitle());
			titleLabel.setText(object.getTitle());
			descriptionTxt.setText(object.getDescription());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		System.out.println("List selection event recived");
		JList list = (JList)e.getSource();
		int index = list.getSelectedIndex();
		if (-1 != index) {
			MonashData object = (MonashData) list.getModel().getElementAt(index);
			System.out.println("Update messageLabel");
			updateLabel(object);
		}
		//updateLabel((String) list.getSelectedValue());
	}

	/**
	 * Build the collection details panel
	 * @return the panel containing collection details.
	 */
	private JPanel buildProjectDetails() {
		ExperimenterData user = PublishAgent.getUserDetails();

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
		descriptionTxt.setText("");
		descriptionTxt.getDocument().addDocumentListener(this);
		descriptionTxt.getDocument().putProperty("name", "description");
		content.add(descriptionTxt, c);  
		//content.add(new JScrollPane(descriptionTxt), "1, 2");
		//System.out.println("Created Collection description field");  
		LogMessage msg = new LogMessage();
		msg.println("Created Collection description field");

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
		tc.setFont(new CustomFont());
		
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
		publishButton = new JButton(controller.getAction(AndsPublishControl.PUBLISH));
		publishButton.setEnabled(false);
		p.add(publishButton);
		p.add(Box.createHorizontalStrut(5));
		p.add(new JButton(controller.getAction(AndsPublishControl.EXIT)));
		return UIUtilities.buildComponentPanelRight(p);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));

		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, "", "Register the following collection with RDA", 
				icons.getIcon(IconManager.MONASH ));
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
		container.add(splitPane, BorderLayout.CENTER);
		container.add(controlsBar, BorderLayout.SOUTH);
	}

	/**
	 * Creates a row.
	 */
	private JPanel createRow()
	{
		return createRow(UIUtilities.BACKGROUND);
	}

	/**
	 * Creates a row.
	 * 
	 * @param background The background of color.
	 */
	private JPanel createRow(Color background)
	{
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		if (background != null)
			row.setBackground(background);
		row.setBorder(null);
		return row;
	}

	/**
	 * Fires property indicating that some text has been changed.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		Document doc = (Document)e.getDocument();
		String name = (String) doc.getProperty("name");
		System.out.println("DocumentListener.changedUpdate activated from " + name);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		//firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false), Boolean.valueOf(true));
		Document doc = (Document)e.getDocument();
		String name = (String) doc.getProperty("name");
		//String description = descriptionTxt.getText();
		//description = description.trim();
		//System.out.println("DocumentListener.insertUpdate activated from " + name);
	}

	/**
	 * Fires property indicating that some text has been removed.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		//firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false), Boolean.valueOf(true));
		//System.out.println("DocumentListener.removeUpdate activated");
	}

	/**
	 * Shows the Add Researcher Dialog
	 * @return Selected choice of how to add Party @see PartyDialog#OPTIONS
	 */
	public String showResearcher() {
		System.out.println("Show add-researcher screen");

		PartyDialog pd = new PartyDialog(this, "Adding Researcher Options");
		//pd.addPropertyChangeListener(manager);
		UIUtilities.centerAndShow(pd);
		String option = pd.getSelectedOption();
		System.out.println("Show next screen based on option, " + option);
		return option;
	}

	/**
	 * Show the search researcher dialog. On successful search
	 * the <code>PartyBean</code> is added into the
	 * {@link AndsPublishModel#partyList}.
	 * @return 	<code>false</code> goes back to add researcher options page<br>
	 * 			<code>true</code> goes back to RDA main screen
	 */
	public boolean searchResearcher() {
		SearchPartyDialog spd = new SearchPartyDialog(this, "Adding Researcher Options", model);
		UIUtilities.centerAndShow(spd);
		PartyBean pb = spd.getPartyBean();
		if (null != pb) {
			String key = pb.getPartyKey();
			model.addParty(key, pb);
			addPartyCheckBox(key, pb);
			return true;
		} else {
			return false;
		}

		// TODO add party info dialog to view and check it
	}

	/**
	 * Adds the checkbox and place the name of researcher 
	 * associated with the collection next to it 
	 * @param key	the party key
	 * @param pb	the <code>PartyBean</code> object
	 */
	private void addPartyCheckBox(String key, PartyBean pb) {
		System.out.println("add party CheckBox for " + pb.toString());

		JCheckBox rcb = new JCheckBox(pb.toString(), true);
		rcb.setBackground(UIUtilities.BACKGROUND_COLOR);
		rcb.setEnabled(true);
		rcb.setName(key);
		rcb.addItemListener(this);

		partyHtable.put(rcb.getName(), pb);	// get PartyBean

		researcherPanel.add(rcb);
		researcherPanel.add(Box.createVerticalStrut(5));
		//researcherPanel.revalidate();
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
			System.out.println(cb.getText() + " selected");
			PartyBean pb = (PartyBean)partyHtable.get(cb.getName());
			pb.setSelected(true);
			model.addParty(cb.getName(), pb);
		} else {
			System.out.println(cb.getText() + " cleared");
			model.removeParty(cb.getName());
			PartyBean pb = (PartyBean)partyHtable.get(cb.getName());
			pb.setSelected(false);
		}
	}

	/** Refreshes the view */
	public void refresh() {
		System.out.println("Refreshing view");
		clearFields();
		setListData(model.getDataCollection());
	}

	private void clearFields() {
		messageLabel.setText(NO_DATA);
		titleLabel.setText("");
		descriptionTxt.setText("");
		publishButton.setEnabled(false);
	}

	/**
	 * Shows the license main screen
	 * @return Selected license choice @see LicenseDialog#OPTIONS
	 */
	public String showLicenseMain() {
		System.out.println("Show License Main screen");

		LicenseDialog ld = new LicenseDialog(this, "Select License");
		UIUtilities.centerAndShow(ld);
		String option = ld.getSelectedOption();
		System.out.println("Show next screen based on option, " + option);
		return option;
	}

	/**
	 * Shows the user defined license screen. Upon
	 * successful return set the license in model.
	 * 
	 * @return 	<code>false</code> goes back to add researcher options page<br>
	 * 			<code>true</code> goes back to RDA main screen
	 */
	public boolean showUDL() {
		System.out.println("Show user defined license screen");

		UDLicenseDialog ld = new UDLicenseDialog(this, "Define Your Own License");
		UIUtilities.centerAndShow(ld);
		LicenceBean udl = ld.getLicense();
		if (udl != null) {
			System.out.println("User DL: " + udl);
			model.setLicense(udl);
			noLicense.setText("");
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
	public boolean showCCL() {
		System.out.println("Show creative commons license screen");

		CCLicenseDialog ld = new CCLicenseDialog(this, "Creative Commons License");
		UIUtilities.centerAndShow(ld);
		LicenceBean ccl = ld.getLicense();
		if (ccl != null) {
			System.out.println("CCL: " + ccl);
			model.setLicense(ccl);
			noLicense.setText("");
			return true;
		} else {
			return false;
		}
	}
}
