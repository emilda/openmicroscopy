/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.testing;

import java.sql.Timestamp;

import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.display.ChannelBinding;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
import ome.model.enums.Family;
import ome.model.enums.Format;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import ome.model.stats.StatsInfo;

/**
 * these method serve as a both client and test data store. An object that has
 * no id is "new"; an object with an id is detached and can represent something
 * serialized from IQuery.
 * 
 * NOTE: this is a bit dangerous, causing model builds to fail sometimes. where
 * else could it live?
 */
public class ObjectFactory {

    public static OriginalFile createFile() {
        OriginalFile ofile = new OriginalFile();
        ofile.setName("testing");
        ofile.setPath("/dev/null");
        ofile.setSha1("abc");
        ofile.setSize(1L);
        ofile.setMimetype("text/plain");

        return ofile;
    }

    public static Thumbnail createThumbnails(Pixels p) {
        Thumbnail t = new Thumbnail();
        t.setMimeType("txt");
        t.setSizeX(1);
        t.setSizeY(1);
        p.addThumbnail(t);
        return t;
    }

    public static Pixels createPixelGraph(Pixels example) {

        Pixels p = new Pixels();
        PhotometricInterpretation pi = new PhotometricInterpretation();
        AcquisitionMode mode = new AcquisitionMode();
        PixelsType pt = new PixelsType();
        DimensionOrder dO = new DimensionOrder();
        Image i = new Image();
        Channel c = new Channel();
        LogicalChannel lc = new LogicalChannel();
        StatsInfo si = new StatsInfo();
        PlaneInfo pl = new PlaneInfo();

        if (example != null) {
            p.setId(example.getId());
            p.setVersion(example.getVersion());

            // everything else unloaded.
            pt.setId(example.getPixelsType().getId());
            pt.unload();
            dO.setId(example.getDimensionOrder().getId());
            dO.unload();
            i.setId(example.getImage().getId());
            i.unload();
            c.setId(example.getChannel(0).getId());
            c.unload();
            // Not needed but useful
            p.addPlaneInfo(example.iteratePlaneInfo().next());
            (p.iteratePlaneInfo().next()).unload();
        }

        else {

            mode.setValue("Wide-field");
            pi.setValue("RGB");

            pt.setValue("int8");

            dO.setValue("XYZTC");

            c.setPixels(p);
            lc.setPhotometricInterpretation(pi);

            // Not required but useful
            si.setGlobalMax(new Double(0.0));
            si.setGlobalMin(new Double(0.0));
            c.setLogicalChannel(lc);
            c.setStatsInfo(si);
            pl.setTheC(new Integer(0));
            pl.setTheZ(new Integer(0));
            pl.setTheT(new Integer(0));
            pl.setDeltaT(new Double(0.0));
            p.addPlaneInfo(pl);
            i.setName("test");
            i.setAcquisitionDate(new Timestamp(System.currentTimeMillis()));
            i.addPixels(p);

        }
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setPhysicalSizeX(1.0);
        p.setPhysicalSizeY(1.0);
        p.setPhysicalSizeZ(1.0);
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
        p.setPixelsType(pt);
        p.setDimensionOrder(dO);
        p.setPhysicalSizeX(new Double(1.0));
        p.setPhysicalSizeY(new Double(1.0));
        p.setPhysicalSizeZ(new Double(1.0));
        p.setImage(i);

        p.addChannel(c);

        return p;
    }

    public static ChannelBinding createChannelBinding() {
        // Prereqs for binding

        Family family = new Family();
        family.setValue("linear");

        ChannelBinding binding = new ChannelBinding();
        binding.setActive(Boolean.FALSE);
        binding.setCoefficient(new Double(1));
        binding.setAlpha(new Integer(1));
        binding.setBlue(new Integer(1));
        binding.setGreen(new Integer(1));
        binding.setRed(new Integer(1));
        binding.setFamily(family);
        binding.setInputEnd(new Double(1.0));
        binding.setInputStart(new Double(1.0));
        binding.setNoiseReduction(Boolean.FALSE);

        return binding;
    }

    public static RenderingDef createRenderingDef() {
        // Prereqs for RenderingDef
        RenderingModel model = new RenderingModel();
        model.setValue("rgb");

        QuantumDef qdef = new QuantumDef();
        qdef.setBitResolution(new Integer(1));
        qdef.setCdEnd(new Integer(1));
        qdef.setCdStart(new Integer(1));

        RenderingDef def = new RenderingDef();
        def.setDefaultT(new Integer(1));
        def.setDefaultZ(new Integer(1));
        def.setModel(model);
        def.setPixels(ObjectFactory.createPixelGraph(null));
        def.setQuantization(qdef);

        return def;
    }

    public static PlaneSlicingContext createPlaneSlicingContext() {
        PlaneSlicingContext enhancement = new PlaneSlicingContext();
        enhancement.setConstant(Boolean.FALSE);
        enhancement.setLowerLimit(new Integer(1));
        enhancement.setPlanePrevious(new Integer(1));
        enhancement.setPlaneSelected(new Integer(1));
        enhancement.setUpperLimit(new Integer(1));

        return enhancement;

    }
}