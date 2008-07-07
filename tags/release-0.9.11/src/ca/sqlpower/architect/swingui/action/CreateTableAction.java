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
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;

public class CreateTableAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(CreateTableAction.class);

	private final ArchitectSwingSession session;
	
	public CreateTableAction(ArchitectSwingSession session) {
        super(session, Messages.getString("CreateTableAction.name"), Messages.getString("CreateTableAction.description"), "new_table"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.session = session;
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
	}

	public void actionPerformed(ActionEvent evt) {
		playpen.fireCancel();
		SQLTable t = null; 
		try {
            t = new SQLTable();
			t.initFolders(true);
		} catch (ArchitectException e) {
			logger.error("Couldn't add folder to table \""+t.getName()+"\"", e); //$NON-NLS-1$ //$NON-NLS-2$
			ASUtils.showExceptionDialog(session, Messages.getString("CreateTableAction.couldNotAddFolder"), e); //$NON-NLS-1$
		}
		t.setName("New_Table"); //$NON-NLS-1$
		
		TablePane tp = new TablePane(t, playpen);
		playpen.addFloating(tp);
		playpen.setMouseMode(PlayPen.MouseModeType.CREATING_TABLE);
	}
}
