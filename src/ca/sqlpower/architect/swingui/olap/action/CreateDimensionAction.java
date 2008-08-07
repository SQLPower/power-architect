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

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;

public class CreateDimensionAction extends AbstractArchitectAction {

    private final Schema schema;
    private final PlayPen playpen;

    public CreateDimensionAction(ArchitectSwingSession session, Schema schema) {
        super(session, "New Dimension...", "Create a new shared dimension in this schema");
        this.schema = schema;
        playpen = session.getArchitectFrame().getOlapSchemaEditor().getOlapPlayPen();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Dimension dim = new Dimension();
            dim.setName("New Dimension");
            schema.addDimension(dim);
            if (playpen.getSelectedContainers().size() == 1) {
                Cube cb = (Cube) playpen.getSelectedContainers().get(0).getModel();
                cb.addDimension(dim);
            }
            new EditDimensionAction(session, session.getArchitectFrame(), dim).actionPerformed(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
