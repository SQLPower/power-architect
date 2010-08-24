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
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.CubeEditPanel;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateCubeAction extends AbstractArchitectAction {

    private final Schema schema;
    
    public CreateCubeAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "New Cube...", "Create a new cube in this schema (c)", OSUtils.CUBE_ADD_ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('c'));
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        PlayPenContentPane pane = getPlaypen().getContentPane();
        pane.begin("Creating a Cube and its CubePane");
        try {
            Cube cube = new Cube();

            int count = 1;
            while (!OLAPUtil.isNameUnique(schema, Cube.class, "New Cube " + count)) {
                count++;
            }
            cube.setName("New Cube " + count);

            CubePane cp = new CubePane(cube, pane);
            CubePlacer cubePlacer = new CubePlacer(cp);
            cubePlacer.dirtyup();
            pane.commit();
        } catch (Throwable ex) {
            pane.rollback("Error creating Cube: " + ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private class CubePlacer extends AbstractPlacer {

        private final CubePane cp;

        CubePlacer(CubePane cp) {
            super(CreateCubeAction.this.getPlaypen());
            this.cp = cp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Cube Properties";
        }

        @Override
        public DataEntryPanel place(Point p) throws SQLObjectException {
            try {
                getSession().getWorkspace().begin("Create Cube");
                schema.addCube(cp.getModel());
                playpen.selectNone();
                playpen.addPlayPenComponent(cp, p);
                cp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                getSession().getWorkspace().commit();
            } catch (Throwable e) {
                getSession().getWorkspace().rollback("Exception occured: " + e.toString());
                throw new RuntimeException(e);
            }

            CubeEditPanel editPanel = new CubeEditPanel(cp.getModel(), cp.getPlayPen(), cp.getPlayPen().getSession()) {
                @Override
                public void discardChanges() {
                    schema.removeCube(cp.getModel());
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
