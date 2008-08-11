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
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.CubeEditPanel;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateCubeAction extends AbstractArchitectAction {

    private final Schema schema;
    
    private final PlayPen pp;

    public CreateCubeAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, "New Cube...", "Create a new cube in this schema");
        this.schema = schema;
        this.pp = pp;
    }

    public void actionPerformed(ActionEvent e) {
        Cube cube = new Cube();
        cube.setName("New Cube");
        CubePane cp = new CubePane(cube, pp.getContentPane());
        CubePlacer cubePlacer = new CubePlacer(cp);
        cubePlacer.dirtyup();
    }

    private class CubePlacer extends AbstractPlacer {

        private final CubePane cp;

        CubePlacer(CubePane cp) {
            super(pp);
            this.cp = cp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Cube Properties";
        }

        @Override
        public DataEntryPanel place(Point p) throws ArchitectException {
            schema.addCube(cp.getModel());
            pp.selectNone();
            pp.addPlayPenComponent(cp, p);
            cp.setSelected(true,SelectionEvent.SINGLE_SELECT);

            CubeEditPanel editPanel = new CubeEditPanel(cp.getModel()) {
                @Override
                public void discardChanges() {
                    schema.removeCube(cp.getModel());
                }
            };
            return editPanel;
        }
    }

}
