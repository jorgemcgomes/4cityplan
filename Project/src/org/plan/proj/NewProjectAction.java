/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proj;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File", id = "org.plan.proj.NewProjectAction")
@ActionRegistration(iconBase = "org/plan/proj/resources/new_big.png", displayName = "#CTL_NewProjectAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 0),
    @ActionReference(path = "Toolbars/File", position = 0)
})
@Messages("CTL_NewProjectAction=New Project")
public final class NewProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        StateManager.getInstance().newProj();
    }
}
