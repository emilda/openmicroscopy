/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.projection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import ome.annotations.PermitAll;
import ome.annotations.RolesAllowed;
import ome.api.IPixels;
import ome.api.IProjection;
import ome.api.ServiceInterface;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;
import ome.io.nio.PixelsService;
import ome.logic.AbstractLevel2Service;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.stats.StatsInfo;
import ome.services.OmeroOriginalFileMetadataProvider;

/**
 * Implements projection functionality for Pixels sets as declared in {@link
 * IProjection}.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta3.1
 */
@Transactional(readOnly = true)
public class ProjectionBean extends AbstractLevel2Service implements IProjection
{
    /** The logger for this class. */
    private static Log log = LogFactory.getLog(ProjectionBean.class);
    
    /** Reference to the service used to retrieve the pixels metadata. */
    protected transient IPixels iPixels;
    
    /** Reference to the service used to retrieve the pixels data. */
    protected transient PixelsService pixelsService;
    
    /**
     * Returns the interface this implementation is for.
     * @see AbstractLevel2Service#getServiceInterface()
     */
    public Class<? extends ServiceInterface> getServiceInterface()
    {
        return IProjection.class;
    }
    
    /**
     * IPixels bean injector. For use during configuration. Can only be called 
     * once.
     */
    public void setIPixels(IPixels iPixels)
    {
        getBeanHelper().throwIfAlreadySet(this.iPixels, iPixels);
        this.iPixels = iPixels;
    }
    
    /**
     * PixelsService bean injector. For use during configuration. Can only be 
     * called once.
     */
    public void setPixelsService(PixelsService pixelsService)
    {
        getBeanHelper().throwIfAlreadySet(this.pixelsService, pixelsService);
        this.pixelsService = pixelsService;
    }
    
