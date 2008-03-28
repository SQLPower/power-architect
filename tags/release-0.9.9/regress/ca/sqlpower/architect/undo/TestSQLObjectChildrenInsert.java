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

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;


public class TestSQLObjectChildrenInsert extends TestCase {
	
	
	
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	
	public void testFolderInsert(){
		
	}
	
	public void testDatabaseInsert()throws ArchitectException {
		
		// setup a playpen like database
		SQLDatabase db = new SQLDatabase();
		UndoManager undoManager = new UndoManager(db);
		db.setPlayPenDatabase(true);
		SQLTable table1 = new SQLTable(db,"table1","remark1","TABLE",true);
		SQLTable table2 = new SQLTable(db,"table2","remark2","TABLE",true);
		SQLTable table3 = new SQLTable(db,"table3","remark3","TABLE",true);
		SQLTable table4 = new SQLTable(db,"table4","remark4","TABLE",true);
		db.addChild(table1);
		db.addChild(table2);
		db.addChild(table3);
		db.addChild(table4);
		db.removeChild(2);
		undoManager.undo();
		assertEquals("There should be 4 children",4,db.getChildCount());
		assertEquals("The first table is in the wrong position",table1,db.getChild(0));
		assertEquals("The Second table is in the wrong position",table2,db.getChild(1));
		assertEquals("The Third table is in the wrong position",table3,db.getChild(2));
		assertEquals("The Fourth table is in the wrong position",table4,db.getChild(3));
		

		undoManager.redo();
		assertEquals("There should be 3 children",3,db.getChildCount());
		assertEquals("The first table is in the wrong position",table1,db.getChild(0));
		assertEquals("The Second table is in the wrong position",table2,db.getChild(1));
		assertEquals("The Third table is in the wrong position",table4,db.getChild(2));
		
	}
	

}
