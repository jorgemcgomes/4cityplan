/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import org.opengis.feature.simple.SimpleFeature;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.GISCore;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

@ActionID(id = "org.plan.zones.AddToZoneAction", category = "Zones")
@ActionRegistration(displayName = "#CTL_AddToZoneAction")
@ActionReference(path = "Menu/Edit/Zones", position = 20)
public final class AddToZoneAction implements ActionListener {

    private final List<ZoneNode> context;

    public AddToZoneAction(List<ZoneNode> context) {
	this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
	Collection<? extends SimpleFeature> lookup = map.getLookup().lookupAll(SimpleFeature.class);
	OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	OWLOntology individuals = om.getIndividualsOntology();
	OWLObjectProperty hasFeatures = om.getFactory().getOWLObjectProperty(GISCore.CONTAINS_FEATURES.iri);

	for (SimpleFeature f : lookup) {
	    OWLNamedIndividual ind = om.getCorrespondingIndividual(f);
	    for (ZoneNode zoneNode : context) {
		OWLObjectPropertyAssertionAxiom assertion = om.getFactory().
			getOWLObjectPropertyAssertionAxiom(hasFeatures, (OWLNamedIndividual) zoneNode.getEntity(), ind);
		om.getOWLManager().addAxiom(individuals, assertion);
		zoneNode.getLayerFeatures().add(f.getIdentifier());
	    }
	}
	om.getIndividualsReasoner().flush();
	map.clearSelectedFeatures();
    }
}
