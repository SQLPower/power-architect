/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.critic;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Icon;

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.swingui.SPSUtils;

/**
 * Proof-of-concept action that criticizes a whole SQLObject tree and pops up
 * a window with the results. This will be replaced by a system that does criticisms
 * "in the background" on a more continual basis.
 */
public class CriticizeAction extends AbstractArchitectAction {

    public static final Icon CRITIC_ICON = SPSUtils.createIcon("critic", Messages.getString("CriticizeAction.description"));
    
    public CriticizeAction(ArchitectSwingSession session) {
        super(session, Messages.getString("CriticizeAction.name"), Messages.getString("CriticizeAction.description"), CRITIC_ICON);
    }
    
    public void actionPerformed(ActionEvent e) {
        criticize();
    }
    
    /**
     * Call to do a full critique of the given session's play pen.
     */
    public void criticize() {
        List<Criticism> criticisms = session.getWorkspace().getCriticManager().criticize(
                session.getTargetDatabase());
        session.getArchitectFrame().updateCriticPanel(criticisms);
    }

}
