/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.util.Collection;
import java.util.Collections;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.windows.WindowManager;
import org.plan.maps.MapProvider;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = "ProcessOntologyTopComponent",persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ProcessOntologyTopComponent",preferredID = "ProcessOntologyTopComponent")
@ActionID(category = "Window", id = "org.plan.proc.ProcessOntologyTopComponent")
@ActionReference(path = "Menu/Window")
public final class ProcessOntologyTopComponent extends TopComponent implements ExplorerManager.Provider, LookupListener {

    private ExplorerManager explorer;

    public ProcessOntologyTopComponent() {
	initComponents();
	setName(NbBundle.getMessage(ProcessOntologyTopComponent.class, "CTL_ProcessOntologyTopComponent"));
	setToolTipText(NbBundle.getMessage(ProcessOntologyTopComponent.class, "HINT_ProcessOntologyTopComponent"));

	explorer = new ExplorerManager();
	explorer.setRootContext(Lookup.getDefault().lookup(WorkflowExplorer.class).getRootNode());
	super.associateLookup(ExplorerUtils.createLookup(explorer, super.getActionMap()));
        BeanTreeView tv = (BeanTreeView) treeView;
        tv.setRootVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeView = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(treeView, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables

    private Lookup.Result<StepNode> stepsResult;
    private Collection<? extends StepNode> previousResult = Collections.EMPTY_LIST;

    @Override
    public void componentOpened() {
	stepsResult = this.getLookup().lookupResult(StepNode.class);
	stepsResult.allInstances();
	stepsResult.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
	stepsResult.removeLookupListener(this);
        stepsResult = null;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends StepNode> selected = stepsResult.allInstances();
        for(StepNode n : previousResult) {
            if(!selected.contains(n) && n instanceof DisplayNode) {
                ((DisplayNode) n).unhighlightFeatures();
            }
        }
        for(StepNode n : selected) {
            if(!previousResult.contains(n) && n instanceof DisplayNode) {
                ((DisplayNode) n).highlightFeatures();
            }
        }
        previousResult = selected;
        ((LandUseTopComponent) WindowManager.getDefault().findTopComponent("LandUseTopComponent")).deselectAll();
        Lookup.getDefault().lookup(MapProvider.class).getMainMap().update();
    }

    void deselectAll() {
        for(StepNode n : previousResult) {
            if(n instanceof DisplayNode) {
                ((DisplayNode) n).unhighlightFeatures();
            }
        }
        previousResult = Collections.EMPTY_LIST;
    }    

    @Override
    public ExplorerManager getExplorerManager() {
	return explorer;
    }   
}