/*
 * Copyright (c) 2026, SQL Power Group Inc.
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
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.SQLScriptDialog;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Generates the DDL for the selected table(s) and shows it in a dialog with
 * Copy and Save buttons. Unlike the whole-model export this works on the
 * individual objects selected on the canvas or in the DB tree.
 */
public class CopyDDLAction extends AbstractArchitectAction {

    public CopyDDLAction(final ArchitectFrame frame) {
        super(frame, Messages.getString("CopyDDLAction.name"), Messages.getString("CopyDDLAction.description"), "fwdSQL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void actionPerformed(ActionEvent e) {
        showDDL(getSelectedTables());
    }

    /**
     * @return the tables currently selected on the play pen (empty if the
     *         selection contains no tables).
     */
    public List<SQLTable> getSelectedTables() {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        if (getPlaypen() != null) {
            for (PlayPenComponent c : getPlaypen().getSelectedItems()) {
                if (c instanceof TablePane) {
                    SQLTable t = ((TablePane) c).getModel();
                    if (t != null && !tables.contains(t)) {
                        tables.add(t);
                    }
                }
            }
        }
        return tables;
    }

    /**
     * Generates and displays the DDL for the given tables.
     */
    public void showDDL(List<SQLTable> tables) {
        if (tables == null || tables.isEmpty()) return;

        SQLDatabase targetDb = getSession().getTargetDatabase();
        if (targetDb == null || targetDb.getDataSource() == null) {
            ASUtils.showExceptionDialogNoReport(frame,
                    Messages.getString("CopyDDLAction.noTargetDatabase"), null); //$NON-NLS-1$
            return;
        }

        try {
            DDLGenerator ddlg = DDLUtils.createDDLGenerator(targetDb.getDataSource());
            ddlg.generateDDLStatements(tables);
            SQLScriptDialog dialog = new SQLScriptDialog(
                    null,
                    Messages.getString("CopyDDLAction.dialogTitle"), //$NON-NLS-1$
                    Messages.getString("CopyDDLAction.header"), //$NON-NLS-1$
                    false,
                    ddlg,
                    targetDb.getDataSource(),
                    false,
                    getSession());
            dialog.setVisible(true);
        } catch (Exception ex) {
            ASUtils.showExceptionDialog(getSession(),
                    Messages.getString("CopyDDLAction.error"), ex); //$NON-NLS-1$
        }
    }
}