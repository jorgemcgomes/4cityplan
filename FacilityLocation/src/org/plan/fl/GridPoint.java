/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import com.vividsolutions.jts.geom.Point;
import java.util.HashSet;
import java.util.Set;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author Jorge
 */
public class GridPoint {
    
    private SimpleFeature feature;
    private Set<GridPoint> neighbours;
    private double people;
    private Set<AbstractProvider> providers;
    private Point point;
    
    public GridPoint(SimpleFeature feat, Point point, double pop) {
        this.feature = feat;
        this.people = pop;
        this.providers = new HashSet<AbstractProvider>(20);
        this.neighbours = new HashSet<GridPoint>(12);
        this.point = point;
    }
    
    SimpleFeature getFeature() {
        return feature;
    }
    
    void addNeighbour(GridPoint point) {
        neighbours.add(point);
    }
    
    double getPop() {
        return people;
    }
    
    Set<GridPoint> getNeighbours() {
        return neighbours;
    }
    
    double distance(GridPoint other) {
        GeodeticCalculator calc = new GeodeticCalculator();
        calc.setStartingGeographicPoint(point.getX(), point.getY());
        calc.setDestinationGeographicPoint(other.point.getX(), other.point.getY());
        return calc.getOrthodromicDistance();
    }
    
    Set<AbstractProvider> getProviders() {
        return providers;
    }

    @Override
    public String toString() {
        return point.toString();
    }
    
    public Point getPoint() {
        return point;
    }
}
