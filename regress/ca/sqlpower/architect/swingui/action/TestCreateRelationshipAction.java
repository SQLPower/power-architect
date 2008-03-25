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
