/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author jorge
 */
public class LastFeature {

    private SimpleFeature feature;

    public LastFeature(SimpleFeature feat) {
        this.feature = feat;
    }

    public SimpleFeature getFeature() {
        return feature;
    }
}
