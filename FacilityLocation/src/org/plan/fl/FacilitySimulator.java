/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import org.openide.util.Lookup;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author Jorge
 */
public class FacilitySimulator extends Simulator {

    private LinkedList<ProviderType> creationQueue = new LinkedList<ProviderType>();
    private int allowedDeaths = 3;
    private HashMap<ProviderType, Integer> registeredDeaths = new HashMap<ProviderType, Integer>();

    @Override
    public void clearEverything() {
        super.clearEverything();
        registeredDeaths.clear();
    }

    void registerDeath(ProviderType type) {
        int current = registeredDeaths.containsKey(type) ? registeredDeaths.get(type) : 0;
        registeredDeaths.put(type, current + 1);
        //updateRelations();
    }

    @Override
    AbstractProvider generateProvider() {
        Set<ProviderType> usedTypes = Lookup.getDefault().lookup(FacilityTypeLoader.class).getUsedTypes();
        if (creationQueue.isEmpty()) {
            creationQueue.addAll(usedTypes);
            Collections.shuffle(creationQueue);
        }
        while (!creationQueue.isEmpty()) {
            ProviderType t = creationQueue.pop();
            // verifica se todos os edificios deste tipo estao satisfeitos
            boolean allSatisfied = true;
            for (AbstractProvider p : super.getProviders()) {
                FacilityProvider fp = (FacilityProvider) p;
                if (fp.getType() == t && !fp.isSatisfied()) {
                    allSatisfied = false;
                    break;
                }
            }

            // calcula o numero de habitantes que ainda nao e servido por um ed deste tipo
            double free = 0;
            for (GridPoint p : super.getGrid().getGridPoints()) {
                boolean isFree = true;
                for (AbstractProvider prov : p.getProviders()) {
                    FacilityProvider fp = (FacilityProvider) prov;
                    if (fp.getType() == t) {
                        isFree = false;
                        break;
                    }
                }
                if (isFree) {
                    free += p.getPop();
                }
            }

            if (usedTypes.contains(t)
                    && (!registeredDeaths.containsKey(t) || registeredDeaths.get(t) < allowedDeaths)
                    && allSatisfied && free >= t.getMinCapacity()) {
                FacilityProvider newProvider = new FacilityProvider(this, t);
                Object[] points = getGrid().getGridPoints().toArray();
                Random r = new Random();
                GridPoint p = (GridPoint) points[r.nextInt(points.length)];
                newProvider.position = p;
                updateRelations();
                return newProvider;
            }
        }
        return null;
    }

    @Override
    public void updateConditions() {
        super.updateConditions();
        for(AbstractProvider p : super.getProviders()) {
            FacilityProvider fp = (FacilityProvider) p;
            fp.affinityFeatures = null;
            fp.dislikeFeatures = null;
        }        
    }
    
    private void updateRelations() {
        /*OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        HashMap<OWLNamedIndividual, FacilityProvider> map = new HashMap<OWLNamedIndividual, FacilityProvider>();
        for(AbstractProvider p : super.getProviders()) {
            FacilityProvider fp = (FacilityProvider) p;
            map.put(fp.getIndividual(), fp);
        }
        for(FacilityProvider p : map.values()) {
            p.providerLoves.clear();
            Set<OWLNamedIndividual> affs = om.getIndividualsReasoner().getInstances(p.getType().getAffinity(), false).getFlattened();
            for(OWLNamedIndividual i : affs) {
                FacilityProvider other = map.get(i);
                if(other != null && other != p) {
                    p.providerLoves.add(other);
                    other.providerLoves.add(p);
                }
            }
            p.providerConflicts.clear();
            Set<OWLNamedIndividual> confls = om.getIndividualsReasoner().getInstances(p.getType().getDislike(), false).getFlattened();
            for(OWLNamedIndividual i : confls) {
                FacilityProvider other = map.get(i);
                if(other != null && other != p) {
                    p.providerConflicts.add(other);
                    other.providerConflicts.add(p);
                }
            }
        }*/
    }
}
