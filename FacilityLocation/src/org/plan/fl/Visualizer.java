/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Expression;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;

/**
 *
 * @author Jorge
 */
public class Visualizer {

    public static final String SHORT_ID_ATTR = "shortID";
    private Simulator sim;
    private SimpleFeatureCollection visibleProvidersFeatures;
    private SelectableMap map;
    private FeatureLayer gridLayer, providersLayer;
    private SimpleFeatureType facType;
    private SimpleFeatureBuilder featureBuilder;
    private HashMap<FacilityProvider, SimpleFeature> visibleProviders = new HashMap<FacilityProvider, SimpleFeature>();

    Visualizer(Simulator sim) {
        this.sim = sim;
        this.map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
        try {
            facType = DataUtilities.createType(
                    "Facility",
                    "*geom:Point:srid=4326,shortID:String");
            featureBuilder = new SimpleFeatureBuilder(facType);
        } catch (SchemaException ex) {
            Exceptions.printStackTrace(ex);
        }
        visibleProvidersFeatures = FeatureCollections.newCollection();
    }

    void clear() {
        map.getMapContent().removeLayer(gridLayer);
        map.getMapContent().removeLayer(providersLayer);
        clearProviders();
    }

    void init() {
        clear();
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        StyleBuilder styleBuilder = new StyleBuilder(styleFactory);

        // Grid layer
        Style style = styleBuilder.createStyle(
                styleBuilder.createPointSymbolizer(
                styleBuilder.createGraphic(null,
                styleBuilder.createMark(StyleBuilder.MARK_SQUARE, Color.LIGHT_GRAY),
                null, 1, 2, 0)));
        gridLayer = new FeatureLayer(sim.getGrid().getGridFeatures(), style);
        map.getMapContent().addLayer(gridLayer);
        map.getPane().layerChanged(new MapLayerListEvent(map.getMapContent(), gridLayer, 0, new MapLayerEvent(gridLayer, MapLayerEvent.DATA_CHANGED)));

        // Providers layer
        PointSymbolizer pointSymb = styleBuilder.createPointSymbolizer(
                styleBuilder.createGraphic(null,
                styleBuilder.createMark(StyleBuilder.MARK_CIRCLE,
                styleBuilder.createFill(Color.RED),
                styleBuilder.createStroke(Color.BLACK)),
                null, 1, 15, 0));
        Font font = styleBuilder.createFont("SansSerif", false, true, 12);
        TextSymbolizer textSymb = styleBuilder.createTextSymbolizer(Color.BLACK, font, SHORT_ID_ATTR);
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("Feature", new Symbolizer[]{pointSymb, textSymb});
        Style providersStyle = styleBuilder.createStyle();
        providersStyle.featureTypeStyles().add(fts);
        providersLayer = new FeatureLayer(visibleProvidersFeatures, providersStyle);
        map.getMapContent().addLayer(providersLayer);
    }
    private static final double D = 0.00008;
    private static final double[][] DISPLACEMENTS = new double[][]{{-D, -D}, {D, D}, {-D, D}, {D, -D}, {0, D}, {D, 0}, {-D, 0}, {0, -D}};

    void update() {
        clearProviders();
        Set<AbstractProvider> providers = sim.getProviders();
        HashSet<Point> occupiedPoints = new HashSet<Point>(providers.size());

        for (AbstractProvider p : providers) {
            FacilityProvider fc = (FacilityProvider) p;
            SimpleFeature f = null;
            Point pt = (Point) fc.position.getFeature().getDefaultGeometry();
            // procura posicao alternativa para nao ficar em cima do outro
            if (occupiedPoints.contains(pt)) {
                GeometryFactory fact = JTSFactoryFinder.getGeometryFactory(null);
                for (double[] displ : DISPLACEMENTS) {
                    Point newPt = fact.createPoint(new Coordinate(pt.getX() + displ[0], pt.getY() + displ[1]));
                    if (!occupiedPoints.contains(newPt)) {
                        pt = newPt;
                        break;
                    }
                }
            }
            occupiedPoints.add(pt);
            featureBuilder.add(pt);
            featureBuilder.add(fc.getShortID());
            f = featureBuilder.buildFeature(fc.getID());
            visibleProvidersFeatures.add(f);
        }
        map.getPane().layerChanged(new MapLayerListEvent(map.getMapContent(), providersLayer, 0, new MapLayerEvent(providersLayer, MapLayerEvent.DATA_CHANGED)));
        textReport();
    }

    void textReport() {
        for (AbstractProvider prov : sim.getProviders()) {
            FacilityProvider fc = (FacilityProvider) prov;
            System.out.println(fc.getShortID() + ": " + fc.getCurrentCapacity()
                    + " / (" + fc.getType().getMinCapacity() + "-" + fc.getType().getMaxCapacity() + ")");
        }
    }

    private void clearProviders() {
        visibleProviders.clear();
        visibleProvidersFeatures.clear();
        // TODO: dirty bug fix
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        featureBuilder.add(geometryFactory.createPoint(new Coordinate(0, 0))); // dirty fix
        featureBuilder.add("");
        SimpleFeature f = featureBuilder.buildFeature(null);
        visibleProvidersFeatures.add(f);
        // END bug fix  
    }
}
