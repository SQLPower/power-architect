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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import ca.sqlpower.architect.ddl.critic.Critic;
import ca.sqlpower.architect.ddl.critic.Criticizer;
import ca.sqlpower.architect.ddl.critic.MySQLCommentCritic;
import ca.sqlpower.architect.ddl.critic.PhysicalNameCritic;
import ca.sqlpower.architect.ddl.critic.PrimaryKeyCritic;
import ca.sqlpower.architect.ddl.critic.RelationshipMappingTypeCritic;
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
import ca.sqlpower.swingui.table.FancyExportableJTable;

/**
 * Proof-of-concept action that criticizes a whole SQLObject tree and pops up
 * a window with the results. This will be replaced by a system that does criticisms
 * "in the background" on a more continual basis.
 */
public class CriticizeAction extends AbstractArchitectAction {

    public CriticizeAction(ArchitectSwingSession session) {
        super(session, Messages.getString("CriticizeAction.name"), Messages.getString("CriticizeAction.description"));
    }
    
    public void actionPerformed(ActionEvent e) {
        List<Critic<SQLObject>> critics = new ArrayList<Critic<SQLObject>>();
        critics.add(new PhysicalNameCritic("Oracle", Pattern.compile("^[a-z_][a-z0-9_]*$", Pattern.CASE_INSENSITIVE), 30));
        critics.add(new MySQLCommentCritic());
        critics.add(new PrimaryKeyCritic());
        critics.add(new RelationshipMappingTypeCritic());
        Criticizer<SQLObject> criticizer = new Criticizer<SQLObject>(critics);
        try {
            recursivelyCriticize(session.getTargetDatabase(), criticizer);
        } catch (SQLObjectException ex) {
            throw new RuntimeException("Unexpected exception (because playpen is already populted)", ex);
        }
        
        FancyExportableJTable table = new FancyExportableJTable(new CriticismTableModel(criticizer));
        JDialog d = SPSUtils.makeOwnedDialog(session.getPlayPen(), "Data Model Evaluation");
        SPSUtils.makeJDialogCancellable(d, null);
        d.setContentPane(new JScrollPane(table));
        d.pack();
        d.setSize(table.getPreferredSize().width, d.getHeight());
        d.setLocationRelativeTo(session.getPlayPen());
        d.setVisible(true);
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
    private void recursivelyCriticize(SQLObject root, Criticizer<SQLObject> criticizer) throws SQLObjectException {
        
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
