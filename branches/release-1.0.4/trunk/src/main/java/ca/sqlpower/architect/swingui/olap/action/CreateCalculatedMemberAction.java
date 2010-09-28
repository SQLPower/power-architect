/*
 * Copyright (c) 2009, SQL Power Group Inc.
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
import ca.sqlpower.architect.olap.MondrianModel.CalculatedMember;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.CalculatedMemberEditPanel;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * A subclass of {@link CreateOLAPChildAction} for creating {@link CalculatedMember}s
 */
public class CreateCalculatedMemberAction extends CreateOLAPChildAction<CubePane, CalculatedMember> {

    public CreateCalculatedMemberAction(ArchitectSwingSession session, PlayPen olapPlayPen) {
        super(session, olapPlayPen, "Calculated Member", CubePane.class, "Cube", 'c', OSUtils.FORMULA_ADD_ICON);
    }

    @Override
    protected CalculatedMember addNewChild(CubePane pane) {
        CalculatedMember m = new CalculatedMember();
        
        int count = 1;
        while (!OLAPUtil.isNameUnique(pane.getModel(), CalculatedMember.class, "New Calculated Member " + count)) {
            count++;
        }
        m.setName("New Calculated Member " + count);
        
        pane.getModel().addCalculatedMember(m);
        return m;   
    }

    @Override
    protected DataEntryPanel createDataEntryPanel(CalculatedMember model) {
        return new CalculatedMemberEditPanel(model);
    }

}
