/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
