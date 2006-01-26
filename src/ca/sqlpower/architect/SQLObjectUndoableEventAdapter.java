package ca.sqlpower.architect;

import javax.swing.undo.UndoableEditSupport;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.UndoManager;
import ca.sqlpower.architect.undo.SQLObjectInsertChildren;
import ca.sqlpower.architect.undo.SQLObjectRemoveChildren;

/**
 * Converts received SQLObjectEvents into UndoableEdits and adds them to an UndoManager. 
 * @author Matt
 */
public class SQLObjectUndoableEventAdapter  implements
		SQLObjectListener {
	private static final Logger logger = Logger.getLogger(SQLObjectUndoableEventAdapter.class);

	private UndoManager undoManager;
	
	public SQLObjectUndoableEventAdapter(UndoManager UndoManager) {
		undoManager = UndoManager;	
	}

	public void dbChildrenInserted(SQLObjectEvent e) {
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectInsertChildren undoEvent = new SQLObjectInsertChildren();
			undoEvent.addEdit(e);
			undoManager.addEdit(undoEvent);
		}
		try{
		ArchitectUtils.listenToHierarchy(this,e.getChildren());
		}
		catch(ArchitectException ex)
		{
			logger.error("SQLObjectUndoableEventAdapter cannot attach to new children",ex);
		}
		
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectRemoveChildren undoEvent = new SQLObjectRemoveChildren();
			undoEvent.addEdit(e);	
			undoManager.addEdit(undoEvent);
		}
		try{
			ArchitectUtils.unlistenToHierarchy(this,e.getChildren());
			}
			catch(ArchitectException ex)
			{
				logger.error("SQLObjectUndoableEventAdapter cannot attach to new children",ex);
		}
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		logger.error("Unexpected structure change event");
	}

}
