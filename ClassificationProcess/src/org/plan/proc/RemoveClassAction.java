/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.proc;

import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

/**
 *
 * @author Jorge
 */
@ActionID(id = "org.plan.proc.RemoveClassAction", category = "Edit")
@ActionRegistration(iconBase = "org/plan/proc/resources/removeclass_big.png", displayName = "CTL_RemoveClassAction")
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 110),
    @ActionReference(path = "Toolbars/Edit", position = 110)
})
public class RemoveClassAction extends ClassifyAction {

    public RemoveClassAction() {
	putValue(NAME, NbBundle.getMessage(AddClassAction.class, "CTL_RemoveClassAction"));
	putValue(SMALL_ICON,new ImageIcon(ImageUtilities.loadImage("org/plan/proc/resources/removeclass_big.png", true)));
    }

    @Override
    protected OWLAxiomChange[] getChanges(OWLClassExpression clazz, OWLNamedIndividual feature) {
	OWLOntology individuals = manager.getIndividualsOntology();
	if (!feature.getTypes(individuals).contains(clazz))
	    return new OWLAxiomChange[]{};

	OWLClassAssertionAxiom classAssertion = 
		manager.getFactory().getOWLClassAssertionAxiom(clazz, feature);
	RemoveAxiom removeAxiom = new RemoveAxiom(individuals, classAssertion);
	return new OWLAxiomChange[]{removeAxiom};
    }

    @Override
    protected OWLAxiomChange[] revertChanges(OWLAxiomChange[] changes) {
	AddAxiom addAxiom = new AddAxiom(manager.getIndividualsOntology(), changes[0].getAxiom());
        return new OWLAxiomChange[]{addAxiom};
    }

    @Override
    protected int getType() {
	return ClassifyAction.REMOVE_CLASS;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new RemoveClassAction();
    }
}
