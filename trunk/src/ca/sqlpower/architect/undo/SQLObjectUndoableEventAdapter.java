package ca.sqlpower.architect.undo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import com.sun.jdi.event.Event;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;

/**
 * Converts received SQLObjectEvents into UndoableEdits and adds them to an UndoManager. 
 * @author Matt
 */
public class SQLObjectUndoableEventAdapter  implements UndoCompoundEventListener,
		SQLObjectListener, PropertyChangeListener, PlayPenComponentListener {
	private static final Logger logger = Logger.getLogger(SQLObjectUndoableEventAdapter.class);

	private UndoManager undoManager;
	private PlayPenComponentEvent movementEvent;
	private CompoundEdit ce;
	private int compoundEditStackCount;
	private int simulMoveCount;
	private HashMap<Object,PlayPenComponentEvent> moveList;
	
	public SQLObjectUndoableEventAdapter(UndoManager UndoManager) {
		undoManager = UndoManager;
		ce = null;
		compoundEditStackCount =0;
		simulMoveCount=0;
		moveList = new HashMap<Object,PlayPenComponentEvent>();
	}
	
	/**
	 * 
	 */
	private void compoundGroupStart() {		
		compoundEditStackCount++;
		if (compoundEditStackCount == 1)
			ce = new CompoundEdit();
		if (logger.isDebugEnabled()) {
			logger.debug("compoundGroupStart: edit stack ="+compoundEditStackCount);
		}
	}
	/**
	 * 
	 */

	private void compoundGroupEnd() {
		if (compoundEditStackCount  <= 0){
			throw new IllegalStateException("No compound edit in progress");
		}
		compoundEditStackCount--;
		if (compoundEditStackCount == 0)
			returnToEditState();   // GOTO carolina || alabama
		if (logger.isDebugEnabled()) {
			logger.debug("compoundGroupEnd: edit stack ="+compoundEditStackCount);
		}
	}
	
	private void addEdit(UndoableEdit undoEdit)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("Adding new edit: "+undoEdit);
		}
		// if we are not in a compound edit
		if (compoundEditStackCount == 0)
		{
			undoManager.addEdit(undoEdit);
		}
		else {	
			ce.addEdit(undoEdit);
		}
	}
	
	public void dbChildrenInserted(SQLObjectEvent e) {
		if (e.isSecondary()) return;
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectInsertChildren undoEvent = new SQLObjectInsertChildren();
			undoEvent.createEditFromEvent(e);
			addEdit(undoEvent);
		}
		try{
		ArchitectUtils.listenToHierarchy(this,e.getChildren());
		ArchitectUtils.addUndoListenerToHierarchy(this,e.getChildren());
		}
		catch(ArchitectException ex)
		{
			logger.error("SQLObjectUndoableEventAdapter cannot attach to new children",ex);
		}
		
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		if(e.isSecondary()) return;
		
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectRemoveChildren undoEvent = new SQLObjectRemoveChildren();
			undoEvent.createEditFromEvent(e);
			addEdit(undoEvent);
		
		}
		try{
			ArchitectUtils.unlistenToHierarchy(this,e.getChildren());
			ArchitectUtils.undoUnlistenToHierarchy(this,e.getChildren());
			}
			catch(ArchitectException ex)
			{
				logger.error("SQLObjectUndoableEventAdapter cannot attach to new children",ex);
		}
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		if (e.isSecondary()) return;
			
		if (e.getSource() instanceof SQLDatabase &&
				e.getPropertyName().equals("shortDisplayName")){
			// this is not undoable at this time.
			
		}
		else
		{
			ArchitectPropertyChangeUndoableEdit undoEvent = new ArchitectPropertyChangeUndoableEdit(e);
			addEdit(undoEvent);
		}
		
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		logger.error("Unexpected structure change event");
		if (e.isSecondary()) return;
		
		// too many changes clear undo
		undoManager.discardAllEdits();
		
		
	}
	
	/**
	 * Return to a single edit state from a compound edit state
	 *
	 */
	private void returnToEditState()
	{
		if ( simulMoveCount != 0 || compoundEditStackCount !=0)
			throw new IllegalStateException("Both the move count ("+simulMoveCount+") and the compound edit stack ("+compoundEditStackCount+") should be 0");
		if(ce!= null) {
			// make sure the edit is no longer in progress
			ce.end();
			// add at least one movement for when ignoring events
			if (movementEvent != null)
			{
				ce.addEdit(new TablePaneLocationEdit(movementEvent));
				movementEvent = null;
			}
			if (ce.canUndo())
			{
				undoManager.addEdit(ce);
			}
			ce = null;
			logger.debug("Adding compound edit to undo manager");		
			
		}
		else
		{
			if (movementEvent != null)
			{
				undoManager.addEdit(new TablePaneLocationEdit(movementEvent));
				movementEvent = null;
			}
		}
		
		logger.debug("Returning to regular state");
	}

	public void componentMoved(PlayPenComponentEvent e) {
				
	}

	public void componentResized(PlayPenComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoveStart(PlayPenComponentEvent e) {
		
		compoundGroupStart();
		simulMoveCount++;
		moveList.put(e.getSource(),e);			
		logger.debug("UndoAdapter Starting move "+ simulMoveCount + "::");
	}

	public void componentMoveEnd(PlayPenComponentEvent e) {
		logger.debug("UndoAdapter ending move "+ simulMoveCount);
		simulMoveCount--;
		compoundGroupEnd();

		
		PlayPenComponentEvent oldEvent = moveList.get(e.getSource());
		if (oldEvent != null)
		{
			oldEvent.setNewPoint(e.getNewPoint());
		}
		
		if (simulMoveCount == 0) {
			if( !oldEvent.getOldPoint().equals( e.getNewPoint()))
			{
				TablePaneLocationEdit tableEdit = new TablePaneLocationEdit (moveList.values());		
				addEdit(tableEdit);
			}
			moveList.clear();		
		}
		
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	public void compoundEditStart(UndoCompoundEvent e) {
		if (logger.isDebugEnabled()) {
			logger.debug("compoundEditStart with event: "+e.toString());
		}
		compoundGroupStart();
		
	}

	public void compoundEditEnd(UndoCompoundEvent e) {
		if (logger.isDebugEnabled()) {
			logger.debug("compoundEditStart with event: "+e.toString());
		}
		compoundGroupEnd();
		
	}
}
