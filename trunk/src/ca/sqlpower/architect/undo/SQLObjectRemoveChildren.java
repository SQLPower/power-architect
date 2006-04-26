package ca.sqlpower.architect.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

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
	public void createToolTip() {
		if (e.getChildren() != null)
		{
			if (e.getChildren()[0] instanceof SQLTable)
			{
				toolTip = "Remove table";
			}
			if (e.getChildren()[0] instanceof SQLColumn)
			{
				toolTip = "Remove column";
			}
			if (e.getChildren()[0] instanceof SQLRelationship)
			{
				toolTip = "Remove relation";
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
		return "Remove "+childList+" from "+e.getSource();
	}
}
