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
package ca.sqlpower.architect.swingui.action;


import java.awt.Point;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.TablePane;

public class TestCreateRelationshipAction extends TestCase {

	PlayPen pp;
	SQLTable fkTable;
	SQLTable pkTable;
	
	protected void setUp() throws Exception {
		super.setUp();
		SQLDatabase db = new SQLDatabase();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = new PlayPen(session);
		fkTable = new SQLTable(db,true);
		TablePane tp = new TablePane(fkTable,pp);
		pp.addTablePane(tp,new Point(1,1));
		 pkTable = new SQLTable(db,true);
		TablePane tp2 = new TablePane(pkTable,pp);
		pp.addTablePane(tp2,new Point(1,1));
		pkTable.addColumn(new SQLColumn());
		pkTable.addColumn(new SQLColumn());
		pkTable.getColumn(0).setPrimaryKeySeq(1);
		pkTable.getColumn(0).setName("pk1");
		pkTable.getColumn(1).setPrimaryKeySeq(1);
		pkTable.getColumn(1).setName("pk2");
		
		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.action.CreateRelationshipAction.doCreateRelationship(SQLTable, SQLTable, PlayPen, boolean)'
	 */
	public void testDoCreateRelationshipIdentifying() throws ArchitectException {
		assertEquals("Oops started out with relationships",0,pp.getRelationships().size());
		CreateRelationshipAction.doCreateRelationship(pkTable,fkTable,pp,true);
		assertEquals("Wrong number of relationships created",1,pp.getRelationships().size());
		assertEquals("Did the relationship create the columns in the fkTable",2,fkTable.getColumns().size());
		List<SQLColumn> columns = fkTable.getColumns();
		assertNotNull("Is the first column a key column?",columns.get(0).getPrimaryKeySeq());
		assertNotNull("Is the second column a key column?",columns.get(1).getPrimaryKeySeq());
		assertEquals("Is the first column pk1?","pk1",columns.get(0).getName());
		assertEquals("Is the second column pk2?","pk2",columns.get(1).getName());
		
	}
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.action.CreateRelationshipAction.doCreateRelationship(SQLTable, SQLTable, PlayPen, boolean)'
	 */
	public void testDoCreateRelationshipNonIdentifying() throws ArchitectException {
		assertEquals("Oops started out with relationships",0,pp.getRelationships().size());
		CreateRelationshipAction.doCreateRelationship(pkTable,fkTable,pp,false);
		assertEquals("Wrong number of relationships created",1,pp.getRelationships().size());
		assertEquals("Did the relationship create the columns in the fkTable",2,fkTable.getColumns().size());
		List<SQLColumn> columns = fkTable.getColumns();
		assertNull("Is the first column a key column?",columns.get(0).getPrimaryKeySeq());
		assertNull("Is the second column a key column?",columns.get(1).getPrimaryKeySeq());
		assertEquals("Is the first column pk1?","pk1",columns.get(0).getName());
		assertEquals("Is the second column pk2?","pk2",columns.get(1).getName());
		
	}
	
	public void testDoCreateRelationshipHicjackColumn() throws ArchitectException {
		fkTable.addColumn(new SQLColumn());
		fkTable.getColumn(0).setName("pk1");
		assertEquals("Oops started out with relationships",0,pp.getRelationships().size());
		CreateRelationshipAction.doCreateRelationship(pkTable,fkTable,pp,true);
		assertEquals("Wrong number of relationships created",1,pp.getRelationships().size());
		assertEquals("Did the relationship create the columns in the fkTable",2,fkTable.getColumns().size());
		List<SQLColumn> columns = fkTable.getColumns();
		assertEquals("Do we only have two columns in the fk table",2,columns.size());
		assertNotNull("Is the first column a key column?",columns.get(0).getPrimaryKeySeq());
		assertNotNull("Is the second column a key column?",columns.get(1).getPrimaryKeySeq());
		assertEquals("Is the first column pk1?","pk1",columns.get(0).getName());
		assertEquals("Is the second column pk2?","pk2",columns.get(1).getName());
		
	}
	

}
