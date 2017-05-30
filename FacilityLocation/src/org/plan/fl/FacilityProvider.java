/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Lookup;
import org.plan.fl.ProviderType.Service;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

/**
 *
 * @author Jorge
 */
public class FacilityProvider extends AbstractProvider {

    private ProviderType type;
    private double currentCapacity = 0;
    private String id;
    private OWLNamedIndividual individual;
    FeatureCollection affinityFeatures, dislikeFeatures;
    Set<FacilityProvider> providerConflicts = new HashSet<FacilityProvider>();
    Set<FacilityProvider> providerLoves = new HashSet<FacilityProvider>();

    FacilityProvider(FacilitySimulator sim, ProviderType type) {
        super(sim);
        this.type = type;
        this.id = RandomStringUtils.randomAscii(10);
        this.setShortID(type.getId());
        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        String iri = om.getIndividualsOntology().getOntologyID().getOntologyIRI() + "#" + id;
        individual = om.getFactory().getOWLNamedIndividual(IRI.create(iri));
        OWLObjectPropertyAssertionAxiom ax = om.getFactory().getOWLObjectPropertyAssertionAxiom(
                om.getFactory().getOWLObjectProperty(FacilityTypeLoader.FacilitySkeleton.HAS_FACILITY_TYPE.iri),
                individual,
                type.getOwlIndividual());
        om.getOWLManager().addAxiom(om.getIndividualsOntology(), ax);
    }

    public OWLNamedIndividual getIndividual() {
        return individual;
    }

    public String getID() {
        return id;
    }

