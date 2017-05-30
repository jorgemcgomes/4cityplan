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
@ActionID(id = "org.plan.proc.AddClassAction", category = "Edit")
@ActionRegistration(iconBase = "org/plan/proc/resources/addclass_big.png", displayName = "CTL_AddClassAction")
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 100),
    @ActionReference(path = "Toolbars/Edit", position = 100)
})
public class AddClassAction extends ClassifyAction {

    public AddClassAction() {
	putValue(NAME, NbBundle.getMessage(AddClassAction.class, "CTL_AddClassAction"));
	putValue(SMALL_ICON,new ImageIcon(ImageUtilities.loadImage("org/plan/proc/resources/addclass_big.png", true)));
    }

    @Override
    protected OWLAxiomChange[] getChanges(OWLClassExpression clazz, OWLNamedIndividual feature) {
	OWLOntology individuals = manager.getIndividualsOntology();
	if (feature.getTypes(individuals).contains(clazz))
	    return new OWLAxiomChange[]{};

	OWLClassAssertionAxiom classAssertion =
		manager.getFactory().getOWLClassAssertionAxiom(clazz, feature);
	AddAxiom addAxiom = new AddAxiom(manager.getIndividualsOntology(), classAssertion);
	return new OWLAxiomChange[]{addAxiom};
    }

    @Override
    protected OWLAxiomChange[] revertChanges(OWLAxiomChange[] changes) {
        RemoveAxiom remAxiom = new RemoveAxiom(manager.getIndividualsOntology(), changes[0].getAxiom());
	return new OWLAxiomChange[]{remAxiom};
    }

    @Override
    protected int getType() {
	return ClassifyAction.ADD_CLASS;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new AddClassAction();
    }
}
