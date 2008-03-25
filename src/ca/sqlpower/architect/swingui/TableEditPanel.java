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
package ca.sqlpower.architect.swingui;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.swingui.DataEntryPanel;

public class TableEditPanel extends JPanel implements DataEntryPanel {

	protected SQLTable table;
	JTextField name;
	JTextField pkName;
	JTextArea remarks;

	public TableEditPanel(ArchitectSwingSession session, SQLTable t) {
		super(new FormLayout());
		addUndoEventListener(session.getUndoManager().getEventAdapter());
		add(new JLabel("Table Name"));
		add(name = new JTextField("", 30));
		add(new JLabel("Primary Key Name"));
		add(pkName = new JTextField("", 30));
		add(new JLabel("Remarks"));
		add(new JScrollPane(remarks = new JTextArea(4, 30)));
		remarks.setLineWrap(true);
		remarks.setWrapStyleWord(true);
		editTable(t);
	}

	public void editTable(SQLTable t) {
		table = t;
		name.setText(t.getName());
        try {
            if (t.getPrimaryKeyIndex() == null) {
                pkName.setEnabled(false);
            } else {
                pkName.setText(t.getPrimaryKeyName());
                pkName.setEnabled(true);
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
		remarks.setText(t.getRemarks());
		name.selectAll();
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
		startCompoundEdit("Table Properties Change");		
        try {	
		    StringBuffer warnings = new StringBuffer();
            //We need to check if the table name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append("The table cannot be assigned a blank name \n");
                
            }
            if (pkName.isEnabled() &&
                    pkName.getText().trim().length() == 0) {
                warnings.append("The primary key cannot be assigned a blank name");                
            }
            
            if (warnings.toString().length() == 0) {
                
                // important: set the primary key name first, because if the primary
                // key was called (for example) new_table_pk, and the table was called
                // new_table, then the user changes the table name to cow_table, the
                // table itself will notice this pattern and automatically change its
                // primary key name to cow_table_pk.  If we set the table name first,
                // the magic still happens, but then we would overwrite the new pk name
                // with the old one from the pk name text field in this panel.
                if (pkName.isEnabled() && table.getPrimaryKeyIndex() != null) {
                    table.getPrimaryKeyIndex().setName(pkName.getText());
                }
                
                table.setName(name.getText());
                table.setRemarks(remarks.getText());                
                return true;
            } else{
                JOptionPane.showMessageDialog(this,warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }            
		} catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
			endCompoundEdit("Ending new compound edit event in table edit panel");
		}
	}

	public void discardChanges() {
	    // TODO revert the changes made
	}
	
	/**
	 * The list of SQLObject property change event listeners
	 * used for undo
	 */
	protected LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

	
	public void addUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}
	
	protected void fireUndoCompoundEvent(UndoCompoundEvent e) {
		Iterator it = undoEventListeners.iterator();
		
		if (e.getType().isStartEvent()) {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditStart(e);
			}
		} else {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditEnd(e);
			}
		} 
	}

	public void startCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START,message));
	}
	
	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END,message));
	}
	
	public JPanel getPanel() {
		return this;
	}

    public String getNameText() {
        return name.getText();
    }

    public void setNameText(String newName) {
        name.setText(newName);
    }

    public String getPkNameText() {
        return pkName.getText();
    }

    public void setPkNameText(String newPkName) {
        pkName.setText(newPkName);
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

}