    @Override
    double evaluatePosition(GridPoint pos) {
        Set<GridPoint> points = sim.getGrid().getGridPoints();

        double occupiedPoints = 0, emptyPoints = 0, enemyPoints = 0, bufferLikes = 0, bufferDislikes = 0, provLikes = 0, provDislikes = 0;
        double occupiedCount = 0, emptyCount = 0, enemyCount = 0, bufferLikesCount = 0, bufferDislikesCount = 0, provLikesCount = 0, provDislikesCount = 0;
        for (GridPoint p : points) {
            double distToPoint = pos.distance(p);

            // Distancia media (ponderada pelo numero de habitantes) do edificio aos patches que por ele sao servidos
            if (p.getProviders().contains(this)) {
                occupiedPoints += p.getPop() * distToPoint;
                occupiedCount += p.getPop();
            }

            // Distancia media (ponderada pelo numero de habitantes) do edificio aos patches vazios que podem ser servidos por ele
            if (canServe(p)) {
                emptyPoints += distToPoint * p.getPop();
                emptyCount += p.getPop();
            }

            // Distancia media ponderada aos patches que pertencem a outro provider que ele pode substituir
            if (!p.getProviders().contains(this)) {
                boolean canOverpower = false;
                for (AbstractProvider prov : p.getProviders()) {
                    if (overpowers((FacilityProvider) prov)) {
                        canOverpower = true;
                        break;
                    }
                }
                if (canOverpower) {
                    enemyPoints += distToPoint * p.getPop();
                    enemyCount += p.getPop();
                }
            }
        }

        // Obtem as features caso o calculo nao tenha ja sido feito previamente
        if (affinityFeatures == null || dislikeFeatures == null) {
            OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
            SelectableMap map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
            Set<OWLNamedIndividual> facilities = om.getIndividualsReasoner().getInstances(
                    om.getFactory().getOWLClass(FacilityTypeLoader.FacilitySkeleton.FACILITY.iri), false).getFlattened();

            if (type.getAffinity() != null) {
                Set<OWLNamedIndividual> affinityInds = om.getIndividualsReasoner().getInstances(type.getAffinity(), false).getFlattened();
                affinityInds.removeAll(facilities);
                HashSet<FeatureId> affinityIds = new HashSet<FeatureId>(affinityInds.size());
                for (OWLNamedIndividual i : affinityInds) {
                    affinityIds.add(om.getCorrespondingFeature(i));
                }
                if (!affinityIds.isEmpty()) {
                    affinityFeatures = map.getFeatures(affinityIds);
                } else {
                    affinityFeatures = FeatureCollections.newCollection();
                }
            }
            if (type.getDislike() != null) {
                Set<OWLNamedIndividual> dislikeInds = om.getIndividualsReasoner().getInstances(type.getDislike(), false).getFlattened();
                dislikeInds.removeAll(facilities);
                HashSet<FeatureId> dislikeIds = new HashSet<FeatureId>(dislikeInds.size());
                for (OWLNamedIndividual i : dislikeInds) {
                    dislikeIds.add(om.getCorrespondingFeature(i));
                }
                if (!dislikeIds.isEmpty()) {
                    dislikeFeatures = map.getFeatures(dislikeIds);
                } else {
                    dislikeFeatures = FeatureCollections.newCollection();
                }
            }
        }

        // Distancia media aos edificios do buffer pela qual prefere proximidade 
        if (affinityFeatures != null) {
            FeatureIterator iter = affinityFeatures.features();
            while (iter.hasNext()) {
                SimpleFeature next = (SimpleFeature) iter.next();
                if (!next.getID().equals(id)) {
                    Geometry geom = (Geometry) next.getDefaultGeometry();
                    bufferLikes += distance(pos.getPoint(), geom.getCentroid());
                    bufferLikesCount += 1;
                }
            }
        }

        // Distancia media aos edificios do buffer pela qual prefere afastamento
        if (dislikeFeatures != null) {
            FeatureIterator iter = dislikeFeatures.features();
            while (iter.hasNext()) {
                SimpleFeature next = (SimpleFeature) iter.next();
                if (!next.getID().equals(id)) {
                    Geometry geom = (Geometry) next.getDefaultGeometry();
                    bufferDislikes += distance(pos.getPoint(), geom.getCentroid());
                    bufferDislikesCount += 1;
                }
            }
        }

        // Distancia media aos outros providers pelos quais tem afinidade
        System.out.println(super.getShortID() + " loves: " + providerLoves.size());
        for (FacilityProvider p : providerLoves) {
            provLikes += distance(pos.getPoint(), p.position.getPoint());
            provLikesCount++;
        }

        // Distancia media aos outros providers pelos quais tem conflictos
        System.out.println(super.getShortID() + "hates: " + providerConflicts.size());
        for (FacilityProvider p : providerConflicts) {
            provDislikes += distance(pos.getPoint(), p.position.getPoint());
            provDislikesCount++;
        }

        occupiedPoints = occupiedCount > 0 ? occupiedPoints / occupiedCount : 0;
        emptyPoints = emptyCount > 0 ? emptyPoints / emptyCount : 0;
        enemyPoints = enemyCount > 0 ? enemyPoints / enemyCount : 0;
        bufferLikes = bufferLikesCount > 0 ? bufferLikes / bufferLikesCount : 0;
        bufferDislikes = bufferDislikesCount > 0 ? bufferDislikes / bufferDislikesCount : 0;
        provLikes = provLikes > 0 ? provLikes / provLikesCount : 0;
        provDislikes = provDislikes > 0 ? provDislikes / provDislikesCount : 0;

        double total =
                -occupiedPoints * sim.topComponent.occupiedSlider.getValue() / 100f
                - emptyPoints * sim.topComponent.emptySlider.getValue() / 100f
                - enemyPoints * sim.topComponent.enemySlider.getValue() / 100f
                - bufferLikes * sim.topComponent.bufferProxSlider.getValue() / 100f
                + bufferDislikes * sim.topComponent.bufferProxSlider.getValue() / 100f
                - provLikes * sim.topComponent.providersProxSlider.getValue() / 100f
                + provDislikes * sim.topComponent.bufferProxSlider.getValue() / 100f;

        System.out.printf("%6.3f >> OCCU: %4.3f | EMPT: %4.3f | ENEM: %4.3f | BLIK: %4.3f | BDLIK: %4.3f | PLIK: %4.3f | PDLIK: %4.3f\n",
                total, occupiedPoints, emptyPoints, enemyPoints, bufferLikes, bufferDislikes, provLikes, provDislikes);
        return total;
    }

