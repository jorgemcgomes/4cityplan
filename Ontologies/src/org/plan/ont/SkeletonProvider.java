/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.ont;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.plan.proj.StateProvider;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author Jorge
 */
public abstract class SkeletonProvider implements BaseOntologyProvider, StateProvider {

    private String extensionOntology;
    private File extensionsFolder;
    private String name;
    private String descr;
    private Ontology skeletonOntology;
    public static final int DEFAULT_ORDER = 10;
    
    protected SkeletonProvider(String name, String descr, Ontology skeleton, File extensionsFolder) {
        this.skeletonOntology = skeleton;
        this.extensionsFolder = extensionsFolder;
        this.name = name;
        this.descr = descr;
    }

    public String getExtensionOntology() {
        return extensionOntology;
    }

    @Override
    public Ontology[] getOntologies() {
        return new Ontology[]{skeletonOntology};
    }

    @Override
    public void newProject() {
        File[] onts = extensionsFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".owl");
            }
        });
        chooseOntology(onts);
    }
    
    private void chooseOntology(File[] choices) {
        System.out.println("num choices: " + choices.length);
        if(choices.length == 0) {
            extensionOntology = OntologyManager.EMPTY_ONTOLOGY;
            return;
        }
        String[] options = new String[choices.length];
        for(int i = 0 ; i < choices.length ; i++) {
            options[i] = choices[i].getName();
        }
        JComboBox dropDown = new JComboBox(options);
        dropDown.setToolTipText(descr);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(dropDown, BorderLayout.CENTER);
        DialogDescriptor dd = new DialogDescriptor(panel, name, true, null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        if(dd.getValue() == NotifyDescriptor.OK_OPTION) {
            File f = choices[dropDown.getSelectedIndex()];
            try {
                OWLOntology added = om.addProjectOntology(new Ontology(null, f, skeletonOntology.addImport()));
                extensionOntology = added.getOntologyID().getOntologyIRI().toString();
            } catch (OWLOntologyCreationException ex) {
                Exceptions.printStackTrace(ex);
                chooseOntology(choices); // TODO: Remover a escolha falhada
            }
        } else {
            extensionOntology = OntologyManager.EMPTY_ONTOLOGY;
        }        
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
        extensionOntology = new String(state);
    }

    @Override
    public byte[] saveProject(File toSave) {
        return extensionOntology.getBytes();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
