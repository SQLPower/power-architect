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

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Abstract base class for the two different index edit actions.  The protected
 * method {@link #makeDialog(SQLIndex)} gets used by subclasses once they've
 * decided which index to edit.
 */
public abstract class EditIndexAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(EditIndexAction.class);

    protected EditIndexAction(ArchitectSwingSession session, String actionName, String actionDescription, String iconResourceName) {
        super(session, actionName, actionDescription, iconResourceName);
    }

    /**
     * Creates and shows the index properties dialog for the given index.
     */
    protected void makeDialog(SQLIndex index) throws ArchitectException {
        final JDialog d;
        final IndexEditPanel editPanel = new IndexEditPanel(index, session);
  
        logger.debug("Showing index edit dialog for " + index); //$NON-NLS-1$
        
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                editPanel, frame,
                Messages.getString("EditIndexAction.dialogTitle"), Messages.getString("EditIndexAction.okOption")); //$NON-NLS-1$ //$NON-NLS-2$
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }
}
