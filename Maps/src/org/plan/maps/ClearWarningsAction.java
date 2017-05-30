/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;

@ActionID(id = "org.plan.maps.impl.ClearWarningsAction", category = "Edit")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_ClearWarningsAction", iconBase = "org/plan/maps/resources/warning_big.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Edit", position = 60),
    @ActionReference(path = "Toolbars/Edit", position = 60)})
public final class ClearWarningsAction implements ActionListener {

    private SelectableMap map;

    @Override
    public void actionPerformed(ActionEvent e) {
	if (map == null) {
	    map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
	}
	map.errorFeatures().clear();
	map.update();
    }
}
