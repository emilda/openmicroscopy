/*
 * ome.formats.OMEROMetadataStore
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.OTF;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.acquisition.StageLabel;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.Binning;
import ome.model.enums.ContrastMethod;
import ome.model.enums.Illumination;
import ome.model.enums.Medium;
import ome.model.enums.PhotometricInterpretation;
import ome.model.experiment.Experiment;
import ome.model.roi.Ellipse;
import ome.model.roi.Line;
import ome.model.roi.Mask;
import ome.model.roi.Point;
import ome.model.roi.Polygon;
import ome.model.roi.Polyline;
import ome.model.roi.Rect;
import ome.model.roi.Roi;
import ome.model.screen.Plate;
import ome.model.screen.Reagent;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;
import ome.conditions.ApiUsageException;
import ome.util.LSID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.perf4j.StopWatch;


/**
 * An OMERO metadata store. This particular metadata store requires the user to
 * be logged into OMERO prior to use with the {@link #login()} method. While
 * attempts have been made to allow the caller to switch back and forth between 
 * Images and Pixels during metadata population it is <b>strongly</b> 
 * encouraged that at least Images and Pixels are populated in ascending order. 
 * For example: Image_1 --> Pixels_1, Pixels_2 followed by Image_2 --> Pixels_1,
 * Pixels2, Pixels_3.
 * 
 * @author Brian W. Loranger brain at lifesci.dundee.ac.uk
 * @author Chris Allan callan at blackcat.ca
 */
