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

import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class InsertIndexAction extends AbstractTableTargetedAction {

    private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
    
    public InsertIndexAction(ArchitectSwingSession session) {
        super(session, "New Index", "New Index (Shortcut I)", "new_index");
        putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
        setEnabled(false);
    }

    
    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling New Index Action");
        putValue(SHORT_DESCRIPTION, "New Index");
    }


    @Override
    void processSQLObject(SQLObject so) throws ArchitectException {
        if (so instanceof SQLTable) {
            makeDialog((SQLTable) so);
        }
    }

    @Override
    void processTablePane(TablePane tp) throws ArchitectException {
        System.err.println("processTablePane tp.getModel(): "+tp.getModel());
        makeDialog(tp.getModel());
    }
    
    private void makeDialog(SQLTable parent) throws ArchitectException {
        
        // This feature has been disabled because it needs a lot of
        // work so that it (a) makes sense, and (b) doesn't put your
        // project in a state where it can't be reopened after saving.
        
        // (check the revision history for the old implementation)
        
        final JDialog d;
        SQLIndex index = new SQLIndex();
        final IndexEditPanel editPanel = new IndexEditPanel(index, parent, session);
  
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                editPanel, frame,
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }
}
