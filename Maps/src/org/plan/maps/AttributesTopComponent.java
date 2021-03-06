/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.util.Collection;
import javax.swing.table.DefaultTableModel;
import org.opengis.feature.Property;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

@TopComponent.Description(preferredID = "AttributesTopComponent",persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AttributesTopComponent",preferredID = "AttributesTopComponent")
@ActionID(category = "Window", id = "org.plan.maps.AttributesTopComponent")
@ActionReference(path = "Menu/Window")
public final class AttributesTopComponent extends TopComponent implements LookupListener {

    public AttributesTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(AttributesTopComponent.class, "CTL_AttributesTopComponent"));
        setToolTipText(NbBundle.getMessage(AttributesTopComponent.class, "HINT_AttributesTopComponent"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableScroll = new javax.swing.JScrollPane();
        propsTable = new javax.swing.JTable();

        propsTable.setAutoCreateRowSorter(true);
        propsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        propsTable.setToolTipText(org.openide.util.NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.propsTable.toolTipText")); // NOI18N
        propsTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        propsTable.setFillsViewportHeight(true);
        tableScroll.setViewportView(propsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable propsTable;
    private javax.swing.JScrollPane tableScroll;
    // End of variables declaration//GEN-END:variables

    private Lookup.Result<LastFeature> result = null;

    @Override
    public void componentOpened() {
        MapProvider mp = Lookup.getDefault().lookup(MapProvider.class);
        result = mp.getMainMap().getLookup().lookupResult(LastFeature.class);
        result.allInstances();
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        result = null;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends LastFeature> allInstances = result.allInstances();
        System.out.println("Attributes: Result changed, size " + allInstances.size());
        DefaultTableModel model = (DefaultTableModel) propsTable.getModel();
        // Mostra atributos
        if (allInstances.size() == 1) {
            SimpleFeature feat = allInstances.iterator().next().getFeature();
            model.getDataVector().removeAllElements();
            model.fireTableDataChanged();
            model.addRow(new Object[]{"Feature ID", feat.getID()});
            for (Property prop : feat.getProperties()) {
                String name = prop.getName().getLocalPart();
                Object value = prop.getValue();
                String valueStr = "";
                if (value instanceof Geometry) {
                    name = "Geometry";
                    valueStr = value.getClass().getSimpleName();
                } else {
                    valueStr = value.toString();
                }
                if (!valueStr.equals("")) {
                    model.addRow(new Object[]{name, valueStr});
                }
            }
        } else {
            model.getDataVector().removeAllElements();
            model.fireTableDataChanged();
        }
    }
}
