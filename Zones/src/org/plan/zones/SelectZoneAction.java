/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.filter.identity.FeatureId;
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

@ActionID(id = "org.plan.zones.SelectZoneAction", category = "Zones")
@ActionRegistration(displayName = "#CTL_SelectZoneAction")
@ActionReference(path = "Menu/Edit/Zones", position = 10)
public final class SelectZoneAction implements ActionListener {

    private final List<ZoneNode> context;

    public SelectZoneAction(List<ZoneNode> context) {
	this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	Set<FeatureId> toSelect = new HashSet<FeatureId>();
	OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	OWLObjectProperty hasFeatures = om.getFactory().getOWLObjectProperty(GISCore.CONTAINS_FEATURES.iri);
	for (ZoneNode zoneNode : context) {
	    OWLNamedIndividual z = (OWLNamedIndividual) zoneNode.getEntity();
	    Set<OWLNamedIndividual> values = om.getIndividualsReasoner().getObjectPropertyValues(z, hasFeatures).getFlattened();
	    for (OWLNamedIndividual i : values) {
		toSelect.add(new FeatureIdImpl(i.getIRI().getFragment()));
	    }
	}
	SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
	map.selectFeatures(toSelect, true);
    }
}
