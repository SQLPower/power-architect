package ca.sqlpower.architect.swingui;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.*;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.CompareDMPanel.StartCompareAction;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class TableEditPanel extends JPanel implements ArchitectPanel {

	protected SQLTable table;
	JTextField name;
	JTextField pkName;
	JTextArea remarks;

	public TableEditPanel(SQLTable t) {
		super(new FormLayout());
		addUndoEventListener(ArchitectFrame.getMainInstance().getProject().getUndoManager().getEventAdapter());
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
		pkName.setText(t.getPrimaryKeyName());
		remarks.setText(t.getRemarks());
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
		startCompoundEdit("Starting new compound edit event in table edit panel");
		try {
			table.setPrimaryKeyName(pkName.getText());
			table.setName(name.getText());
			table.setRemarks(remarks.getText());
		} finally {
			endCompoundEdit("Ending new compound edit event in table edit panel");
		}
		return true;
	}

	public void discardChanges() {
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
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_START,message));
	}
	
	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_END,message));
	}
	
	public JPanel getPanel() {
		return this;
	}
}
