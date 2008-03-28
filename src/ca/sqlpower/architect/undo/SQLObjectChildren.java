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

import javax.swing.undo.AbstractUndoableEdit;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;

public abstract class SQLObjectChildren extends AbstractUndoableEdit {

	protected SQLObjectEvent e;
	protected String toolTip;
	
	public SQLObjectChildren() {
		super();
		// TODO Auto-generated constructor stub
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
