/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.GISCore;
import org.plan.ont.OntologyManager;
import org.plan.zones.PopSkeleton;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 *
 * @author Jorge
 */
public class Grid {

    private SimpleFeatureCollection gridFeatures;
    private GridPoint[][] gridMatrix;
    private Set<GridPoint> grid;
    public SimpleFeatureType locationType;

    public Grid() {
        try {
            locationType = DataUtilities.createType(
                    "Location",
                    "*geom:Point:srid=4326");
        } catch (SchemaException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void createGrid() {
        FacilityLocationTopComponent tc = (FacilityLocationTopComponent) WindowManager.getDefault().findTopComponent("FacilityLocationTopComponent");
        int gridSpace = tc.gridSize.getValue();

        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
        OWLNamedIndividual interv = om.getFactory().getOWLNamedIndividual(GISCore.INTERVENTION_ZONE.iri);
        OWLObjectProperty contFeats = om.getFactory().getOWLObjectProperty(GISCore.CONTAINS_FEATURES.iri);
        Set<OWLNamedIndividual> feats = om.getIndividualsReasoner().getObjectPropertyValues(interv, contFeats).getFlattened();
        Set<FeatureId> ids = new HashSet<FeatureId>(feats.size());
        for (OWLNamedIndividual ind : feats) {
            ids.add(om.getCorrespondingFeature(ind));
        }
        FeatureCollection features = map.getFeatures(ids); // TODO: remover nao-poligonos
        ReferencedEnvelope bounds = features.getBounds();
        GeodeticCalculator calc = new GeodeticCalculator(bounds.getCoordinateReferenceSystem());
        calc.setStartingGeographicPoint(bounds.getMinX(), bounds.getMaxY());
        calc.setDestinationGeographicPoint(bounds.getMaxX(), bounds.getMaxY());
        double xDist = calc.getOrthodromicDistance();
        calc.setStartingGeographicPoint(bounds.getMaxX(), bounds.getMinY());
        calc.setDestinationGeographicPoint(bounds.getMaxX(), bounds.getMaxY());
        double yDist = calc.getOrthodromicDistance();

        double xInterval = gridSpace * bounds.getSpan(0) / xDist;
        double yInterval = gridSpace * bounds.getSpan(1) / yDist;

        double popDensity = loadDensity(interv);
        double pointPop = popDensity * Math.pow(gridSpace * 0.001, 2);

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        gridFeatures = FeatureCollections.newCollection();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(locationType);

        grid = new HashSet<GridPoint>();
        gridMatrix = new GridPoint[(int) (xDist / gridSpace)][(int) (yDist / gridSpace)];
        double x = bounds.getMinX();
        for (int i = 0; i < gridMatrix.length; i++) {
            double y = bounds.getMinY();
            for (int j = 0; j < gridMatrix[0].length; j++) {
                Point point = geometryFactory.createPoint(new Coordinate(x, y));
                if (checkInside(point, features)) {
                    //featureBuilder.add(point); //TODO: isto deveria funcionar
                    featureBuilder.add(geometryFactory.createPoint(new Coordinate(y, x))); // dirty fix - coordenadas trocadas
                    SimpleFeature f = featureBuilder.buildFeature(null);
                    featureBuilder.reset();
                    GridPoint p = new GridPoint(f, point, pointPop);
                    gridMatrix[i][j] = p;
                    gridFeatures.add(f);
                    grid.add(p);
                }
                y += yInterval;
            }
            x += xInterval;
        }

        System.out.println("Total grid points: " + grid.size());
        System.out.println("Total pop: " + grid.size() * pointPop);

        fillNeighbours();
    }

    void fillNeighbours() {
        // Percorre a grelha
        for (int i = 0; i < gridMatrix.length; i++) {
            for (int j = 0; j < gridMatrix[i].length; j++) {
                GridPoint p = gridMatrix[i][j];
                if (p != null) {
                    // Percorre os vizinhos
                    for (int ni = i - 1; ni <= i + 1; ni++) {
                        for (int nj = j - 1; nj <= j + 1; nj++) {
                            if ((ni != i || nj != j)
                                    && ni > 0 && ni < gridMatrix.length
                                    && nj > 0 && nj < gridMatrix[ni].length
                                    && gridMatrix[ni][nj] != null) {
                                p.addNeighbour(gridMatrix[ni][nj]);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param zone
     * @return Hab/Km2
     */
    int loadDensity(OWLNamedIndividual zone) {
        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        Set<OWLNamedIndividual> pop = om.getIndividualsReasoner().getObjectPropertyValues(
                zone,
                om.getFactory().getOWLObjectProperty(PopSkeleton.Entity.HAS_RESIDENT_POPULATION.iri)).getFlattened();
        if(pop.isEmpty()) {
            return 0;
        }
        Set<OWLLiteral> values = om.getIndividualsReasoner().getDataPropertyValues(
                pop.iterator().next(),
                om.getFactory().getOWLDataProperty(PopSkeleton.Entity.POPULATION_DENSITY.iri));
        if (!values.isEmpty()) {
            return values.iterator().next().parseInteger();
        }
        return 0;
    }

    private boolean checkInside(Point p, FeatureCollection zone) {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Filter filter = ff.contains(ff.property(zone.getSchema().getGeometryDescriptor().getLocalName()), ff.literal(p));
        FeatureCollection filtered = zone.subCollection(filter);
        return !filtered.isEmpty();
    }

    SimpleFeatureCollection getGridFeatures() {
        return gridFeatures;
    }

    Set<GridPoint> getGridPoints() {
        return grid;
    }

    // TOOD: guardar ordens
    List<GridPoint> getSortedPoints(final GridPoint center) {
        ArrayList<GridPoint> list = new ArrayList<GridPoint>(grid);
        Collections.sort(list, new Comparator<GridPoint>() {

            @Override
            public int compare(GridPoint p1, GridPoint p2) {
                double dist1 = p1.distance(center);
                double dist2 = p2.distance(center);
                if (dist1 == dist2) {
                    return 0;
                } else if (dist1 > dist2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }
}
