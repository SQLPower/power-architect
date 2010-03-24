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

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.MeasureEditPanel;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.DataEntryPanel;

public class CreateMeasureAction extends CreateOLAPChildAction<CubePane, Measure> {
    
    public CreateMeasureAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "Measure", CubePane.class, "Cube", 'm', OSUtils.MEASURE_ADD_ICON);
    }

    @Override
    protected Measure addNewChild(CubePane pane) {
        Measure m = new Measure();
        
        int count = 1;
        while (!OLAPUtil.isNameUnique(pane.getModel(), Measure.class, "New Measure " + count)) {
            count++;
        }
        m.setName("New Measure " + count);
        
        pane.getModel().addMeasure(m);
        return m;
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(Measure model) {
        try {
            return new MeasureEditPanel(model) {
                @Override
                public boolean applyChanges() {
                    boolean applied = super.applyChanges();
                    return applied;
                }
            };
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        }
    }
    
}
