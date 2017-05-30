/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proj;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.windows.WindowManager;

@ActionID(category = "File", id = "org.plan.proj.OpenProjectAction")
@ActionRegistration(iconBase = "org/plan/proj/resources/open_big.png", displayName = "#CTL_OpenProjectAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 10),
    @ActionReference(path = "Toolbars/File", position = 10)
})
public final class OpenProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	StateManager pm = StateManager.getInstance();
	JFileChooser chooser = new JFileChooser();
	FileNameExtensionFilter filter = new FileNameExtensionFilter("4Plan Project", "4plan");
	chooser.setFileFilter(filter);
	int returnVal = chooser.showOpenDialog(WindowManager.getDefault().findTopComponent("MainMapTopComponent"));
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File f = chooser.getSelectedFile();
	    pm.load(f);
	}
    }
}
