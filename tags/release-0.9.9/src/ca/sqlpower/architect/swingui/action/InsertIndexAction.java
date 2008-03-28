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

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.TablePane;

public class InsertIndexAction extends AbstractTableTargetedAction {

    private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
    
    public InsertIndexAction(ArchitectSwingSession session) {
        super(session, "New Index", "New Index", "new_index");
        putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        setEnabled(false);
    }

    
    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling New Index Action");
        putValue(SHORT_DESCRIPTION, "New Index");
    }


    @Override
    void processSQLObject(SQLObject so) throws ArchitectException {
        if (so instanceof SQLTable) {
            makeDialog((SQLTable) so);
        }
    }

    @Override
    void processTablePane(TablePane tp) throws ArchitectException {
        System.err.println("processTablePane tp.getModel(): "+tp.getModel());
        makeDialog(tp.getModel());
    }
    
    private void makeDialog(SQLTable parent) throws ArchitectException {
        
        // This feature has been disabled because it needs a lot of
        // work so that it (a) makes sense, and (b) doesn't put your
        // project in a state where it can't be reopened after saving.
        
        // (check the revision history for the old implementation)
        
        JOptionPane.showMessageDialog(session.getArchitectFrame(),
                "Index editing support will be available in\n" +
                "an upcoming release of the Power*Architect.");
    }
}
