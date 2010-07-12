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

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.query.QueryFrame;

/**
 * An action that opens a SQLQuery panel.
 */
public class SQLQueryAction extends AbstractArchitectAction  {

    public SQLQueryAction(ArchitectFrame frame) {
        super(frame, Messages.getString("SQLQueryAction.name"), Messages.getString("SQLQueryAction.description"), "query");
    }
    
    public void actionPerformed(ActionEvent e) {
        JFrame sqlQueryDialog = new QueryFrame(getSession(), Messages.getString("SQLQueryAction.dialogTitle"));
        sqlQueryDialog.setVisible(true);
    }

}
