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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.openmicroscopy.shoola.agents.monash.PublishAgent;
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
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
						implements ActionListener 
{

	/** The default size of the window. */
	protected static final Dimension 	DEFAULT_SIZE = new Dimension(700, 400);

	/** The icon to use for the dialog. */
	private Icon 						icon;

	/** Title panel for displaying title as well as other messages in sub-title */
	private TitlePanel tp;

	/**
	 * Creates a new instance.
	 * @param parent	the component
	 * @param title	the title to display on the dialog
	 * @param message	the error message to display
	 * @param icon	the icon for the dialog
	 */
	public MonashDialog(JFrame parent, String title, String subtitle, Icon icon) 
	{
		super(parent);
		setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		this.icon = icon;
		initialize(title, subtitle);
		//setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	}

	private void initialize(String title, String subtitle) 
	{
		//if (icon == null) icon = UIManager.getIcon("monashIcon");
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			setTitle(title);
			initComponents();
			buildGUI(subtitle);
			setSize(DEFAULT_SIZE);
			//pack();
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void buildGUI(String subtitle) 
	{
		JComponent component;
		component = buildContentPane();
		component.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 0));
		Container c = getContentPane();
		tp = new TitlePanel(getTitle(), subtitle, icon);
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

	/**
	 * Show dialog based on input parameters
	 * 
	 * @param parent	the Frame in which the dialog is displayed; 
	 * 					if null, or if the parentComponent has no Frame, a 
	 * 					default Frame is used
	 * @param dialog_title	the title string for the dialog
	 * @param message	the error message to display
	 * @param messageType	the type of message to be displayed, {@link JOptionPane}
	 * 		possible values are ERROR_MESSAGE, WARNING_MESSAGE, INFORMATION_MESSAGE
	 * @param ex		the exception
	 */
	public static void showDialog(Component parent, String dialog_title,
			String message, int messageType, Exception ex)
	{
		LogMessage msg = new LogMessage();
		msg.println(message);
		if (messageType == JOptionPane.ERROR_MESSAGE) {
			if (ex != null) {
				msg.println("Reason: " + ex.getMessage());
			}
			Logger logger = PublishAgent.getRegistry().getLogger();
			logger.error(parent, msg);
		}
		JOptionPane.showMessageDialog(parent, msg.toString(), 
				dialog_title, 	//	the title for the dialog
				messageType);
	}
	
	/**
	 * Shows error dialog 
	 * @param parent	the Frame in which the dialog is displayed
	 * @param message	the error message to display
	 * @param ex		the exception that caused the error
	 */
	public static void showErrDialog(Component parent, String message, Exception ex)
	{
		showDialog(parent, Constants.BACKEND_ERROR, message, JOptionPane.ERROR_MESSAGE, ex);
	}
	
	/**
	 * Shows warning dialog 
	 * @param parent	the Frame in which the dialog is displayed
	 * @param message	the warning message to display
	 * @param ex		the exception that caused the error
	 */
	public static void showWarnDialog(Component parent, String message, Exception ex)
	{
		showDialog(parent, Constants.WARN_MESSAGE, message, JOptionPane.WARNING_MESSAGE, ex);
	}
	
	/**
	 * Shows information dialog 
	 * @param parent	the Frame in which the dialog is displayed
	 * @param message	the message to display
	 */
	public static void showInfoDialog(Component parent, String message)
	{
		showDialog(parent, Constants.INFO_MESSAGE, message, JOptionPane.PLAIN_MESSAGE, null);
	}

	/** Return the icon for the dialog */
	public Icon getIcon() {
		return icon;
	}

	/** sets the icon for the dialog */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	/** Sets the sub-title in the title panel */
	public void setMessage(String message) {
		tp.setTextHeader(message);
	}
}
