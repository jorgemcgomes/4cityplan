/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import javax.swing.SwingUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.openide.util.Lookup;

/**
 *
 * @author jorge
 */
public abstract class SelectedFeaturesAction extends ContextAction {

    protected Lookup.Result<SimpleFeature> mapLookup;
    protected SelectableMap map;

    public SelectedFeaturesAction() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
                Lookup lkp = Lookup.getDefault().lookup(MapProvider.class).getMainMap().getLookup();
                mapLookup = lkp.lookupResult(SimpleFeature.class);
                listenResult(mapLookup);
            }
        });
    }
}
