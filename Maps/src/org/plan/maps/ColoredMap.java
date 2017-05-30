/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.maps;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapContent;
import org.geotools.styling.Fill;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapPane;
import org.opengis.filter.identity.FeatureId;

/**
 *
 * @author Jorge
 */
public interface ColoredMap {

    public void addFeatureLayer(File file) throws IOException;
    public LinkedList<File> getFeatureLayers();
    public FeatureCollection getFeatures(Set<FeatureId> ids);
    public void addColorLayer(String key, String descr, Color color);
    public void addColorLayer(String key, String descr,
            Color fillColor, float fillOpacity,
            Color borderColor, float borderOpacity, float borderThickness);
    public void addColorLayer(String key, String descr, Fill fill, Stroke border);
    public Set<FeatureId> getLayerFeatures(String key);
    public void removeColorLayer(String key);
    public void removeAllColorLayers();
    public void removeColors(Set<FeatureId> features);
    public void resetMap();
    public void update();
    public MapContent getMapContent();
    public JMapPane getPane();

}
