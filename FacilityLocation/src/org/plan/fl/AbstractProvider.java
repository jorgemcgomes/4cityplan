/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jorge
 */
public abstract class AbstractProvider {
    
    protected GridPoint position;
    protected int totalMoves = 0;
    protected int starvingTurns = 0;
    private boolean fixed;
    private String shortID = "DEF";
    protected Simulator sim;
    
    AbstractProvider(Simulator sim) {
        this.sim = sim;
    }
    
    void setShortID(String id) {
        shortID = id;
    }
    
    String getShortID() {
        return shortID;
    }
    
    void satisfy() {
        GridPoint p = findSatisfyLocation();
        doSatisfaction(p);
        totalMoves++;
    }
    
    private GridPoint findSatisfyLocation() {
        GridPoint best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for(GridPoint p : position.getNeighbours()) {
            double val = evaluatePosition(p);
            if(val > bestValue) {
                best = p;
                bestValue = val;
            }
        }
        double selfEval = evaluatePosition(position);
        if(best != null && selfEval < bestValue) {
            return best;
        } else {
            return position;
        }
    }
    
    private void doSatisfaction(final GridPoint p) {
        position = p;
        Set<AbstractProvider> conflicts = occupyAreaAround();
        List<AbstractProvider> orderedConflicts = new ArrayList(conflicts);
        Collections.sort(orderedConflicts, new Comparator<AbstractProvider>() {
            @Override
            public int compare(AbstractProvider prov1, AbstractProvider prov2) {
                double dist1 = prov1.position.distance(p);
                double dist2 = prov2.position.distance(p);
                if(dist1 == dist2) {
                    return 0;
                } else if(dist1 > dist2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }); 
        for(AbstractProvider prov : orderedConflicts) {
            LinkedList<AbstractProvider> chain = new LinkedList();
            chain.add(this);
            prov.escape(chain, p);
        }        
    }
    
    private void escape(LinkedList<AbstractProvider> attackChain, GridPoint attackerMove) {
        if(!attackChain.contains(this)) {
            GridPoint escapeLocation = findEscapeLocation(attackerMove);
            doFlee(attackChain, escapeLocation);
        }
    }
    
    private GridPoint findEscapeLocation(GridPoint attackerMove) {
        GridPoint best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double currentDist = attackerMove.distance(position);
        for(GridPoint neighbour : position.getNeighbours()) {
            if(attackerMove.distance(neighbour) > currentDist) {
                double val = evaluatePosition(neighbour);
                if(val > bestValue) {
                    best = neighbour;
                    bestValue = val;
                }                
            }
        }
        if(best != null) {
            return best;
        } else {
            return position;
        }
    }
    
    private void doFlee(LinkedList<AbstractProvider> attackChain, final GridPoint escapeLocation) {
        position = escapeLocation;
        Set<AbstractProvider> conflicts = occupyAreaAround();        
        List<AbstractProvider> orderedConflicts = new ArrayList(conflicts); 
        Collections.sort(orderedConflicts, new Comparator<AbstractProvider>() {
            @Override
            public int compare(AbstractProvider prov1, AbstractProvider prov2) {
                double dist1 = prov1.position.distance(escapeLocation);
                double dist2 = prov2.position.distance(escapeLocation);
                if(dist1 == dist2) {
                    return 0;
                } else if(dist1 > dist2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        for(AbstractProvider conflict : orderedConflicts) {
            LinkedList<AbstractProvider> newChain = new LinkedList(attackChain);
            newChain.addFirst(this);
            conflict.escape(newChain, escapeLocation);
        }
    }
    
    abstract void die();
    abstract double evaluatePosition(GridPoint pos);
    abstract Set<AbstractProvider> occupyAreaAround();
    abstract boolean isSatisfied();
    
}
