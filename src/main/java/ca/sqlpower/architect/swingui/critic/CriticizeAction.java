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

import ca.sqlpower.architect.ddl.critic.Criticizer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
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
        Criticizer criticizer = new Criticizer(
                session.getWorkspace().getCriticManager().createCritics());
        try {
            recursivelyCriticize(session.getTargetDatabase(), criticizer);
        } catch (SQLObjectException ex) {
            throw new RuntimeException("Unexpected exception (because playpen is already populted)", ex);
        }
        session.getArchitectFrame().updateCriticPanel(criticizer);
    }

    /**
     * 
     * @param root
     *            The SQLObject to criticize
     * @param criticizer
     *            The criticizer that will examine the subtree at root and
     *            accumulate criticisms about it
     * @throws SQLObjectException
     *             if the (sub)tree under root is not already populated, and an
     *             attempt to populate it fails
     */
    @SuppressWarnings("unchecked")
    private void recursivelyCriticize(SQLObject root, Criticizer criticizer) throws SQLObjectException {
        
        // skip types that don't warrant criticism
        if ( (!(root instanceof SQLDatabase)) &&
             (!(root instanceof SQLRelationship.ColumnMapping)) ) {
            criticizer.criticize(root);
        }
        
        for (SQLObject child : (List<SQLObject>) root.getChildren()) {
            if (child instanceof SQLImportedKey
                    && ((SQLTable) root).getImportedKeys().contains(child)) {
                // skip contents of every imported keys folder, or else we will visit every relationship twice
                continue;
            }
            recursivelyCriticize(child, criticizer);
        }
    }
}
