/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.plan.proj.StateProvider;
import org.plan.proj.StateManager;

/**
 *
 * @author Jorge
 */
@ServiceProviders(value={
    @ServiceProvider(service=MapProvider.class),
    @ServiceProvider(service=StateProvider.class)}
)
public class GeotoolsMapProvider implements MapProvider, StateProvider {

    private SelectableMap mainMap;

    @Override
    public synchronized SelectableMap getMainMap() {
	if(mainMap == null) {
	    mainMap = new SelectableMapImpl();
	}
	return mainMap;
    }

    @Override
    public SelectableMap createSelectableMap() {
	return new SelectableMapImpl();
    }

    @Override
    public ColoredMap createColoredMap() {
	return new ColoredMapImpl();
    }

    @Override
    public void restoreProject(File toLoad, byte[] state) {
	getMainMap().resetMap();
	ArrayList<String> layers = (ArrayList<String>) StateManager.bytesToObject(state);
	for(String l : layers) {
	    try {
                File f = new File(toLoad.getParentFile(), l);
		getMainMap().addFeatureLayer(f);
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }

    @Override
    public byte[] saveProject(File toSave) {
	LinkedList<File> layers = getMainMap().getFeatureLayers();
        ArrayList<String> fileNames = new ArrayList<String>(layers.size());
        for(File f : layers) {
            fileNames.add(f.getName());
        }
	return StateManager.objectToBytes(fileNames);
    }

    @Override
    public int getOrder() {
	return 0;
    }

    @Override
    public void newProject() {
        ;
    }

}
