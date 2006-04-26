package ca.sqlpower.architect;

import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

public class CountingUndoCompoundEventListener implements
		UndoCompoundEventListener, SQLObjectListener {

	// ========= Undo Compound Listener ==========
	private int editDepth;
	private int editsInLastGroup;
	private int editsBeforeLastGroup;
	
	public void compoundEditStart(UndoCompoundEvent e) {
		if (editDepth == 0) {
			editsBeforeLastGroup = liveEdits;
		}
		editDepth++;
	}

	/**
	 * Ends the current compound edit. This method makes sure there was already
	 * a compound edit in progress. If there was not a compound edit in
	 * progress, throws IllegalStateException. In that case, there is either a
	 * bug in the code you're listening to, or you started listening to an
	 * object that was already in the middle of a compound edit.
	 */
	public void compoundEditEnd(UndoCompoundEvent e) {
		if (editDepth == 0) {
			throw new IllegalStateException("Compound edit depth was already 0");
		}
		editDepth--;
		if (editDepth == 0) {
			editsInLastGroup = liveEdits;
			liveEdits = 0;
		}
	}

	/**
	 * Returns how many compound edits have started minus how many
	 * compound edits have finished. Due to fail-fast check on {@link #compoundEditEnd(UndoCompoundEvent)},
	 * this will never be less than 0.
	 */
	public int getEditDepth() {
		return editDepth;
	}

	/**
	 * Returns the number of edits that happened in the last complete
	 * group of compound edits.  Does not count edits inside the current
	 * compound edit (if there is one).
	 */
	public int getEditsInLastGroup() {
		return editsInLastGroup;
	}
	
	/**
	 * Returns the number of edits that happened outside of any compound
	 * group, before the current compound group started.  If not currently
	 * in a compound group ({@link #getEditDepth()} returns 0), this returns
	 * the count from before the last group started, not the current live edit
	 * count.
	 */
	public int getEditsBeforeLastGroup() {
		return editsBeforeLastGroup;
	}

	// ========= SQL Object Listener ==========
	private int liveEdits;
	
	public void dbChildrenInserted(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		liveEdits++;
	}
}
