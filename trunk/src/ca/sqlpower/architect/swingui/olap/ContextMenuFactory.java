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

package ca.sqlpower.architect.swingui.olap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PopupMenuFactory;
import ca.sqlpower.architect.swingui.olap.action.EditCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.EditMeasureAction;
import ca.sqlpower.architect.swingui.olap.action.EditSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.EditVirtualCubeAction;

/**
 * Creates context menus for the tree or the playpen. (Or anyone else who wants one).
 */
public class ContextMenuFactory implements PopupMenuFactory {

    private final ArchitectSwingSession session;
    private final OLAPEditSession oSession;
    
    public ContextMenuFactory(ArchitectSwingSession session, OLAPEditSession oSession) {
        this.session = session;
        this.oSession = oSession;
    }
    
    /**
     * Creates a context menu for the given olap object. This menu should be
     * appropriate for the tree or the PlayPen.
     * <p>
     * TODO: support multi-select (pass in a list of selected objects)
     * 
     * @param obj The items the menu is for. If no items are selected, pass in null.
     * @return
     */
    public JPopupMenu createContextMenu(OLAPObject obj) {
        JPopupMenu m = new JPopupMenu();
        
        if (obj == null) {
            m.add(oSession.getCreateCubeAction());
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateVirtualCubeAction());
            m.addSeparator();
            m.add(oSession.getExportSchemaAction());
        } else if (obj instanceof Schema) {
            m.add(oSession.getCreateCubeAction());
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateVirtualCubeAction());
            m.addSeparator();
            m.add(new EditSchemaAction(session,(Schema) obj, oSession.getOlapPlayPen()));
            m.addSeparator();
            m.add(oSession.getExportSchemaAction());
        } else if (obj instanceof Dimension) {
            m.add(oSession.getCreateHierarchyAction());
            m.addSeparator();
            m.add(new EditDimensionAction(session, (Dimension)obj, oSession.getOlapPlayPen()));
        } else if (obj instanceof Cube) {
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateMeasureAction());
            m.addSeparator();
            m.add(new EditCubeAction(session, (Cube)obj, oSession.getOlapPlayPen()));
        } else if (obj instanceof VirtualCube) {
            m.add(oSession.getCreateCubeAction());
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateMeasureAction());
            m.addSeparator();
            m.add(new EditVirtualCubeAction(session, (VirtualCube)obj, oSession.getOlapPlayPen()));
        } else if (obj instanceof Measure) {
            m.add(new EditMeasureAction(session, (Measure) obj, oSession.getOlapPlayPen()));
        }
        
        if (obj != null && !(obj instanceof Schema)) {
            m.addSeparator();
            m.add(oSession.getOLAPDeleteSelectedAction());
        }
        
        return m;
    }

    /**
     * Gathers the selected items from the play pen and returns the appropriate
     * popup menu.
     */
    public JPopupMenu createPopupMenu() {
        List<OLAPObject> selectedObjects = new ArrayList<OLAPObject>();
        PlayPen pp = oSession.getOlapPlayPen();
        for (PlayPenComponent ppc : pp.getSelectedItems()) {
            if (ppc.getModel() instanceof OLAPObject) {
                selectedObjects.add((OLAPObject) ppc.getModel());
            }
        }
        if (selectedObjects.isEmpty()) {
            return createContextMenu(null);
        } else {
            return createContextMenu(selectedObjects.get(0));
        }
    }
}
