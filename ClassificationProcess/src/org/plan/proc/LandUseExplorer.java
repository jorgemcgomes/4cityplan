/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.plan.ont.BaseOntologyProvider;
import org.plan.ont.OWLEntityNode;
import org.plan.ont.Ontology;
import org.plan.ont.OntologyManager;
import org.plan.ont.SkeletonProvider;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value = {
    @ServiceProvider(service = BaseOntologyProvider.class),
    @ServiceProvider(service = StateProvider.class),
    @ServiceProvider(service = LandUseExplorer.class)})
public class LandUseExplorer extends SkeletonProvider {
    
    public static String LAND_USE_SKELETON = "http://www.semanticweb.org/ontologies/LandUseSkeleton.owl";
    public static String LAND_USE_SKELETON_FILE = "LandUseSkeleton.owl";
    public static String LAND_USE_EXTENSIONS = "LandUseExtensions";
    
    private LandUseNode root;
    private Map<OWLClass, LandUseNode> categories = new HashMap<OWLClass, LandUseNode>();
    private OntologyManager manager;
    private OWLReasoner reasoner;
    private SubClassFactory factory;

    public LandUseExplorer() {
        super("Land Use", 
                "Choose Land Use ontology", 
                new Ontology(LAND_USE_SKELETON, InstalledFileLocator.getDefault().locate(LAND_USE_SKELETON_FILE, "org.netbeans.modules.ClassificationProcess", false), true), 
                InstalledFileLocator.getDefault().locate(LAND_USE_EXTENSIONS, "org.netbeans.modules.ClassificationProcess", false));
    }
    
    Node getRoot() {
        factory = new SubClassFactory(null);
        return new AbstractNode(Children.create(factory, true));
    }

    @Override
    public void newProject() {
        System.out.println("new land use");
        super.newProject();
        System.out.println(super.getExtensionOntology());
        loadLandUse();
        factory.refresh();
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
        super.restoreProject(toLoad, state);
        loadLandUse();
        factory.refresh();
    }

    @Override
    public int getOrder() {
        return 10;
    }

    /*
     * Land Use Structure
     */
    
    private void loadLandUse() {
        categories.clear();
        manager = Lookup.getDefault().lookup(OntologyManager.class);
        reasoner = manager.getReasoner(IRI.create(super.getExtensionOntology()));
        OWLClass r = manager.getFactory().getOWLClass(LandUseSkeletons.LAND_USE_CATEGORY.iri);
        root = createNode(r);
        fillNodeChilds(root);
    }

    private LandUseNode createNode(OWLClass clazz) {
        LandUseNode node = new LandUseNode();
        node.category = clazz;
        node.description = OWLEntityNode.displayName(clazz);
        categories.put(clazz, node);

        OWLNamedIndividual puned = manager.getFactory().getOWLNamedIndividual(clazz.getIRI());
        OWLDataProperty hasColor = manager.getFactory().getOWLDataProperty(LandUseSkeletons.HAS_COLOR.iri);
        Set<OWLLiteral> values = reasoner.getDataPropertyValues(puned, hasColor);
        if (values.size() > 0) {
            String color = values.iterator().next().getLiteral();
            node.color = Color.decode(color);
        } else {
            Set<OWLClass> superClasses = reasoner.getSuperClasses(clazz, false).getFlattened();
            superClasses.add(clazz);
            for (OWLClass superClass : superClasses) {
                Set<OWLClassExpression> ss = superClass.getSuperClasses(manager.getAllOntologies());
                for (OWLClassExpression s : ss) {
                    Set<OWLClassExpression> conjunction = s.asConjunctSet();
                    for (OWLClassExpression exp : conjunction) {
                        if (exp instanceof OWLDataHasValue) {
                            OWLDataHasValue r = (OWLDataHasValue) exp;
                            if (r.getProperty().equals(hasColor)) {
                                String color = r.getValue().getLiteral();
                                node.color = Color.decode(color);
                            }
                        }
                    }
                }
            }
        }
        return node;
    }    
    
    private void fillNodeChilds(LandUseNode node) {
        Set<OWLClass> subClasses = reasoner.getSubClasses(node.category, true).getFlattened();
        for (OWLClass clazz : subClasses) {
            if (!clazz.isBottomEntity()) {
                LandUseNode child = createNode(clazz);
                node.childs.add(child);
                fillNodeChilds(child);
            }
        }
    }
    
    public LandUseNode getLandUseNode(OWLClass category) {
        if (category == null) {
            return root;
        }
        return categories.get(category);
    }

    public class LandUseNode {

        private OWLClass category;
        private Color color;
        private String description;
        private LinkedList<LandUseNode> childs = new LinkedList<LandUseNode>();

        public OWLClass getCategory() {
            return category;
        }

        public Color getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public List<LandUseNode> getChilds() {
            return childs;
        }

        public boolean isSubclass(LandUseNode n) {
            if (n == this) {
                return true;
            }
            for (LandUseNode subN : n.getChilds()) {
                if (this.isSubclass(subN)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /*
     * Nodes explorer
     */

    Children getChildren(LandUseNode n) {
        ChildFactory fact = new SubClassFactory(n);
        if (n.childs.size() > 0) {
            return Children.create(fact, true);
        } else {
            return Children.LEAF;
        }
    }

    private class SubClassFactory extends ChildFactory<LandUseNode> {

        private LandUseNode node;

        private SubClassFactory(LandUseNode node) {
            this.node = node;
        }

        @Override
        protected boolean createKeys(List<LandUseNode> list) {
            if(LandUseExplorer.this.getExtensionOntology() == null) {
                return true;
            }
            if(node == null) {
                list.add(root);
                return true;
            }
            list.addAll(node.childs);
            Collections.sort(list, new Comparator<LandUseNode>() {
                @Override
                public int compare(LandUseNode o1, LandUseNode o2) {
                    return o1.getDescription().compareTo(o2.getDescription());
                }
            });
            return true;
        }

        @Override
        protected Node createNodeForKey(LandUseNode key) {
            return new CategoryNode(key);
        }
        
        void refresh() {
            super.refresh(true);
        }
    }
}
