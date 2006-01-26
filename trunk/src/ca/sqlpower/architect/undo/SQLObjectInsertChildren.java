package ca.sqlpower.architect.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;

public class SQLObjectInsertChildren extends SQLObjectChildren {
	private static final Logger logger = Logger.getLogger(SQLObjectInsertChildren.class);
	
	@Override
	public void undo() throws CannotUndoException {
		removeChildren();
	}
	
	@Override
	public void redo() throws CannotRedoException {
		try {
			addChildren();
		} catch (ArchitectException e) {
			logger.error("redo: caught exception", e);
			throw new CannotRedoException();
		}
	}
	
	@Override
	public String getPresentationName() {
		
		return "Insert Children Into "+e.getSQLSource().getName();
	}
}
