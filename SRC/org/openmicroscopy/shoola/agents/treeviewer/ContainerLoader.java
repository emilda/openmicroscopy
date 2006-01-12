/*
 * org.openmicroscopy.shoola.agents.treeviewer.ContainerLoader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryGroupData;
import pojos.ProjectData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ContainerLoader
    extends DataBrowserLoader
{
 
    /** The set of node IDs. */
    private Set         nodeIDs;
    
    /** The type of the node. */
    private Class       nodeType;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /** 
     * Checks if the specified class if supported. 
     * 
     * @param type The type to control.
     */
    private void checkClassType(Class type)
    {
        if ((type.equals(ProjectData.class)) ||
                (type.equals(CategoryGroupData.class))) return;
        throw new IllegalArgumentException("Type not supported");
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param type          One of the type supported by this class.
     * @param nodeID        The ID of the container node.        
     */
    public ContainerLoader(Browser viewer, Class type, int nodeID)
    {
        super(viewer);
        checkClassType(type);
        nodeType = type;
        nodeIDs = new HashSet(1);
        nodeIDs.add(new Integer(nodeID));
    }

    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        handle = dmView.loadContainerHierarchy(nodeType, nodeIDs, false, 
                		viewer.getRootLevel(), viewer.getRootID(), this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer. 
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        viewer.setContainerNodes((Set) result, viewer.getSelectedDisplay());
    }
    
}
