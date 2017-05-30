package org.plan.fl;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A list of checkboxes which maintains a list of custom objects
 * http://www.lime49.com/
 * Copyright 2009 by Harry Jennerway
 * All rights reserved.
 */
public class CheckBoxList<T> extends JList {
    protected static Border noFocusBorder = new EmptyBorder(1, 4, 1, 4);
    private CheckBoxListModel<T> model;

    /**
     * Initializes a new CheckBoxList with the specified object collection
     * @param items The items to add to the list
     */
    public CheckBoxList(LinkedHashMap<T, Boolean> items) {
        model = new CheckBoxListModel<T>(items);
        setModel(model);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if(index != -1) {
                    ListItem<T> item = (ListItem<T>)model.getElementAt(index);
                    item.selected = !item.selected;
                    revalidate();
                    repaint();
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new CellRenderer());
    }

    /**
     * Selects all items in the list
     */
    public void selectAll() {
        model.selectAll();
        revalidate();
        repaint();
    }

    /**
     * Deselects all items in the list
     */
    public void selectNone() {
        model.selectNone();
        revalidate();
        repaint();
    }

    @Override
    public String getToolTipText(MouseEvent evt) {
        int index = locationToIndex(evt.getPoint());
        if(index == -1) {
            return "";
        } else {
            ListItem<T> item = (ListItem<T>)model.getElementAt(index);
            return item.dataItem.toString();
        }
    }

    /**
     * Renders ListItems as JCheckBoxes whose text is set by the .toString() method of the dataItem in the Item
     */
    protected class CellRenderer implements ListCellRenderer {
        private JCheckBox checkBox;
        private Box containerBox;
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            if(containerBox == null) {
                containerBox = Box.createHorizontalBox();
                checkBox = new JCheckBox();
                checkBox.setEnabled(isEnabled());
                checkBox.setFont(getFont());
                checkBox.setFocusPainted(false);
                checkBox.setBorderPainted(true);
                checkBox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
                containerBox.add(checkBox);
            }
            checkBox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkBox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            if(value != null) {
                ListItem item = (ListItem)value;
                checkBox.setText(item.dataItem.toString());
                checkBox.setSelected(item.selected);
            }
            return containerBox;
        }
    }

    /**
     * Returns the list of items in the CheckBoxList
     * @return The list of items in the CheckBoxList
     */
    public LinkedHashMap<T, Boolean> getItems() {
        return model.getItems();
    }

    /**
     * Maintains a sorted list of ListItems for rendering a CheckBoxList
     * @param <T> The type of objects in the list
     */
    class CheckBoxListModel<T> extends AbstractListModel {
        private LinkedList<ListItem<T>> items = new LinkedList<ListItem<T>>(); // damn Java for lacking an indexOf method in LinkedHashMap

        /**
         * Initializes a new CheckBoxListModel
         * @param items The items to add to the list
         */
        public CheckBoxListModel(LinkedHashMap<T, Boolean> items) {
            Iterator<T> iter = items.keySet().iterator();
            while(iter.hasNext()) {
                T acc = iter.next();
                this.items.add(new ListItem<T>(acc, items.get(acc)));
            }
        }

        /**
         * Returns the size of the list
         * @return The size of the list
         */
        public int getSize() {
            return items.size();
        }

        /**
         * Returns the ListItem at the specified index
         * @param index The index of the item to find
         * @return The ListItem at the specified index
         */
        public ListItem<T> getElementAt(int index) {
            return items.get(index);
        }

        /**
         * Returns the collection of items in the list
         * @return The collection of items in the list
         */
        private LinkedHashMap<T, Boolean> getItems() {
            LinkedHashMap<T, Boolean> map = new LinkedHashMap<T, Boolean>();
            for(ListItem<T> item : items) {
                map.put(item.dataItem, item.selected);
            }
            return map;
        }

        /**
         * Selects all items in the list
         */
        public void selectAll() {
            for(ListItem<T> item : items) {
                item.selected = true;
            }
        }

        /**
         * Deselects all items in the list
         */
        public void selectNone() {
            for(ListItem<T> item : items) {
                item.selected = false;
            }
        }
    }

    /**
     * Holds an item of the specified type and a value indicating whether or not it is selected
     * @param <T> The type of the item
     */
    class ListItem<T> {
        public T dataItem;
        public boolean selected;
        
        /**
         * Initializes a new ListItem
         * @param dataItem The item to display
         * @param selected <c>true</c> if the item is selected, otherwise <c>false</c>
         */
        public ListItem(T dataItem, boolean selected) {
            this.dataItem = dataItem;
            this.selected = selected;
        }
    }
}
