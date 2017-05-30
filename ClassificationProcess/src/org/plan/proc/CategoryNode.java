/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.proc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.plan.proc.LandUseExplorer.LandUseNode;

/**
 *
 * @author Jorge
 */
public class CategoryNode extends ClassifyNode {

    private LandUseNode node;

    CategoryNode(LandUseNode node) {
	super(node);
	this.node = node;
    }

    @Override
    public Image getIcon(int type) {
	if (node.getColor() != null) {
	    BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = img.createGraphics();
	    g.setBackground(new Color(0,0,0,0));
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g.setColor(node.getColor());
	    g.fillOval(1, 1, 14, 14);
	    g.setColor(Color.BLACK);
	    g.drawOval(0, 0, 15, 15);
	    return img;
	} else {
	    return super.getIcon(type);
	}
    }
}
