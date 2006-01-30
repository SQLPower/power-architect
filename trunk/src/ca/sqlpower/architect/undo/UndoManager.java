/**
 * 
 */
package ca.sqlpower.architect.undo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * @author Matt
 *
 */
public class UndoManager extends javax.swing.undo.UndoManager {

	private static final Logger logger = Logger.getLogger(UndoManager.class);
	/* The undo and redo buttons which get their text and image updated by the
	 * undo manager
	 */ 
	
	private Action undo;
	private Action redo;
	private PlayPen playPen;
	private boolean undoing;
	private boolean redoing;
	
	/**
	 * setup the undo manager to use these default undo and redo buttons
	 */
	public UndoManager(Action Undo, Action Redo)
	{
		super();
		init(Undo, Redo);
	}
	
	public UndoManager() {
		Action undo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt ) {
				undo();
			}
		};
	
		undo.putValue(Action.SMALL_ICON, ASUtils.createJLFIcon("general/Undo",
				"Undo",
				ArchitectFrame.getMainInstance().getSwingUserSettings().getInt(SwingUserSettings.ICON_SIZE, 24)));
		undo.putValue(Action.SHORT_DESCRIPTION, "Undo");
		undo.putValue(Action.NAME,"Undo");
		undo.setEnabled(false);
		
		Action redo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt ) {
				redo();
			}
		};
		
		redo.setEnabled(false);
		redo.putValue(Action.SMALL_ICON, ASUtils.createJLFIcon("general/Redo",
				"Redo",
				ArchitectFrame.getMainInstance().getSwingUserSettings().getInt(SwingUserSettings.ICON_SIZE, 24)));
		redo.putValue(Action.SHORT_DESCRIPTION, "Redo");
		redo.putValue(Action.NAME,"Redo");
		init(undo, redo);
	}
	/**
	 * Initializes the undo/redo actions
	 */
	private final void init(Action Undo, Action Redo) {
		undo = Undo;
		redo = Redo;
		
		
		
	}
	
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		
		if( !(isUndoing() || isRedoing())){
			logger.debug("Added new undoableEdit to undo manager "+anEdit);
			boolean success = super.addEdit(anEdit);
			refreshUndoRedo();
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
		refreshUndoRedo(undo,redo);
		undoing = false;
	}

	/**
	 * Just calls super.redo() then refreshes the undo/redo actions.
	 */
	@Override
	public synchronized void redo() throws CannotRedoException {
		redoing =true;
		super.redo();
		refreshUndoRedo(undo,redo);
		redoing =false;
	}
	
	public void refreshUndoRedo(){
		refreshUndoRedo(undo,redo);
	}
	/**
	 * Updates the enable state and message for the Undo/Redo buttons
	 * 
	 * @param Undo Action that gets the new Undo message
	 * @param Redo Action that gets the new Redo message
	 */
	public void refreshUndoRedo(Action undo, Action redo)
	{
		/* update the buttons with a new undo and redo states */ 
		if (undo != null)
		{
			undo.putValue(Action.SHORT_DESCRIPTION, this.getUndoPresentationName());
			undo.setEnabled(this.canUndo());
		}
		
		if (redo != null)
		{	
				 
			
			redo.putValue(Action.SHORT_DESCRIPTION, this.getRedoPresentationName());
			redo.setEnabled(this.canRedo());
		}
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
	
	
	public Action getRedo() {
		return redo;
	}

	public void setRedo(Action redo) {
		this.redo = redo;
	}

	public Action getUndo() {
		return undo;
	}

	public void setUndo(Action undo) {
		this.undo = undo;
	}

	public boolean isRedoing() {
		return redoing;
	}

	public boolean isUndoing() {
		return undoing;
	}

	public void setPlayPen(PlayPen playPen) throws ArchitectException {
		if (this.playPen != playPen)
		{
			SQLObjectUndoableEventAdapter undoAdapter =new SQLObjectUndoableEventAdapter(this);
			this.playPen = playPen;
			this.discardAllEdits();
			ArchitectUtils.listenToHierarchy(undoAdapter,playPen.getDatabase());
			playPen.addUndoEventListener(undoAdapter);
		}
		
		
	}

}
