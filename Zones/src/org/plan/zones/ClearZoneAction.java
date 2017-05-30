/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.GISCore;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

@ActionID(id = "org.plan.zones.ClearZoneAction", category = "Zones")
@ActionRegistration(displayName = "#CTL_ClearZoneAction")
@ActionReference(path = "Menu/Edit/Zones", position = 40)
public final class ClearZoneAction implements ActionListener {

    private final List<ZoneNode> context;

    public ClearZoneAction(List<ZoneNode> context) {
	this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();

	OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	OWLOntology individuals = om.getIndividualsOntology();
	OWLObjectProperty hasFeatures = om.getFactory().getOWLObjectProperty(GISCore.CONTAINS_FEATURES.iri);
	for (ZoneNode zoneNode : context) {
	    zoneNode.getLayerFeatures().clear();
	    OWLNamedIndividual z = (OWLNamedIndividual) zoneNode.getEntity();
	    Set<OWLIndividual> values = z.getObjectPropertyValues(hasFeatures, individuals);
	    for (OWLIndividual i : values) {
		OWLObjectPropertyAssertionAxiom assertion = om.getFactory().
			getOWLObjectPropertyAssertionAxiom(hasFeatures, z, i);
		om.getOWLManager().removeAxiom(individuals, assertion);
	    }
	}
	om.getIndividualsReasoner().flush();
	map.update();
    }
}
