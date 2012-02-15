/*
 * org.openmicroscopy.shoola.agents.monash.FilterByTag 
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
package org.openmicroscopy.shoola.agents.monash;

//Java imports
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.monash.util.Constants;
import org.openmicroscopy.shoola.agents.monash.view.AndsPublish;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.TagAnnotationData;

/** 
 * Loads the objects tagged with {@link Constants#REGISTER_RDA_TAG}.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class FilterByTag 
	extends PublishLoader
{

	/** The collection of objects to filter. */
    private List<Long> nodeIds;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The type of objects.*/
    private Class nodeType;
    
    /**
     * 
     * @param nodeType
     * @param viewer
     * @param nodeIds
     */
	public FilterByTag(AndsPublish viewer, Class nodeType, List<Long> nodeIds)
	{
		super(viewer);
		this.nodeIds = nodeIds;
		this.nodeType = nodeType;
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see PublishLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Filters the nodes.
	 * @see PublishLoader#load()
	 */
	public void load()
	{
		handle = mhView.filterByAnnotation(nodeType, nodeIds, 
				TagAnnotationData.class,
				Arrays.asList(Constants.REGISTER_RDA_TAG), -1, this);//
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see PublishLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == AndsPublish.DISCARDED) return;
    	viewer.setFilteredData(nodeType, (Collection<Long>) result);
    }
    
}
