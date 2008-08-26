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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.VirtualCubeEditPanel;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateVirtualCubeAction extends AbstractArchitectAction {

    private final Schema schema;

    public CreateVirtualCubeAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "New Virtual Cube...", "Create a new Virtual Cube in this schema", (String) null);
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        VirtualCube vCube = new VirtualCube();

        int count = 1;
        while (!OLAPUtil.isNameUnique(schema, VirtualCube.class, "New Virtual Cube " + count)) {
            count++;
        }
        vCube.setName("New Virtual Cube " + count);
        
        VirtualCubePane cp = new VirtualCubePane(vCube, playpen.getContentPane());
        VirtualCubePlacer cubePlacer = new VirtualCubePlacer(cp);
        cubePlacer.dirtyup();
    }

    private class VirtualCubePlacer extends AbstractPlacer {

        private final VirtualCubePane vcp;

        VirtualCubePlacer(VirtualCubePane vcp) {
            super(CreateVirtualCubeAction.this.playpen);
            this.vcp = vcp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Virtual Cube Properties";
        }

        @Override
        public DataEntryPanel place(Point p) throws ArchitectException {
            schema.addVirtualCube(vcp.getModel());
            playpen.selectNone();
            playpen.addPlayPenComponent(vcp, p);
            vcp.setSelected(true,SelectionEvent.SINGLE_SELECT);

            VirtualCubeEditPanel editPanel = new VirtualCubeEditPanel(vcp.getModel()) {
                @Override
                public void discardChanges() {
                    schema.removeVirtualCube(vcp.getModel());
                }
            };
            return editPanel;
        }
    }
}