    /* (non-Javadoc)
     * @see ome.api.IProjection#projectStack(long, ome.model.enums.PixelsType, int, int, int, int, int, int)
     */
    @RolesAllowed("user")
    public byte[] projectStack(long pixelsId, PixelsType pixelsType,
                               int algorithm, int timepoint, int channelIndex, 
                               int stepping, int start, int end)
    {
        ProjectionContext ctx = new ProjectionContext();
        ctx.pixels = iQuery.get(Pixels.class, pixelsId);
        OriginalFileMetadataProvider metadataProvider =
        	new OmeroOriginalFileMetadataProvider(iQuery);
        PixelBuffer pixelBuffer = 
        	pixelsService.getPixelBuffer(ctx.pixels, metadataProvider, false);
        if (pixelsType == null)
        {
            pixelsType = ctx.pixels.getPixelsType();
        }
        else
        {
            pixelsType = iQuery.get(PixelsType.class, pixelsType.getId());
        }
        try
        {
            ctx.planeSizeInPixels = 
                ctx.pixels.getSizeX() * ctx.pixels.getSizeY();
            int planeSize = 
                ctx.planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
            byte[] buf = new byte[planeSize];
            ctx.from = pixelBuffer.getStack(channelIndex, timepoint);
            ctx.to = new PixelData(pixelsType, ByteBuffer.wrap(buf));

            switch (algorithm)
            {
                case IProjection.MAXIMUM_INTENSITY:
                {
                    projectStackMax(ctx, stepping, start, end, false);
                    break;
                }
                case IProjection.MEAN_INTENSITY:
                {
                    projectStackMean(ctx, stepping, start, end, false);
                    break;
                }
                case IProjection.SUM_INTENSITY:
                {
                    projectStackSum(ctx, stepping, start, end, false);
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException(
                            "Unknown algorithm: " + algorithm);
                }
            }
            return buf;
        }
        catch (IOException e)
        {
            String error = String.format(
                    "I/O error retrieving stack C=%d T=%d: %s",
                    channelIndex, timepoint, e.getMessage());
            log.error(error, e);
            throw new ResourceError(error);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            String error = String.format(
                    "C=%d or T=%d out of range for Pixels Id %d: %s",
                    channelIndex, timepoint, ctx.pixels.getId(), e.getMessage());
            log.error(error, e);
            throw new ValidationException(error);
        }
        finally
        {
            try
            {
                pixelBuffer.close();
            }
            catch (IOException e)
            {
                log.error("Buffer did not close successfully.", e);
                throw new ResourceError(
                        e.getMessage() + " Please check server log.");
            }
        }
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#projectPixels(long, ome.model.enums.PixelsType, int, int, int, java.util.List, int, int, int, java.lang.String)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long projectPixels(long pixelsId, PixelsType pixelsType, 
                              int algorithm, int tStart, int tEnd, 
                              List<Integer> channels, int stepping,
                              int zStart, int zEnd, String name)
    {
        // First, copy and resize our image with sizeZ = 1.
        ProjectionContext ctx = new ProjectionContext();
        ctx.pixels = iQuery.get(Pixels.class, pixelsId);
        Image image = ctx.pixels.getImage();
        name = name == null? image.getName() + " Projection" : name;
        long newImageId = 
            iPixels.copyAndResizeImage(image.getId(), null, null, 1, null,
                                       channels, name, false);
        Image newImage = iQuery.get(Image.class, newImageId);
        Pixels newPixels = newImage.getPixels(0);
        if (pixelsType == null)
        {
            pixelsType = ctx.pixels.getPixelsType();
        }
        else
        {
            pixelsType = iQuery.get(PixelsType.class, pixelsType.getId());
        }
        newPixels.setPixelsType(pixelsType);
        
        // Project each stack for each channel and each timepoint in the
        // entire image, copying into the pixel buffer the projected pixels.
        OriginalFileMetadataProvider metadataProvider =
        	new OmeroOriginalFileMetadataProvider(iQuery);
        PixelBuffer sourceBuffer = 
        	pixelsService.getPixelBuffer(ctx.pixels, metadataProvider, false);
        PixelBuffer destinationBuffer = 
            pixelsService.getPixelBuffer(newPixels, metadataProvider, true);
        ctx.planeSizeInPixels = ctx.pixels.getSizeX() * ctx.pixels.getSizeY();
        int planeSize = 
            ctx.planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
        byte[] buf = new byte[planeSize];
        ctx.to = new PixelData(pixelsType, ByteBuffer.wrap(buf));
        int newC = 0;
        try
        {
            for (Integer c : channels)
            {
                ctx.minimum = Double.MAX_VALUE;
                ctx.maximum = Double.MIN_VALUE;
                for (int t = tStart; t <= tEnd; t++)
                {
                    try
                    {
                        ctx.from = sourceBuffer.getStack(c, t);
                        switch (algorithm)
                        {
                            case IProjection.MAXIMUM_INTENSITY:
                            {
                                projectStackMax(ctx, stepping, zStart, zEnd, true);
                                break;
                            }
                            case IProjection.MEAN_INTENSITY:
                            {
                                projectStackMean(ctx, stepping, zStart, zEnd, true);
                                break;
                            }
                            case IProjection.SUM_INTENSITY:
                            {
                                projectStackSum(ctx, stepping, zStart, zEnd, true);
                                break;
                            }
                            default:
                            {
                                throw new IllegalArgumentException(
                                        "Unknown algorithm: " + algorithm);
                            }
                        }
                        destinationBuffer.setPlane(buf, 0, newC, t);
                    }
                    catch (IOException e)
                    {
                        String error = String.format(
                                "I/O error retrieving stack C=%d T=%d: %s",
                                c, t, e.getMessage());
                        log.error(error, e);
                        throw new ResourceError(error);
                    }
                    catch (DimensionsOutOfBoundsException e)
                    {
                        String error = String.format(
                                "C=%d or T=%d out of range for Pixels Id %d: %s",
                                c, t, ctx.pixels.getId(), e.getMessage());
                        log.error(error, e);
                        throw new ValidationException(error);
                    }
                }
                // Handle the change of minimum and maximum for this channel.
                Channel channel = newPixels.getChannel(newC);
                StatsInfo si = new StatsInfo();
                si.setGlobalMin(ctx.minimum);
                si.setGlobalMax(ctx.maximum);
                channel.setStatsInfo(si);
                // Set our methodology
                newPixels.setMethodology(
                        IProjection.METHODOLOGY_STRINGS[algorithm]);
                newC++;
            }
        }
        finally
        {
            try
            {
                sourceBuffer.close();
                destinationBuffer.close();
            }
            catch (IOException e)
            {
                log.error("Buffer did not close successfully.", e);
                throw new ResourceError(
                        e.getMessage() + " Please check server log.");
            }
        }
        newImage = iUpdate.saveAndReturnObject(newImage);
        return newImage.getId();
    }
    
