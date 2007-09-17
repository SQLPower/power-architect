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

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.TablePane;

public class InsertColumnAction extends AbstractTableTargetedAction {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
	
	public InsertColumnAction(ArchitectSwingSession session) {
        super(session, "New Column", "New Column", "new_column");
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_C,0));
		setEnabled(false);
	}



    void processSQLObject(SQLObject so) throws ArchitectException {
        SQLTable st = null;
        int idx = 0;
        if (so instanceof SQLTable) {
        	logger.debug("user clicked on table, so we shall try to add a column to the end of the table.");
        	st = (SQLTable) so;
        	idx = st.getColumnsFolder().getChildCount();
        	logger.debug("SQLTable click -- idx set to: " + idx);						
        } else if (so instanceof SQLColumn) {
        	// iterate through the column list to figure out what position we are in...
        	logger.debug("trying to determine insertion index for table.");
        	SQLColumn sc = (SQLColumn) so;
        	st = sc.getParentTable();
        	idx = st.getColumnIndex(sc);
        	if (idx == -1)  {
        		// not found
        		logger.debug("did not find column, inserting at start of table.");
        		idx = 0;
        	}
        } else {
        	idx = 0;
        }
        st.addColumn(idx, new SQLColumn());
        EditColumnAction editColumnAction = new EditColumnAction(session);
        editColumnAction.makeDialog(st, idx);
        
    }

    void processTablePane(TablePane tp) throws ArchitectException {
        int idx = tp.getSelectedColumnIndex();
        
        if (idx < 0) idx = tp.getModel().getColumnsFolder().getChildCount();
        
        tp.getModel().addColumn(idx, new SQLColumn());
        EditColumnAction editColumnAction = new EditColumnAction(session);
        editColumnAction.makeDialog(tp.getModel(), idx);
    }

    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling Insert Column Action");
        putValue(SHORT_DESCRIPTION, "Insert Column");
    }

}
