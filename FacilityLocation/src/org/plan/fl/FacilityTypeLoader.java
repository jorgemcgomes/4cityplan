/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.WindowManager;
import org.plan.fl.ProviderType.Service;
import org.plan.ont.BaseOntologyProvider;
import org.plan.ont.OWLEntityNode;
import org.plan.ont.Ontology;
import org.plan.ont.OntologyManager;
import org.plan.ont.SkeletonProvider;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value = {
    @ServiceProvider(service = BaseOntologyProvider.class),
    @ServiceProvider(service = StateProvider.class),
    @ServiceProvider(service = FacilityTypeLoader.class)})
public class FacilityTypeLoader extends SkeletonProvider {

    public static String FACILITY_SKELETON = "http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl";
    public static String FACILITY_SKELETON_FILE = "FacilitiesSkeleton.owl";
    public static String FACILITY_EXTENSIONS = "FacilityExtensions";
    
    public enum FacilitySkeleton {

        FACILITY_TYPE("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#FacilityType"),
        MAX_CAPACITY("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#maxCapacity"),
        MIN_CAPACITY("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#minCapacity"),
        IRRADIATION("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#hasIrradiation"),
        SHORT_ID("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#shortID"),
        SERVICE("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#Service"),
        PROVIDES_SERVICES("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#providesServices"),
        HAS_FACILITY_TYPE("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#hasFacilityType"),
        HAS_AFFINITY("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#hasAffinity"),
        HAS_DISLIKE("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#hasDislike"),
        FACILITY("http://www.semanticweb.org/ontologies/FacilityLocationSkeleton.owl#Facility");
        
        public final IRI iri;

        FacilitySkeleton(String str) {
            iri = IRI.create(str);
        }
    }

    public FacilityTypeLoader() {
        super("Facility Location",
                "Choose Facilities ontology",
                new Ontology(FACILITY_SKELETON, InstalledFileLocator.getDefault().locate(FACILITY_SKELETON_FILE, "org.netbeans.modules.FacilityLocation", false), true),
                InstalledFileLocator.getDefault().locate(FACILITY_EXTENSIONS, "org.netbeans.modules.FacilityLocation", false));
    }
    
    private FacilityLocationTopComponent tc;

    private Set<ProviderType> loadTypes() {
        tc = (FacilityLocationTopComponent) WindowManager.getDefault().findTopComponent("FacilityLocationTopComponent");
        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        OWLReasoner reasoner = om.getIndividualsReasoner();
        
        // Load services
        Set<OWLNamedIndividual> services = reasoner.getInstances(
                om.getFactory().getOWLClass(FacilitySkeleton.SERVICE.iri), true).getFlattened();
        HashMap<OWLNamedIndividual, Service> allServices = new HashMap<OWLNamedIndividual, Service>();
        for(OWLNamedIndividual service : services) {
            Service s = new Service(service);
            allServices.put(service, s);
        }
        
        // Load types
        Set<OWLNamedIndividual> types = reasoner.getInstances(
                om.getFactory().getOWLClass(FacilitySkeleton.FACILITY_TYPE.iri), true).getFlattened();
        Set<ProviderType> availableTypes = new HashSet<ProviderType>();
        for(OWLNamedIndividual type : types) {
            // name
            String name = OWLEntityNode.displayName(type);
            
            // short id
            Set<OWLLiteral> values = reasoner.getDataPropertyValues(type, om.getFactory().getOWLDataProperty(FacilitySkeleton.SHORT_ID.iri));
            String id = values.isEmpty() ? name : values.iterator().next().getLiteral();
            
            ProviderType newType = new ProviderType(type, id, name);
            
            // max capacity
            values = reasoner.getDataPropertyValues(type, om.getFactory().getOWLDataProperty(FacilitySkeleton.MAX_CAPACITY.iri));
            if(values.isEmpty()) {
                continue;
            }
                newType.setMaxCapacity(values.iterator().next().parseInteger());
            
            // min capacity
            values = reasoner.getDataPropertyValues(type, om.getFactory().getOWLDataProperty(FacilitySkeleton.MIN_CAPACITY.iri));
            if(values.isEmpty()) {
                continue;
            }
                newType.setMinCapacity(values.iterator().next().parseInteger());
            
            // irradiation
            values = reasoner.getDataPropertyValues(type, om.getFactory().getOWLDataProperty(FacilitySkeleton.IRRADIATION.iri));
            if(values.isEmpty()) {
                continue;
            }
            newType.setIrradiation(values.iterator().next().parseInteger());
            
            // services
            Set<OWLNamedIndividual> servs = reasoner.getObjectPropertyValues(type, om.getFactory().getOWLObjectProperty(FacilitySkeleton.PROVIDES_SERVICES.iri)).getFlattened();
            if(servs.isEmpty()) {
                continue;
            }
            for(OWLNamedIndividual s : servs) {
                newType.getServices().add(allServices.get(s));
            }
            
            // likes & dislikes
            // TODO: devia generalizar para varias assertions
            OWLObjectProperty affinity = om.getFactory().getOWLObjectProperty(FacilitySkeleton.HAS_AFFINITY.iri);
            OWLObjectProperty dislike = om.getFactory().getOWLObjectProperty(FacilitySkeleton.HAS_DISLIKE.iri);
            Set<OWLClassExpression> individualTypes = type.getTypes(om.getAllOntologies());
            for(OWLClassExpression exp : individualTypes) {
                if(exp instanceof OWLObjectSomeValuesFrom) {
                    OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) exp;
                    // like
                    if(some.getProperty().equals(affinity)) {
                        newType.setAffinity(some.getFiller());
                    // dislike
                    } else if(some.getProperty().equals(dislike)) {
                        newType.setDislike(some.getFiller());
                    }
                }
            }
            
            availableTypes.add(newType);
        }
        
        return availableTypes;
    }
    
    public Set<ProviderType> getUsedTypes() {
        LinkedHashMap<ProviderType, Boolean> typesSelection = tc.getTypesSelection();
        HashSet<ProviderType> types = new HashSet<ProviderType>();
        for(Entry<ProviderType, Boolean> e : typesSelection.entrySet()) {
            if(e.getValue()) {
                types.add(e.getKey());
            }
        }
        return types;
    }

    @Override
    public void newProject() {
        super.newProject();
        refreshTypes();
    }
    
    @Override
    public void restoreProject(File toLoad, byte[] state) {
        super.restoreProject(toLoad, state);
        refreshTypes();
    }

    @Override
    public int getOrder() {
        return 30;
    }
    
    private void refreshTypes() {
        Set<ProviderType> types = loadTypes();
        tc.updateList(types);        
    }
}
