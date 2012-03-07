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

import ij.gui.MultiLineLabel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/** 
 * Dialog showing the options for license associated with the data collection.
 * @see #OPTIONS
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since OME3.3
 */
public class LicenseDialog extends MonashDialog {

	private static Hashtable<String, String> OPTIONS;

	/** Button to close and dispose of the window. */
	private JButton 			cancelButton;
	
	/** Button to go to next screen. */
	private JButton				nextButton;
	
	/** List of choices of license creation. */
	private JRadioButton[] 		radioButtons;
	
	/** To group {@link #radioButtons}. */
	private ButtonGroup 		group;

    /** Selected choice {@link #OPTIONS}. */
	private String 				selectedOption;
    
    /** Description of the panel. */
    private static final String DESCRIPTION = "Select the license you " +
    		"want to apply to this experiment so that interested people\n" + 
    		"understand what they are entitled to do with your published data";
	
    /** The tooltip of the {@link #cancelButton}. */
	private static final String		CANCEL_TOOLTIP = "Cancel this action";
	
	/** The tooltip of the {@link #nextButton}. */
	private static final String		NEXT_TOOLTIP = "Go to the next page";

	/** Action ID to close the dialog. */
	private static final int		CANCEL = 0;
	
	/** Action ID to go to the next page and close the dialog. */
	private static final int		NEXT = 1;

	/** Action names and description of choices of party creation. */
	public static final String LICENSE_OPTION_CCL = "CCL";
	public static final String LICENSE_OPTION_UDL = "UDL";
	private static final String CCL = "Creative Commons Copyrigt License (Recommended)";
	private static final String UDL = "Define Your Own License";
	
	/**
	 * Instantiates the dialog showing the license options
	 * 
	 * @param parent	the parent window
	 * @param title		title of the dialog
	 */
	public LicenseDialog(JFrame parent, String title) {
		super(parent, title, "", null);
	}

	/**
	 * Reacts to click on buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		selectedOption = group.getSelection().getActionCommand();
		
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				selectedOption = null;
				close();
				break;
			case NEXT:
				close();
				break;
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

		MultiLineLabel label = new MultiLineLabel(DESCRIPTION);
		box.add(label, c);

		for (int i = 0; i < radioButtons.length; i++) {
			c.gridy++;
            box.add(radioButtons[i], c);
        }

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
    	bar.add(cancelButton);
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
		cancelButton = new JButton("Cancel");
		formatButton(cancelButton, 'C', CANCEL_TOOLTIP, CANCEL, this);
		
		nextButton = new JButton("Next");
		formatButton(nextButton, 'N', NEXT_TOOLTIP, NEXT, this);
		this.getRootPane().setDefaultButton(nextButton);
		
		OPTIONS = new Hashtable<String, String>();
		OPTIONS.put(LICENSE_OPTION_UDL, UDL);
		OPTIONS.put(LICENSE_OPTION_CCL, CCL);
		
        radioButtons = new JRadioButton[OPTIONS.size()];
        group = new ButtonGroup();
        
        Enumeration<String> e = OPTIONS.keys();
        int i=0;
        while (e.hasMoreElements()) {
          String key = e.nextElement();
          
          radioButtons[i] = new JRadioButton(OPTIONS.get(key));
          radioButtons[i].setActionCommand(key);
          group.add(radioButtons[i]);
          i++;
        }
        radioButtons[0].setSelected(true);
	}

	/**
	 * Return the selected licenses option. 
	 * Null if user cancels the action or closes the dialog.
	 * @see #OPTIONS
	 * @return selected option
	 */
	public String getSelectedOption() {
		return selectedOption;
	}
}
