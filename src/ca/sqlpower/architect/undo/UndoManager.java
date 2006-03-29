/*  
 * This code belongs to SQL Power
 */
package ca.sqlpower.architect.undo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;

/**
 * @author Matt
 *
 */
public class UndoManager extends javax.swing.undo.UndoManager {

	private static final Logger logger = Logger.getLogger(UndoManager.class);
	
	/**
	 * Converts received SQLObjectEvents into UndoableEdits and adds them to an UndoManager. 
	 * @author Matt
	 */
	public class SQLObjectUndoableEventAdapter  implements UndoCompoundEventListener,
			SQLObjectListener, PropertyChangeListener, PlayPenComponentListener {
		
		
		private PlayPenComponentEvent movementEvent;
		private CompoundEdit ce;
		private int compoundEditStackCount;
		private int simulMoveCount;
		private HashMap<Object,PlayPenComponentEvent> moveList;
		
		public SQLObjectUndoableEventAdapter() {
			
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
				returnToEditState();   
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
				UndoManager.this.addEdit(undoEdit);
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
			UndoManager.this.discardAllEdits();
			
			
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
				// add at least one movement when ignoring move events
				if (movementEvent != null)
				{
					ce.addEdit(new TablePaneLocationEdit(movementEvent));
					movementEvent = null;
				}
				if (ce.canUndo())
				{
					logger.debug("Adding compound edit "+ ce +" to undo manager");
					UndoManager.this.addEdit(ce);
				} else {
					logger.debug("Compound edit "+ ce +" is not undoable so we are not adding it");
				}
				
				ce = null;
			}
			else
			{
				if (movementEvent != null)
				{
					UndoManager.this.addEdit(new TablePaneLocationEdit(movementEvent));
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
			if ( simulMoveCount <= 0 )
				throw new IllegalStateException("Trying to move a component that has not started");
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
				logger.debug("compoundEditEnd with event: "+e.toString());
			}
			compoundGroupEnd();
			
		}
	}

	private SQLObjectUndoableEventAdapter eventAdapter = new SQLObjectUndoableEventAdapter();
	private boolean undoing;
	private boolean redoing;
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	
	/**
	 * Creates a new UndoManager and attaches it to the given PlayPen's
	 * component and SQL Object model events.
	 * 
	 * @param playPen
	 *            The play pen to track undo/redo history for.
	 * @throws ArchitectException
	 *             If the manager fails to listen to all objects in the play
	 *             pen's database hierarchy.
	 */
	public UndoManager(PlayPen playPen) throws ArchitectException {
		init(playPen, playPen.getDatabase());
	}
	
	public UndoManager(SQLObject sqlObjectRoot) throws ArchitectException {
		init(null, sqlObjectRoot);
	}
	
	private final void init(PlayPen playPen, SQLObject sqlObjectRoot) throws ArchitectException {
		ArchitectUtils.listenToHierarchy(eventAdapter,sqlObjectRoot);
		ArchitectUtils.addUndoListenerToHierarchy(eventAdapter,sqlObjectRoot);
		if (playPen != null) {			
			playPen.addUndoEventListener(eventAdapter);
			playPen.getPlayPenContentPane().addPlayPenComponentListener(eventAdapter);
		}
	}
	
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		
		if( !(isUndoing() || isRedoing())){
			logger.debug("Added new undoableEdit to undo manager "+anEdit);
			boolean success = super.addEdit(anEdit);
			fireStateChanged();
			return success; 
		}
		// processing an edit so we pretend to absorb this edit
		return true;
	}
	
	/**
	 * Just calls super.undo() then refreshes the undo/redo actions.
	 */
	@Override
	public synchronized void undo() throws CannotUndoException {
		undoing = true;
		super.undo();
		fireStateChanged();
		undoing = false;
	}

	/**
	 * Just calls super.redo() then refreshes the undo/redo actions.
	 */
	@Override
	public synchronized void redo() throws CannotRedoException {
		redoing =true;
		super.redo();
		fireStateChanged();
		redoing =false;
	}
	
	
	
	/* Public getters and setters appear after this point */

	public int getUndoableEditCount(){
		if (editToBeUndone() == null) return 0;
		int count;
		// edits is a 0 based vector
		count = this.edits.indexOf(this.editToBeUndone())+1;
		return count;
	}
	
	public int getRedoableEditCount(){
		if (editToBeRedone() == null) return 0;
		int count;
		count = edits.size() -this.edits.indexOf(this.editToBeRedone());
		return count;
	}
	
	public boolean isRedoing() {
		return redoing;
	}

	public boolean isUndoing() {
		return undoing;
	}



	public SQLObjectUndoableEventAdapter getEventAdapter() {
		return eventAdapter;
	}
	
	
	// Change event support
	
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}
	
	/**
	 * Notifies listeners that the undo/redo list might have changed.
	 */
	public void fireStateChanged() {
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener l : changeListeners) {
			l.stateChanged(event);
		}
	}

	
}
