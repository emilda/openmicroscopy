/*
 * pojos.TimeAnnotationData 
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
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import ome.model.annotations.TimestampAnnotation;

/** 
 * Basic time annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class TimeAnnotationData 
	extends AnnotationData
{

	/** Creates a default instance. */
	public TimeAnnotationData()
	{
		this(null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param annotation The annotation to wrap.
	 */
	public TimeAnnotationData(TimestampAnnotation annotation)
	{
		super(annotation);
	}

	/**
	 * Returns the time value.
	 * 
	 * @return See above
	 */
	public Timestamp getTime() { return (Timestamp) getContent(); }
	
	/**
	 * Returns the timestamp of the annotation.
	 * @see AnnotationData#getContent()
	 */
	public Object getContent()
	{
		return ((TimestampAnnotation) asAnnotation()).getTimeValue();
	}

	/**
	 * Returns the time as a string.
	 * @see AnnotationData#getContentAsString()
	 */
	public String getContentAsString() { return getTime().toString(); }

	/**
	 * Sets the time annotation.
	 * @see AnnotationData#setContent(Object)
	 */
	public void setContent(Object content)
	{
		if (content == null)
			throw new IllegalArgumentException("Time value cannot be null.");
		if (!(content instanceof Timestamp))
			throw new IllegalArgumentException("Content must be of type " +
					"Timestamp");
		((TimestampAnnotation) asAnnotation()).setTimeValue(
				(Timestamp) content);
	}
	
}