public class OMEROMetadataStore
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(OMEROMetadataStore.class);

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO query service */
    private IQuery iQuery;

    /** OMERO update service */
    private IUpdate iUpdate;

    /** A map of imageIndex vs. Image object ordered by first access. */
    private Map<Integer, Image> imageList = 
    	new LinkedHashMap<Integer, Image>();

    /** A map of pixelsIndex vs. Pixels object ordered by first access. */
    private Map<Integer, Pixels> pixelsList = 
    	new LinkedHashMap<Integer, Pixels>();
    
    /** A map of screenIndex vs. Screen object ordered by first access. */
    private Map<Integer, Screen> screenList = 
    	new LinkedHashMap<Integer, Screen>();

    /** A map of plateIndex vs. Plate object ordered by first access. */
    private Map<Integer, Plate> plateList = 
    	new LinkedHashMap<Integer, Plate>();

    /** A map of wellIndex vs. Well object ordered by first access. */
    private Map<Integer, Well> wellList = new LinkedHashMap<Integer, Well>();
    
    /** A map of instrumentIndex vs. Instrument object ordered by first access. */
    private Map<Integer, Instrument> instrumentList = 
    	new LinkedHashMap<Integer, Instrument>();
    
    /** A map of imageIndex vs. ROIs */    
    private Map<Integer, Map<Integer, Roi>> roiMap = 
    	new HashMap<Integer, Map<Integer, Roi>>();
    
    /** A list of all objects we've received from the client and their LSIDs. */
    private Map<LSID, IObject> lsidMap = new HashMap<LSID, IObject>();
        
    /**
     * Updates a given model object in our object graph.
     * @param lsid LSID of model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should are used to describe the model
     * object's graph location.
     */
    public void updateObject(String lsid, IObject sourceObject,
    		                 Map<String, Integer> indexes)
    {
    	lsidMap.put(new LSID(lsid), sourceObject);
    	if (sourceObject instanceof Image)
    	{
    		handle(lsid, (Image) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof StageLabel)
    	{
    		handle(lsid, (StageLabel) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Pixels)
    	{
    		handle(lsid, (Pixels) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Channel)
    	{
    		handle(lsid, (Channel) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof LogicalChannel)
    	{
    		handle(lsid, (LogicalChannel) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof PlaneInfo)
    	{
    		handle(lsid, (PlaneInfo) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Instrument)
    	{
    		handle(lsid, (Instrument) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Objective)
    	{
    		handle(lsid, (Objective) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Detector)
    	{
    		handle(lsid, (Detector) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Dichroic)
    	{
    		handle(lsid, (Dichroic) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Filter)
    	{
    		handle(lsid, (Filter) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof FilterSet)
    	{
    		handle(lsid, (FilterSet) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Laser)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Filament)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Arc)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof OTF)
    	{
    		handle(lsid, (OTF) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof ImagingEnvironment)
    	{
    		handle(lsid, (ImagingEnvironment) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof DetectorSettings)
    	{
    		handle(lsid, (DetectorSettings) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof LightSettings)
    	{
    		handle(lsid, (LightSettings) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof ObjectiveSettings)
    	{
    		handle(lsid, (ObjectiveSettings) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Screen)
    	{
    	    handle(lsid, (Screen) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Plate)
    	{
    	    handle(lsid, (Plate) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Well)
    	{
    	    handle(lsid, (Well) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Reagent)
    	{
    	    handle(lsid, (Reagent) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof WellSample)
    	{
    	    handle(lsid, (WellSample) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof OriginalFile)
        {
            handle(lsid, (OriginalFile) sourceObject, indexes);
        }
    	else if (sourceObject instanceof Annotation)
    	{
    		handle(lsid, (Annotation) sourceObject, indexes); 
    	}
    	else if (sourceObject instanceof Experiment)
    	{
    		handle(lsid, (Experiment) sourceObject, indexes); 
    	}
    	else if (sourceObject instanceof Roi)
    	{
    	    handle(lsid, (Roi) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Rect)
    	{
    	    handle(lsid, (Rect) sourceObject, indexes);
    	}
        else if (sourceObject instanceof Point)
        {
            handle(lsid, (Point) sourceObject, indexes);
        }
        else if (sourceObject instanceof Polygon)
        {
            handle(lsid, (Polygon) sourceObject, indexes);
        }
        else if (sourceObject instanceof Polyline)
        {
            handle(lsid, (Polyline) sourceObject, indexes);
        }
        else if (sourceObject instanceof Ellipse)
        {
            handle(lsid, (Ellipse) sourceObject, indexes);
        }
        else if (sourceObject instanceof Line)
        {
            handle(lsid, (Line) sourceObject, indexes);
        }
        else if (sourceObject instanceof Mask)
        {
            handle(lsid, (Mask) sourceObject, indexes);
        }
        else
    	{
    		throw new ApiUsageException(
    			"Missing object handler for object type: "
    				+ sourceObject.getClass());
    	}
    }
    
    /**
     * Updates our object graph references.
     * @param referenceCache Client side LSID reference cache.
     */
    public void updateReferences(Map<String, String[]> referenceCache)
    {
    	for (String target : referenceCache.keySet())
    	{
    		for (String reference : referenceCache.get(target))
    		{
    			LSID targetLSID = new LSID(target);
    			IObject targetObject = lsidMap.get(targetLSID);
    			LSID referenceLSID = new LSID(reference);
    			IObject referenceObject = lsidMap.get(
    					new LSID(stripCustomSuffix(reference)));
    			if (targetObject instanceof DetectorSettings)
    			{
    				if (referenceObject instanceof Detector)
    				{
    					handleReference((DetectorSettings) targetObject,
    							(Detector) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof Image)
    			{
    				if (referenceObject instanceof Instrument)
    				{
    					handleReference((Image) targetObject,
    							(Instrument) referenceObject);
    					continue;
    				}
    				if (referenceObject instanceof Annotation)
    				{
    					handleReference((Image) targetObject,
    							(Annotation) referenceObject);
    					continue;
    				}
    				if (referenceLSID.toString().contains("DatasetI"))
    				{
    					int colonIndex = reference.indexOf(":");
    					long datasetId = Long.parseLong(
    							reference.substring(colonIndex + 1));
    					referenceObject = new Dataset(datasetId, false);
    					handleReference((Image) targetObject,
    							(Dataset) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof LightSource)
    			{
    				if (referenceObject instanceof LightSource)
    				{
    					handleReference((LightSource) targetObject,
    							(LightSource) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof LightSettings)
    			{
    				if (referenceObject instanceof LightSource)
    				{
    					handleReference((LightSettings) targetObject,
    							(LightSource) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof LogicalChannel)
    			{
    				if (referenceObject instanceof Filter)
    				{
    					handleReference((LogicalChannel) targetObject,
						                (Filter) referenceObject,
						                referenceLSID);
    					continue;
    				}
    				if (referenceObject instanceof FilterSet)
    				{
    					handleReference((LogicalChannel) targetObject,
    							        (FilterSet) referenceObject);
    					continue;
    				}
    				if (referenceObject instanceof OTF)
    				{
    					handleReference((LogicalChannel) targetObject,
    							(OTF) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof OTF)
    			{
    				if (referenceObject instanceof Objective)
    				{
    					handleReference((OTF) targetObject,
    							(Objective) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof ObjectiveSettings)
    			{
    				if (referenceObject instanceof Objective)
    				{
    					handleReference((ObjectiveSettings) targetObject,
    							(Objective) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof WellSample)
    			{
    				if (referenceObject instanceof Image)
    				{
    					handleReference((WellSample) targetObject,
    							(Image) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof Pixels)
    			{
    				if (referenceObject instanceof OriginalFile)
    				{
    					handleReference((Pixels) targetObject,
    							(OriginalFile) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof FilterSet)
    			{
    				if (referenceObject instanceof Filter)
    				{
    					handleReference((FilterSet) targetObject,
						                (Filter) referenceObject,
						                referenceLSID);
    					continue;
    				}
    				if (referenceObject instanceof Dichroic)
    				{
    					handleReference((FilterSet) targetObject,
    							        (Dichroic) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof Plate)
    			{
    				if (referenceLSID.toString().contains("ScreenI"))
    				{
    					int colonIndex = reference.indexOf(":");
    					long screenId = Long.parseLong(
    							reference.substring(colonIndex + 1));
    					referenceObject = new Screen(screenId, false);
    					handleReference((Plate) targetObject,
    							(Screen) referenceObject);
    					continue;
    				}
    				if (referenceObject instanceof Annotation)
    				{
    					handleReference((Plate) targetObject,
    							(Annotation) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof Well)
    			{
    				if (referenceObject instanceof Reagent)
    				{
    					handleReference((Well) targetObject,
    							        (Reagent) referenceObject);
    					continue;
    				}
    			}
    			else if (targetObject instanceof FileAnnotation)
    			{
    				if (referenceObject instanceof OriginalFile)
    				{
    					handleReference((FileAnnotation) targetObject,
    							(OriginalFile) referenceObject);
    					continue;
    				}
    			}

    			throw new ApiUsageException(String.format(
    					"Missing reference handler for %s(%s) --> %s(%s) reference.",
    					reference, referenceObject, target, targetObject));
    		}
    	}
    }
    
    /**
     * Strips custom, reference only suffixes from LSID so that the object
     * may be correctly looked up.
     * @param LSID The LSID string to strip the suffix from.
     * @return A new LSID string with the suffix stripped or <code>LSID</code>.
     */
    private String stripCustomSuffix(String LSID)
    {
    	if (LSID.endsWith("OMERO_EMISSION_FILTER")
    		|| LSID.endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		return LSID.substring(0, LSID.lastIndexOf(':'));
    	}
    	return LSID;
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should are used to describe the model
     * object's graph location.
     */
    private void handle(String LSID, Image sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int imageIndex = indexes.get("imageIndex");
        imageList.put(imageIndex, sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Pixels sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int imageIndex = indexes.get("imageIndex");
    	imageList.get(imageIndex).addPixels(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Channel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Pixels p = getPixels(indexes.get("imageIndex"), 0);
    	p.addChannel(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LogicalChannel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Channel c = getChannel(indexes.get("imageIndex"),
    			               indexes.get("logicalChannelIndex"));
    	c.setLogicalChannel(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, PlaneInfo sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int imageIndex = indexes.get("imageIndex");
    	int pixelsIndex = indexes.get("pixelsIndex");
    	Pixels p = imageList.get(imageIndex).getPixels(pixelsIndex);
    	p.addPlaneInfo(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Instrument sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int instrumentIndex = indexes.get("instrumentIndex");
    	instrumentList.put(instrumentIndex, sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, StageLabel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = getImage(indexes.get("imageIndex"));
    	i.setStageLabel(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Objective sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = getInstrument(indexes.get("instrumentIndex"));
    	i.addObjective(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Detector sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = getInstrument(indexes.get("instrumentIndex"));
    	i.addDetector(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LightSource sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addLightSource(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, OTF sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addOTF(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Dichroic sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addDichroic(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Filter sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addFilter(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, FilterSet sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addFilterSet(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, ImagingEnvironment sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = imageList.get(indexes.get("imageIndex"));
    	i.setImagingEnvironment(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, DetectorSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	LogicalChannel lc = getLogicalChannel(indexes.get("imageIndex"),
    			                              indexes.get("logicalChannelIndex"));
    	lc.setDetectorSettings(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LightSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	LogicalChannel lc = getLogicalChannel(indexes.get("imageIndex"),
    			                              indexes.get("logicalChannelIndex"));
    	lc.setLightSourceSettings(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, ObjectiveSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = getImage(indexes.get("imageIndex"));
    	i.setObjectiveSettings(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Plate sourceObject,
                        Map<String, Integer> indexes)
    {
    	int plateIndex = indexes.get("plateIndex");
        wellList = new LinkedHashMap<Integer, Well>();
        plateList.put(plateIndex, sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Well sourceObject,
                        Map<String, Integer> indexes)
    {
        int plateIndex = indexes.get("plateIndex");
        int wellIndex = indexes.get("wellIndex");
        getPlate(plateIndex).addWell(sourceObject);  
        wellList.put(wellIndex, sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Screen sourceObject,
                        Map<String, Integer> indexes)
    {
    	int screenIndex = indexes.get("screenIndex");
    	screenList.put(screenIndex, sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Reagent sourceObject,
                        Map<String, Integer> indexes)
    {
        int screenIndex = indexes.get("screenIndex");
        getScreen(screenIndex).addReagent(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, WellSample sourceObject,
                        Map<String, Integer> indexes)
    {
        int plateIndex = indexes.get("plateIndex");
        int wellIndex = indexes.get("wellIndex");
        Well w = getWell(plateIndex, wellIndex);
        w.addWellSample(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Roi sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Image i = getImage(imageIndex);
        Map<Integer, Roi> rois = roiMap.get(imageIndex);
        if (rois == null)
        {
            rois = new HashMap<Integer, Roi>();
            roiMap.put(imageIndex, rois);
        }
        rois.put(roiIndex, sourceObject);
        i.addRoi(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Rect sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Point sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Polygon sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Polyline sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }
   
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Ellipse sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Line sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Mask sourceObject,
                        Map<String, Integer> indexes)
    {
        int imageIndex = indexes.get("imageIndex");
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(imageIndex, roiIndex);
        r.addShape(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, OriginalFile sourceObject,
                        Map<String, Integer> indexes)
    {
        // No-op.
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Annotation sourceObject,
                        Map<String, Integer> indexes)
    {
        // No-op.
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Experiment sourceObject,
                        Map<String, Integer> indexes)
    {
        // No-op.
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(DetectorSettings target, Detector reference)
    {
    	target.setDetector(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Instrument reference)
    {
    	target.setInstrument(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Dataset reference)
    {
    	target.linkDataset(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightSource target, LightSource reference)
    {
    	// The only possible linkage at this point is a Laser's pump.
    	Laser laser = (Laser) target;
    	laser.setPump(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightSettings target, LightSource reference)
    {
    	target.setLightSource(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LogicalChannel target, OTF reference)
    {
    	target.setOtf(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LogicalChannel target, FilterSet reference)
    {
    	target.setFilterSet(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph. This method handles <b>secondary</b> excitation and 
     * emission filters so requires the LSID be passed in as well.
     * @param target Target model object.
     * @param reference Reference model object.
     * @param referenceLSID LSID of the reference object.
     */
    private void handleReference(LogicalChannel target, Filter reference,
    		                     LSID referenceLSID)
    {
    	if (referenceLSID.toString().endsWith("OMERO_EMISSION_FILTER"))
    	{
    		target.setSecondaryEmissionFilter(reference);
    	}
    	else if (referenceLSID.toString().endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		target.setSecondaryExcitationFilter(reference);
    	}
    	else
    	{
    		throw new ApiUsageException(String.format(
    				"Unable to handle LogicalChannel --> Filter reference: %s",
    				referenceLSID));
    	}
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(OTF target, Objective reference)
    {
    	target.setObjective(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(ObjectiveSettings target, Objective reference)
    {
    	target.setObjective(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(WellSample target, Image reference)
    {
        reference.addWellSample(target);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Pixels target, OriginalFile reference)
    {
        target.linkOriginalFile(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FilterSet target, Dichroic reference)
    {
        target.setDichroic(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FilterSet target, Filter reference,
    		                     LSID referenceLSID)
    {
    	if (referenceLSID.toString().endsWith("OMERO_EMISSION_FILTER"))
    	{
    		target.setEmFilter(reference);
    	}
    	else if (referenceLSID.toString().endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		target.setExFilter(reference);
    	}
    	else
    	{
    		throw new ApiUsageException(String.format(
    				"Unable to handle FilterSet --> Filter reference: %s",
    				referenceLSID));
    	}
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Plate target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Plate target, Screen reference)
    {
        target.linkScreen(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Well target, Reagent reference)
    {
        target.linkReagent(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FileAnnotation target, OriginalFile reference)
    {
        target.setFile(reference);
    }
    /**
     * Retrieves an object from the internal object graph by LSID.
     * @param lsid LSID of the object.
     * @return See above. <code>null</code> if the object is not in the
     * internal LSID map.
     */
    public IObject getObjectByLSID(LSID lsid)
    {
    	return lsidMap.get(lsid);
    }
    
    /**
     * Returns an Image model object based on its indexes within the OMERO data
     * model.
     * @param imageIndex Image index.
     * @return See above.
     */
    private Image getImage(int imageIndex)
    {
    	return imageList.get(imageIndex);
    }
    
    /**
     * Returns a Pixels model object based on its indexes within the OMERO data
     * model.
     * @param imageIndex Image index.
     * @param pixelsIndex Pixels index.
     * @return See above.
     */
    private Pixels getPixels(int imageIndex, int pixelsIndex)
    {
    	return getImage(imageIndex).getPixels(pixelsIndex);
    }
    
    /**
     * Returns an Instrument model object based on its indexes within the OMERO
     * data model.
     * @param instrumentIndex Instrument index.
     * @return See above.
     */
    private Instrument getInstrument(int instrumentIndex)
    {
    	return instrumentList.get(instrumentIndex);
    }
    
    /**
     * Returns a Channel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param logicalChannelIndex Logical channel index.
     * @return See above.
     */
    private Channel getChannel(int imageIndex, int logicalChannelIndex)
    {
    	return getPixels(imageIndex, 0).getChannel(logicalChannelIndex); 
    }
    
    /**
     * Returns a LogicalChannel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param logicalChannelIndex Logical channel index.
     * @return See above.
     */
    private LogicalChannel getLogicalChannel(int imageIndex,
    		                                 int logicalChannelIndex)
    {
    	return getChannel(imageIndex, logicalChannelIndex).getLogicalChannel();
    }

    /**
     * Returns a Screen model object based on its indexes within the
     * OMERO data model.
     * @param screenIndex Screen index.
     * @return See above.
     */ 
    private Screen getScreen(int screenIndex)
    {
        return screenList.get(screenIndex);
    }
    
    /**
     * Returns a Plate model object based on its indexes within the
     * OMERO data model.
     * @param plateIndex Plate index.
     * @return See above.
     */ 
    private Plate getPlate(int plateIndex)
    {
        return plateList.get(plateIndex);
    }

    /**
     * Returns a Well model object based on its indexes within the
     * OMERO data model.
     * @param plateIndex Plate index.
     * @param wellIndex Well index
     * @return See above.
     */ 
    private Well getWell(int plateIndex, int wellIndex)
    {
        return wellList.get(wellIndex);
    }

    /**
     * Returns a Roi model object based on its indexes within the
     * OMERO data model.
     * @param plateIndex Plate index.
     * @param wellIndex Well index
     * @return See above.
     */
    private Roi getRoi(int imageIndex, int roiIndex)
    {
        return roiMap.get(imageIndex).get(roiIndex);
    }
    
    /**
     * Empty constructor for testing purposes.
     */
    public OMEROMetadataStore() {}
    
    /**
     * Creates a new instance.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     * @throws MetadataStoreException if the factory is null or there
     *             is another error instantiating required services.
     */
    public OMEROMetadataStore(ServiceFactory factory)
    	throws Exception
    {
        if (factory == null)
            throw new Exception("Factory argument cannot be null.");
        sf = factory;
        // Now initialize all our services
        initializeServices(sf);
    }
    
    /**
     * Private class used by constructor to initialize the services of the 
     * service factory.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     */
    private void initializeServices(ServiceFactory sf)
    {
        // Now initialize all our services
        iQuery = sf.getQueryService();
        iUpdate = sf.getUpdateService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        imageList = new LinkedHashMap<Integer, Image>();
        pixelsList = new LinkedHashMap<Integer, Pixels>();
        screenList = new LinkedHashMap<Integer, Screen>();
        plateList = new LinkedHashMap<Integer, Plate>();
        wellList = new LinkedHashMap<Integer, Well>();
        instrumentList = new LinkedHashMap<Integer, Instrument>();
        lsidMap = new LinkedHashMap<LSID, IObject>();
    }
    
    /**
     * The return value of save may not have sorted, ascending identities. This
     * method is in place to reorder a Pixels list based on the ordered and
     * saved Image IDs.
     * @param pixelsList Pixels list to be reordered in place.
     * @param imageIds Ordered list of Image IDs.
     */
    private void reorderPixelsListByImageIds(List<Pixels> pixelsList,
    		                                 List<Long> imageIds)
    {
    	Map<Long, Pixels> imageIdPixelsMap = new HashMap<Long, Pixels>();
    	for (Pixels pixels : pixelsList)
    	{
    		imageIdPixelsMap.put(pixels.getImage().getId(), pixels);
    	}
    	for (int i = 0; i < imageIds.size(); i++)
    	{
    		Long imageId = imageIds.get(i);
    		pixelsList.set(i, imageIdPixelsMap.get(imageId));
    	}
    }
    
    /**
     * Compares two enumerations by reference and by ID.
     * @param a First OMERO model object.
     * @param b Second OMERO model object.
     * @return <code>true</code> if <code>a == null && b == null</code>, 
     * <code>a == b</code> or <code>a.getId() == b.getId()</code>.
     */
    private static boolean compare(IEnum a, IEnum b)
    {
    	if (a == null && b == null)
    	{
    		return true;
    	}
    	if (a == null || b == null)
    	{
    		return false;
    	}
    	return a.getId() == b.getId();
    }
    
    /**
     * Compares two objects by reference existence and by value.
     * @param a First object.
     * @param b Second object.
     * @return <code>true</code> if <code>a == null && b == null</code> or
     * <code>a.equals(b)</code>.
     */
    private static boolean compare(Object a, Object b)
    {
    	if (a == null && b == null)
    	{
    		return true;
    	}
    	if (a == null || b == null)
    	{
    		return false;
    	}
    	return a.equals(b);
    }
    
    /**
     * Checks the entire object graph for sections that may be collapsed if
     * the data is derived from a Plate. Collapsible points:
     * <ul>
     *   <li>Image --> ObjectiveSettings</li>
     *   <li>Image --> Channel --> LogicalChannel --> LightSettings</li>
     *   <li>Image --> Channel --> LogicalChannel --> DetectorSettings</li>
     * </ul>
     */
    private void checkAndCollapseGraph()
    {
    	// Ensure we're working with an SPW data set.
    	if (plateList.size() == 0)
    	{
    		return;
    	}
    	// Collapse down ObjectiveSettings by uniqueness
    	Set<ObjectiveSettings> objectiveSettings =
    		new HashSet<ObjectiveSettings>();
    	Set<LightSettings> lightSettings = new HashSet<LightSettings>();
    	Set<DetectorSettings> detectorSettings = 
    		new HashSet<DetectorSettings>();
    	Set<LogicalChannel> logicalChannels = new HashSet<LogicalChannel>();
    	Pixels pixels;
    	Channel channel;
    	LogicalChannel lc;
    	for (Image image : imageList.values())
    	{
    		pixels = image.getPrimaryPixels();
    		image.setObjectiveSettings(
    				getUniqueObjectiveSettings(objectiveSettings, image));
    		for (int c = 0; c < pixels.sizeOfChannels(); c++)
    		{
    			channel = pixels.getChannel(c);
    			lc = channel.getLogicalChannel();
    			lc.setLightSourceSettings(
    					getUniqueLightSettings(lightSettings, lc));
    			lc.setDetectorSettings(
    					getUniqueDetectorSettings(detectorSettings, lc));
    			channel.setLogicalChannel(
    					getUniqueLogicalChannel(logicalChannels, lc));
    		}
    	}
    	log.info("Unique objective settings: " + objectiveSettings.size());
    	log.info("Unique light settings: " + lightSettings.size());
    	log.info("Unique detector settings: " + detectorSettings.size());
    	log.info("Unique logical channels: " + logicalChannels.size());
    }
    
    /**
     * Finds the matching unique settings for an image.
     * @param uniqueSettings Set of existing unique settings.
     * @param image Image to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getObjectiveSettings() == null</code>.
     */
    private ObjectiveSettings getUniqueObjectiveSettings(
    		Set<ObjectiveSettings> uniqueSettings, Image image)
    {
    	ObjectiveSettings s1 = image.getObjectiveSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (ObjectiveSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getCorrectionCollar(), s2.getCorrectionCollar())
    			&& compare(s1.getMedium(), s2.getMedium())
    			&& s1.getObjective() == s2.getObjective()
    			&& compare(s1.getRefractiveIndex(), s2.getRefractiveIndex()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique settings for a logical channel.
     * @param uniqueSettings Set of existing unique settings.
     * @param lc Logical channel to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getLightSourceSettings() == null</code>.
     */
    private LightSettings getUniqueLightSettings(
    		Set<LightSettings> uniqueSettings, LogicalChannel lc)
    {
    	LightSettings s1 = lc.getLightSourceSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (LightSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getAttenuation(), s2.getAttenuation())
    			&& s1.getLightSource() == s2.getLightSource()
    			&& s1.getMicrobeamManipulation()
    			   == s2.getMicrobeamManipulation()
    			&& compare(s1.getWavelength(), s2.getWavelength()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique settings for a logical channel.
     * @param uniqueSettings Set of existing unique settings.
     * @param lc Logical channel to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getDetectorSettings() == null</code>.
     */
    private DetectorSettings getUniqueDetectorSettings(
    		Set<DetectorSettings> uniqueSettings, LogicalChannel lc)
    {
    	DetectorSettings s1 = lc.getDetectorSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (DetectorSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getBinning(), s2.getBinning())
    			&& s1.getDetector() == s2.getDetector()
    			&& compare(s1.getGain(), s2.getGain())
    			&& compare(s1.getOffsetValue(), s2.getOffsetValue())
    			&& compare(s1.getReadOutRate(), s2.getReadOutRate())
    			&& compare(s1.getVoltage(), s2.getVoltage()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique logical channel.
     * @param uniqueChannels Set of existing unique logical channels.
     * @param lc Logical channel to compare for uniqueness.
     * @return Matched unique logical channel or <code>null</code> if
     * <code>lc == null</code>.
     */
    private LogicalChannel getUniqueLogicalChannel(
    		Set<LogicalChannel> uniqueChannels, LogicalChannel lc)
    {
    	if (lc == null)
    	{
    		return null;
    	}
    	for (LogicalChannel lc2 : uniqueChannels)
    	{
    		if (compare(lc.getMode(), lc2.getMode())
    			&& compare(lc.getContrastMethod(), lc2.getContrastMethod())
    			&& compare(lc.getIllumination(), lc2.getIllumination())
    			&& compare(lc.getPhotometricInterpretation(),
    					   lc2.getPhotometricInterpretation())
    			&& lc.getDetectorSettings() == lc2.getDetectorSettings()
    			&& compare(lc.getEmissionWave(), lc2.getEmissionWave())
    			&& compare(lc.getExcitationWave(), lc2.getExcitationWave())
    			&& lc.getFilterSet() == lc2.getFilterSet()
    			&& compare(lc.getFluor(), lc2.getFluor())
    			&& lc.getLightSourceSettings() == lc2.getLightSourceSettings()
    			&& compare(lc.getName(), lc2.getName())
    			&& compare(lc.getNdFilter(), lc2.getNdFilter())
    			&& lc.getOtf() == lc.getOtf()
    			&& compare(lc.getPinHoleSize(), lc2.getPinHoleSize())
    			&& compare(lc.getPockelCellSetting(), lc2.getPockelCellSetting())
    			&& compare(lc.getSamplesPerPixel(), lc2.getSamplesPerPixel())
    			&& lc.getSecondaryEmissionFilter()
    			   == lc2.getSecondaryEmissionFilter()
    			&& lc.getSecondaryExcitationFilter()
    			   == lc2.getSecondaryExcitationFilter())
    		{
    			return lc2;
    		}
    	}
    	uniqueChannels.add(lc);
    	return lc;
    }

    /**
     * Saves the current object graph to the database.
     * 
     * @return List of the Pixels objects with their attached object graphs
     * that have been saved.
     */
    public List<Pixels> saveToDB()
    {
    	// Check the entire object graph, optimizing and sections that may
    	// be collapsed.
    	checkAndCollapseGraph();
    	// Save the entire Image rooted graph using the "insert only"
    	// saveAndReturnIds(). DISABLED until we can find out what is causing
    	// the extreme memory usage on the graph reload.
    	StopWatch s1 = new CommonsLogStopWatch("omero.saveImportGraph");
    	Image[] imageArray = 
    		imageList.values().toArray(new Image[imageList.size()]);
    	IObject[] saved = iUpdate.saveAndReturnArray(imageArray);
    	s1.stop();
    	
    	// To conform loosely with the method contract, reload a subset of
    	// the original graph so that it may be manipulated by the caller.
    	//StopWatch s2 = new CommonsLogStopWatch("omero.buildReturnCollection");
    	//Parameters p = new Parameters();
    	//p.addIds(imageIdList);
    	/*
    	List<Pixels> toReturn = iQuery.findAllByQuery(
    			"select p from Pixels as p " +
    			"left outer join fetch p.channels as c " +
    			"left outer join fetch p.image as i " +
    			"left outer join fetch i.annotationLinks as a_link " +
    			"left outer join fetch a_link.child as a " +
    			"left outer join fetch a.file " +
    			"left outer join fetch i.wellSamples as ws " +
    			"left outer join fetch ws.well as w " +
    			"left outer join fetch w.plate as pl " +
    			"left outer join fetch pl.annotationLinks as pl_a_link " +
    			"left outer join fetch pl_a_link.child as pl_a " +
    			"left outer join fetch pl_a.file " +
    			"where i.id in (:ids)", p);
    	reorderPixelsListByImageIds(toReturn, imageIdList);
    	pixelsList = new LinkedHashMap<Integer, Pixels>();
    	*/
    	List<Pixels> toReturn = new ArrayList<Pixels>();
    	Image image;
    	Pixels pixels;
    	for (int i = 0; i < saved.length; i++)
    	{
    		image = (Image) saved[i];
    		pixels = image.getPrimaryPixels();
    		pixelsList.put(i, pixels);
    		toReturn.add(pixels);
    	}
    	//s2.stop();
   		return toReturn;
    }
    
    /**
     * Synchronize the minimum and maximum intensity values with those
     * specified by the client and save them in the DB.
     * @param imageChannelGlobalMinMax Minimums and maximums to update.
     */
    public void populateMinMax(double[][][] imageChannelGlobalMinMax)
    {
    	List<Channel> channelList = new ArrayList<Channel>();
    	double[][] channelGlobalMinMax;
    	double[] globalMinMax;
    	Channel channel;
    	StatsInfo statsInfo;
    	Pixels pixels, unloadedPixels;
    	for (int i = 0; i < imageChannelGlobalMinMax.length; i++)
    	{
    		channelGlobalMinMax = imageChannelGlobalMinMax[i];
    		pixels = pixelsList.get(i);
    		unloadedPixels = new Pixels(pixels.getId(), false);
    		for (int c = 0; c < channelGlobalMinMax.length; c++)
    		{
    			globalMinMax = channelGlobalMinMax[c];
    			channel = pixels.getChannel(c);
    			statsInfo = new StatsInfo();
    			statsInfo.setGlobalMin(globalMinMax[0]);
    			statsInfo.setGlobalMax(globalMinMax[1]);
    			channel.setStatsInfo(statsInfo);
    			channel.setPixels(unloadedPixels);
    			channelList.add(channel);
    		}
    	}
    	Channel[] toSave = channelList.toArray(new Channel[channelList.size()]);
    	iUpdate.saveArray(toSave);
    }
}