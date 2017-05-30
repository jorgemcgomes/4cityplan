/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.util.Set;
import javax.swing.ImageIcon;
import org.opengis.feature.simple.SimpleFeature;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author jorge
 */
public class InterventionZoneAction extends ZoneAction {

    public InterventionZoneAction() {
 	putValue(NAME, NbBundle.getMessage(InterventionZoneAction.class, "CTL_InterventionZoneAction"));
	putValue(SMALL_ICON,new ImageIcon(ImageUtilities.loadImage("org/plan/zones/resources/intervention_big.png", true)));
    }

    @Override
    protected void processFeature(SimpleFeature feature) {
        OWLNamedIndividual ind = manager.getCorrespondingIndividual(feature);
        Set<OWLClassExpression> types = ind.getTypes(manager.getIndividualsOntology());
        if (types.contains(bufferClass)) {
            OWLClassAssertionAxiom ax =
                    manager.getFactory().getOWLClassAssertionAxiom(bufferClass, ind);
            manager.getOWLManager().removeAxiom(manager.getIndividualsOntology(), ax);
            bufferFeatures.remove(feature.getIdentifier());
        }
        if (!types.contains(interventionClass)) {
            OWLClassAssertionAxiom ax =
                    manager.getFactory().getOWLClassAssertionAxiom(interventionClass, ind);
            manager.getOWLManager().addAxiom(manager.getIndividualsOntology(), ax);
            interventionFeatures.add(feature.getIdentifier());
        }
        super.restrictions.add(feature.getIdentifier());
    }
}
