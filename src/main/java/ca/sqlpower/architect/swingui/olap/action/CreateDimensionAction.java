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
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.olap.DimensionEditPanel;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateDimensionAction extends AbstractArchitectAction {

    private final Schema schema;

    public CreateDimensionAction(ArchitectSwingSession session, Schema schema, PlayPen pp) {
        super(session, pp, "New Dimension...", "Create a new shared dimension in this schema (d)", OSUtils.DIMENSION_ADD_ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('d'));
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        Dimension dim = new Dimension();
        dim.setParent(schema);
        
        int count = 1;
        while (!OLAPUtil.isNameUnique(schema, Dimension.class, "New Dimension " + count)) {
            count++;
        }
        dim.setName("New Dimension " + count);
        
        if (getPlaypen().getSelectedContainers().size() >= 1) {
            // TODO add a DimensionUsage to the selected cube(s)
        }
        DimensionPlacer dp = new DimensionPlacer(
                new DimensionPane(dim, getPlaypen().getContentPane()));
        dp.dirtyup();
    }

    private class DimensionPlacer extends AbstractPlacer {

        private final DimensionPane dp;

        DimensionPlacer(DimensionPane cp) {
            super(CreateDimensionAction.this.getPlaypen());
            this.dp = cp;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Dimension Properties";
        }

        @Override
        public DataEntryPanel place(final Point p) throws SQLObjectException {
            DimensionEditPanel editPanel = new DimensionEditPanel(dp.getModel()) {
                @Override
                public boolean applyChanges() {
                    if (super.applyChanges()) {
                        try {
                            getSession().getWorkspace().begin("Create a dimension");
                            schema.addDimension(dp.getModel());
                            playpen.selectNone();
                            playpen.addPlayPenComponent(dp, p);
                            dp.setSelected(true,SelectionEvent.SINGLE_SELECT);
                            getSession().getWorkspace().commit();
                            return true;
                        } catch (Throwable e) {
                            getSession().getWorkspace().rollback("Error occurred: " + e.toString());
                            throw new RuntimeException(e);
                        }
                    } else {
                        return false;
                    }
                }
            };
            return editPanel;
        }
    }

}


