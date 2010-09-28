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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.sqlrunner.ArchitectSQLRunnerConfigurationManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

import com.darwinsys.sql.ConfigurationManager;
import com.darwinsys.sql.SQLRunnerGUI;

/**
 * Invoke the SQLRunner functionality
 */
public class SQLRunnerAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(SQLRunnerAction.class);

    static ConfigurationManager configManager;

    public SQLRunnerAction(ArchitectSwingSession session) {
        super(session, Messages.getString("SQLRunnerAction.name"), Messages.getString("SQLRunnerAction.description"), "query"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        configManager = new ArchitectSQLRunnerConfigurationManager(session);
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("Showing SQLRunnerGUI"); //$NON-NLS-1$
        new SQLRunnerGUI(new ArchitectSQLRunnerConfigurationManager(session),Messages.getString("SQLRunnerAction.dialogTitle")); // Sets itself visible; this is all we need here. //$NON-NLS-1$
    }

}