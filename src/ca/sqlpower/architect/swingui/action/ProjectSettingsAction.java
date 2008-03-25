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

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ProjectSettingsPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class ProjectSettingsAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	public ProjectSettingsAction(ArchitectSwingSession session) {
        super(session, "Project Settings...", "Project Settings");
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug(getValue(SHORT_DESCRIPTION) + " invoked");
		
		final ProjectSettingsPanel settingsPanel = new ProjectSettingsPanel(session);

		final JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
				settingsPanel,
				frame,
				"Project Settings",
				DataEntryPanelBuilder.OK_BUTTON_LABEL );		

		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
}
