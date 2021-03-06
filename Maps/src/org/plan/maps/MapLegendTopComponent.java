/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.maps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
//import org.openide.util.ImageUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.plan.maps.ColoredMapImpl.ColorLayer;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = "MapLegendTopComponent",persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MapLegendTopComponent",preferredID = "MapLegendTopComponent")
@ActionID(category = "Window", id = "org.plan.maps.MapLegendTopComponent")
@ActionReference(path = "Menu/Window")
public final class MapLegendTopComponent extends TopComponent implements ExplorerManager.Provider {

    private ExplorerManager explorer;
    private LegendFactory factory;

    public MapLegendTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(MapLegendTopComponent.class, "CTL_MapLegendTopComponent"));
        setToolTipText(NbBundle.getMessage(MapLegendTopComponent.class, "HINT_MapLegendTopComponent"));
        explorer = new ExplorerManager();
        factory = new LegendFactory();
        explorer.setRootContext(new AbstractNode(Children.create(factory, true)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listView = new ListView();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(listView, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(listView, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane listView;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return explorer;
    }
    private List<ColorLayer> layers;

    void updateLegend(List<ColorLayer> layers) {
        this.layers = layers;
        factory.refresh();
    }

    private class LegendFactory extends ChildFactory<ColorLayer> {

        @Override
        protected boolean createKeys(List<ColorLayer> list) {
            if (layers != null) {
                list.addAll(layers);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(ColorLayer key) {
            return new LegendNode(key);
        }

        void refresh() {
            super.refresh(false);
        }
    }

    class LegendNode extends AbstractNode {

        private Color fill;
        private Color border;
        private String descr;

        private LegendNode(ColorLayer layer) {
            super(Children.LEAF);
            fill = layer.getFillColor();
            border = layer.getBorderColor();
            descr = layer.getDescription();
            super.setDisplayName(descr);
        }

        @Override
        public Image getIcon(int type) {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            if (border != null) {
                g.setColor(border);
                g.drawRect(0, 0, 15, 15);
            }
            if (fill != null) {
                g.setColor(fill);
                g.fillRect(1, 1, 14, 14);
            }
            return img;
        }
    }
}
