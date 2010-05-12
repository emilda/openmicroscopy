/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Hosts information related to a parameter for the script.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ScriptComponent 
	extends JPanel
{
	
	/** Indicates the required field. */
	static final String REQUIRED = "*";

	/** The number of columns. */
	static int COLUMNS = 10;
	
	/** The component to host. */
	private JComponent component;
	
	/** The text associated to the component. */
	private JLabel label;
	
	/** Component indicating the units or other text. */
	private JLabel unitLabel;
	
	/** 
	 * The text explaining the component. It should only be set for
	 * collections and maps.
	 */
	private JLabel info;
	
	/** Indicates if a value is required. */
	private boolean required;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param component The component to host.
	 * @param parameter The 
	 */
	ScriptComponent(JComponent component, String parameter)
	{
		if (component == null)
			throw new IllegalArgumentException("No component specified.");
		this.component = component;
		//format
		label = UIUtilities.setTextFont(parameter.replace(
				ScriptObject.PARAMETER_SEPARATOR, 
				ScriptObject.PARAMETER_UI_SEPARATOR));
		label.setToolTipText(component.getToolTipText());
		required = false;
	}
	
	/**
	 * Sets the text explaining the component when the component is a list
	 * or a map.
	 * 
	 * @param text The value to set.
	 */
	void setInfo(String text)
	{
		if (text == null || text.trim().length() == 0) return;
		info = new JLabel();
		Font f = info.getFont();
		info.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
	}
	
	/**
	 * Sets to <code>true</code> if a value is required for the field,
	 * <code>false</code> otherwise.
	 * 
	 * @param required The value to set.
	 */
	void setRequired(boolean required)
	{ 
		this.required = required; 
		if (required) label = UIUtilities.setTextFont(label.getText()+" *");
		if (component instanceof ComplexParamPane) {
			((ComplexParamPane) component).initialize(required);
		}
	}
	
	/**
	 * Sets the text of the unit label.
	 * 
	 * @param text The value to set.
	 */
	void setUnit(String text)
	{
		if (text == null || text.trim().length() == 0) return;
		if (unitLabel == null) unitLabel = new JLabel();
		unitLabel.setText(text);
	}
	
	/**
	 * Returns <code>true</code> if a value is required for that component.
	 * 
	 * @return See above.
	 */
	boolean isRequired() { return required; }
	
	/**
	 * Returns the component hosted.
	 * 
	 * @return See above.
	 */
	JComponent getComponent()
	{ 
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.add(component);
		if (unitLabel != null) p.add(unitLabel);
		return p; 
	}
	
	/** Builds and lays out the UI. */
	void buildUI()
	{
		double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, 
				{TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(getLabel(), "0, 0");
		add(getComponent(), "2, 0");
	}
	
	/**
	 * Returns the label associated to the component.
	 * 
	 * @return See above.
	 */
	JComponent getLabel()
	{ 
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(label);
		if (info != null) p.add(info);
		return UIUtilities.buildComponentPanel(p, 0, 0); 
	}
	
	/** 
	 * Returns the value associated to a script.
	 * 
	 * @return See above.
	 */
	Object getValue()
	{
		return ScriptComponent.getComponentValue(component);
	}
	
	/** 
	 * Helper method. Returns the value associated to a script.
	 * 
	 * @param c The component to handle.
	 * @return See above.
	 */
	static Object getComponentValue(JComponent c)
	{
		if (c == null) return null;
		if (c instanceof JCheckBox) {
			JCheckBox box = (JCheckBox) c;
			return box.isSelected();
		} else if (c instanceof NumericalTextField) {
			return ((NumericalTextField) c).getValueAsNumber();
		} else if (c instanceof JTextField) {
			JTextField field = (JTextField) c;
			String value = field.getText();
			if (value == null) return null;
			return value.trim();
		} else if (c instanceof JComboBox) {
			JComboBox box = (JComboBox) c;
			Object o = box.getSelectedItem();
			if (o instanceof String)
				return ((String) o).replace(ScriptObject.PARAMETER_UI_SEPARATOR, 
						ScriptObject.PARAMETER_SEPARATOR);
			return o;
		} else if (c instanceof ComplexParamPane)
			return ((ComplexParamPane) c).getValue();
			
		return null;
	}
	
}