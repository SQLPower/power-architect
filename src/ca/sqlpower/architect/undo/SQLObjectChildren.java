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

import javax.swing.undo.AbstractUndoableEdit;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;

public abstract class SQLObjectChildren extends AbstractUndoableEdit {

	protected SQLObjectEvent e;
	protected String toolTip;
	
	public SQLObjectChildren() {
		super();
	}
	
	public void createEditFromEvent(SQLObjectEvent event){
		
		e = event;
		createToolTip();
		
	}
	public abstract void createToolTip();
	
	public void removeChildren(){
		int changed[] =e.getChangedIndices();
		SQLObject sqlObject= e.getSQLSource();
		SQLObject parent = sqlObject.getParent();
		try {
			if (parent != null) {
				parent.setMagicEnabled(false);
			}
			sqlObject.setMagicEnabled(false);
			for (int ii = 0; ii < changed.length;ii++)
			{
				sqlObject.removeChild(changed[ii]);
			}
		}finally {
			sqlObject.setMagicEnabled(true);
			if (parent != null) {
				parent.setMagicEnabled(true);
			}
		}
	}
	
	
	public void addChildren() throws ArchitectException {
	
		int changed[] = e.getChangedIndices();
		SQLObject sqlObject= e.getSQLSource();
		SQLObject children[] = e.getChildren();
		SQLObject parent = sqlObject.getParent();
		try{
			if (parent != null) {
				parent.setMagicEnabled(false);
			}
			sqlObject.setMagicEnabled(false);
			for (int ii = 0; ii < changed.length; ii++) {
				sqlObject.addChild(changed[ii], children[ii]);
				
			}
		}finally {
			if (parent != null) {
				parent.setMagicEnabled(true);
			}
			sqlObject.setMagicEnabled(true);
		}
	}
	
	
	@Override
	public boolean canRedo() {
		return true;
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}
	
	@Override
	public String getPresentationName() {
		
		return toolTip;
	}
	
}
