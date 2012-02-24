/*
 * org.openmicroscopy.shoola.agents.events.metadata.AnnotatedEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Collection;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;

/** 
 * Event indicating that the objects have been annotated.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class AnnotatedEvent 
	extends RequestEvent
{

	/** The data object annotated.*/
	private Collection<DataObject> data;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The annotated object.
	 */
	public AnnotatedEvent(Collection<DataObject> data)
	{
		this.data = data;
	}
	
}
