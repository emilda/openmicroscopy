/*
 * pojos.LongAnnotationData 
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
package pojos;


//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.annotations.LongAnnotation;

/** 
 * Wraps a long annotation.
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
public class LongAnnotationData 
	extends AnnotationData
{

	/**
	 * Creates a new instance.
	 * 
	 * @param value The rating value. One of the contants defined by 
	 * 				this class.
	 */
	public LongAnnotationData(long value)
	{
		super(LongAnnotation.class);
		setDataValue(value);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param value The rating value. One of the contants defined by 
	 * 				this class.
	 */
	public LongAnnotationData()
	{
		super(LongAnnotation.class);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param annotation 	The {@link LongAnnotation} object corresponding to 
	 * 						this <code>DataObject</code>. 
     *            			Mustn't be <code>null</code>.
	 */
	public LongAnnotationData(LongAnnotation annotation)
	{
		super(annotation);
	}
	
	/**
	 * Sets the rating value.
	 * 
	 * @param value The value to set. Must be One of the contants defined by 
	 * 				this class.
	 */
	public void setDataValue(long value)
	{
		Long l = new Long(value);
		((LongAnnotation) asAnnotation()).setLongValue(l);
	}
	
	/**
	 * Returns the value.
	 * 
	 * @return See above.
	 */
	public long getDataValue() 
	{
		return ((Long) getContent()).longValue();
	}
	
	/**
	 * Returns the rating value.
	 * @see AnnotationData#getContent()
	 */
	public Object getContent()
	{
		return ((LongAnnotation) asAnnotation()).getLongValue();
	}
	
	/**
	 * Returns the value as a string.
	 * @see AnnotationData#getContentAsString()
	 */
	public String getContentAsString() { return ""+getDataValue(); }

	/**
	 * Sets the text annotation.
	 * @see AnnotationData#setContent(Object)
	 */
	public void setContent(Object content)
	{
		if (content == null) return;
		if (content instanceof Number) {
			Number n = (Number) content;
			setDataValue(n.longValue());
		} else
			throw new IllegalArgumentException("Value not supported.");
	}
	
}
