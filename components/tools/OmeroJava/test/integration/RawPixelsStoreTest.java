/*
 * integration.RawPixelsStoreTest 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.RawPixelsStorePrx;
import omero.model.Image;
import omero.model.Pixels;

/** 
 * Collections of tests for the <code>RawPixelsStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class RawPixelsStoreTest 
	extends AbstractTest
{
	
    /**
     * Tests to set a plane and retrieve it, this method will test the 
     * <code>setPlane</code> and <code>getPlane</code>.
     * 
     * @throws Exception Thrown if an error occurred.
     * @see RawFileStoreTest#testUploadFile()
     */
    @Test
    public void testSetGetPlane() 
    	throws Exception 
    {
    	Image image = mmFactory.createImage(ModelMockFactory.SIZE_X, 
    			ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z, 
    			ModelMockFactory.SIZE_T, 1);
    	image = (Image) iUpdate.saveAndReturnObject(image);
    	Pixels pixels = image.getPrimaryPixels();
    	int v = pixels.getSizeX().getValue()*pixels.getSizeY().getValue(); 
    	RawPixelsStorePrx svc = factory.createRawPixelsStore();
    	svc.setPixelsId(pixels.getId().getValue(), false);
    	int size = svc.getPlaneSize();
    	assertTrue(size >= v);
    	byte[] data = new byte[size];
    	svc.setPlane(data, 0, 0, 0);
    	
    	byte[] r = svc.getPlane(0, 0, 0);
    	assertNotNull(r);
    	assertTrue(r.length == data.length);
    }
    
}