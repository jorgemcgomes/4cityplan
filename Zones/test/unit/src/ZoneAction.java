/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.zones;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Lookup;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectedFeaturesAction;
import org.plan.ont.EntityName;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLClass;

/**
 *
 * @author jorge
 */
abstract class ZoneAction extends SelectedFeaturesAction {

    protected Set<FeatureId> bufferFeatures;
    protected Set<FeatureId> interventionFeatures;
    protected Set<FeatureId> restrictions;
    protected OntologyManager manager;
    protected OWLClass bufferClass;
    protected OWLClass interventionClass;

    ZoneAction() {
        ZoneManager zm = Lookup.getDefault().lookup(ZoneManager.class);
        bufferFeatures = zm.getBufferFeatures();
        interventionFeatures = zm.getInterventionFeatures();
        restrictions = Lookup.getDefault().lookup(MapProvider.class).getMainMap().getRestrictions();
        manager = Lookup.getDefault().lookup(OntologyManager.class);
        bufferClass = manager.getFactory().getOWLClass(EntityName.BUFFER_FEATURE.iri());
        interventionClass = manager.getFactory().getOWLClass(EntityName.INTERVENTION_FEATURE.iri());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<? extends SimpleFeature> allInstances = super.mapLookup.allInstances();
        for(SimpleFeature f : allInstances) {
            processFeature(f);
        }
        map.clearSelectedFeatures();
    }

    protected abstract void processFeature(SimpleFeature feature);

}
