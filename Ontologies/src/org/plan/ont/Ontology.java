/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author Jorge
 */
public class Ontology implements Serializable {
    
    private String iri;
    private File location;
    private boolean addImport;

    public Ontology(String iri, File location, boolean addImport) {
        this.iri = iri;
        this.location = location;
        this.addImport = addImport;
    }

    public boolean addImport() {
        return addImport;
    }

    public String getIri() {
        return iri;
    }

    public File getLocation() {
        return location;
    }

    public void setAddImport(boolean addImport) {
        this.addImport = addImport;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public void setLocation(File location) {
        this.location = location;
    }
}
