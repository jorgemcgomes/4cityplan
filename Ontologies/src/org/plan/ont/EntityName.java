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
public enum EntityName {

    ROOT_STEP(OntologyName.WORKFLOW_SKELETON, "RootStep"),
    FIRST_SUBSTEP(OntologyName.WORKFLOW_SKELETON, "hasFirstSubstep"),
    NEXT_STEP(OntologyName.WORKFLOW_SKELETON, "hasNextStep"),
    SHOW_FEATURES(OntologyName.WORKFLOW_SKELETON, "showFeatures"),
    SHOW_CATEGORY(OntologyName.WORKFLOW_SKELETON, "showCategory"),
    DISPLAY_STEP(OntologyName.WORKFLOW_SKELETON, "DisplayStep"),
    CLASSIFY_STEP(OntologyName.WORKFLOW_SKELETON, "ClassifyStep"),

    BUFFER_ZONE(OntologyName.LAND_USE_SKELETON, "BufferZone"),
    INTERVENTION_ZONE(OntologyName.LAND_USE_SKELETON, "InterventionZone"),
    HAS_COLOR(OntologyName.LAND_USE_SKELETON, "hasColor"),
    ZONE(OntologyName.LAND_USE_SKELETON, "Zone"),
    CONTAINS_FEATURES(OntologyName.LAND_USE_SKELETON, "containsFeatures"),
    HAS_CATEGORY(OntologyName.LAND_USE_SKELETON, "hasCategory"),
    LAND_USE_CATEGORY(OntologyName.LAND_USE_SKELETON, "LandUseCategory"),

    HAS_RESIDENT_POPULATION(OntologyName.POPULATION, "hasResidentPopulation"),
    RESIDENT_POPULATION(OntologyName.POPULATION, "ResidentPopulation"),
    POPULATION_INDICATOR(OntologyName.POPULATION, "populationIndicator"),
    HAS_DISTRIBUTION(OntologyName.POPULATION, "hasDistribution"),
    INDICATOR_LEVEL(OntologyName.POPULATION, "indicatorLevel"),
    POPULATION_NUMBER(OntologyName.POPULATION, "populationNumber");


    private final IRI iri;

    EntityName(OntologyName ont, String str) {
        iri = IRI.create(ont.iri().toString() + "#" + str);
    }

    public IRI iri() {
        return iri;
    }
}
