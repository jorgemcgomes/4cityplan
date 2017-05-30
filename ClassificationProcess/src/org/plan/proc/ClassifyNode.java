/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.plan.proc.LandUseExplorer.LandUseNode;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author Jorge
 */
class ClassifyNode extends DisplayNode {

    private OWLClassExpression assertion;

    ClassifyNode(OWLNamedIndividual ind) {
	super(ind);
	this.assertion = super.getQuery();
    }

    ClassifyNode(LandUseNode node) {
        super(node);
        this.assertion = super.getQuery();
    }

    OWLClassExpression getAssertion() {
	return assertion;
    }

    @Override
    public String getHtmlDisplayName() {
	return "<b>" + super.getDisplayName()+"</b>";
    }

    @Override
    public Image getIcon (int type) {
	return ImageUtilities.loadImage ("org/plan/proc/resources/class_small.png");
    }

    @Override
    protected String getDefinition() {
	return super.getDefinition() + "<p><b>Classification assertion:</b><br>"+
		manager.renderObject(assertion)+"</p>";
    }

}
