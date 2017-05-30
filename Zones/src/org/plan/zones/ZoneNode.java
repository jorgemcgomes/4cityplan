/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.Color;
import java.awt.Image;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.geotools.filter.identity.FeatureIdImpl;
import org.jfree.chart.JFreeChart;
import org.opengis.filter.identity.FeatureId;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.GISCore;
import org.plan.ont.OWLEntityNode;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author jorge
 */
public class ZoneNode extends OWLEntityNode {

    private PopulationEditor popEditor;
    private Set<FeatureId> layerFeatures;

    ZoneNode(OWLNamedIndividual zoneIndividual) {
	super(Children.LEAF, zoneIndividual);
	SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
	String id = zoneIndividual.getIRI().toString();
	if (zoneIndividual.getIRI().equals(GISCore.BUFFER_ZONE.iri)) {
	    map.addColorLayer(id, "Zone " + zoneIndividual.getIRI().getFragment(),
		    new Color(218, 233, 243), 1, new Color(186, 190, 230), 1, 1);
	} else if (zoneIndividual.getIRI().equals(GISCore.INTERVENTION_ZONE.iri)) {
	    map.addColorLayer(id, "Zone " + zoneIndividual.getIRI().getFragment(),
		    new Color(243, 233, 218), 1, new Color(230, 190, 186), 1, 1);
	} else {
	    map.addColorLayer(id, "Zone " + zoneIndividual.getIRI().getFragment(),
		    new Color(218, 243, 233), 1, new Color(186, 230, 190), 1, 1);
	}
	layerFeatures = map.getLayerFeatures(id);
        
        createSheet();
    }

    @Override
    public Image getIcon(int type) {
	return ImageUtilities.loadImage("org/plan/zones/resources/zone_small.png");
    }

    @Override
    public Action[] getActions(boolean context) {
	LinkedList<Action> actions = new LinkedList<Action>();
	List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/Zones/");
	for (Action a : actionsForPath) {
	    actions.add(a);
	}
	actions.add(null);
	actions.addAll(Arrays.asList(super.getActions(context)));
	Action[] array = new Action[actions.size()];
	return actions.toArray(array);
    }

    void highlightFeatures() {
	OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	OWLNamedIndividual z = (OWLNamedIndividual) getEntity();
	OWLObjectProperty hasFeatures = om.getFactory().getOWLObjectProperty(GISCore.CONTAINS_FEATURES.iri);
	Set<OWLNamedIndividual> inds = om.getIndividualsReasoner().getObjectPropertyValues(z, hasFeatures).getFlattened();
	for (OWLNamedIndividual i : inds) {
	    layerFeatures.add(new FeatureIdImpl(i.getIRI().getFragment()));
	}
    }

    Set<FeatureId> getLayerFeatures() {
	return layerFeatures;
    }

    @Override
    protected Sheet createSheet() {
	OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	IRI residentIRI = IRI.create(PopSkeleton.ONTOLOGY + "#ResidentPop_" + getEntity().getIRI().getFragment());
	OWLNamedIndividual residentInd = om.getFactory().getOWLNamedIndividual(residentIRI);
	OWLClass residentClass = om.getFactory().getOWLClass(PopSkeleton.Entity.RESIDENT_POPULATION.iri);
	OWLOntology individuals = om.getIndividualsOntology();

	if (!individuals.containsIndividualInSignature(residentIRI)) {
	    // Put into ontology
	    OWLClassAssertionAxiom ax = om.getFactory().getOWLClassAssertionAxiom(residentClass, residentInd);
	    OWLObjectProperty hasRes = om.getFactory().getOWLObjectProperty(PopSkeleton.Entity.HAS_RESIDENT_POPULATION.iri);
	    OWLObjectPropertyAssertionAxiom ax2 = om.getFactory().getOWLObjectPropertyAssertionAxiom(hasRes, (OWLIndividual) getEntity(), residentInd);
	    om.getOWLManager().addAxiom(individuals, ax);
	    om.getOWLManager().addAxiom(individuals, ax2);
	    om.getIndividualsReasoner().flush();
	}

	popEditor = new PopulationEditor(residentClass, residentInd);
	return popEditor.getSheet();
    }

    Collection<JFreeChart> getCharts() {
	if (popEditor == null) {
	    return Collections.EMPTY_LIST;
	}
	return popEditor.getCharts();
    }
}
