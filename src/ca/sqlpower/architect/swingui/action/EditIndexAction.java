/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

public class EditIndexAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(EditIndexAction.class);

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt; 

    
    public EditIndexAction(ArchitectSwingSession session) {
        super(session, "Index Properties...", "Index Properties", "IndexProperties");
        dbt = frame.getDbTree();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
            TreePath [] selections = dbt.getSelectionPaths();
            logger.debug("selections length is: " + selections.length);
            if (selections.length != 1) {
                JOptionPane.showMessageDialog(dbt, "To indicate which index you like edit, please select a single index header.");
            } else {
                TreePath tp = selections[0];
                SQLObject so = (SQLObject) tp.getLastPathComponent();
                SQLIndex si = null;

                if (so instanceof SQLIndex) {
                    logger.debug("user clicked on index, so we shall try to edit the index properties.");
                    si = (SQLIndex) so;
                    try {
                        makeDialog(si);
                    } catch (ArchitectException e) {
                        throw new ArchitectRuntimeException(e);
                    } 
                } else {
                    JOptionPane.showMessageDialog(dbt, "To indicate which index name you would like to edit, please select a single index header.");
                }
            }
        } else {
            // unknown action command source, do nothing
        }   
    }

    
    private void makeDialog(SQLIndex index) throws ArchitectException {
        final JDialog d;
        final IndexEditPanel editPanel = new IndexEditPanel(index, session);
  
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                editPanel, frame,
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }
}