    private double distance(Point p1, Point p2) {
        GeodeticCalculator calc = new GeodeticCalculator();
        calc.setStartingGeographicPoint(p1.getX(), p1.getY());
        calc.setDestinationGeographicPoint(p2.getX(), p2.getY());
        return calc.getOrthodromicDistance();

    }

    @Override
    Set<AbstractProvider> occupyAreaAround() {
        Set<AbstractProvider> conflicts = new HashSet<AbstractProvider>(sim.getProviders().size());
        // percorrer os patches do centro para o limite definido pela irradiacao
        List<GridPoint> sortedPoints = sim.getGrid().getSortedPoints(position);
        for (GridPoint p : sortedPoints) {
            p.getProviders().remove(this);
        }
        currentCapacity = 0;
        int patchCount = 0;
        for (GridPoint p : sortedPoints) {
            double distanceToPoint = position.distance(p);
            if (distanceToPoint > type.getIrradiation()) {
                continue;
            }

            // verificar se o path a ocupar esta junto da area de ocupacao
            boolean hasNeighbour = false;
            for (GridPoint n : p.getNeighbours()) {
                if (n.getProviders().contains(this)) {
                    hasNeighbour = true;
                    break;
                }
            }

            // se ainda tiver capacidade para o ocupar e estiver junto a area
            if (p.getPop() + currentCapacity < type.getMaxCapacity() && (currentCapacity == 0 || hasNeighbour)) {
                // remover providers com o mesmo conjunto de servicos (ou menor) e a uma distancia maior
                Iterator<AbstractProvider> iter = p.getProviders().iterator();
                while (iter.hasNext()) {
                    FacilityProvider otherProv = (FacilityProvider) iter.next();
                    if (this.overpowers(otherProv) && otherProv.position.distance(p) > distanceToPoint) {
                        otherProv.currentCapacity -= p.getPop();
                        conflicts.add(otherProv);
                        iter.remove();
                    }
                }

                // Colocar como provider
                if (canServe(p)) {
                    p.getProviders().add(this);
                    this.currentCapacity += p.getPop();
                    patchCount++;
                    // Remover os outros que ficaram com servicos redundantes
                    iter = p.getProviders().iterator();
                    while (iter.hasNext()) {
                        FacilityProvider otherProv = (FacilityProvider) iter.next();
                        if (otherProv != this && this.overpowers(otherProv)) {
                            otherProv.currentCapacity -= p.getPop();
                            conflicts.add(otherProv);
                            iter.remove();
                        }
                    }
                }
            }
        }

        return conflicts;
    }

    @Override
    boolean isSatisfied() {
        return currentCapacity > type.getMinCapacity();
    }

    // Se este provider oferece servicos que o point ainda nao possui
    private boolean canServe(GridPoint point) {
        HashSet<Service> providedServices = new HashSet<Service>();
        for (AbstractProvider prov : point.getProviders()) {
            FacilityProvider p = (FacilityProvider) prov;
            providedServices.addAll(p.getType().getServices());
        }
        HashSet<Service> servs = new HashSet<Service>(type.getServices());
        servs.removeAll(providedServices);
        return !servs.isEmpty();
    }

    // Se este provider cobre todos os servicos oferecidos por outro
    private boolean overpowers(FacilityProvider other) {
        HashSet<Service> set = new HashSet<Service>(other.getType().getServices());
        set.removeAll(type.getServices());
        return set.isEmpty();
    }

    public ProviderType getType() {
        return type;
    }

    public double getCurrentCapacity() {
        return currentCapacity;
    }

    @Override
    void die() {
        FacilitySimulator fsim = (FacilitySimulator) sim;
        OntologyManager om = Lookup.getDefault().lookup(OntologyManager.class);
        OWLEntityRemover remover = new OWLEntityRemover(om.getOWLManager(), Collections.singleton(om.getIndividualsOntology()));
        individual.accept(remover);
        om.getOWLManager().applyChanges(remover.getChanges());
        fsim.registerDeath(type);
    }
}
