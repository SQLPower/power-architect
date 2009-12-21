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

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

public class InsertColumnAction extends AbstractTableTargetedAction {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
	
	public InsertColumnAction(ArchitectSwingSession session) {
        super(session, Messages.getString("InsertColumnAction.name"), Messages.getString("InsertColumnAction.description"), "new_column"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		putValue(ACTION_COMMAND_KEY, PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_C,0));
		setEnabled(false);
	}



    void processSQLObject(SQLObject so) throws SQLObjectException {
        SQLTable st = null;
        int idx = 0;
        if (so instanceof SQLTable) {
        	logger.debug("user clicked on table, so we shall try to add a column to the end of the table."); //$NON-NLS-1$
        	st = (SQLTable) so;
        	idx = st.getColumns().size();
        	logger.debug("SQLTable click -- idx set to: " + idx);						 //$NON-NLS-1$
        } else if (so instanceof SQLColumn) {
        	// iterate through the column list to figure out what position we are in...
        	logger.debug("trying to determine insertion index for table."); //$NON-NLS-1$
        	SQLColumn sc = (SQLColumn) so;
        	st = sc.getParent();
        	idx = st.getColumnIndex(sc);
        	if (idx == -1)  {
        	    throw new IllegalStateException("Selected column '" + sc.getName() + 
        	            "' could not be found in parent table '" + st.getName() + "'");
        	} else {
        	    //This is so that the column is added after the selected the column
        	    idx++;
        	}
        }
        if (st == null) {
            throw new NullPointerException("The SQLObject must be a instance of SQLTable or SQLColumn");
        }
        
        // Expecting the playpen and dbtree selctions to be synchronized.
        TablePane tp = (TablePane) playpen.getSelectedItems().get(0);
        
        EditColumnAction editColumnAction = new EditColumnAction(session);
        editColumnAction.showDialog(st, idx, true, tp);
        
    }

    void processTablePane(TablePane tp) throws SQLObjectException {
        int idx = tp.getSelectedItemIndex();
        
        if (idx < 0) idx = tp.getModel().getColumns().size();
        
        EditColumnAction editColumnAction = new EditColumnAction(session);
        
        //The actual column is added to the table when the user presses OK
        //Its added to the end of the table, if a table is selected or added right after
        //the selected column
        editColumnAction.showDialog(tp.getModel(), (tp.getSelectedItemIndex() < 0? idx: idx+1), true, tp);
    }

    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling Insert Column Action"); //$NON-NLS-1$
        putValue(SHORT_DESCRIPTION, Messages.getString("InsertColumnAction.shortDescription")); //$NON-NLS-1$
    }

}
