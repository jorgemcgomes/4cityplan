/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import org.plan.ont.GISCore;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

@ActionID(id = "org.plan.zones.AddZoneAction", category = "Zones")
@ActionRegistration(displayName = "#CTL_AddZoneAction")
@ActionReference(path = "Menu/Edit/Zones", position = 20)
public final class AddZoneAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine("Please enter the zone name.",
		"Zone Name", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
	DialogDisplayer.getDefault().notify(nd);
	if(nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
	    OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
	    String name = nd.getInputText();
	    OWLNamedIndividual zoneInd = om.getFactory().getOWLNamedIndividual(
		    IRI.create(om.getIndividualsOntology().getOntologyID().getOntologyIRI().toString()+"#"+name));
	    OWLClass zoneClass = om.getFactory().getOWLClass(GISCore.ZONE.iri);
	    OWLClassAssertionAxiom ax = om.getFactory().getOWLClassAssertionAxiom(zoneClass, zoneInd);
	    om.getOWLManager().addAxiom(om.getIndividualsOntology(), ax);
	    om.getIndividualsReasoner().flush();
            ZonesTopComponent tc = (ZonesTopComponent) WindowManager.getDefault().findTopComponent("ZonesTopComponent");
	    tc.refreshZones();
	}
    }
}
