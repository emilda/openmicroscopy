/*
 * org.openmicroscopy.shoola.util.roi.io.InputServerStrategy
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.io;



//Java imports
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_BOLD;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.FONT_ITALIC;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.STROKE_CAP;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH;

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.geom.BezierPath.Node;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.io.util.SVGTransform;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.EllipseData;
import pojos.LineData;
import pojos.PointData;
import pojos.ROIData;
import pojos.RectangleData;
import pojos.ShapeData;
import pojos.PolygonData;
import pojos.MaskData;
import pojos.PolylineData;
import pojos.ShapeSettingsData;
import pojos.TextData;


/** 
 * Transforms the ROI server into the corresponding UI objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class InputServerStrategy
{

	/** Identifies the transformation attribute. */
	public final static AttributeKey<AffineTransform> TRANSFORM = 
		  new AttributeKey<AffineTransform>("transform", null, true);
	
	/**
	 * The map of the default values of each object along with the keys used.
	 */
	private final static Map<AttributeKey, Object>	DEFAULT_ATTRIBUTES;
	static
	{
		DEFAULT_ATTRIBUTES = new HashMap<AttributeKey, Object>();
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.FILL_COLOR,
				ShapeSettingsData.DEFAULT_FILL_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.STROKE_COLOR,
			ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.TEXT_COLOR,
			IOConstants.DEFAULT_TEXT_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.FONT_SIZE, 
				ShapeSettingsData.DEFAULT_FONT_SIZE);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.FONT_BOLD, 
				ShapeSettingsData.DEFAULT_FONT_STYLE);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.STROKE_WIDTH, 
				ShapeSettingsData.DEFAULT_STROKE_WIDTH);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.TEXT, "Text");
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
			IOConstants.DEFAULT_MEASUREMENT_TEXT_COLOUR);
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.SHOWMEASUREMENT, 
				Boolean.valueOf(false));
		DEFAULT_ATTRIBUTES.put(MeasurementAttributes.SHOWTEXT, 
				Boolean.valueOf(false));
	}
	
	/** Holds the ROIs which have been created. */
	private List<ROI>			roiList;
	
	/** The ROIComponent. */
	private ROIComponent		component;
	
	/**
	 * Adds any missing basic attributes from the default attributes map, 
	 * to the figure.
	 * 
	 * @param figure The figure to handle.
	 */
	private void addMissingAttributes(ROIFigure figure)
	{
		Map<AttributeKey, Object> attributes=figure.getAttributes();
		Iterator<AttributeKey> i = DEFAULT_ATTRIBUTES.keySet().iterator();
		AttributeKey key;
		while (i.hasNext())
		{
			key = i.next();
			if (!attributes.containsKey(key)) 
				key.set(figure, DEFAULT_ATTRIBUTES.get(key));
		}
	}
	
	/**
	 * Transforms a server ROI into its UI representation.
	 * 
	 * @param roi The object to transform.
	 * @param readOnly The object is readOnly.
	 * @return See above.
	 */
	private ROI createROI(ROIData roi, boolean readOnly)
		throws NoSuchROIException, ROICreationException
	{
		long id = roi.getId();
		//ROI newROI = component.createROI(id, readOnly);
		ROI newROI = component.createROI(id, id <= 0);
		newROI.setOwnerID(roi.getOwner().getId());
		if (roi.getNamespaces().size() != 0) {
			String s = roi.getNamespaces().get(0);
			newROI.setAnnotation(AnnotationKeys.NAMESPACE, s);
			if (roi.getNamespaceKeywords().size() != 0)
				newROI.setAnnotation(AnnotationKeys.KEYWORDS, 
						UIUtilities.listToCSV(roi.getNamespaceKeywords(s)));
		}
			
		if (roi.getNamespaceKeywords().size() != 0)
			newROI.setAnnotation(AnnotationKeys.KEYWORDS, 
					UIUtilities.listToCSV(roi.getNamespaceKeywords(
							roi.getNamespaces().get(0))));
		ROIShape shape;
		ShapeData shapeData;
		Iterator<List<ShapeData>> i = roi.getIterator();
		List<ShapeData> list;
		Iterator<ShapeData> j;
		Coord3D c;
		while (i.hasNext()) {
			list = (List<ShapeData>) i.next();
			j = list.iterator();
			while (j.hasNext()) {
				shapeData = (ShapeData) j.next();
				shape = createROIShape(shapeData, newROI, readOnly);
				if (shape != null) {
					shape.getFigure().setMeasurementUnits(
							component.getMeasurementUnits());
					c = shape.getCoord3D();
					if (c != null) {
						if (!component.containsShape(newROI.getID(), c)) {
							component.addShape(newROI.getID(), c, shape);
						}
					}
				}
			}
		}
		return newROI;
	}
	
	/**
	 * Transforms the shape into its corresponding the UI object.
	 * 
	 * @param data 	The object to transform.
	 * @param roi	The UI ROI hosting the newly created shape.
	 * @param readOnly The object is readOnly.
	 * @return See above.
	 */
	private ROIShape createROIShape(ShapeData data, ROI roi, boolean readOnly)
	{
		int z = data.getZ();
		int t = data.getT();
		if (z < 0 || t < 0) return null;
		Coord3D coord = new Coord3D(z, t);
		ROIFigure fig = createROIFigure(data, readOnly);
		// Check that the parent element is not a text element, as they have not
		// got any other text associated with them.
		addMissingAttributes(fig);
		ROIShape shape = new ROIShape(roi, coord, fig, fig.getBounds());
		shape.setROIShapeID(data.getId());
		return shape;
	}
	
	/**
	 * Creates a figure corresponding to the passed shape.
	 * 
	 * @param shape The shape to transform.
 	 * @param readOnly The object is readOnly.
	 * @return See above.
	 */
	private ROIFigure createROIFigure(ShapeData shape, boolean readOnly)
	{
		ROIFigure fig = createROIFigure(shape);
		fig.setReadOnly(readOnly);
		return fig;
	}
	
	/**
	 * Creates a figure corresponding to the passed shape.
	 * 
	 * @param shape The shape to transform.
	 * @return See above.
	 */
	private ROIFigure createROIFigure(ShapeData shape)
	{
		if (shape instanceof RectangleData) {
			return createRectangleFigure((RectangleData) shape);			
		} else if (shape instanceof EllipseData) {
			return createEllipseFigure((EllipseData) shape);
		} else if (shape instanceof LineData) {
			return createLineFigure((LineData) shape);			
		} else if (shape instanceof PointData) {
			return createPointFigure((PointData) shape);	
		} else if (shape instanceof PolylineData) {
			return createPolyOrlineFigure((PolylineData) shape);			
		} else if (shape instanceof PolygonData) {
			return createPolygonFigure((PolygonData) shape);			
		} else if (shape instanceof MaskData) {
			return createMaskFigure((MaskData) shape);			
		} else if (shape instanceof TextData) {
			return createTextFigure((TextData) shape);			
		}
		return null;
	}
	
	/**
	 * Transforms the passed ellipse into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasureEllipseFigure createEllipseFigure(EllipseData data)
	{
		double cx = data.getX();
		double cy = data.getY();
		double rx = data.getRadiusX();
		double ry = data.getRadiusY();
		
		double x = cx-rx;
		double y = cy-ry;
		double width = rx*2d;
		double height = ry*2d;
		MeasureEllipseFigure fig = new MeasureEllipseFigure(data.getText(), 
				x, y, width, height, data.isReadOnly(), 
					data.isClientObject());
		fig.setEllipse(x, y, width, height);
		addShapeSettings(fig, data.getShapeSettings());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			
		}
		
		return fig;
	}
	
	/**
	 * Transforms the passed ellipse into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasurePointFigure createPointFigure(PointData data)
	{
		double r = 5;
		double x = data.getX()-r;
		double y = data.getY()-r;
		
		MeasurePointFigure fig = new MeasurePointFigure(data.getText(), x, y, 
				2*r, 2*r, data.isReadOnly(), data.isClientObject());

		addShapeSettings(fig, data.getShapeSettings());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		return fig;
	}
	
	/**
	 * Transforms the passed textData into its UI corresponding object.
	 * 
	 * @param data The ellipse to transform.
	 * @return See above.
	 */
	private MeasureTextFigure createTextFigure(TextData data)
	{
		double x = data.getX();
		double y = data.getY();
		
		MeasureTextFigure fig = new MeasureTextFigure(x, y, 
					data.isReadOnly(), data.isClientObject());
		fig.setText(data.getText());
		addShapeSettings(fig, data.getShapeSettings());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		return fig;
	}
	
	/**
	 * Transforms the passed rectangle into its UI corresponding object.
	 * 
	 * @param data The rectangle to transform.
	 * @return See above.
	 */
	private MeasureRectangleFigure createRectangleFigure(RectangleData data)
	{
		double x = data.getX();
		double y = data.getY();
		double width = data.getWidth();
		double height = data.getHeight();
		
		MeasureRectangleFigure fig = new MeasureRectangleFigure(x, y, width, 
				height, data.isReadOnly(), data.isClientObject());
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {}
		
		return fig;
	}
	
	/**
	 * Transforms the passed mask into its UI corresponding object.
	 * 
	 * @param data The mask to transform.
	 * @return See above.
	 */
	private MeasureMaskFigure createMaskFigure(MaskData data)
	{
		
		double x = data.getX();
		double y = data.getY();
		double width = data.getWidth();
		double height = data.getHeight();
		BufferedImage mask = data.getMaskAsBufferedImage();
		MeasureMaskFigure fig = new MeasureMaskFigure(x, y, width, 
				height, mask, data.isReadOnly(), data.isClientObject());
		fig.setVisible(true);
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return fig;
	}
	
	
	/**
	 * Transforms the passed line into its UI corresponding object.
	 * 
	 * @param data The line to transform.
	 * @return See above.
	 */
	private MeasureLineFigure createLineFigure(LineData data)
	{
		double x1 = data.getX1();
		double y1 = data.getY1();
		double x2 = data.getX2();
		double y2 = data.getY2();
		
		MeasureLineFigure fig = new MeasureLineFigure(data.isReadOnly(), 
				data.isClientObject());
		fig.removeAllNodes();
		fig.addNode(new Node(new Double(x1), new Double(y1)));
		fig.addNode(new Node(new Double(x2), new Double(y2)));
		
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		return fig;
	}
	

	/**
	 * Transforms the polygon into its UI corresponding object.
	 * 
	 * @param data The polygon to transform.
	 * @return See above.
	 */
	private MeasureBezierFigure createPolygonFigure(PolygonData data)
	{
		MeasureBezierFigure fig = new MeasureBezierFigure(false, data.isReadOnly(),
				data.isClientObject());
		List<Point2D.Double> points = data.getPoints();
		List<Point2D.Double> points1 = data.getPoints1();
		List<Point2D.Double> points2 = data.getPoints2();
		List<Integer> mask = data.getMaskPoints();
		for (int i = 0; i < points.size(); i++)
		{
			fig.addNode(new Node(mask.get(i), points.get(i), points1.get(i), 
					points2.get(i)));
		}
		
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		fig.setClosed(true);		
		return fig;
	}
	
	/**
	 * Test to see if the polyline object passed in is actually a line/multi
	 * segment line or a polyline.
	 * 
	 * @param data The polyline to transform.
	 * @return See above.
	 */
	private ROIFigure createPolyOrlineFigure(PolylineData data)
	{
		List<Integer> mask = data.getMaskPoints();
		
		boolean line = true;
		for (int i = 0 ; i < mask.size(); i++)
		{
			if (mask.get(i) != 0)
				line = false;
		}
		
		if (line) return createLineFromPolylineFigure(data);
		else return createPolylineFromPolylineFigure(data);
	}	
		
	/**
	 * Transforms the passed polyline into a line figure are it has only move
	 * to operations.
	 * 
	 * @param data The polyline to transform.
	 * @return See above.
	 */
	private ROIFigure createLineFromPolylineFigure(PolylineData data)
	{
		List<Point2D.Double> points = data.getPoints();
		MeasureLineFigure fig = new MeasureLineFigure(data.isReadOnly(), 
				data.isClientObject());
		fig.removeAllNodes();
		
		for (int i = 0; i < points.size(); i++)
			fig.addNode(new Node(points.get(i)));
		
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}	
		return fig;
	}
	
	/**
	 * Transforms the passed polyline into a Polyline shape.
	 * 
	 * @param data The polyline to transform.
	 * @return See above.
	 */
	private ROIFigure createPolylineFromPolylineFigure(PolylineData data)
	{
		List<Point2D.Double> points = data.getPoints();
		List<Point2D.Double> points1 = data.getPoints1();
		List<Point2D.Double> points2 = data.getPoints2();
		List<Integer> mask = data.getMaskPoints();
		MeasureBezierFigure fig = new MeasureBezierFigure(false, 
				data.isReadOnly(), data.isClientObject());
	
		for (int i=0; i<points.size(); i++)
		{
			Node newNode = new Node(mask.get(i), points.get(i), 
					points1.get(i), points2.get(i));
			fig.addNode(newNode);
		}
		
		addShapeSettings(fig, data.getShapeSettings());
		fig.setText(data.getText());
		AffineTransform transform;
		try {
			transform = SVGTransform.toTransform(data.getTransform());
			TRANSFORM.set(fig, transform);
		} catch (IOException e) {
		}	
		return fig;
	}
	
	/**
	 * Adds the settings to the figure.
	 * 
	 * @param figure	The figure to handle.
	 * @param data		The settings to set.
	 */
	private void addShapeSettings(ROIFigure figure, ShapeSettingsData data)
	{
		STROKE_WIDTH.set(figure, data.getStrokeWidth());
		STROKE_COLOR.set(figure, data.getStrokeColor());
		FILL_COLOR.set(figure, data.getFillColor());
		FONT_FACE.set(figure, data.getFont());
		FONT_SIZE.set(figure, new Double(data.getFontSize()));
		FONT_ITALIC.set(figure, data.isFontItalic());
		FONT_BOLD.set(figure, data.isFontBold());
		STROKE_CAP.set(figure, data.getLineCap());
	}
	
	/** Creates a new instance. */
	InputServerStrategy()
	{
		roiList = new ArrayList<ROI>();
	}
	
	/**
	 * Converts the server ROIs and adds them to <code>ROIComponent</code>.
	 * 
	 * @param rois The ROIs to convert.
	 * @param component ROIComponent.
	 * @param userID The identifier of the user.
	 * @return See above.
	 * @throws ROICreationException if ROI cannot be created.
	 * @throws NoSuchROIException if there is an error creating line connection 
	 * figure.
	 */
	List<ROI> readROI(Collection rois, ROIComponent component, boolean readOnly,
			long userID)
			throws ROICreationException, NoSuchROIException
	{
		if (component == null)
			throw new IllegalArgumentException("No component.");
		this.component = component;
		Iterator i = rois.iterator();
		Object o;
		ROIData roi;
		boolean r;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof ROIData) {
				roi = (ROIData) o;
				r = readOnly;
				if (!readOnly) {
					if (roi.getOwner().getId() != userID) 
						r = true;
				}
				roiList.add(createROI(roi, r));
			}
		}
		return roiList;
	}
	
}
