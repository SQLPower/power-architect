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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.query.QueryFrame;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * This action will pop up the query tool in Architect and select all of the
 * values of the given table to give the user information about what is in
 * the table.
 */
public class ShowTableContentsAction extends AbstractArchitectAction {
    
    /**
     * The contents of this table will be displayed when the action executes.
     */
    private final SQLTable table;

    public ShowTableContentsAction(ArchitectSwingSession session, SQLTable table) {
        super(session, Messages.getString("ShowTableContentsAction.name"), Messages.getString("ShowTableContentsAction.description"));
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        String sql = "SELECT * FROM " + table.toQualifiedName();
        JFrame sqlQueryDialog = new QueryFrame(getSession(), Messages.getString("SQLQueryAction.dialogTitle"), table.getParentDatabase(), sql);
        sqlQueryDialog.setVisible(true);
    }

}
