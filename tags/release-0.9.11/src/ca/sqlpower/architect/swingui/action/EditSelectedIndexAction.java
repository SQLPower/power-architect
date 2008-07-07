/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;

/**
 * An action that, when invoked, examines the selection in the DBTree and pops up
 * the IndexEditPanel for the selected index.  If no index is selected (or there
 * is a multiple selection), an error dialog will appear instead.
 */
public class EditSelectedIndexAction extends EditIndexAction {

    private static final Logger logger = Logger.getLogger(EditSelectedIndexAction.class);
    
    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt; 
    
    /**
     * Creates a new instance of this action which, when invoked, will edit the
     * currently-selected SQLIndex.  Normally only one instance of this action
     * will be created in a particular ArchitectSession.  To create an action that
     * edits a specific index, see {@link EditSpecificIndexAction}.
     * 
     * @param session The ArchitectSession to which this action belongs.
     */
    public EditSelectedIndexAction(ArchitectSwingSession session) {
        super(session, Messages.getString("EditSelectedIndexAction.name"), Messages.getString("EditSelectedIndexAction.description"), "edit_index"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dbt = frame.getDbTree();
    }

    public void actionPerformed(ActionEvent evt) {
        TreePath [] selections = dbt.getSelectionPaths();
        logger.debug("selections length is: " + selections.length); //$NON-NLS-1$
        if (selections.length != 1) {
            JOptionPane.showMessageDialog(dbt, Messages.getString("EditSelectedIndexAction.instructions")); //$NON-NLS-1$
        } else {
            TreePath tp = selections[0];
            SQLObject so = (SQLObject) tp.getLastPathComponent();
            SQLIndex si = null;

            if (so instanceof SQLIndex) {
                logger.debug("user clicked on index, so we shall try to edit the index properties."); //$NON-NLS-1$
                si = (SQLIndex) so;
                try {
                    makeDialog(si);
                } catch (ArchitectException e) {
                    throw new ArchitectRuntimeException(e);
                } 
            } else {
                JOptionPane.showMessageDialog(dbt, Messages.getString("EditSelectedIndexAction.instructions")); //$NON-NLS-1$
            }
        }
    }
}
