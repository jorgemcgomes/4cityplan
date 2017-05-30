/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.plan.proj;

import java.io.File;

/**
 *
 * @author jorge
 */
public interface StateProvider {

    public void newProject();
    public void restoreProject(File toLoad, byte[] state);
    public byte[] saveProject(File toSave);
    public int getOrder();
    
}