/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.ObjectTranslator;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class PropertiesUI   
	extends AnnotationUI
	implements ActionListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";

    private static final String DETAILS = "Details";
    
    /** Description of the {@link #infoButton} button. */
    private static final String INFO_DESCRIPTION = "View image's info.";
  
    /** Description of the {@link #downloadButton} button. */
    private static final String	DOWNLOAD_DESCRIPTION = "Download the " +
    												"archived files";
    
   
    /** Action commnand ID indicating to dowload the archived files if any. */
    private static final int	DOWNLOAD_ACTION = 0;
    
    /** Action command ID indicating to display the info of the image. */
    private static final int	INFO_ACTION = 1;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField          nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea          	descriptionArea;
    
    /** Button to download the archived files. */
    private JButton				downloadButton;
    
    /** Button to download the archived files. */
    private JButton				infoButton;

    /** Panel hosting the main display. */
    private JComponent			contentPanel;

    /** Downloads the archived files. */
    private void download()
    { 
    	
    }
    
    /** Shows the image's info. */
    private void showInfo()
    { 
    	
    }
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(PermissionData permissions)
    {
        JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //The owner is the only person allowed to modify the permissions.
        //boolean isOwner = model.isObjectOwner();
        //Owner
        JLabel label = UIUtilities.setTextFont(ObjectTranslator.OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isUserRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isUserWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 0, l, c");
        content.add(p, "1, 0, l, c");  
        //Group
        label = UIUtilities.setTextFont(ObjectTranslator.GROUP);
        p = new JPanel();
        box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isGroupRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isGroupWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 1, l, c");
        content.add(p, "1,1, l, c"); 
        //OTHER
        label = UIUtilities.setTextFont(ObjectTranslator.WORLD);
        p = new JPanel();
        box =  new JCheckBox(ObjectTranslator.READ);
        box.setSelected(permissions.isWorldRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(ObjectTranslator.WRITE);
        box.setSelected(permissions.isWorldWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 2, l, c");
        content.add(p, "1, 2, l, c"); 
        return content;
    }
    
    /**
     * Lays out the key/value (String, String) pairs.
     * 
     * @param details The map to handle.
     * @return See above.
     */
    private JPanel layoutDetails(Map details)
    {
    	JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        //c.insets = new Insets(3, 3, 3, 3);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        return content;
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        IconManager icons = IconManager.getInstance();
        downloadButton = new JButton(icons.getIcon(IconManager.DOWNLOAD));
    	downloadButton.setToolTipText(DOWNLOAD_DESCRIPTION);
    	downloadButton.addActionListener(this);
    	downloadButton.setActionCommand(""+DOWNLOAD_ACTION);
    	
    	infoButton = new JButton(icons.getIcon(IconManager.INFO));
    	infoButton.setToolTipText(INFO_DESCRIPTION);
    	infoButton.addActionListener(this);
    	infoButton.setActionCommand(""+INFO_ACTION);
    	UIUtilities.unifiedButtonLookAndFeel(downloadButton);
    	UIUtilities.unifiedButtonLookAndFeel(infoButton);
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 100;
        //if (model.isAnnotatable()) height = 50;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 0,
        				height} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        //content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        content.add(UIUtilities.setTextFont("ID"), "0, 0, l, c");
        l = new JLabel(""+model.getRefObjectID());
        //l.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        content.add(l, "1, 0, f, c");
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(nameArea, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        l = UIUtilities.setTextFont("Description");
        int h = l.getFontMetrics(l.getFont()).getHeight()+5;
        layout.setRow(3, h);
        content.add(l, "0, 3, l, c");
        JScrollPane pane = new JScrollPane(descriptionArea);
        content.add(pane, "1, 3, 1, 4");
        return content;
    }
    
    /** 
     * Builds and lays out the controls buttons in a tool bar.
     * 
     * @return See above.
     */
    private JComponent buildToolBar()
    {
    	JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(infoButton); 
        bar.add(downloadButton); 
        return UIUtilities.buildComponentPanel(bar);
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
    	contentPanel.setLayout(new BoxLayout(contentPanel, 
    								BoxLayout.Y_AXIS));
    	contentPanel.add(buildContentPanel());
        ExperimenterData exp = model.getRefObjectOwner();
        if (exp != null) {
        	JPanel p = new JPanel();
        	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        	JPanel details = layoutDetails(
        				EditorUtil.transformExperimenterData(exp));
        	PermissionData perm = model.getRefObjectPermissions();
        	p.add(details);
        	if (perm != null && !(model.getRefObject() instanceof ImageData)) {
        		p.add(buildPermissions(perm));
        	}
        	UIUtilities.setBoldTitledBorder(DETAILS, p);
        	TreeComponent tree = new TreeComponent();
        	tree.insertNode(p, UIUtilities.buildCollapsePanel(DETAILS), 
        					false);
        	contentPanel.add(tree);
            contentPanel.add(new JPanel());
        }
        if (model.getRefObject() instanceof ImageData) {
        	contentPanel.add(buildToolBar());
        }
        add(contentPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link EditorModel}.
     * 				Mustn't be <code>null</code>.                            
     */
    PropertiesUI(EditorModel model)
    {
       super(model);
       initComponents();
       UIUtilities.setBoldTitledBorder(getComponentTitle(), this);
    }   

    /**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		nameArea.setText(model.getRefObjectName());
        descriptionArea.setText(model.getRefObjectDescription());
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        nameArea.setEnabled(b);
        descriptionArea.setEnabled(b);
        buildGUI();
	}
	
    /** Sets the focus on the name area. */
	void setFocusOnName() { nameArea.requestFocus(); }
   
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DOWNLOAD_ACTION:
				download();
				break;
			case INFO_ACTION:
				showInfo();
		}
		
	}
	
}
