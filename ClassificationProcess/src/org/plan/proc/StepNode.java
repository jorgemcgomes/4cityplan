/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import org.plan.ont.OWLEntityNode;
import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.plan.proc.LandUseExplorer.LandUseNode;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author Jorge
 */
public class StepNode extends OWLEntityNode {

    StepNode(OWLNamedIndividual ind) {
	super(Lookup.getDefault().lookup(WorkflowExplorer.class).getChildren(ind), ind);
    }

    StepNode(LandUseNode useNode) {
        super(Lookup.getDefault().lookup(LandUseExplorer.class).getChildren(useNode), useNode.getCategory());
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/plan/proc/resources/step_small.png");
    }

    @Override
    protected String getDefinition() {
	return super.getDefinition() + "<p><b>Step Individiual:</b><br>" + manager.renderObject(super.getEntity())+"</p>";
    }


}
