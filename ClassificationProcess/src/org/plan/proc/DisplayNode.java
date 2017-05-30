/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.OWLEntityNode;
import org.plan.proc.LandUseSkeletons;
import org.plan.proc.LandUseExplorer.LandUseNode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author jorge
 */
class DisplayNode extends StepNode {

    private OWLClassExpression query;
    private List<LandUseNode> categoriesRoots = Collections.EMPTY_LIST;
    private int count = -1;
    private Set<String> keys = new HashSet<String>();

    DisplayNode(OWLNamedIndividual ind) {
        super(ind);
        query = findQuery(ind);
        OWLClassExpression colorCategory = findCategories(ind);
        if (colorCategory != null) {
            categoriesRoots = new LinkedList<LandUseNode>();
            if (!colorCategory.isAnonymous()) {
                LandUseNode ln = Lookup.getDefault().lookup(LandUseExplorer.class).getLandUseNode(colorCategory.asOWLClass());
                if (ln != null) {
                    categoriesRoots.add(ln);
                }
            }
            Set<OWLClass> subClasses = manager.getIndividualsReasoner().
                    getSubClasses(colorCategory, true).getFlattened();
            for (OWLClass sub : subClasses) {
                LandUseNode ln = Lookup.getDefault().lookup(LandUseExplorer.class).getLandUseNode(sub);
                if (ln != null) {
                    categoriesRoots.add(ln);
                }
            }
        }
        setCount(count);
    }

    DisplayNode(LandUseNode node) {
        super(node);
        categoriesRoots = new ArrayList<LandUseNode>(1);
        categoriesRoots.add(node);
        OWLObjectProperty hasCategory = manager.getFactory().getOWLObjectProperty(LandUseSkeletons.HAS_CATEGORY.iri);
        query = manager.getFactory().getOWLObjectSomeValuesFrom(hasCategory, node.getCategory());
        setCount(count);
    }

    private OWLClassExpression findQuery(OWLNamedIndividual ind) {
        Set<OWLClassExpression> types = ind.getTypes(manager.getAllOntologies()); // meter todas as onts
        OWLObjectProperty showFeats = manager.getFactory().getOWLObjectProperty(LandUseSkeletons.SHOW_FEATURES.iri);
        Set<OWLClassExpression> queries = new HashSet<OWLClassExpression>();
        for (OWLClassExpression t : types) {
            OWLObjectSomeValuesFrom r;
            if (t instanceof OWLObjectSomeValuesFrom
                    && (r = (OWLObjectSomeValuesFrom) t).getProperty().equals(showFeats)) {
                queries.add(r.getFiller());
            }
        }
        return manager.getFactory().getOWLObjectIntersectionOf(queries);
    }

    // TODO: metodo automatico
    private OWLClassExpression findCategories(OWLNamedIndividual ind) {
        Set<OWLClassExpression> types = ind.getTypes(manager.getAllOntologies()); // todas as onts
        Set<OWLClassExpression> categories = new HashSet<OWLClassExpression>();
        OWLObjectProperty showCat = manager.getFactory().getOWLObjectProperty(LandUseSkeletons.SHOW_CATEGORY.iri);
        for (OWLClassExpression t : types) {
            OWLObjectSomeValuesFrom r;
            if (t instanceof OWLObjectSomeValuesFrom
                    && (r = (OWLObjectSomeValuesFrom) t).getProperty().equals(showCat)) {
                categories.add(r.getFiller());
            }
        }
        if (categories.size() > 1) {
            return manager.getFactory().getOWLObjectUnionOf(categories);
        } else if (categories.size() == 1) {
            return categories.iterator().next();
        } else {
            autoFindCategories(query, categories);
            if (categories.size() > 1) {
                return manager.getFactory().getOWLObjectUnionOf(categories);
            } else if (categories.size() == 1) {
                return categories.iterator().next();
            } else {
                return null;
            }
        }
    }

    private void autoFindCategories(OWLClassExpression exp, Set<OWLClassExpression> categories) {
        if (exp instanceof OWLNaryBooleanClassExpression) {
            OWLNaryBooleanClassExpression nary = (OWLNaryBooleanClassExpression) exp;
            for (OWLClassExpression e : nary.getOperands()) {
                autoFindCategories(e, categories);
            }
        } else if (exp instanceof OWLObjectCardinalityRestriction || exp instanceof OWLQuantifiedObjectRestriction) {
            OWLQuantifiedRestriction r = (OWLQuantifiedRestriction) exp;
            autoFindCategories((OWLClassExpression) r.getFiller(), categories);
        } else if (exp instanceof OWLClass) {
            OWLClass clazz = exp.asOWLClass();
            if (Lookup.getDefault().lookup(LandUseExplorer.class).getLandUseNode(clazz) != null) {
                categories.add(clazz);
            }
        }
    }

