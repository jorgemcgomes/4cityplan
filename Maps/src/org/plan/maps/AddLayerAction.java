/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

@ActionID(id = "org.plan.maps.AddLayerAction", category = "Edit")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_AddLayerAction", iconBase = "org/plan/maps/resources/addlayer_big.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Edit", position = 50),
    @ActionReference(path = "Toolbars/Edit", position = 50)})
public final class AddLayerAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	JFileDataStoreChooser dialog = new JFileDataStoreChooser("shp");
	dialog.setMultiSelectionEnabled(true);
	File[] files;
	if (dialog.showOpenDialog(WindowManager.getDefault().findTopComponent("MainMapTopComponent")) == JFileChooser.APPROVE_OPTION) {
	    files = dialog.getSelectedFiles();
	    if (files == null) {
		return;
	    }
	    try {
		SelectableMapImpl map = (SelectableMapImpl) Lookup.getDefault().lookup(MapProvider.class).getMainMap();
		for (File file : files) {
		    map.addFeatureLayer(file);
		}
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }
}
