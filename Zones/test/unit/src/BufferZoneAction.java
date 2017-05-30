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
public class BufferZoneAction extends ZoneAction {

    /*public BufferZoneAction() {
 	putValue(NAME, NbBundle.getMessage(BufferZoneAction.class, "CTL_BufferZoneAction"));
	putValue(SMALL_ICON,new ImageIcon(ImageUtilities.loadImage("org/plan/zones/resources/buffer_big.png", true)));
    }

    @Override
    protected void processFeature(SimpleFeature feature) {
        OWLNamedIndividual ind = manager.getCorrespondingIndividual(feature);
        Set<OWLClassExpression> types = ind.getTypes(manager.getIndividualsOntology());
        if (types.contains(super.interventionClass)) {
            OWLClassAssertionAxiom ax =
                    manager.getDataFactory().getOWLClassAssertionAxiom(interventionClass, ind);
            manager.getOWLManager().removeAxiom(manager.getIndividualsOntology(), ax);
            interventionFeatures.remove(feature.getIdentifier());
        }
        if (!types.contains(bufferClass)) {
            OWLClassAssertionAxiom ax =
                    manager.getDataFactory().getOWLClassAssertionAxiom(bufferClass, ind);
            manager.getOWLManager().addAxiom(manager.getIndividualsOntology(), ax);
            bufferFeatures.add(feature.getIdentifier());
        }
        super.restrictions.add(feature.getIdentifier());
    }*/
}