    void highlightFeatures() {
        OWLReasoner reasoner = manager.getIndividualsReasoner();
        OWLObjectProperty hasCategory = manager.getFactory().getOWLObjectProperty(LandUseSkeletons.HAS_CATEGORY.iri);
        Set<OWLNamedIndividual> remainingInstances = reasoner.getInstances(query, false).getFlattened();
        setCount(remainingInstances.size());
        this.unhighlightFeatures();
        SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();

        // Classifica cada individuo quanto a sua categoria
        Map<OWLNamedIndividual, LandUseNode> indToNode = new LinkedHashMap<OWLNamedIndividual, LandUseNode>(15);
        Stack<LandUseNode> stack = new Stack<LandUseNode>();
        for (LandUseNode ln : categoriesRoots) {
            if (ln != null) {
                stack.push(ln);
            }
        }
        while (!stack.empty()) {
            LandUseNode pop = stack.pop();
            OWLClassExpression q = manager.getFactory().getOWLObjectIntersectionOf(query,
                    manager.getFactory().getOWLObjectSomeValuesFrom(hasCategory, pop.getCategory()));
            Set<OWLNamedIndividual> instances = reasoner.getInstances(q, false).getFlattened();
            if (instances.size() > 0) {
                remainingInstances.removeAll(instances);
                for (OWLNamedIndividual i : instances) {
                    indToNode.put(i, pop);
                }

                // procura nas subcategorias
                for (LandUseNode n : pop.getChilds()) {
                    stack.push(n);
                }
            }
        }

        // Remove os que foram classificados com uma classe sem cor
        Iterator<Entry<OWLNamedIndividual, LandUseNode>> iter = indToNode.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<OWLNamedIndividual, LandUseNode> next = iter.next();
            if (next.getValue().getColor() == null) {
                iter.remove();
                remainingInstances.add(next.getKey());
            }
        }

        // Remove as cores repetidas devido a relacao de subclasse
        LinkedHashSet<LandUseNode> usedNodes = new LinkedHashSet<LandUseNode>(30);
        usedNodes.addAll(indToNode.values());
        Set<LandUseNode> cleaned = (Set<LandUseNode>) usedNodes.clone();
        for (LandUseNode n1 : usedNodes) {
            for (LandUseNode n2 : usedNodes) {
                if (n1 != n2 && n1.getColor().equals(n2.getColor()) && n1.isSubclass(n2)) {
                    cleaned.remove(n1);
                    break;
                }
            }
        }

        // Gera as labels
        HashMap<Color, String> legends = new HashMap<Color, String>((int) (usedNodes.size() / 0.75f), 0.75f);
        for (LandUseNode n : usedNodes) {
            String legend = legends.get(n.getColor());
            if (legend != null) {
                legends.put(n.getColor(), legend + " / " + n.getDescription());
            } else {
                legends.put(n.getColor(), n.getDescription());
            }
        }

        // Cria os layers
        for (Entry<Color, String> e : legends.entrySet()) {
            System.out.println(e.getKey().toString() + ": " + e.getValue()); // test
            String key = getEntity().getIRI().getFragment() + e.getKey().toString();
            keys.add(key);
            map.addColorLayer(key, e.getValue(), e.getKey());
            Set<FeatureId> ids = map.getLayerFeatures(key);
            for (Entry<OWLNamedIndividual, LandUseNode> ind : indToNode.entrySet()) {
                if (ind.getValue().getColor().equals(e.getKey())) {
                    ids.add(new FeatureIdImpl(ind.getKey().getIRI().getFragment()));
                }
            }
        }

        // Mete cor no que sobra
        String key = getEntity().getIRI().getFragment() + "default";
        map.addColorLayer(key, manager.renderObject(query), Color.WHITE);
        keys.add(key);
        Set<FeatureId> layerFeatures = map.getLayerFeatures(key);
        for (OWLNamedIndividual i : remainingInstances) {
            layerFeatures.add(new FeatureIdImpl(i.getIRI().getFragment()));
        }
    }

    void unhighlightFeatures() {
        SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
        for (String key : keys) {
            map.removeColorLayer(key);
        }
    }

    OWLClassExpression getQuery() {
        return query;
    }

    void setCount(int i) {
        this.count = i;
        String name = OWLEntityNode.displayName(super.getEntity());
        if (count >= 0) {
            super.setDisplayName(name + " (" + count + ")");
        } else {
            super.setDisplayName(name);
        }
    }

    int getCount() {
        return count;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/plan/proc/resources/query_small.png");
    }

    @Override
    protected String getDefinition() {
        ArrayList<String> cats = new ArrayList<String>(categoriesRoots.size());
        for (LandUseNode lun : categoriesRoots) {
            if (lun != null) {
                cats.add(lun.getCategory().getIRI().getFragment());
            }
        }
        String def = "<p><b>Show Features:</b><br>"
                + "<i>Query:</i><br>" + manager.renderObject(query) + "<br>"
                + "<i>Color categories:</i><br>" + cats.toString() + "</p>";
        return super.getDefinition() + def;
    }
}
