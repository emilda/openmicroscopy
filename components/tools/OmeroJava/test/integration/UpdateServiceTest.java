/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


//Third-party libraries
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.OptimisticLockException;
import omero.RInt;
import omero.api.IAdminPrx;
import omero.api.IMetadataPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.TagAnnotationI;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Collections of tests for the <code>IUpdate</code> service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class UpdateServiceTest 
	extends TestCase 
{

	/** Reference to the log. */
    protected static Log log = LogFactory.getLog(UpdateServiceTest.class);

	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    private omero.client client;
    
    /**
     * A root-client object.
     */
    private omero.client root;

    /** Helper reference to the <code>Service factory</code>. */
    private ServiceFactoryPrx factory;
    
    /** Helper reference to the <code>IQuery</code> service. */
    private IQueryPrx iQuery;
    
    /** Helper reference to the <code>IUpdate</code> service. */
    private IUpdatePrx iUpdate;

    /** Helper reference to the <code>IAdmin</code> service. */
    private IAdminPrx iAdmin;
    
    /**
     * Creates a default image and returns it.
     *
     * @param time The acquisition time.
     * @return See above.
     */
    private Image simpleImage(long time)
    {
        // prepare data
        Image img = new ImageI();
        img.setName(rstring("image1"));
        img.setDescription(rstring("descriptionImage1"));
        img.setAcquisitionDate(rtime(time));
        return img;
    }
    
	/**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception
    {
        client = new omero.client();
        factory = client.createSession();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
        iAdmin = factory.getAdminService();
        // administrator client
        String rootpass = client.getProperty("omero.rootpass");
        root = new omero.client(new String[]{"--omero.user=root",
                "--omero.pass=" + rootpass});
        root.createSession();
    }

    /**
     * Closes the session.
     * @throws Exception Thrown if an error occurred.
     */
    @Override
    @AfterClass
    public void tearDown() throws Exception
    {
        client.__del__();
        root.__del__();
    }

    /**
     * Test to create an image and make sure the version is correct.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken" })
    public void testVersionHandling() 
    	throws Exception
    {
        Image img = simpleImage(0);
        img.setName(rstring("version handling"));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        sent.setDescription(rstring("version handling update"));
        RInt version = sent.getVersion();

        // Version incremented
        Image sent2 = (Image) iUpdate.saveAndReturnObject(sent);
        RInt version2 = sent2.getVersion();
        assertTrue(version.getValue() != version2.getValue());

        // Resetting; should get error
        sent2.setVersion(version);
        CommentAnnotation iann = new CommentAnnotationI();
        iann.setTextValue( rstring(" version handling "));
        try {
            iUpdate.saveAndReturnObject(sent2);
            fail("Need optmistic lock exception.");
        } catch (OptimisticLockException e) {
            // ok.
        }

        // Fixing the change;
        // now it should work.
        sent2.setVersion( version2 );
        iUpdate.saveAndReturnObject(iann);

    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLinks() 
    	throws Exception
    {
    	/*TODO: rewrite test
    	Image img = new ImageI();
        img.setName(rstring("duplinks"));
        img.setAcquisitionDate( rtime(0) );

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks"));

        img.linkDataset(ds);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ds = img.linkedDatasetList().get(0);

        List imgLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));
                */
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnArray</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateDatasetImageLink() 
    	throws Exception 
    {
    	/*
    	Image img = new ImageI();
        img.setName(rstring("duplinks2"));
        img.setAcquisitionDate(rtime(0));

        Dataset ds = new DatasetI();
        ds.setName(rstring("duplinks2"));

        img.linkDataset(ds);

        List<IObject> l = new ArrayList<IObject>();
        l.add(img);
        List<IObject> retVal = iUpdate.saveAndReturnArray(l);
        assertTrue(retVal.size() == 1);
        
        assertTrue(retVal.get(0) instanceof Image);
        img = (Image) retVal.get(0);
        ds = img.linkedDatasetList().get(0);
        List dsLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("parent.id", ds.getId()));

       
        List imgLinks = iQuery.findAllByQuery("from DatasetImageLink",
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(imgLinks.size() > 0);
        assertTrue(dsLinks.size() > 0);
 */
    }
    
    /**
     * Test to link datasets and images using the 
     * <code>saveAndReturnObject</code> method.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNoDuplicateProjectDatasetLink() 
    	throws Exception 
    {
    	/*TODO: rewrite test
    	String name = "TEST:" + System.currentTimeMillis();

        // Save Project.
        Project p = new ProjectI();
        p.setName(rstring(name));
        p = (Project) iUpdate.saveAndReturnObject(p);

        // Update it.
        ProjectData pd = new ProjectData(p);
        pd.setDescription("....testnodups....");
        Project send = (Project) pd.asIObject();
        assertEquals(p.getId().getValue(), pd.getId());
        assertEquals(send.getId().getValue(), pd.getId());

        Project result = (Project) iUpdate.saveAndReturnObject(send);
        ProjectData test = new ProjectData(result);
        assertEquals(test.getId(), p.getId().getValue());
        */
    }
    
    /**
     * Test to make sure that the version does not increase after an update.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken", "ticket:118" })
    public void tesVersionNotIncreasingAfterUpdate()
            throws Exception 
    {
        CommentAnnotation ann = new CommentAnnotationI();
        Image img = simpleImage(0);

        img.setName(rstring("version_test"));
        img.setAcquisitionDate( rtime(0) );
        ann.setTextValue(rstring("version_test"));
        img.linkAnnotation(ann);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ann = (CommentAnnotation) img.linkedAnnotationList().get(0);

        assertNotNull(img.getId());
        assertNotNull(ann.getId());

        int origVersion = img.getVersion().getValue();
        // No longer exists int orig_ann_version = ann.getVersion().intValue();

        ann.setTextValue(rstring("updated version_test"));

        ann = (CommentAnnotation) iUpdate.saveAndReturnObject(ann);
        img = (Image) iQuery.get(Image.class.getName(), img.getId().getValue()); 

        // No longer existsint new_ann_version = ann.getVersion().intValue();
        int newVersion = img.getVersion().getValue();

        assertFalse(ann.getTextValue().getValue().contains("updated"));
        assertTrue(origVersion == newVersion);
    }
    
    /**
     * Test to make sure that the version number does not increase 
     * when invoking the <code>SaveAndReturnObject</code> on an Object 
     * not modified.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "versions", "broken", "ticket:118" })
    public void testVersionNotIncreasingOnUnmodifiedObject() 
    	throws Exception 
    {
        Image img = new ImageI();
        img.setName(rstring("no vers. increment")) ;
        img.setAcquisitionDate(rtime(0));
        img = (Image) iUpdate.saveAndReturnObject(img);

        Image test = (Image) iUpdate.saveAndReturnObject(img);

        fail("must move details correction to the merge event listener "
                + "or version will always be incremented. ");

        assertTrue(img.getVersion().equals(test.getVersion()));
    }
    

    /**
     * Tests the creation of a project without datasets.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyProject() 
    	throws Exception
    {
    	Project data = new ProjectI();
        data.setName(rstring("project1"));
        data.setDescription(rstring("descriptionProject1"));
        Project p = (Project) iUpdate.saveAndReturnObject(data);
        assertNotNull(p);
        ProjectData pd = new ProjectData(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == pd.getId());
    	assertTrue(p.getName().getValue() == pd.getName());
    	assertTrue(p.getDescription().getValue() == pd.getDescription());
    }
    
    /**
     * Tests the creation of a dataset.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyDataset() 
    	throws Exception
    {
    	DatasetData data = new DatasetData();
        data.setName("dataset1");
        data.setDescription("descriptionDataset1");
        Dataset p = (Dataset) 
        	factory.getUpdateService().saveAndReturnObject(data.asIObject());
        assertNotNull(p);
        DatasetData d = new DatasetData(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == d.getId());
    	assertTrue(p.getName().getValue() == d.getName());
    	assertTrue(p.getDescription().getValue() == d.getDescription());
    }
    
    /**
     * Tests the creation of a dataset.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyImage() 
    	throws Exception
    {
    	Image data = new ImageI();
        data.setName(rstring("image1"));
        data.setDescription(rstring("descriptionImage1"));
        data.setAcquisitionDate(rtime(0));
        Image p = (Image) 
        	factory.getUpdateService().saveAndReturnObject(data);
        ImageData img = new ImageData(p);
    	assertNotNull(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == img.getId());
    	assertTrue(p.getName().getValue() == img.getName());
    	assertTrue(p.getDescription().getValue() == img.getDescription());
    }
    
    /**
     * Tests the creation of a screen
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyScreen() 
    	throws Exception
    {
    	ScreenData data = new ScreenData();
        data.setName("screen1");
        data.setDescription("screen1");
        Screen p = (Screen) 
        	factory.getUpdateService().saveAndReturnObject(data.asIObject());
        data = new ScreenData(p);
    	assertNotNull(p);
    	assertTrue(p.getId().getValue() > 0);
    	assertTrue(p.getId().getValue() == data.getId());
    	assertTrue(p.getName().getValue() == data.getName());
    	assertTrue(p.getDescription().getValue() == data.getDescription());
    }
    
    /**
     * Test to create a project and link datasets to it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateProjectAndLinkDatasets() 
    	throws Exception 
    {
        String name = " 2&1 " + System.currentTimeMillis();
        Project p = new ProjectI();
        p.setName(rstring(name));

        p = (Project) iUpdate.saveAndReturnObject(p);

        Dataset d1 = new DatasetI();
        d1.setName(rstring(name));
        d1 = (Dataset) iUpdate.saveAndReturnObject(d1);

        Dataset d2 = new DatasetI();
        d2.setName(rstring(name));
        d2 = (Dataset) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        //links dataset and project.
        iUpdate.saveAndReturnArray(links);
        
        //load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());
       
        StringBuilder sb = new StringBuilder();
        sb.append("select p from Project p ");
        sb.append("left outer join fetch p.datasetLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Project) iQuery.findByQuery(sb.toString(), param);
        
        //Check the conversion of Project to ProjectData
        ProjectData pData = new ProjectData(p);
        Set<DatasetData> datasets = pData.getDatasets();
        //We should have 2 datasets
        assertTrue(datasets.size() == 2);
        int count = 0;
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        while (i.hasNext()) {
        	dataset = i.next();
			if (dataset.getId() == d1.getId().getValue() ||
					dataset.getId() == d2.getId().getValue()) count++;
		}
        assertTrue(count == 2);
    }
    
    /**
     * Test to create a dataset and link images to it.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreateDatasetAndLinkImages() 
    	throws Exception 
    {
        String name = " 2&1 " + System.currentTimeMillis();
        Dataset p = new DatasetI();
        p.setName(rstring(name));

        p = (Dataset) iUpdate.saveAndReturnObject(p);

        Image d1 = new ImageI();
        d1.setName(rstring(name));
        d1.setAcquisitionDate(rtime(0));
        d1 = (Image) iUpdate.saveAndReturnObject(d1);

        Image d2 = new ImageI();
        d2.setAcquisitionDate(rtime(0));
        d2.setName(rstring(name));
        d2 = (Image) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        //links dataset and project.
        iUpdate.saveAndReturnArray(links);
        
        //load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());
       
        StringBuilder sb = new StringBuilder();
        sb.append("select p from Dataset p ");
        sb.append("left outer join fetch p.imageLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Dataset) iQuery.findByQuery(sb.toString(), param);
        
        //Check the conversion of Project to ProjectData
        DatasetData pData = new DatasetData(p);
        Set<ImageData> images = pData.getImages();
        //We should have 2 datasets
        assertTrue(images.size() == 2);
        int count = 0;
        Iterator<ImageData> i = images.iterator();
        ImageData image;
        while (i.hasNext()) {
        	image = i.next();
			if (image.getId() == d1.getId().getValue() ||
					image.getId() == d2.getId().getValue()) count++;
		}
        assertTrue(count == 2);
    }
    
    /**
     * Test to unlink projects and datasets from just one side.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkProjectAndDatasetFromJustOneSide() 
    	throws Exception 
    {
    	/*
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
        */
    }
    

    /**
     * Test to unlink datasets and images.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkDatasetAndImage() 
    	throws Exception
    {

    	/*
        // Method 1:
        Image img = saveImage(true);
        List updated = unlinkImage(img);
        iUpdate.saveCollection(updated);

        // Make sure it's not linked.
        List list = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 2:
        img = saveImage(true);
        updated = unlinkImage(img);
        iContainer.updateDataObjects(updated, null);

        List list2 = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 3:
        img = saveImage(true);
        Dataset target = img.linkedDatasetList().get(0);
        // For querying
        DatasetImageLink dslink = img.findDatasetImageLink(target).iterator()
                .next();

        img.unlinkDataset(target);
        img = (Image) iContainer.updateDataObject(img, null);

        IObject test = iQuery.find(DatasetImageLink.class.getName(), 
        		dslink.getId().getValue());
        assertNull(test);

        // Method 4;
        Dataset d = new DatasetI();
        d.setName(rstring("unlinking"));
        Project p = new ProjectI();
        p.setName(rstring("unlinking"));
        p = (Project) iContainer.createDataObject(p, null);
        d = (Dataset) iContainer.createDataObject(d, null);

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d);
        */
    }
    
    /**
     * Test to unlink datasets and images from just one side.
     * @throws Exception
     */
    @Test(groups = { "broken", "ticket:541" })
    public void testUnlinkDatasetAndImageFromJustOneSide() 
    	throws Exception 
    {
    	/*
        Image img = saveImage(true);
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLinkFromBoth(link, false);

        iContainer.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) 
        	iQuery.find(DatasetImageLink.class.getName(), 
        			link.getId().getValue());

        assertNull(test);
        */
    }
    
    /**
     * Test to handle duplicate links
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDuplicateProjectDatasetLink() 
    	throws Exception 
    {

    	/*TODO: rewrite test
        String string = "duplinksagain" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName(rstring(string));

        Project p = new ProjectI();
        p.setName(rstring(string));

        d.linkProject(p);
        d = (Dataset) iContainer.createDataObject(d, null);
        List<Project> orig = d.linkedProjectList();
        Set orig_ids = new HashSet();
        for (Project pr : orig) {
            orig_ids.add(pr.getId().getValue());
        }

        DatasetData dd = new DatasetData(d);
        Dataset toSend = dd.asDataset();

        Dataset updated = (Dataset) iContainer.updateDataObject(toSend, null);

        List<Project> updt = updated.linkedProjectList();
        Set updt_ids = new HashSet();
        for (Project pr : updt) {
            updt_ids.add(pr.getId().getValue());
        }

        if (log.isDebugEnabled()) {
            log.debug(orig_ids);
            log.debug(updt_ids);
        }

        assertTrue(updt_ids.containsAll(orig_ids));
        assertTrue(orig_ids.containsAll(updt_ids));
        */
    }
    
    
    //Annotation section
    /**
     * Tests to update a textual annotation.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUpdateTextualAnnotation() 
    	throws Exception 
    {
    	/* Test: rewrite test
        DataObject annotatedObject;
        AnnotationData data;

        Dataset d = new DatasetI();
        d.setName(rstring("update_annotation"));
        d = (Dataset) iContainer.createDataObject(d, null);
        annotatedObject = new DatasetData(d);

        data = new TextualAnnotationData("update_annotation");
       
        IObject updated = iContainer.updateDataObject(
        		annotatedObject.asIObject(), null);

        DatasetAnnotationLink link = 
        	((Dataset) updated).linkAnnotation(data.asAnnotation());
        link = (DatasetAnnotationLink) iContainer.updateDataObject(link, null);
        link.getChild().unload();

        DataObject toReturn = 
        	new TextualAnnotationData((CommentAnnotation) link.getChild());
        	*/
    }
    
    /**
     * Tests the creation of tag annotation, linked it to an image by a
     * user and link it to the same image by a different user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUpdateSameTagAnnotationUsedByTwoUsers() throws Exception
    {

        String groupName = iAdmin.getEventContext().groupName;

        //create an image.
        Image image = (Image) iUpdate.saveAndReturnObject(simpleImage(0));

        //create the tag.
        TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));

        Annotation data = (Annotation) iUpdate.saveAndReturnObject(tag);
        //link the image and the tag
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());

        IObject o1 = iUpdate.saveAndReturnObject(l);
        assertNotNull(o1);
        CreatePojosFixture2 fixture = CreatePojosFixture2.withNewUser(root, 
        		groupName);

        l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());
        // l.getDetails().setOwner(fixture.e);
        IObject o2 = fixture.iUpdate.saveAndReturnObject(l);
        assertNotNull(o2);

        long self = factory.getAdminService().getEventContext().userId;

        assertTrue(o1.getId().getValue() != o2.getId().getValue());
        assertTrue(o1.getDetails().getOwner().getId().getValue() == self);
        assertTrue(o2.getDetails().getOwner().getId().getValue() ==
            fixture.e.getId().getValue());
    }
    
}