/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author Jorge
 */
public enum LandUseSkeletons {

    ROOT_STEP("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#RootStep"),
    FIRST_SUBSTEP("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#hasFirstSubstep"),
    NEXT_STEP("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#hasNextStep"),
    SHOW_FEATURES("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#showFeatures"),
    SHOW_CATEGORY("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#showCategory"),
    DISPLAY_STEP("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#DisplayStep"),
    CLASSIFY_STEP("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl#ClassifyStep"),
    HAS_COLOR("http://www.semanticweb.org/ontologies/LandUseSkeleton.owl#hasColor"),
    HAS_CATEGORY("http://www.semanticweb.org/ontologies/LandUseSkeleton.owl#hasCategory"),
    LAND_USE_CATEGORY("http://www.semanticweb.org/ontologies/LandUseSkeleton.owl#LandUseCategory");
    
    public final IRI iri;

    LandUseSkeletons(String str) {
        iri = IRI.create(str);
    }
}