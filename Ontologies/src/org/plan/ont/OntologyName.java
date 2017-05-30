/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author jorge
 */
public enum OntologyName {

    LAND_USE_SKELETON("http://www.semanticweb.org/ontologies/LandUseSkeleton.owl", "LandUseSkeleton.owl"),
    LAND_USE("http://www.semanticweb.org/ontologies/SSx.owl", "SSx/SSx.owl"),
    //LAND_USE("http://www.semanticweb.org/ontologies/LBCS.owl", "LBCS/LBCS.owl"),
    WORKFLOW_SKELETON("http://www.semanticweb.org/ontologies/WorkflowSkeleton.owl", "WorkflowSkeleton.owl"),
    WORKFLOW("http://www.semanticweb.org/ontologies/SSxWorkflow.owl", "SSx/SSxWorkflow.owl"),
    //WORKFLOW("http://www.semanticweb.org/ontologies/LBCSWorkflow.owl", "LBCS/LBCSWorkflow.owl"),
    POPULATION("http://www.semanticweb.org/ontologies/Population.owl", "Population.owl");

    public static final String FOLDER = "/org/plan/ont/resources/";

    private IRI iri;
    private String fileName = null;
    OntologyName(String iri, String fileName) {
        this.iri = IRI.create(iri);
        this.fileName = fileName;
    }
    public IRI iri() {
        return iri;
    }
    public String file() {
        return fileName;
    }
}
