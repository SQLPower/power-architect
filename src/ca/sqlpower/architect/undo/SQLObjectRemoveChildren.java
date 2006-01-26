package ca.sqlpower.architect.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;

public class SQLObjectRemoveChildren extends SQLObjectChildren {
	private static final Logger logger = Logger.getLogger(SQLObjectRemoveChildren.class);

	@Override
	public void undo() throws CannotUndoException {
		try {
			addChildren();
		} catch (ArchitectException e) {
			logger.error("Can't undo: caught exception", e);
			throw new CannotUndoException();
		}
	}
	
	@Override
	public void redo() throws CannotRedoException {
		removeChildren();
	}
	@Override
	public String getPresentationName() {
		
		return "Remove Children From "+e.getSQLSource().getName();
	}
}
