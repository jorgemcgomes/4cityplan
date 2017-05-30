/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author jorge
 */
public class StateManager {
    
    private static StateManager instance;
    private File location;
    
    synchronized static StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
        }
        return instance;
    }
    
    private StateManager() {
        // Do nothing
    }
    
    void newProj() {
        Collection<? extends StateProvider> all =
                Lookup.getDefault().lookupAll(StateProvider.class);
        List<? extends StateProvider> providerList = new LinkedList<StateProvider>(all);
        Collections.sort(providerList, new Comparator<StateProvider>() {
            
            @Override
            public int compare(StateProvider t1, StateProvider t2) {
                return t1.getOrder() > t2.getOrder() ? 1 : -1;
            }
        });
        for (StateProvider prov : providerList) {
            prov.newProject();
        }
    }
    
    void saveAs(File file) {
        if (!file.getName().endsWith(".4plan")) {
            file = new File(file.getPath() + ".4plan");
        }
        this.location = file;
        Collection<? extends StateProvider> all =
                Lookup.getDefault().lookupAll(StateProvider.class);
        List<? extends StateProvider> providerList = new LinkedList<StateProvider>(all);
        Collections.sort(providerList, new Comparator<StateProvider>() {
            
            @Override
            public int compare(StateProvider t1, StateProvider t2) {
                return t1.getOrder() > t2.getOrder() ? 1 : -1;
            }
        });
        
        LinkedHashMap<String, Serializable> map = new LinkedHashMap<String, Serializable>();
        for (StateProvider prov : providerList) {
            map.put(prov.getClass().getName(), prov.saveProject(location));
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(location);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(map);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    void save() throws IOException {
        if (location == null) {
            throw new IOException("Location not defined yet");
        } else {
            saveAs(location);
        }
    }
    
    void load(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fis);
            LinkedHashMap<String, byte[]> map;
            map = (LinkedHashMap<String, byte[]>) in.readObject();
            Collection<? extends StateProvider> allProviders = Lookup.getDefault().lookupAll(StateProvider.class);
            for (Entry<String, byte[]> e : map.entrySet()) {
                for (StateProvider provider : allProviders) {
                    if (provider.getClass().getName().equals(e.getKey())) {
                        provider.restoreProject(file, e.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static byte[] objectToBytes(Serializable ser) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(ser);
            bytes = bos.toByteArray();
            out.close();
            bos.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return bytes;
    }
    
    public static Object bytesToObject(byte[] bytes) {
        Object o = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            o = in.readObject();
            bis.close();
            in.close();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return o;
    }
}
