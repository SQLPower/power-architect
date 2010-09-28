/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
