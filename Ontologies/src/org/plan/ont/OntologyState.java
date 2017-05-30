/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;

/**
 *
 * @author Jorge
 */
class OntologyState implements Serializable {

    String individualsFile;
    String individualsIRI;
    HashSet<Ontology> projectOntologies;
}
