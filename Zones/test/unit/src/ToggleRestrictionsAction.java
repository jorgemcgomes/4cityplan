/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.plan.maps.MapProvider;
import org.plan.maps.SelectableMap;

public final class ToggleRestrictionsAction extends AbstractAction implements Presenter.Toolbar {

    private SelectableMap map;

    public ToggleRestrictionsAction() {
        map = Lookup.getDefault().lookup(MapProvider.class).getMainMap();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Nothing to do here
    }

    @Override
    public Component getToolbarPresenter() {
        final Icon icon = new ImageIcon(ImageUtilities.loadImage("org/plan/zones/resources/restrict_big.png"));
        final JToggleButton button = new JToggleButton(icon);
        button.setToolTipText(NbBundle.getMessage(ToggleRestrictionsAction.class, "CTL_ToggleRestrictionsAction"));
        button.setSelected(false);
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(button.isSelected()) {
                    map.enableRestrictions(true);
                } else {
                    map.enableRestrictions(false);
                }
            }
        });
        return button;
    }
}
