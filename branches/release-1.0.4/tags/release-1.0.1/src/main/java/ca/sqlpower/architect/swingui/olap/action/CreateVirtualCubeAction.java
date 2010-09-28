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

import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.architect.swingui.olap.VirtualCubeEditPanel;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateVirtualCubeAction extends AbstractArchitectAction {

    private final Schema schema;

    public CreateVirtualCubeAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "New Virtual Cube...", "Create a new Virtual Cube in this schema (v)", OSUtils.VIRTUAL_CUBE_ADD_ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('v'));
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        PlayPenContentPane pane = getPlaypen().getContentPane();
        pane.begin("Creating VirtualCube and VirtualCubePane");
        try {
            VirtualCube vCube = new VirtualCube();

            int count = 1;
            while (!OLAPUtil.isNameUnique(schema, VirtualCube.class, "New Virtual Cube " + count)) {
                count++;
            }
            vCube.setName("New Virtual Cube " + count);

            VirtualCubePane cp = new VirtualCubePane(vCube, getPlaypen().getContentPane());
            VirtualCubePlacer cubePlacer = new VirtualCubePlacer(cp);
            cubePlacer.dirtyup();
            pane.commit();
        } catch (Throwable ex) {
            pane.rollback("Error creating Cube: " + ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private class VirtualCubePlacer extends AbstractPlacer {

        private final VirtualCubePane vcp;

        VirtualCubePlacer(VirtualCubePane vcp) {
            super(CreateVirtualCubeAction.this.getPlaypen());
            this.vcp = vcp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Virtual Cube Properties";
        }

        @Override
        public DataEntryPanel place(Point p) throws SQLObjectException {
            
            try {
                getSession().getWorkspace().begin("Create a virtual cube");
                schema.addVirtualCube(vcp.getModel());            
                playpen.selectNone();
                playpen.addPlayPenComponent(vcp, p);
                vcp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                getSession().getWorkspace().commit();
            } catch (Throwable e) {
                getSession().getWorkspace().rollback("Error occurred: " + e.toString());
                throw new RuntimeException(e);
            }

            VirtualCubeEditPanel editPanel = new VirtualCubeEditPanel(vcp.getModel()) {
                @Override
                public void discardChanges() {
                    schema.removeVirtualCube(vcp.getModel());
                }
                
                @Override
                public boolean applyChanges() {
                    return super.applyChanges();
                }
            };
            return editPanel;
        }
    }
}
