/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author Jorge
 */
public class ProviderType {
    
    private String id;
    private String name;
    private int minCapacity;
    private int maxCapacity;
    private int irradiation;
    private Set<Service> services = new HashSet<Service>();
    private OWLNamedIndividual owlIndividual;
    private OWLClassExpression affinity;
    private OWLClassExpression dislike;

    public ProviderType(OWLNamedIndividual individual, String id, String name) {
        this.owlIndividual = individual;
        this.id = id;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }    

    public int getIrradiation() {
        return irradiation;
    }

    public void setIrradiation(int irradiation) {
        this.irradiation = irradiation;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(int minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Set<Service> getServices() {
        return services;
    }

    public OWLNamedIndividual getOwlIndividual() {
        return owlIndividual;
    }

    @Override
    public String toString() {
        return name+" ("+id+")";
    }

    public OWLClassExpression getAffinity() {
        return affinity;
    }

    public void setAffinity(OWLClassExpression affinity) {
        this.affinity = affinity;
    }

    public OWLClassExpression getDislike() {
        return dislike;
    }

    public void setDislike(OWLClassExpression dislike) {
        this.dislike = dislike;
    }
    
    public static class Service {
        
        private OWLNamedIndividual owlIndividual;
        
        public Service(OWLNamedIndividual owlIndividual) {
            this.owlIndividual = owlIndividual;
        }
    }
}
