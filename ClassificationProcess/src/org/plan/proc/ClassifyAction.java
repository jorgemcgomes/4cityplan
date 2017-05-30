/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import org.plan.maps.SelectedFeaturesAction;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.geotools.filter.identity.FeatureIdImpl;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public abstract class ClassifyAction extends SelectedFeaturesAction {

    public static final int ADD_CLASS = 1, REMOVE_CLASS = -1;
    protected OntologyManager manager;
    private Lookup.Result<ClassifyNode> categoryLookup;

    public ClassifyAction() {
	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		Lookup lkp = new ProxyLookup(
                        WindowManager.getDefault().findTopComponent("ProcessOntologyTopComponent").getLookup(),
			WindowManager.getDefault().findTopComponent("LandUseTopComponent").getLookup());
		categoryLookup = lkp.lookupResult(ClassifyNode.class);
		listenResult(categoryLookup);
	    }
	});
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
	InputOutput io = IOProvider.getDefault().getIO("Status", false);
	Collection<? extends SimpleFeature> selected = mapLookup.allInstances();
	manager = Lookup.getDefault().lookup(OntologyManager.class);

	for (ClassifyNode classifyNode : categoryLookup.allInstances()) {
	    ProgressHandle ph = ProgressHandleFactory.createHandle("Processing category " + classifyNode.getDisplayName());
	    ph.setInitialDelay(1);
	    ph.start();

	    Set<OWLNamedIndividual> modified = classify(classifyNode, selected);
	    if(modified.size() > 0) {
		classifyNode.highlightFeatures();
	    }

	    io.getOut().println(modified.size() + " features modified with " + classifyNode.getDisplayName());
	    ph.finish();
	}
	map.clearSelectedFeatures();
    }

    private Set<OWLNamedIndividual> classify(final ClassifyNode node, Collection<? extends SimpleFeature> features) {
	OWLClassExpression clazz = node.getAssertion();
	final OWLReasoner reasoner = manager.getIndividualsReasoner();
	final Set<FeatureId> errors = map.errorFeatures();
	final InputOutput io = IOProvider.getDefault().getIO("Status", false);

	// Store changes
	final LinkedHashMap<OWLNamedIndividual, OWLAxiomChange[]> changes = new LinkedHashMap<OWLNamedIndividual, OWLAxiomChange[]>();

	// 1st Pass
	for (SimpleFeature feat : features) {
	    OWLNamedIndividual ind = manager.getCorrespondingIndividual(feat);
	    OWLAxiomChange[] ch = getChanges(clazz, ind);
	    if (ch.length > 0) {
		changes.put(ind, ch);
		manager.getOWLManager().applyChanges(Arrays.asList(ch));
	    }
	}

	// Check consistency
	reasoner.flush();
	if (reasoner.isConsistent()) {
	    return changes.keySet();
	}

	// Not consistent - Remove changes
	for (OWLAxiomChange[] ch : changes.values()) {
	    OWLAxiomChange[] revert = revertChanges(ch);
	    manager.getOWLManager().applyChanges(Arrays.asList(revert));
	}
	reasoner.flush();

	// Ask for second pass
	NotifyDescriptor d = new NotifyDescriptor.Confirmation(
		"Would you like to find which features are inconsistent? "
		+ "(May take a while)", "Inconsistencies Found", NotifyDescriptor.YES_NO_OPTION);
	if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
	    ProgressRunnable<Set<OWLNamedIndividual>> secondPass = new ProgressRunnable() {
		@Override
		public Set<OWLNamedIndividual> run(ProgressHandle ph) {
		    // Second pass
		    ph.switchToDeterminate(100);
		    Set<OWLNamedIndividual> modified = new HashSet<OWLNamedIndividual>(changes.size());
		    Set<Entry<OWLNamedIndividual, OWLAxiomChange[]>> changesSet = changes.entrySet();
		    int j = 0;
		    for (Entry<OWLNamedIndividual, OWLAxiomChange[]> e : changesSet) {
			ph.progress((int) ((double) j / changesSet.size() * 100));
			// Adicionar mudancas
			manager.getOWLManager().applyChanges(Arrays.asList(e.getValue()));
			reasoner.flush();
			if (!reasoner.isConsistent()) {
			    // Remover mudancas se nao ficar consistente
			    String featName = e.getKey().getIRI().getFragment();
			    io.getErr().println("Inconsistent feature when classifying "
				    + node.getDisplayName() + ": " + featName);
			    errors.add(new FeatureIdImpl(featName));
			    OWLAxiomChange[] revert = revertChanges(e.getValue());
			    manager.getOWLManager().applyChanges(Arrays.asList(revert));
			} else {
			    modified.add(e.getKey());
			}
			j++;
		    }
		    reasoner.flush();
		    ph.finish();
		    return modified;
		}
	    };
	    return ProgressUtils.showProgressDialogAndRun(secondPass, "Seeking inconsistencies", false);
	} else {
	    return Collections.EMPTY_SET;
	}
    }

    protected abstract OWLAxiomChange[] getChanges(OWLClassExpression clazz, OWLNamedIndividual feature);

    protected abstract OWLAxiomChange[] revertChanges(OWLAxiomChange[] changes);

    protected abstract int getType();
}
