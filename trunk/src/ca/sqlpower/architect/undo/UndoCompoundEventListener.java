package ca.sqlpower.architect.undo;

import java.util.EventListener;

import ca.sqlpower.architect.ArchitectException;

public interface UndoCompoundEventListener extends EventListener {

	public void compoundEditStart(UndoCompoundEvent e);
	public void compoundEditEnd(UndoCompoundEvent e);

}
