package ca.sqlpower.architect.undo;

import java.util.EventListener;

import ca.sqlpower.architect.ArchitectException;

public interface UndoCompoundEventListener extends EventListener {

	public void dragAndDropStart(UndoCompoundEvent e);
	public void dragAndDropEnd(UndoCompoundEvent e);
	public void multiSelectStart(UndoCompoundEvent e);
	public void multiSelectEnd(UndoCompoundEvent e);
	public void propertyGroupStart(UndoCompoundEvent e);
	public void propertyGroupEnd(UndoCompoundEvent e);
}
