/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.plan.ont.BaseOntologyProvider;
import org.plan.ont.Ontology;
import org.plan.ont.OntologyManager;
import org.plan.ont.SkeletonProvider;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value = {
    @ServiceProvider(service = BaseOntologyProvider.class),
    @ServiceProvider(service = StateProvider.class),
    @ServiceProvider(service = WorkflowExplorer.class)})
public class WorkflowExplorer extends SkeletonProvider {

    public static String WORKFLOW_SKELETON = "http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl";
    public static String WORKFLOW_SKELETON_FILE = "WorkflowSkeleton.owl";
    public static String WORKFLOW_EXTENSIONS = "WorkflowExtensions";    
    
    private Node rootStep;
    private Set<OWLNamedIndividual> displaySteps;
    private Set<OWLNamedIndividual> classifySteps;
    private OntologyManager man;
    private StepChildrenFactory factory;
    private OWLReasoner reasoner;

    public WorkflowExplorer() {
        super("Classification workflow", 
                "Choose Classification Workflow ontology", 
                new Ontology(WORKFLOW_SKELETON, InstalledFileLocator.getDefault().locate(WORKFLOW_SKELETON_FILE, "org.netbeans.modules.ClassificationProcess", false), false), 
                InstalledFileLocator.getDefault().locate(WORKFLOW_EXTENSIONS, "org.netbeans.modules.ClassificationProcess", false));
    }    
    
    Node getRootNode() {
        if (rootStep == null) {
            man = Lookup.getDefault().lookup(OntologyManager.class);
            OWLNamedIndividual root = man.getFactory().getOWLNamedIndividual(LandUseSkeletons.ROOT_STEP.iri);
            factory = new StepChildrenFactory(root);
            rootStep = new AbstractNode(Children.create(factory, true));
        }
        return rootStep;
    }
    
    @Override
    public void newProject() {
        super.newProject();
        reasoner = man.getReasoner(IRI.create(super.getExtensionOntology()));
        displaySteps = null;
        classifySteps = null;
        factory.refresh();
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
        super.restoreProject(toLoad, state);
        reasoner = man.getReasoner(IRI.create(super.getExtensionOntology()));
        displaySteps = null;
        classifySteps = null;        
        factory.refresh();
    }
    
    @Override
    public int getOrder() {
        return 20;
    }
    
    Children getChildren(OWLNamedIndividual ind) {
        OWLObjectProperty hasFirstSubstep = man.getFactory().getOWLObjectProperty(LandUseSkeletons.FIRST_SUBSTEP.iri);
        NodeSet<OWLNamedIndividual> childs = reasoner.getObjectPropertyValues(ind, hasFirstSubstep);
        if (childs.isEmpty()) {
            return Children.LEAF;
        } else {
            OWLNamedIndividual i = childs.getFlattened().iterator().next();
            StepChildrenFactory fact = new StepChildrenFactory(i);
            return Children.create(fact, true);
        }
    }

    private class StepChildrenFactory extends ChildFactory<OWLNamedIndividual> {

        private OWLNamedIndividual firstSubstep;

        StepChildrenFactory(OWLNamedIndividual substep) {
            this.firstSubstep = substep;
        }

        @Override
        protected boolean createKeys(List<OWLNamedIndividual> list) {
            if(WorkflowExplorer.this.getExtensionOntology() == null) {
                return true;
            }
            OWLNamedIndividual lastStep = firstSubstep;
            OWLObjectProperty hasNext = man.getFactory().getOWLObjectProperty(LandUseSkeletons.NEXT_STEP.iri);

            boolean end = false;
            list.add(firstSubstep);
            while (!end) {
                NodeSet<OWLNamedIndividual> ns = reasoner.getObjectPropertyValues(lastStep, hasNext);
                if (ns.isEmpty()) {
                    end = true;
                } else {
                    lastStep = ns.getFlattened().iterator().next();
                    list.add(lastStep);
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(OWLNamedIndividual key) {
	    // Only made once
            if(displaySteps == null) {
                displaySteps = reasoner.getInstances(
                        man.getFactory().getOWLClass(LandUseSkeletons.DISPLAY_STEP.iri), false).getFlattened();
                classifySteps = reasoner.getInstances(
                        man.getFactory().getOWLClass(LandUseSkeletons.CLASSIFY_STEP.iri), false).getFlattened();
            }

            if(classifySteps.contains(key)) {
                return new ClassifyNode(key);
            } else if(displaySteps.contains(key)) {
                return new DisplayNode(key);
            } else {
                return new StepNode(key);
            }
        }
        
        void refresh() {
            super.refresh(true);
        }
    }
}
