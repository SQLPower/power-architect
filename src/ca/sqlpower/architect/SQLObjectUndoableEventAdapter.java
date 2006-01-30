package ca.sqlpower.architect;

import javax.swing.undo.CompoundEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.UndoManager;
import ca.sqlpower.architect.undo.SQLObjectInsertChildren;
import ca.sqlpower.architect.undo.SQLObjectRemoveChildren;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

/**
 * Converts received SQLObjectEvents into UndoableEdits and adds them to an UndoManager. 
 * @author Matt
 */
public class SQLObjectUndoableEventAdapter  implements UndoCompoundEventListener,
		SQLObjectListener {
	private static final Logger logger = Logger.getLogger(SQLObjectUndoableEventAdapter.class);

	private UndoManager undoManager;
	
	public enum UndoState {DRAG_AND_DROP,MULTI_SELECT,MULTI_DRAG_AND_DROP,REGULAR};
	private UndoState state;
	private CompoundEdit ce; 
	
	public SQLObjectUndoableEventAdapter(UndoManager UndoManager) {
		undoManager = UndoManager;
		state = UndoState.REGULAR;
		ce = null;
	}

	public void dragAndDropStart(UndoCompoundEvent e) 
	{
		if (state != UndoState.REGULAR ) 
		{
			if(ce!= null) {
				// make sure the edit is no longer in progress
				ce.end();
				undoManager.addEdit(ce);
				ce = null;
				logger.debug("Adding compound edit to undo manager");
			}
			state = UndoState.REGULAR;
		}
		
		logger.debug("Undo moving to drag and drop state");
		state = UndoState.DRAG_AND_DROP;
		ce = new CompoundEdit();
		
	}
	public void dragAndDropEnd(UndoCompoundEvent e){
		
		if (state != UndoState.DRAG_AND_DROP ){
			if(ce!= null) {
				// make sure the edit is no longer in progress
				ce.end();
				undoManager.addEdit(ce);
				ce = null;
				logger.debug("Adding compound edit to undo manager");
			}
		}
		if(ce!= null) {
			// make sure the edit is no longer in progress
			ce.end();
			undoManager.addEdit(ce);
			ce = null;
			logger.debug("Adding compound edit to undo manager");
		}
		logger.debug("Undo moving to regular state from DND");
		state = UndoState.REGULAR;
	}
	
	public void multiSelectStart(UndoCompoundEvent e){
		if(state != UndoState.DRAG_AND_DROP && state != UndoState.REGULAR){
			if(ce!= null) {
				// make sure the edit is no longer in progress
				ce.end();
				undoManager.addEdit(ce);
				ce = null;
				logger.debug("Adding compound edit to undo manager");
			}
		}
		if (state == UndoState.DRAG_AND_DROP)
		{
			state = UndoState.MULTI_DRAG_AND_DROP;
			logger.debug("Undo moving to multi drag and drop state");
		}
		if (state == UndoState.REGULAR)
		{
			state = UndoState.MULTI_SELECT;
			ce = new CompoundEdit();
			logger.debug("Undo moving to Multi Select state");
		}
	}
	
	public void multiSelectEnd(UndoCompoundEvent e){
		if(state != UndoState.MULTI_SELECT && state != UndoState.MULTI_DRAG_AND_DROP){
			if(ce!= null) {
				// make sure the edit is no longer in progress
				ce.end();
				undoManager.addEdit(ce);
				ce = null;
				logger.debug("Adding compound edit to undo manager");
			}
		}
		if (state == UndoState.MULTI_DRAG_AND_DROP)
		{
			state = UndoState.DRAG_AND_DROP;
			logger.debug("Undo moving to drag and drop state");
		}
		if (state == UndoState.MULTI_SELECT)
		{
			state = UndoState.REGULAR;
			if(ce!= null) {
				// make sure that the edit is no longer in progress
				ce.end();
				undoManager.addEdit(ce);
				ce = null;
				logger.debug("Adding compound edit to undo manager");
			}
			logger.debug("Undo moving to regular state");
		}
	}
	
	
	public void dbChildrenInserted(SQLObjectEvent e) {
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectInsertChildren undoEvent = new SQLObjectInsertChildren();
			if (state == UndoState.REGULAR)
			{
				undoEvent.createEditFromEvent(e);
				undoManager.addEdit(undoEvent);
			}
			else
			{
				undoEvent.createEditFromEvent(e);
				ce.addEdit(undoEvent);
			}
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
			if (state == UndoState.REGULAR)
			{
				undoEvent.createEditFromEvent(e);
				undoManager.addEdit(undoEvent);
			}
			else
			{
				undoEvent.createEditFromEvent(e);
				ce.addEdit(undoEvent);
			}
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
		// too many changes clear undo
		undoManager.discardAllEdits();
		
	}

	public UndoState getState() {
		return state;
	}
	
	

}
