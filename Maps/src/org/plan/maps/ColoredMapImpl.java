package org.plan.maps;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.action.InfoAction;
import org.geotools.swing.action.PanAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jorge
 */
public class ColoredMapImpl extends MapPanel implements ColoredMap {

    public static final String DEFAULT_LAYER_KEY = "default";
    protected MapContent mapContent;
    private Style polyStyle, lineStyle, pointStyle;
    private HashMap<String, ColorLayer> colorLayers;
    private LinkedList<String> layerOrder;
    protected StyleBuilder styleBuilder;
    private StyleFactory styleFactory;
    private FilterFactory2 filterFactory;
    private LinkedList<File> layerFiles = new LinkedList<File>();

    ColoredMapImpl() {
        super();
        styleFactory = CommonFactoryFinder.getStyleFactory(null);
        styleBuilder = new StyleBuilder(styleFactory);
        filterFactory = CommonFactoryFinder.getFilterFactory2(null);
        colorLayers = new HashMap<String, ColorLayer>();
        layerOrder = new LinkedList<String>();

        polyStyle = styleBuilder.createStyle();
        polyStyle.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());
        lineStyle = styleBuilder.createStyle();
        lineStyle.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());
        pointStyle = styleBuilder.createStyle();
        pointStyle.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());

        this.addColorLayer(DEFAULT_LAYER_KEY, "Default", new Color(240, 240, 240), 1, Color.LIGHT_GRAY, 1, 1);
        ColorLayer c = colorLayers.get(DEFAULT_LAYER_KEY);
        c.polygonRule.setElseFilter(true);
        c.lineRule.setElseFilter(true);
        c.pointRule.setElseFilter(true);

        initMap();
        initToolbar();
    }

    private void initToolbar() {
        addToolbarAction(new ZoomInAction(getPane()));
        addToolbarAction(new ZoomOutAction(getPane()));
        addToolbarAction(new PanAction(getPane()));
        addToolbarAction(new InfoAction(getPane()));
        addToolbarAction(new ResetAction(getPane()));
    }

    private void initMap() {
        mapContent = new MapContent();
        mapContent.setTitle("Map");
        getPane().setMapContent(mapContent);
    }

    @Override
    public void addFeatureLayer(File file) throws IOException {
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource source = store.getFeatureSource();
        FeatureCollection<SimpleFeatureType, SimpleFeature> memColl = FeatureCollections.newCollection();
        memColl.addAll(source.getFeatures());
        layerFiles.add(file);

        SimpleFeatureType schema = (SimpleFeatureType) source.getSchema();
        Class geomType = schema.getGeometryDescriptor().getType().getBinding();
        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            mapContent.addLayer(new FeatureLayer(memColl, polyStyle));
        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            mapContent.addLayer(new FeatureLayer(memColl, lineStyle));
        } else {
            mapContent.addLayer(new FeatureLayer(memColl, pointStyle));
        }

        getPane().reset();
    }

    @Override
    public void addColorLayer(String key, String descr, Color color) {
        addColorLayer(key, descr, color, 1, Color.BLACK, 1, 1);
    }

    @Override
    public final void addColorLayer(String key, String descr, Color fillColor, float fillOpacity, Color borderColor, float borderOpacity, float borderThickness) {
        Fill fill = styleBuilder.createFill(fillColor, fillOpacity);
        Stroke stroke = styleBuilder.createStroke(borderColor, borderThickness, borderOpacity);
        addColorLayer(key, descr, fill, stroke);
    }

    @Override
    public void addColorLayer(String key, String descr, Fill fill, Stroke border) {
        if (colorLayers.containsKey(key)) {
            removeColorLayer(key);
        }

        ColorLayer cl = new ColorLayer(descr, fill, border);
        int next = colorLayers.size() < numTopLayers() + 1
                ? colorLayers.size()
                : colorLayers.size() - numTopLayers();
        colorLayers.put(key, cl);
        layerOrder.add(next, key);
        cl.addToStyle(next);
    }

    @Override
    public void removeColorLayer(String key) {
        ColorLayer cl = colorLayers.get(key);
        if (cl != null) {
            cl.removeFromStyle();
        }
        colorLayers.remove(key);
        layerOrder.remove(key);
    }

    protected int numTopLayers() {
        return 0;
    }

    @Override
    public Set<FeatureId> getLayerFeatures(String key) {
        ColorLayer cl = colorLayers.get(key);
        if (cl != null) {
            return cl.filter;
        }
        return null;
    }

    @Override
    public void resetMap() {
        //mapContent.clearLayerList(); TODO: fix
        for (int i = 1; i < layerOrder.size() - numTopLayers(); i++) {
            removeColorLayer(layerOrder.get(i));
        }
    }

    @Override
    public LinkedList<File> getFeatureLayers() {
        return layerFiles;
    }

    @Override
    public void removeAllColorLayers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeColors(Set<FeatureId> features) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update() {
        ArrayList<ColorLayer> layers = new ArrayList<ColorLayer>(colorLayers.size());
        for (String l : layerOrder) {
            ColorLayer cl = colorLayers.get(l);
            if (cl.filter.size() > 0) {
                layers.add(cl);
            }
        }
        Collections.reverse(layers);
        MapLegendTopComponent legend = (MapLegendTopComponent) WindowManager.getDefault().findTopComponent("MapLegendTopComponent");
        legend.updateLegend(layers);  
        for (Layer lay : mapContent.layers()) {
            getPane().layerChanged(new MapLayerListEvent(mapContent, lay, 0, new MapLayerEvent(lay, MapLayerEvent.STYLE_CHANGED)));
        }
    }

    @Override
    public FeatureCollection getFeatures(Set<FeatureId> ids) {
        Filter filter = filterFactory.id(ids);
        FeatureCollection col = FeatureCollections.newCollection();
        for (Layer l : mapContent.layers()) {
            try {
                col.addAll(l.getFeatureSource().getFeatures(filter));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return col;
    }

    @Override
    public MapContent getMapContent() {
        return mapContent;
    }

    class ColorLayer {

        private Color fillColor, borderColor;
        private String description;
        private Rule polygonRule;
        private Rule lineRule;
        private Rule pointRule;
        private Set<FeatureId> filter;

        private ColorLayer(String descr, Fill fill, Stroke border) {
            this.description = descr;
            this.filter = Collections.synchronizedSet(new HashSet<FeatureId>());
            try {
                this.fillColor = Color.decode(((Literal) fill.getColor()).getValue().toString());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            try {
                this.borderColor = Color.decode(((Literal) border.getColor()).getValue().toString());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            PolygonSymbolizer polSym = styleBuilder.createPolygonSymbolizer(border, fill);
            polygonRule = styleBuilder.createRule(polSym);
            polygonRule.setFilter(filterFactory.id(filter));

            float thick = 2;
            Expression w = filterFactory.literal((Double) (((Literal) border.getWidth()).getValue()) + thick + 1);
            Expression join = filterFactory.literal(StyleBuilder.LINE_JOIN_MITRE);
            Expression cap = filterFactory.literal(StyleBuilder.LINE_CAP_BUTT);
            Stroke outerLine = styleFactory.createStroke(border.getColor(), w, border.getOpacity(), join, cap, border.getDashArray(), border.getDashOffset(), border.getGraphicFill(), border.getGraphicStroke());
            Stroke innerLine = styleBuilder.createStroke(fill.getColor(), filterFactory.literal(thick), fill.getOpacity());
            innerLine.setLineCap(cap);
            innerLine.setLineJoin(join);
            LineSymbolizer out = styleBuilder.createLineSymbolizer(outerLine);
            LineSymbolizer in = styleBuilder.createLineSymbolizer(innerLine);
            lineRule = styleBuilder.createRule(new Symbolizer[]{out, in});
            lineRule.setFilter(filterFactory.id(filter));

            Mark m = styleBuilder.createMark(StyleBuilder.MARK_CIRCLE, fill, border);
            Graphic g = styleBuilder.createGraphic(null, m, null, 1, 7, 0);
            PointSymbolizer pointSym = styleBuilder.createPointSymbolizer(g);
            pointRule = styleBuilder.createRule(pointSym);
            pointRule.setFilter(filterFactory.id(filter));
        }

        private void addToStyle(int index) {
            polyStyle.featureTypeStyles().get(0).rules().add(index, polygonRule);
            lineStyle.featureTypeStyles().get(0).rules().add(index, lineRule);
            pointStyle.featureTypeStyles().get(0).rules().add(index, pointRule);
        }

        private void removeFromStyle() {
            polyStyle.featureTypeStyles().get(0).rules().remove(polygonRule);
            lineStyle.featureTypeStyles().get(0).rules().remove(lineRule);
            pointStyle.featureTypeStyles().get(0).rules().remove(pointRule);
        }

        Color getFillColor() {
            return fillColor;
        }

        Color getBorderColor() {
            return borderColor;
        }

        String getDescription() {
            return description;
        }
    }
}
