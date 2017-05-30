/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author Jorge
 */
public enum GISCore {

    BUFFER_ZONE("http://www.semanticweb.org/ontologies/GISCore.owl#BufferZone"),
    INTERVENTION_ZONE("http://www.semanticweb.org/ontologies/GISCore.owl#InterventionZone"),
    ZONE("http://www.semanticweb.org/ontologies/GISCore.owl#Zone"),
    CONTAINS_FEATURES("http://www.semanticweb.org/ontologies/GISCore.owl#containsFeatures"),
    FEATURE("http://www.semanticweb.org/ontologies/GISCore.owl#Feature");
    
    public final IRI iri;

    GISCore(String str) {
        iri = IRI.create(str);
    }
}