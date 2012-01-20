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
import javax.swing.JTextField;

import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog to Search Party in the Research Master
 * @see #OPTIONS
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since OME3.3
 */
public class SearchPartyDialog extends MonashDialog {

	/** Button to add research main page. */
	private JButton 		backButton;
	
	/** Button to search for Party in the Research Master. */
	private JButton			searchButton;
	
	/** The field holding the name of the party to search for. */
    private JTextField		searchField;
    
    /** Party searched. */
	private String 			party = null;
    
    /** Description of the panel. */
    private static final String DESCRIPTION = "Enter the researcher name or email below:";
	
    /** The tooltip of the {@link #backButton}. */
	private static final String		BACK_TOOLTIP = "Go back to previous page.";
	
	/** The tooltip of the {@link #searchButton}. */
	private static final String		SEARCH_TOOLTIP = "Search for Party in the Research Master.";

	/** Action ID to close the dialog. */
	private static final int		BACK = 0;
	
	/** Action ID to go to the next page and close the dialog. */
	private static final int		SEARCH = 1;

	public SearchPartyDialog(JFrame parent, String title) {
		super(parent, title, "", null);
	}

	/**
	 * Reacts to click on buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case BACK:
				party = null;
				close();
				break;
			case SEARCH:
				party = searchField.getText();
				searchRM();
				close();
				break;
		}
	}
	
	private void searchRM() {
		System.out.println("Search for party: " + party); // cnOrEmail
		String token = PublishAgent.getMonashOmeroDS();
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
        box.add(searchField, c);
        
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
    	bar.add(searchButton);
    	bar.add(Box.createHorizontalStrut(10));
    	return bar;
	}

	/** 
	 * Implemented as specified by the {@link MonashDialog}.
	 * @see MonashDialog#initComponents()
	 */
	protected void initComponents() {
		
		searchField = new JTextField(30);
		searchField.setBackground(UIUtilities.BACKGROUND_COLOR);
    	searchField.setEnabled(true);
    	searchField.setEditable(true);
    	
    	//Ensure the text field always gets the first focus.
        /*addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
            	searchField.requestFocusInWindow();
            }
        });*/
        
		backButton = new JButton("Back");
		formatButton(backButton, 'B', BACK_TOOLTIP, BACK, this);
		
		searchButton = new JButton("Next");
		formatButton(searchButton, 'S', SEARCH_TOOLTIP, SEARCH, this);
		
	}

	/**
	 * Return the selected party option. 
	 * Null if user cancels the action or closes the dialog.
	 * @see #OPTIONS
	 * @return selected option
	 */
	public String getParty() {
		return party;
	}
}
