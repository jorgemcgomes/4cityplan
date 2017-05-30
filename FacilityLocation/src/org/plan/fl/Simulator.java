/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jorge
 */
public abstract class Simulator {

    private int turnCounter = 0;
    private int cooldownCounter = 10000;
    
    private LinkedList<AbstractProvider> moveList = new LinkedList();
    private AbstractProvider currentActive;
    private Visualizer vis;
    private Grid grid;
    private Set<AbstractProvider> providers = new HashSet();
    
    FacilityLocationTopComponent topComponent;

    
    public Simulator() {
        grid = new Grid();
        vis = new Visualizer(this);
    }
    
    public void setup() {
        clearEverything();
        topComponent = (FacilityLocationTopComponent) WindowManager.getDefault().findTopComponent("FacilityLocationTopComponent");
        grid.createGrid();
        vis.init();
    }
    
    public void clearEverything() {
        for(AbstractProvider p : providers) {
            p.die();
        }
        moveList.clear();
        currentActive = null;
        turnCounter = 0;
        cooldownCounter = 100000;
        providers.clear();
        vis.clear();
    }

    public void step() {
        System.out.println("--- STEP ---");
        moveProviders();
        turnCounter++;
    }
    
    public void updateMap() {
        vis.update();
    }

    private void moveProviders() {
        // Escolhe o novo provider a actuar
        if (currentActive == null || turnCounter >= topComponent.turnLength.getValue()) {
            turnCounter = 0;
            System.out.println("NEW TURN");
            
            // Possivel morte daquele que acabou o seu turno
            if (currentActive != null) {
                if (currentActive.isSatisfied()) {
                    currentActive.starvingTurns = 0;
                } else {
                    currentActive.starvingTurns++;
                }
                if (currentActive.starvingTurns > topComponent.deathResistance.getValue()) {
                    currentActive.die();
                    providers.remove(currentActive);
                    for (GridPoint p : grid.getGridPoints()) {
                        p.getProviders().remove(currentActive);
                    }
                }
            }
            currentActive = null;

            // Possivel nascimento de um novo
            cooldownCounter++;
            if (cooldownCounter >= topComponent.cooldown.getValue()) {
                cooldownCounter = 0;
                AbstractProvider newBorn = generateProvider();
                if (newBorn != null) {
                    System.out.println("PROVIDER CREATED: " + newBorn.getShortID());
                    providers.add(newBorn);
                    currentActive = newBorn;
                }
            }
            
            // Caso nao tenha nascido nenhum, escolhe o provider que se vai satisfazer da lista
            if (currentActive == null) {
                // A lista ja esta vazia
                if (moveList.isEmpty()) {
                    ArrayList<AbstractProvider> notSatisfied = new ArrayList(providers.size());
                    for(AbstractProvider prov : providers) {
                        if(!prov.isSatisfied()) {
                            notSatisfied.add(prov);
                        }
                    }
                    if(notSatisfied.isEmpty()) {
                        moveList.addAll(providers);
                    } else {
                        moveList.addAll(notSatisfied);
                    }
                    Collections.shuffle(moveList);
                } 
                currentActive = moveList.poll();
            }
        }

        // Actua o provider escolhido
        if(currentActive != null) {
            System.out.println("Satisfying " + currentActive);
            currentActive.satisfy();
        }
    }
    
    Grid getGrid() {
        return grid;
    }
    
    Set<AbstractProvider> getProviders() {
        return providers;
    }

    abstract AbstractProvider generateProvider();
    
    public void updateConditions() {
        
    }
}
