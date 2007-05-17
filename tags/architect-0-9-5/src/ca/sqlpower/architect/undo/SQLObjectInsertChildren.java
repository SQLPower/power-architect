package ca.sqlpower.architect.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

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
	public void createToolTip() {
		if (e.getChildren().length > 0)
		{
			if (e.getChildren()[0] instanceof SQLTable)
			{
				toolTip = "Add table";
			}
			if (e.getChildren()[0] instanceof SQLColumn)
			{
				toolTip = "Add column";
			}
			if (e.getChildren()[0] instanceof SQLRelationship)
			{
				toolTip = "Add relation";
			}
			if (e.getChildren().length>1)
			{
				toolTip = toolTip+"s";
			}
		}
		
	}
	
	@Override
	public String toString() {
		StringBuffer childList = new StringBuffer();
		childList.append("{");
		for (SQLObject child : e.getChildren()) {
			childList.append(child).append(", ");
		}
		childList.append("}");
		return "Insert "+childList+" into "+e.getSource();
	}
}
