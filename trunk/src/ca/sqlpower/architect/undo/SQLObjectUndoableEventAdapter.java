package ca.sqlpower.architect.undo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.undo.CompoundEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.PlayPenComponentListener;

/**
 * Converts received SQLObjectEvents into UndoableEdits and adds them to an UndoManager. 
 * @author Matt
 */
public class SQLObjectUndoableEventAdapter  implements UndoCompoundEventListener,
		SQLObjectListener, PropertyChangeListener, PlayPenComponentListener {
	private static final Logger logger = Logger.getLogger(SQLObjectUndoableEventAdapter.class);

	private UndoManager undoManager;
	private PlayPenComponentEvent movementEvent;
	public enum UndoState {DRAG_AND_DROP,MULTI_SELECT,MULTI_DRAG_AND_DROP,REGULAR,PROPERTY_CHANGE_GROUP,MOVING};
	private UndoState state;
	private CompoundEdit ce;
	private int propertyGroupCount;
	private int simulMoveCount;
	private HashMap<Object,PlayPenComponentEvent> moveList;
	
	public SQLObjectUndoableEventAdapter(UndoManager UndoManager) {
		undoManager = UndoManager;
		state = UndoState.REGULAR;
		ce = null;
		propertyGroupCount =0;
		simulMoveCount = 0;
		moveList = new HashMap<Object,PlayPenComponentEvent>();
	}

	/**
	 *  Process a start drag and drop state change
	 */
	public void dragAndDropStart(UndoCompoundEvent e) 
	{
		if (state != UndoState.REGULAR ) 
		{
			returnToHomeState();
			
		}
		
		logger.debug("Undo moving to drag and drop state");
		state = UndoState.DRAG_AND_DROP;
		ce = new CompoundEdit();
		
	}
	/**
	 * Process an end drag and drop state change
	 */
	public void dragAndDropEnd(UndoCompoundEvent e){
		
		if (state != UndoState.DRAG_AND_DROP ){
			
		}
		returnToHomeState();
		logger.debug("Undo moving to regular state from DND");
		
	}
	
	/**
	 * Process a multi select start state change
	 */
	public void multiSelectStart(UndoCompoundEvent e){
		if(state != UndoState.DRAG_AND_DROP && state != UndoState.REGULAR){
			returnToHomeState();
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
	
	/**
	 * process a multi select end state change
	 */
	public void multiSelectEnd(UndoCompoundEvent e){
		if(state != UndoState.MULTI_SELECT && state != UndoState.MULTI_DRAG_AND_DROP){
			returnToHomeState();
		}
		if (state == UndoState.MULTI_DRAG_AND_DROP)
		{
			state = UndoState.DRAG_AND_DROP;
			logger.debug("Undo moving to drag and drop state");
		}
		if (state == UndoState.MULTI_SELECT)
		{
			
			returnToHomeState();
			logger.debug("Undo moving to regular state");
		}
	}
		
	/**
	 * 
	 */
	public void propertyGroupStart(UndoCompoundEvent e) {
		if (state == UndoState.PROPERTY_CHANGE_GROUP)
		{
			propertyGroupCount++;
		}
		else if (state== UndoState.REGULAR ) {
			if (propertyGroupCount != 0)
			{
				returnToHomeState();
			}
			propertyGroupCount++;
			ce = new CompoundEdit();
			state = UndoState.PROPERTY_CHANGE_GROUP;
		}
		
	}
	/**
	 * 
	 */

	public void propertyGroupEnd(UndoCompoundEvent e) {
		if (state == UndoState.PROPERTY_CHANGE_GROUP && propertyGroupCount >1)
		{
			propertyGroupCount--;
		}
		else
		{
			returnToHomeState();
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
			else {
				
				undoEvent.createEditFromEvent(e);
				if (ce== null)
				{
					ce = new CompoundEdit();
				}
				ce.addEdit(undoEvent);
			}
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
		if (e.getSource() instanceof SQLDatabase ||
				e.getSource() instanceof SQLTable.Folder)
		{
			SQLObjectRemoveChildren undoEvent = new SQLObjectRemoveChildren();
			if (state == UndoState.REGULAR)
			{
				undoEvent.createEditFromEvent(e);
				undoManager.addEdit(undoEvent);
			}
			else {
				undoEvent.createEditFromEvent(e);
				if (ce== null)
				{
					ce = new CompoundEdit();
				}
				ce.addEdit(undoEvent);
			}
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
		if (e.getSource() instanceof SQLDatabase &&
				e.getPropertyName().equals("shortDisplayName")){
			// this is not undoable at this time.
			
		}
		else
		{
			ArchitectPropertyChangeUndoableEdit undoEvent = new ArchitectPropertyChangeUndoableEdit(e);
			if (state == UndoState.REGULAR)
			{
				undoManager.addEdit(undoEvent);
			}
			else
			{
				if (ce== null)
				{
					ce = new CompoundEdit();
				}
				ce.addEdit(undoEvent);
			}
		}
		
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		logger.error("Unexpected structure change event");
		// too many changes clear undo
		undoManager.discardAllEdits();
		
	}

	public UndoState getState() {
		return state;
	}

	
	public void returnToHomeState()
	{
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
		state = UndoState.REGULAR;
		propertyGroupCount=0;
		simulMoveCount =0;
		logger.debug("Returning to regular state");
	}

	public void componentMoved(PlayPenComponentEvent e) {
				
	}

	public void componentResized(PlayPenComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoveStart(PlayPenComponentEvent e) {
		logger.debug("Starting a move in "+state);
		if (state == UndoState.MOVING) {
			simulMoveCount++;
			moveList.put(e.getSource(),e);
		}
		else 
		{
			if (simulMoveCount != 0)
			{
				returnToHomeState();
			}			
			moveList.put(e.getSource(),e);
			simulMoveCount++;
			state = UndoState.MOVING ;
		}	
		logger.debug("UndoAdapter Starting move "+ simulMoveCount + "::");
		
	}

	public void componentMoveEnd(PlayPenComponentEvent e) {
		logger.debug("UndoAdapter ending move "+ simulMoveCount);
		if (state == UndoState.MOVING  && simulMoveCount >1)
		{
			simulMoveCount--;
			
			PlayPenComponentEvent oldEvent = moveList.get(e.getSource());
			if (oldEvent != null)
			{
				oldEvent.setNewPoint(e.getNewPoint());
			}
			
		}
		else if(simulMoveCount ==1)
		{	
			PlayPenComponentEvent oldEvent = moveList.get(e.getSource());
			if (oldEvent != null )
			{
				oldEvent.setNewPoint(e.getNewPoint());
			}
			if( !oldEvent.getOldPoint().equals( e.getNewPoint()))
			{
				TablePaneLocationEdit tableEdit = new TablePaneLocationEdit (moveList.values());		
				if (ce != null)
				{
					ce.addEdit(tableEdit);
				}
				else 
				{
					undoManager.addEdit(tableEdit);
				}
			}
			moveList.clear();		
			returnToHomeState();
		}	
		
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}
}
