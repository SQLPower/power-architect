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
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.DimensionEditPanel;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateDimensionAction extends AbstractArchitectAction {

    private final Schema schema;

    public CreateDimensionAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "New Dimension...", "Create a new shared dimension in this schema", null);
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Dimension dim = new Dimension();
            dim.setName("New Dimension");
            if (playpen.getSelectedContainers().size() >= 1) {
                // TODO add a DimensionUsage to the selected cube(s)
            }
            DimensionPlacer dp = new DimensionPlacer(
                    new DimensionPane(dim, playpen.getContentPane()));
            dp.dirtyup();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private class DimensionPlacer extends AbstractPlacer {

        private final DimensionPane dp;

        DimensionPlacer(DimensionPane cp) {
            super(CreateDimensionAction.this.playpen);
            this.dp = cp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Dimension Properties";
        }

        @Override
        public DataEntryPanel place(Point p) throws ArchitectException {
            schema.addDimension(dp.getModel());
            playpen.selectNone();
            playpen.addPlayPenComponent(dp, p);
            dp.setSelected(true,SelectionEvent.SINGLE_SELECT);

            DimensionEditPanel editPanel = new DimensionEditPanel(dp.getModel()) {
                @Override
                public void discardChanges() {
                    schema.removeDimension(dp.getModel());
                    playpen.getContentPane().remove(dp);
                }
            };
            return editPanel;
        }
    }

}