    /**
     * Projects a stack based on the maximum intensity at each XY coordinate.
     * @param ctx The context of our projection.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMax(ProjectionContext ctx, int stepping,
                                 int start, int end, boolean doMinMax)
    {
        int currentPlaneStart;
        double projectedValue, stackValue;
        double minimum = ctx.minimum;
        double maximum = ctx.maximum;
        for (int i = 0; i < ctx.planeSizeInPixels; i++)
        {
            projectedValue = 0;
            for (int z = start; z <= end; z += stepping)
            {
                currentPlaneStart = ctx.planeSizeInPixels * z;
                stackValue = ctx.from.getPixelValue(currentPlaneStart + i);
                if (stackValue > projectedValue)
                {
                    projectedValue = stackValue;
                }
            }
            ctx.to.setPixelValue(i, projectedValue);
            if (doMinMax)
            {
                minimum = projectedValue < minimum? projectedValue : minimum;
                maximum = projectedValue > maximum? projectedValue : maximum;
            }
        }
        ctx.minimum = minimum;
        ctx.maximum = maximum;
    }
    
    /**
     * Projects a stack based on the mean intensity at each XY coordinate.
     * @param from The raw pixel data from the stack to project from.
     * @param ctx The context of our projection.
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMean(ProjectionContext ctx, int stepping, 
                                  int start, int end, boolean doMinMax)
    {
        projectStackMeanOrSum(ctx, stepping, start, end, true, doMinMax);
    }
    
    /**
     * Projects a stack based on the sum intensity at each XY coordinate.
     * @param ctx The context of our projection.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackSum(ProjectionContext ctx, int stepping, 
                                 int start, int end, boolean doMinMax)
    {
        projectStackMeanOrSum(ctx, stepping, start, end, false, doMinMax);
    }
    
    /**
     * Projects a stack based on the sum intensity at each XY coordinate with
     * the option to also average the sum intensity.
     * @param ctx The context of our projection.
     * @param pixelsType The destination Pixels type. If <code>null</code>, the
     * source Pixels set pixels type will be used.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @param mean Whether or not we're performing an average post sum
     * intensity projection.
     * @param doMinMax Whether or not to calculate the minimum and maximum of
     * the projected pixel data.
     */
    private void projectStackMeanOrSum(ProjectionContext ctx, int stepping,
                                       int start, int end, 
                                       boolean mean, boolean doMinMax)
    {
        double planeMaximum = ctx.to.getMaximum();

        int currentPlaneStart;
        double projectedValue, stackValue;
        double minimum = ctx.minimum;
        double maximum = ctx.maximum;
        for (int i = 0; i < ctx.planeSizeInPixels; i++)
        {
            projectedValue = 0;
            int projectedPlaneCount = 0;
            for (int z = start; z < end; z += stepping)
            {
                currentPlaneStart = ctx.planeSizeInPixels * z;
                stackValue = ctx.from.getPixelValue(currentPlaneStart + i);
                projectedValue += stackValue;
                projectedPlaneCount++;
            }
            if (mean)
            {
                projectedValue = projectedValue / projectedPlaneCount;
            }
            if (projectedValue > planeMaximum)
            {
                projectedValue = planeMaximum;
            }
            ctx.to.setPixelValue(i, projectedValue);
            if (doMinMax)
            {
                minimum = projectedValue < minimum? projectedValue : minimum;
                maximum = projectedValue > maximum? projectedValue : maximum;
            }
        }
        ctx.minimum = minimum;
        ctx.maximum = maximum;
    }
    
    /**
     * Stores the context of a projection operation.
     * 
     * Class is static to prevent any instances from holding onto
     * {@link ProjectionBean} instances.
     *
     * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
     * @since OMERO-Beta3.1
     */
    private static class ProjectionContext
    {
        /** The Pixels set we're currently working on. */
        public Pixels pixels;
        
        /** Count of the number of pixels per plane for <code>pixels</code>. */
        public int planeSizeInPixels;
        
        /** Current minimum for the projected pixel data. */
        public double minimum = Double.MAX_VALUE;
        
        /** Current maximum for the projected pixel data. */
        public double maximum = Double.MIN_VALUE;
        
        /** The raw pixel data from the stack to project from. */
        public PixelData from;
        
        /** The raw pixel data buffer to project into. */
        public PixelData to;
    }
}