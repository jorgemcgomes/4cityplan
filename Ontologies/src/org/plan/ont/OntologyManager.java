/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.geotools.filter.identity.FeatureIdImpl;
import org.mindswap.pellet.PelletOptions;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.plan.proj.StateManager;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value = {
    @ServiceProvider(service = OntologyManager.class),
    @ServiceProvider(service = StateProvider.class)})
public class OntologyManager implements StateProvider {

    public static final String EMPTY_ONTOLOGY = "http://www.semanticweb.org/ontologies/4cityplan_emptyontology.owl";
    private OWLOntologyManager owlManager;
    private OWLDataFactory factory;
    private OWLObjectRenderer renderer;
    private OWLOntology individualsOntology;
    private PelletReasoner individualsReasoner;
    private HashMap<IRI, PelletReasoner> reasoners = new HashMap();
    private HashSet<Ontology> projectOntologies = new HashSet<Ontology>();
    private HashSet<Ontology> baseOntologies = new HashSet<Ontology>();

    public OntologyManager() {
        PelletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
        PelletOptions.USE_COMPLETION_QUEUE = true;
        PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
        PelletOptions.USE_SMART_RESTORE = false;
        PelletOptions.DL_SAFE_RULES = true;
        PelletOptions.USE_CONTINUOUS_RULES = true;

        // Inicializar as estruturas
        owlManager = OWLManager.createOWLOntologyManager();
        factory = owlManager.getOWLDataFactory();
        renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

        // Cria a ontologia vazia
        try {
            owlManager.createOntology(IRI.create(OntologyManager.EMPTY_ONTOLOGY));
        } catch (OWLOntologyCreationException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Carregas as ontologias base
        Collection<? extends BaseOntologyProvider> res = Lookup.getDefault().lookupAll(BaseOntologyProvider.class);
        for (BaseOntologyProvider prov : res) {
            baseOntologies.addAll(Arrays.asList(prov.getOntologies()));
        }
        for (Ontology b : baseOntologies) {
            owlManager.addIRIMapper(new SimpleIRIMapper(IRI.create(b.getIri()), IRI.create(b.getLocation())));
        }
        for (Ontology b : baseOntologies) {
            try {
                owlManager.loadOntology(IRI.create(b.getIri()));
            } catch (OWLOntologyCreationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }


    /*
     * Ontologies management
     */
    public OWLOntology getOntology(IRI iri) {
        return owlManager.getOntology(iri);
    }

    public synchronized OWLOntology addProjectOntology(Ontology ont) throws OWLOntologyCreationException {
        // TODO: verificar se ja existe
        OWLOntology loaded = owlManager.loadOntologyFromOntologyDocument(ont.getLocation());
        IRI iri = loaded.getOntologyID().getOntologyIRI();
        ont.setIri(iri.toString());
        owlManager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(ont.getLocation())));
        projectOntologies.add(ont);
        if (ont.addImport()) {
            OWLImportsDeclaration imports = factory.getOWLImportsDeclaration(iri);
            owlManager.applyChange(new AddImport(individualsOntology, imports));
        }
        return loaded;
    }

    public synchronized OWLReasoner getReasoner(IRI iri) {
        if (reasoners.containsKey(iri)) {
            return reasoners.get(iri);
        } else if (owlManager.contains(iri)) {
            PelletReasoner r = PelletReasonerFactory.getInstance().createNonBufferingReasoner(getOntology(iri));
            reasoners.put(iri, r);
            return r;
        } else {
            return null;
        }

    }

    public OWLOntology getEntityOntology(IRI iri) {
        IRI res;
        if (!iri.toString().contains("#")) {
            res = iri;
        } else {
            String base = iri.toString().split("#")[0];
            res = IRI.create(base);
        }
        return owlManager.getOntology(res);
    }

    public Set<OWLOntology> getAllOntologies() {
        return owlManager.getOntologies();
    }

    /*
     * Utilities
     */
    public OWLDataFactory getFactory() {
        return factory;
    }

    public OWLOntologyManager getOWLManager() {
        return owlManager;
    }

    public String renderObject(OWLObject obj) {
        return renderer.render(obj);
    }

    /*
     * Individuals Ontology
     */
    public synchronized OWLOntology getIndividualsOntology() {
        return individualsOntology;
    }

    public synchronized OWLReasoner getIndividualsReasoner() {
        return individualsReasoner;
    }

    public OWLNamedIndividual getCorrespondingIndividual(SimpleFeature feature) {
        IRI indIri = IRI.create(getIndividualsOntology().getOntologyID().getOntologyIRI().toString()
                + "#" + feature.getID());
        return factory.getOWLNamedIndividual(indIri);
    }

    public FeatureId getCorrespondingFeature(OWLNamedIndividual ind) {
        String id = ind.getIRI().getFragment();
        return new FeatureIdImpl(id);
    }

    /*
     * State management
     */
    private void init() {
        if (individualsOntology != null) {
            owlManager.removeOntology(individualsOntology);
            for (Ontology ont : projectOntologies) {
                owlManager.removeOntology(getOntology(IRI.create(ont.getIri())));
            }
            individualsOntology = null;
            if (individualsReasoner != null) {
                individualsReasoner.dispose();
                individualsReasoner = null;
            }
        }
        for (PelletReasoner r : reasoners.values()) {
            r.dispose();
        }
        reasoners.clear();
        projectOntologies.clear();

    }

    @Override
    public void newProject() {
        init();
        try {
            // Cria a ontologia dos individuos
            String random = Long.toHexString((long) (Math.random() * Long.MAX_VALUE));
            IRI iri = IRI.create("http://www.semanticweb.org/ontologies/individuals" + random + ".owl");
            individualsOntology = owlManager.createOntology(iri);
            for (Ontology b : baseOntologies) {
                if (b.addImport()) {
                    OWLImportsDeclaration imports = factory.getOWLImportsDeclaration(IRI.create(b.getIri()));
                    owlManager.applyChange(new AddImport(individualsOntology, imports));
                }
            }
            individualsReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(individualsOntology);
        } catch (OWLOntologyCreationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public byte[] saveProject(File toSave) {
        String ontName = toSave.getName() + ".owl";
        try {
            owlManager.saveOntology(individualsOntology, IRI.create(new File(toSave.getParentFile(), ontName)));
        } catch (OWLOntologyStorageException ex) {
            Exceptions.printStackTrace(ex);
        }
        OntologyState st = new OntologyState();
        st.individualsFile = ontName;
        st.individualsIRI = individualsOntology.getOntologyID().getOntologyIRI().toString();
        st.projectOntologies = projectOntologies;
        return StateManager.objectToBytes(st);
    }

    public static Object bytesToObject(byte[] bytes) {
        Object o = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            o = in.readObject();
            bis.close();
            in.close();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return o;
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
        init();
        OntologyState st = (OntologyState) bytesToObject(state);
        try {
            // Carregar as outras ontologias
            for (Ontology o : st.projectOntologies) {
                owlManager.addIRIMapper(new SimpleIRIMapper(IRI.create(o.getIri()), IRI.create(o.getLocation())));
            }
            for (Ontology o : st.projectOntologies) {
                System.out.println("loading: " + o.getIri());
                if (!owlManager.contains(IRI.create(o.getIri()))) {
                    owlManager.loadOntology(IRI.create(o.getIri()));
                }
            }

            // Carregar a ontologia dos individuos
            File indsFile = new File(toLoad.getParentFile(), st.individualsFile);
            individualsOntology = owlManager.loadOntologyFromOntologyDocument(indsFile);
            String loadedIRI = individualsOntology.getOntologyID().getOntologyIRI().toString();
            if (!loadedIRI.equals((String) st.individualsIRI)) {
                throw new OWLOntologyCreationException("Loaded ontology IRI doesnt match with expected");
            }
            individualsReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(individualsOntology);

        } catch (OWLOntologyCreationException ex) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    "Error when loading ontology. Moving on with empty one.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
