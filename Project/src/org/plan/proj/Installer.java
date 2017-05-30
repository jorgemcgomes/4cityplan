/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proj;

import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // do something
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                 StartupDialog diag = new StartupDialog(WindowManager.getDefault().getMainWindow(), true);
                 diag.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                 diag.setVisible(true);
            }
        });
    }
}
