/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.Color;
import java.util.Set;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.EntityName;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author jorge
 */
@ServiceProvider(service = ZoneManager.class)
public class ZoneManager {

    private SelectableMap map;
    public static final String BUFFER_KEY = "bufferfeatures";
    public static final String INTERVENTION_KEY = "interventionfeatures";

    public ZoneManager() {
	map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
        initLayers();
   }

    private void initLayers() {
	map.addColorLayer(BUFFER_KEY, "Buffer features", new Color(236, 240, 244), 1, new Color(181, 201, 223), 1, 1);
	map.addColorLayer(INTERVENTION_KEY, "Intervention features", new Color(241, 235, 228), 1, new Color(218, 204, 186), 1, 1);
    }

    public synchronized void refresh() {
	OntologyManager man = Lookup.getDefault().lookup(OntologyManager.class);
	OWLClass buffer = man.getFactory().getOWLClass(EntityName.BUFFER_FEATURE.iri());
	OWLClass intervention = man.getFactory().getOWLClass(EntityName.INTERVENTION_FEATURE.iri());
	OWLReasoner reasoner = man.getIndividualsReasoner();

	initLayers();
	Set<FeatureId> bufferFeats = getBufferFeatures();
	Set<FeatureId> interventionFeats = getInterventionFeatures();
	bufferFeats.clear();
	interventionFeats.clear();

	for (OWLNamedIndividual i : reasoner.getInstances(buffer, true).getFlattened()) {
	    bufferFeats.add(new FeatureIdImpl(i.getIRI().getFragment()));
	}
	for (OWLNamedIndividual i : reasoner.getInstances(intervention, true).getFlattened()) {
	    interventionFeats.add(new FeatureIdImpl(i.getIRI().getFragment()));
	}
	
	map.getRestrictions().clear();
	map.getRestrictions().addAll(bufferFeats);
	map.getRestrictions().addAll(interventionFeats);
    }

    synchronized Set<FeatureId> getBufferFeatures() {
	return map.getLayerFeatures(BUFFER_KEY);
    }

    synchronized Set<FeatureId> getInterventionFeatures() {
	return map.getLayerFeatures(INTERVENTION_KEY);
    }
}
