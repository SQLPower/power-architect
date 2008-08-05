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

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.olap.MondrianDef.Dimension;
import ca.sqlpower.architect.olap.MondrianDef.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;

public class CreateDimensionAction extends AbstractArchitectAction {

    private final Schema schema;

    public CreateDimensionAction(ArchitectSwingSession session, Schema schema) {
        super(session, "New Dimension...", "Create a new shared dimension in this schema");
        this.schema = schema;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Dimension d = new Dimension();
            d.setInstanceName("New Dimension");
            schema.getDimensions().add(d);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
