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

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog to base all Monash Dialogs.
 *
 * @author  Sindhu Emilda &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sindhu.emilda@monash.edu">sindhu.emilda@monash.edu</a>
 * @version 1.0
 * @since OME3.3
 */
public abstract class MonashDialog extends JDialog
		implements ActionListener {
	
	/** The default size of the window. */
	protected static final Dimension 	DEFAULT_SIZE = new Dimension(700, 400);
	private Icon icon;
	private String message;

	public MonashDialog(JFrame parent, String title, String message, Icon icon) {
		super(parent);
		setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		this.icon = icon;
		this.message = message;
		initialize(title);
		//setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	}

	private void initialize(String title) {
		//if (icon == null) icon = UIManager.getIcon("monashIcon");
		setTitle(title);
		initComponents();
		buildGUI();
		pack();
		setSize(DEFAULT_SIZE);
	}

	private void buildGUI() {
		JComponent component;
		component = buildContentPane();
		component.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 0));
		Container c = getContentPane();
        TitlePanel tp = new TitlePanel(getTitle(), message, icon);
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
		c.add(component, BorderLayout.LINE_START);
		c.add(UIUtilities.buildComponentPanelRight(buildToolBar()), BorderLayout.SOUTH);
	}
	
	/**
	 * Formats the specified button.
	 * 
	 * @param b			The button to format.
	 * @param mnemonic	The key-code that indicates a mnemonic key.
	 * @param tooltip	The button's tooltip.
	 * @param actionID	The action id associated to the passed button.
	 * @param partyDialog 
	 */
	protected void formatButton(JButton b, int mnemonic, String tooltip, int
			actionID, MonashDialog parent)
	{
		b.setMnemonic(mnemonic);
        b.setOpaque(false);
        b.setToolTipText(tooltip);
        b.addActionListener(parent);
        b.setActionCommand(""+actionID);
	}
	
	/** Hides the window and disposes. */
	protected void close()
	{
		setVisible(false);
		dispose();
		//firePropertyChange(ppty_name, oldV, newV);
		
		//Handle window closing correctly.
        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/** 
     * Subclasses implement this method.
     * Initialize here all the components used to build the GUI
     */
	protected abstract void initComponents();
	
	/** 
     * Subclasses implement this method.
     * Build the Dialog GUI in this method
     */
	protected abstract JComponent buildContentPane();
	
	/** 
     * Subclasses implement this method.
     * Build the controls component required for the GUI
     */
	protected abstract JComponent buildToolBar();

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
