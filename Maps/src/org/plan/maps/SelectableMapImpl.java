/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.styling.Fill;
import org.geotools.styling.Stroke;
import org.geotools.swing.action.MapAction;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jorge
 */
class SelectableMapImpl extends ColoredMapImpl implements SelectableMap, Lookup.Provider {

    private FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
    private Set<FeatureId> selected;
    private Set<FeatureId> selectable;
    private boolean restrictionsEnabled = false;
    private Lookup lookup;
    private InstanceContent selectedLookup;
    private LastFeature lastFeature;
    public static final String SELECTED_LAYER_KEY = "selected";
    public static final String ERROR_LAYER_KEY = "error";
    public static final Color SELECTED_COLOR = Color.YELLOW;

    SelectableMapImpl() {
	super();
	selectedLookup = new InstanceContent();
	lookup = new AbstractLookup(selectedLookup);
	selectable = new HashSet<FeatureId>();

	super.addToolbarAction(new SelectAction());
	super.addToolbarAction(new ClearSelectionAction());

	super.addColorLayer(SELECTED_LAYER_KEY, "Selected Features", Color.YELLOW, 0.5f, Color.ORANGE, 1, 1);

	Fill fill = styleBuilder.createFill(Color.WHITE, 0);
	Stroke stroke = styleBuilder.createStroke(Color.RED, 2, new float[]{10, 5});
	super.addColorLayer(ERROR_LAYER_KEY, "Error", fill, stroke);

	selected = super.getLayerFeatures(SELECTED_LAYER_KEY);
    }

    @Override
    protected int numTopLayers() {
	return 2;
    }

    @Override
    public Set<FeatureId> errorFeatures() {
	return super.getLayerFeatures(ERROR_LAYER_KEY);
    }

    @Override
    public void resetMap() {
	super.resetMap();
	clearSelectedFeatures();
	selectable.clear();
	enableRestrictions(false);
	errorFeatures().clear();
    }

    @Override
    public Lookup getLookup() {
	return lookup;
    }

    @Override
    public void enableRestrictions(boolean enable) {
	this.restrictionsEnabled = enable;
	if (restrictionsEnabled) {
	    Collection<? extends SimpleFeature> s = lookup.lookupAll(SimpleFeature.class);
	    for (SimpleFeature f : s) {
		if (!selectable.contains(f.getIdentifier())) {
		    selectedLookup.remove(f);
		    selected.remove(f.getIdentifier());
		}
	    }
	    update();
	}
    }

    @Override
    public Set<FeatureId> getRestrictions() {
	return selectable;
    }

    @Override
    public void clearSelectedFeatures() {
	Collection<? extends SimpleFeature> s = lookup.lookupAll(SimpleFeature.class);
	for (SimpleFeature f : s) {
	    selectedLookup.remove(f);
	}
	selected.clear();
	if (lastFeature != null) {
	    selectedLookup.remove(lastFeature);
	    lastFeature = null;
	}
	update();
    }

    private void selectFeatures(ReferencedEnvelope envelope, boolean positive) {
        for(Layer layer : mapContent.layers()) {
	    if (layer.isVisible() && layer.isSelected()) {
		FeatureSource source = layer.getFeatureSource();
		String geomName = source.getSchema().getGeometryDescriptor().getLocalName();
		Filter filter = filterFactory.intersects(
			filterFactory.property(geomName),
			filterFactory.literal(envelope));
		try {
		    FeatureIterator<SimpleFeature> iter = source.getFeatures(filter).features();
		    select(iter, positive);
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }            
        }
	update();
    }

    @Override
    public void selectFeatures(Set<FeatureId> feats, boolean positive) {
        for(Layer layer : mapContent.layers()) {
	    if (layer.isVisible() && layer.isSelected()) {
		FeatureSource source = layer.getFeatureSource();
		Filter filter = filterFactory.id(feats);
		try {
		    FeatureIterator<SimpleFeature> iter = source.getFeatures(filter).features();
		    select(iter, positive);
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }
	}
	update();
    }

    private void select(FeatureIterator<SimpleFeature> iter, boolean positive) {
	int justSelectedCount = 0;
	SimpleFeature justSelected = null;
	while (iter.hasNext()) {
	    SimpleFeature f = iter.next();
	    if (positive && (!restrictionsEnabled || selectable.contains(f.getIdentifier()))) {
		justSelected = f;
		justSelectedCount++;
		selected.add(f.getIdentifier());
		selectedLookup.add(f);
	    } else {
		selected.remove(f.getIdentifier());
		selectedLookup.remove(f);
		if (lastFeature != null && f.getID().equals(lastFeature.getFeature().getID())) {
		    selectedLookup.remove(lastFeature);
		    lastFeature = null;
		}
	    }
	}
	if (lastFeature != null) {
	    selectedLookup.remove(lastFeature);
	    lastFeature = null;
	}
	if (justSelectedCount == 1) {
	    lastFeature = new LastFeature(justSelected);
	    selectedLookup.add(lastFeature);
	    System.out.println("Map: last-feature " + lastFeature.getFeature().getID());
	}
	iter.close();
    }

    private class ClearSelectionAction extends MapAction {

	ClearSelectionAction() {
	    super.init(getPane(), "", "Reset Selection", null);
	    super.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("/org/plan/maps/resources/reset_selection.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    clearSelectedFeatures();
	}
    }

    private class SelectAction extends MapAction {

	SelectAction() {
	    super.init(getPane(), "", "Select Features", null);
	    super.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("/org/plan/maps/resources/select.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    getPane().setCursorTool(new SelectTool());
	}
    }

    private class SelectTool extends CursorTool {

	private Cursor cursor;
	private Point2D startDragPos;
	private boolean dragged;

	public SelectTool() {
	    super();
	    setMapPane(getPane());
	    cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	    startDragPos = new DirectPosition2D();
	    dragged = false;
	}

	@Override
	public void onMouseClicked(MapMouseEvent e) {
	    if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3) {
		return;
	    }
	    Point screenPos = e.getPoint();
	    Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);

	    AffineTransform screenToWorld = getPane().getScreenToWorldTransform();
	    Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
	    ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect,
		    getPane().getMapContent().getCoordinateReferenceSystem());

	    selectFeatures(bbox, e.getButton() == MouseEvent.BUTTON1);
	}

	@Override
	public void onMousePressed(MapMouseEvent ev) {
	    startDragPos = ev.getWorldPos();
	}

	@Override
	public void onMouseDragged(MapMouseEvent ev) {
	    dragged = true;
	}

	@Override
	public void onMouseReleased(MapMouseEvent ev) {
	    if (ev.getButton() != MouseEvent.BUTTON1 && ev.getButton() != MouseEvent.BUTTON3) {
		return;
	    }
	    DirectPosition2D endDragPos = ev.getWorldPos();
	    if (dragged && !endDragPos.equals(startDragPos)) {
		Envelope2D env = new Envelope2D();
		env.setFrameFromDiagonal(startDragPos, endDragPos);
		dragged = false;
		selectFeatures(new ReferencedEnvelope(env, env.getCoordinateReferenceSystem()), ev.getButton() == MouseEvent.BUTTON1);
	    }
	}

	@Override
	public Cursor getCursor() {
	    return cursor;
	}

	@Override
	public boolean drawDragBox() {
	    return true;
	}
    }
}
