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
import org.openide.awt.ActionRegistration;
import org.openide.windows.WindowManager;

@ActionID(category = "File", id = "org.plan.proj.SaveAsAction")
@ActionRegistration(iconBase = "org/plan/proj/resources/save_big.png", displayName = "#CTL_SaveAsAction")
@ActionReference(path = "Menu/File", position = 30)
public final class SaveAsAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	StateManager pm = StateManager.getInstance();
	JFileChooser chooser = new JFileChooser();
	FileNameExtensionFilter filter = new FileNameExtensionFilter("4Plan Project", "4plan");
	chooser.setFileFilter(filter);
	int returnVal = chooser.showSaveDialog(WindowManager.getDefault().findTopComponent("MainMapTopComponent"));
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File f = chooser.getSelectedFile();
	    pm.saveAs(f);
	}
        
    }
}
