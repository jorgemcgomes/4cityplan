/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextPane;
import org.apache.commons.lang.ArrayUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 *
 * @author Jorge
 */
public class OWLEntityNode extends AbstractNode {

    protected OWLEntity owlEntity;
    protected static OntologyManager manager;

    public OWLEntityNode(Children children, OWLEntity entity) {
        super(children, Lookups.singleton(entity));
        this.owlEntity = entity;
        super.setDisplayName(displayName(entity));
        if (manager == null) {
            manager = Lookup.getDefault().lookup(OntologyManager.class);
        }
    }

    public OWLEntity getEntity() {
        return owlEntity;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    protected String getDefinition() {
        return "<p><b>Entity:</b><br>" + manager.renderObject(owlEntity) + "</p>";
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = new Action[]{new ShowDefinition(), null};
        Action[] superActions = super.getActions(context);
        return (Action[]) ArrayUtils.addAll(actions, superActions);
    }

    private class ShowDefinition extends AbstractAction {

        public ShowDefinition() {
            putValue(NAME, "Show definition");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String def = getDefinition();
            JTextPane p = new JTextPane();
            p.setPreferredSize(new Dimension(400, 300));
            p.setEditable(false);
            p.setContentType("text/html");
            p.setText("<font face=\"monospace\" size=\"3\">" + def + "</font>");
            DialogDescriptor d = new DialogDescriptor(p, owlEntity.getIRI().getFragment() + " definition", false, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    public static String displayName(OWLEntity entity) {
        OWLLiteral[] labels = labels(entity);
        for (OWLLiteral l : labels) {
            String lStr = l.getLiteral();
            if (lStr != null && !lStr.equals("")) {
                return lStr;
            }
        }
        return entity.getIRI().getFragment();
    }

    public static String mainDescription(OWLEntity entity) {
        OWLLiteral[] descrs = descriptions(entity);
        for (OWLLiteral l : descrs) {
            String lStr = l.getLiteral();
            if (lStr != null && !lStr.equals("")) {
                return lStr;
            }
        }
        return "";
    }

    public static OWLLiteral[] labels(OWLEntity entity) {
        return getAnnotations(OWLRDFVocabulary.RDFS_LABEL.getIRI(), entity);
    }

    public static OWLLiteral[] descriptions(OWLEntity entity) {
        return getAnnotations(OWLRDFVocabulary.RDFS_COMMENT.getIRI(), entity);
    }

    private static OWLLiteral[] getAnnotations(IRI propertyIRI, OWLEntity entity) {
        if (manager == null) {
            manager = Lookup.getDefault().lookup(OntologyManager.class);
        }
        OWLAnnotationProperty prop = manager.getFactory().getOWLAnnotationProperty(propertyIRI);
        OWLOntology ont = manager.getEntityOntology(entity.getIRI());
        if (ont == null || prop == null) {
            return new OWLLiteral[]{};
        }

        Set<OWLAnnotation> annotations = entity.getAnnotations(ont, prop);
        ArrayList<OWLLiteral> res = new ArrayList<OWLLiteral>(annotations.size());
        for (OWLAnnotation anno : annotations) {
            if (anno.getValue() instanceof OWLLiteral) {
                res.add((OWLLiteral) anno.getValue());
            }
        }

        OWLLiteral[] t = new OWLLiteral[res.size()];
        return res.toArray(t);
    }
}
