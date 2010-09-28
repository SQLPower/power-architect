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

import javax.swing.JPopupMenu;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PopupMenuFactory;
import ca.sqlpower.architect.swingui.action.ExportPlaypenToPDFAction;
import ca.sqlpower.architect.swingui.olap.action.CreateEnergonCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.EditHierarchyAction;
import ca.sqlpower.architect.swingui.olap.action.EditLevelAction;
import ca.sqlpower.architect.swingui.olap.action.EditMeasureAction;
import ca.sqlpower.architect.swingui.olap.action.EditSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.EditVirtualCubeAction;

/**
 * Creates context menus for the tree or the playpen. (Or anyone else who wants one).
 */
public class OLAPContextMenuFactory implements PopupMenuFactory {

    private final ArchitectSwingSession session;
    private final OLAPEditSession oSession;
    
    public OLAPContextMenuFactory(ArchitectSwingSession session, OLAPEditSession oSession) {
        this.session = session;
        this.oSession = oSession;
    }
    
    /**
     * Creates a context menu for the given olap object. This menu should be
     * appropriate for the tree or the PlayPen.
     * 
     * @param sourceComponent
     *            The source component which the popup menu for, if the given
     *            source component is null, he a popup for the playpen is
     *            created instead
     * @return The popup menu for the given source
     */
    public JPopupMenu createPopupMenu(Object sourceComponent) {
        OLAPObject obj = (OLAPObject) sourceComponent;
        JPopupMenu m = new JPopupMenu();
        if (obj == null) {
            m.add(oSession.getCreateCubeAction());
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateVirtualCubeAction());
            m.addSeparator();
            m.add(oSession.getExportSchemaAction());
            m.add(new ExportPlaypenToPDFAction(session, oSession.getOlapPlayPen()));
        } else if (obj instanceof Schema) {
            m.add(oSession.getCreateCubeAction());
            m.add(oSession.getCreateDimensionAction());
            m.add(oSession.getCreateVirtualCubeAction());
            m.add(new CreateEnergonCubeAction(session, oSession.getOlapPlayPen()));
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
            m.add(oSession.getCreateCalculatedMemberAction());
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
        } else if (obj instanceof Hierarchy) {
            m.add(new EditHierarchyAction(session, (Hierarchy) obj, oSession.getOlapPlayPen()));
            m.add(oSession.getCreateLevelAction());
        } else if (obj instanceof Level) {
            m.add(new EditLevelAction(session, (Level) obj, oSession.getOlapPlayPen()));
        }
        
        if (obj != null && !(obj instanceof Schema)) {
            m.addSeparator();
            m.add(oSession.getOLAPDeleteSelectedAction());
        }
        
        return m;
        
    }
}
