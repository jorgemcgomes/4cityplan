/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.maps;

import java.util.Set;
import org.opengis.filter.identity.FeatureId;
import org.openide.util.Lookup;

/**
 *
 * @author Jorge
 */
public interface SelectableMap extends ColoredMap, Lookup.Provider {

    public void enableRestrictions(boolean enable);
    public Set<FeatureId> getRestrictions();
    public void clearSelectedFeatures();
    public void selectFeatures(Set<FeatureId> feats, boolean positive);
    public Set<FeatureId> errorFeatures();

}
