/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.action.enterprise;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl;

public class RefreshProjectAction extends AbstractAction {

    private static final ImageIcon REFRESH_ICON = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow_refresh16.png"));
    
    final ArchitectSession session;

    public RefreshProjectAction(ArchitectSession session) {
        super("Refresh", REFRESH_ICON);
        putValue(Action.SHORT_DESCRIPTION, "Refresh");
        
        if (session.isEnterpriseSession()) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        this.session = session;
    }
    
    public void actionPerformed(ActionEvent e) {
        ((ArchitectSwingSessionImpl) session).refresh();
    }
}
