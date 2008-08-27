/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.olap.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.OLAPTree;

/**
 * This action deletes OLAPObjects that are selected on the OLAPTree using removeChild methods. 
 */
public class OLAPDeleteSelectedAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(OLAPDeleteSelectedAction.class);
    
    private final OLAPEditSession editSession;
    
    private OLAPTree tree;
    
    public OLAPDeleteSelectedAction(ArchitectSwingSession session, OLAPEditSession editSession) {
        super(session, editSession.getOlapPlayPen(), "Delete Selected", "Delete selected item.", (String) null);
        this.editSession = editSession;
        this.tree = editSession.getOlapTree();
    }
    
    public void actionPerformed(ActionEvent arg0) {
        List<OLAPObject> itemsToDelete = getDeletableItems();
        if (itemsToDelete.size() > 1 ) {
            int decision = JOptionPane.showConfirmDialog(editSession.getDialog(), "Are you sure you want to delete the following " + itemsToDelete.size() + " items?",
                            "Multiple Delete", JOptionPane.YES_NO_OPTION);
            if (decision != JOptionPane.YES_OPTION ) {
                return;
            }
        } else if (itemsToDelete.size() < 1) {
            JOptionPane.showMessageDialog(playpen, "No items to delete!");
            return;
        }   
        try {
            playpen.startCompoundEdit("OLAP Delete");
            for (OLAPObject oo : itemsToDelete) {
                if (!parentWasDeleted(oo)) {
                    oo.getParent().removeChild(oo);
                }
            }
        } finally {
            playpen.endCompoundEdit("OLAP Delete End");
        }
        
    }
    
    /**
     * Determines whether or not an ancestor has already been removed.
     */
    private boolean parentWasDeleted(OLAPObject oo) {
        if (oo == null) {
            return true;
        }
        if (oo instanceof Schema) {
            return false;
        } 
        return parentWasDeleted(oo.getParent());
    }

    /**
     * Extracts the list of items we should try to delete from the OLAPTree's
     * selection list.
     */
    private List<OLAPObject> getDeletableItems() {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null) return Collections.emptyList();
        List <OLAPObject> deleteItems = new ArrayList<OLAPObject>(selectionPaths.length);
        for (int i = 0; i < selectionPaths.length; i++) {
            if (   selectionPaths[i].getPathCount() > 1 &&
                   selectionPaths[i].getPathComponent(0) == editSession.getOlapSession().getSchema()) {
                deleteItems.add((OLAPObject) selectionPaths[i].getLastPathComponent());
            } else {
                logger.debug("Skipping non-deletable object: " +
                        selectionPaths[i].getLastPathComponent());
            }
        }
        
        // A set of OLAPObjects of which are selected automatically because their 
        // child has been selected.  
        Set<OLAPObject> objectsWithSelectedItems = new HashSet<OLAPObject>();
        
        for (ListIterator<OLAPObject> it = deleteItems.listIterator(); it.hasNext(); ) {
            OLAPObject item = it.next();
            if (item instanceof Dimension) {
                if (item.getParent() instanceof Cube) {
                    objectsWithSelectedItems.add(item.getParent());
                }
            } else if (item instanceof Measure) {
                objectsWithSelectedItems.add(item.getParent());
            } else if (item instanceof DimensionUsage) {
                objectsWithSelectedItems.add(item.getParent());
            } else if (item instanceof VirtualCubeDimension || item instanceof VirtualCubeMeasure) {
                objectsWithSelectedItems.add(item.getParent());
            } else if (item instanceof Hierarchy) {
                if (item.getParent().getParent() instanceof Schema) {
                    // If the Hierarchy is in a public dimension, then the DimensionPane
                    // is also selected and we don't want to delete that.
                    objectsWithSelectedItems.add(item.getParent());
                } else if (item.getParent().getParent() instanceof Cube) {
                    // If the Hierarchy is in a cube, then the CubePane
                    // is also selected and we don't want to delete that.
                    objectsWithSelectedItems.add(item.getParent().getParent());
                }
            } else if (item instanceof Level){
                if (item.getParent() instanceof Hierarchy) {
                    if (item.getParent().getParent().getParent() instanceof Schema) {
                        // If the Level is in a public dimension, then the DimensionPane
                        // is also selected and we don't want to delete that.
                        objectsWithSelectedItems.add(item.getParent().getParent());
                    } else if (item.getParent().getParent().getParent() instanceof Cube) {
                        // If the Level is in a cube, then the CubePane
                        // is also selected and we don't want to delete that.
                        objectsWithSelectedItems.add(item.getParent().getParent().getParent());
                    }
                }
            } else {
                // for now, allow any other item to be deleted.
            }
        }
        // When a item is selected in the playpen that is contained in a cubePane, 
        // virtualCubePane or DimensionPane, then the matching pane is also selected.
        // In this case we want to delete the selected item(s), but NOT the pane!
        deleteItems.removeAll(objectsWithSelectedItems);
        
        return deleteItems;
    }

}
