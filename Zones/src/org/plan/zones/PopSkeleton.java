/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.io.File;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.WindowManager;
import org.plan.ont.BaseOntologyProvider;
import org.plan.ont.Ontology;
import org.plan.ont.SkeletonProvider;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value = {
    @ServiceProvider(service = BaseOntologyProvider.class),
    @ServiceProvider(service = StateProvider.class)})
public class PopSkeleton extends SkeletonProvider {

    public static String ONTOLOGY = "http://www.semanticweb.org/ontologies/PopulationSkeleton.owl";
    public static String ONTOLOGY_FILE = "PopulationSkeleton.owl";
    public static String EXTENSIONS = "PopulationExtensions";

    public PopSkeleton() {
        super("Population",
                "Choose population indicators ontology",
                new Ontology(ONTOLOGY, InstalledFileLocator.getDefault().locate(ONTOLOGY_FILE, "org.netbeans.modules.Zones", false), true),
                InstalledFileLocator.getDefault().locate(EXTENSIONS, "org.netbeans.modules.Zones", false));
    }

    public enum Entity {

        HAS_RESIDENT_POPULATION("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#hasResidentPopulation"),
        RESIDENT_POPULATION("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#ResidentPopulation"),
        POPULATION_INDICATOR("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#populationIndicator"),
        HAS_DISTRIBUTION("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#hasDistribution"),
        INDICATOR_LEVEL("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#indicatorLevel"),
        POPULATION_NUMBER("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#populationNumber"),
        POPULATION_DENSITY("http://www.semanticweb.org/ontologies/PopulationSkeleton.owl#populationDensity");
        public final IRI iri;

        Entity(String str) {
            iri = IRI.create(str);
        }
    }

    @Override
    public void newProject() {
        super.newProject();
        ZonesTopComponent tc = (ZonesTopComponent) WindowManager.getDefault().findTopComponent("ZonesTopComponent");
        tc.refreshZones();
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
        super.restoreProject(toLoad, state);
        ZonesTopComponent tc = (ZonesTopComponent) WindowManager.getDefault().findTopComponent("ZonesTopComponent");
        tc.refreshZones();
    }
    
    @Override
    public int getOrder() {
        return 40;
    }    
}
