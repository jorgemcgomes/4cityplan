/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.maps;

/**
 *
 * @author Jorge
 */
public interface MapProvider {

    public SelectableMap getMainMap();
    public SelectableMap createSelectableMap();
    public ColoredMap createColoredMap();

}
