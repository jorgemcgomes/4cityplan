/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jorge
 */
@ServiceProvider(service = BaseOntologyProvider.class)
public class CoreOntologyProvider implements BaseOntologyProvider {
    
    public static final String CORE_ONTOLOGY = "http://www.semanticweb.org/ontologies/GISCore.owl";

    @Override
    public Ontology[] getOntologies() {
        return new Ontology[]{new Ontology(
                CORE_ONTOLOGY, 
                InstalledFileLocator.getDefault().locate("GISCore.owl", "org.netbeans.modules.Ontologies", false), 
                true)};
    }
    
}
