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
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;


public class TestSQLRelationship extends SQLTestCase {

    /**
     * Set up by {@link #setUp()} to have this structure:
     * <pre>
     * CREATE TABLE parent (
     *   pkcol_1 INTEGER NOT NULL,
     *   pkcol_2 INTEGER NOT NULL,
     *   attribute_1 INTEGER NOT NULL
     * );
     * </pre>
     * <p>
     * Note there are no columns in this table's primary key.
     */
	private SQLTable parentTable;
    
    /**
     * Set up by {@link #setUp()} to have this structure:
     * <pre>
     * CREATE TABLE parent (
     *   child_pkcol_1 INTEGER NOT NULL,
     *   child_pkcol_2 INTEGER NOT NULL,
     *   child_attribute INTEGER NOT NULL
     * );
     * </pre>
     * <p>
     * Note there are no columns in this table's primary key.
     */
	private SQLTable childTable1;
    
    /**
     * Set up by {@link #setUp()} to have this structure:
     * <pre>
     * CREATE TABLE parent (
     *   child2_pkcol_1 INTEGER NOT NULL,
     *   child2_pkcol_2 INTEGER NOT NULL,
     *   child2_attribute INTEGER NOT NULL
     * );
     * </pre>
     * <p>
     * Note there are no columns in this table's primary key.
     */
	private SQLTable childTable2;
    
	private SQLRelationship rel1;
	private SQLRelationship rel2;
    
    /**
     * The SQLDatabase that contains parentTable, childTable1, childTable2,
     * rel1, and rel2 after {@link #setUp()} has run.
     */
	private SQLDatabase database;
	
	public TestSQLRelationship(String name) throws Exception {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("-------------Starting New Test "+getName()+" -------------");
		
		database = new SQLDatabase();
		parentTable = new SQLTable(database, "parent", null, "TABLE", true);
		SQLColumn pkcol1 = new SQLColumn(parentTable, "pkcol_1", Types.INTEGER, 10, 0);
		SQLColumn pkcol2 = new SQLColumn(parentTable, "pkcol_2", Types.INTEGER, 10, 0);
        parentTable.addColumn(pkcol1);
        parentTable.addColumn(pkcol2);
		parentTable.addColumn(new SQLColumn(parentTable, "attribute_1", Types.INTEGER, 10, 0));
		
		SQLIndex parentTablePK = new SQLIndex();
		parentTablePK.setPrimaryKeyIndex(true);
		parentTablePK.addChild(parentTablePK.new Column(pkcol1, AscendDescend.UNSPECIFIED));
		parentTablePK.addChild(parentTablePK.new Column(pkcol2, AscendDescend.UNSPECIFIED));
		parentTablePK.setName("parentTable_pk");
		parentTable.addIndex(parentTablePK);
		database.addChild(parentTable);
		
		childTable1 = new SQLTable(database, "child_1", null, "TABLE", true);
		childTable1.addColumn(new SQLColumn(childTable1, "child_pkcol_1", Types.INTEGER, 10, 0));
		childTable1.addColumn(new SQLColumn(childTable1, "child_pkcol_2", Types.INTEGER, 10, 0));
		childTable1.addColumn(new SQLColumn(childTable1, "child_attribute", Types.INTEGER, 10, 0));
		database.addChild(childTable1);
		
		childTable2 = new SQLTable(database, "child_2", null, "TABLE", true);
		childTable2.addColumn(new SQLColumn(childTable2, "child2_pkcol_1", Types.INTEGER, 10, 0));
		childTable2.addColumn(new SQLColumn(childTable2, "child2_pkcol_2", Types.INTEGER, 10, 0));
		childTable2.addColumn(new SQLColumn(childTable2, "child2_attribute", Types.INTEGER, 10, 0));
		database.addChild(childTable2);
		
		rel1 = new SQLRelationship();
		rel1.setIdentifying(true);
		rel1.attachRelationship(parentTable,childTable1,false);
		rel1.setName("rel1");
		rel1.addMapping(parentTable.getColumn(0), childTable1.getColumn(0));
		rel1.addMapping(parentTable.getColumn(1), childTable1.getColumn(1));
	
		rel2 = new SQLRelationship();
		rel2.setName("rel2");
		rel2.attachRelationship(parentTable,childTable2,true);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Returns one of the relationships that setUp makes.
	 * Right now, it's rel1.
	 */
	@Override
	protected SQLObject getSQLObjectUnderTest() {
		return rel1;
	}
	
	public void testSetPhysicalName() {
		CountingSQLObjectListener l = new CountingSQLObjectListener();
		rel1.addSQLObjectListener(l);
		
		// ensure all event counts start with 0
		assertEquals(0, l.getInsertedCount());
		assertEquals(0, l.getRemovedCount());
		assertEquals(0, l.getChangedCount());
		assertEquals(0, l.getStructureChangedCount());
		
		rel1.setPhysicalName("test_new_name");
		
		// ensure only dbObjectChanged was called (we omit this check for the remainder of the tests)
		assertEquals(0, l.getInsertedCount());
		assertEquals(0, l.getRemovedCount());
		assertEquals(1, l.getChangedCount());
		assertEquals(0, l.getStructureChangedCount());
		
		assertEquals("new name didn't stick", "test_new_name", rel1.getPhysicalName());
		
		rel1.setPhysicalName("test_new_name");
		assertEquals(1, l.getChangedCount());

		rel1.setPhysicalName("test_actual_new_name");
		assertEquals(2, l.getChangedCount());

		rel1.setPhysicalName(null);
		assertEquals(3, l.getChangedCount());
		assertEquals("new name didn't go back to logical name", rel1.getName(), rel1.getPhysicalName());

		rel1.setPhysicalName(null);
		assertEquals(3, l.getChangedCount());

		// double-check that none of the other event types got fired
		assertEquals(0, l.getInsertedCount());
		assertEquals(0, l.getRemovedCount());
		assertEquals(0, l.getStructureChangedCount());
	}

	public void testReadFromDB() throws Exception {
		Connection con = null;
		Statement stmt = null;
		String lastSQL = null;
		try {
			con = db.getConnection();
			stmt = con.createStatement();

			try {
				stmt.executeUpdate("DROP TABLE relationship_test_child");
			} catch (SQLException ex) {
				System.out.println("Ignoring SQL Exception; assume relationship_test_child didn't exist.");
				System.out.println(ex.getMessage());
			}

			try {
				stmt.executeUpdate("DROP TABLE relationship_test_parent");
			} catch (SQLException ex) {
				System.out.println("Ignoring SQL Exception; assume relationship_test_parent didn't exist.");
				System.out.println(ex.getMessage());
			}

			lastSQL = "CREATE TABLE relationship_test_parent (\n" +
					  " pkcol_1 integer not null,\n" +
					  " pkcol_2 integer not null,\n" +
					  " attribute_1 integer not null)";
			stmt.executeUpdate(lastSQL);

			lastSQL = "CREATE TABLE relationship_test_child (\n" +
			          " parent_pkcol_1 integer not null,\n" +
			          " parent_pkcol_2 integer not null,\n" +
			          " child_attribute_1 integer not null)";
			stmt.executeUpdate(lastSQL);
			
			lastSQL = "ALTER TABLE relationship_test_parent\n" +
			          " ADD CONSTRAINT relationship_test_pk\n" +
			          " PRIMARY KEY (pkcol_1 , pkcol_2)";
			stmt.executeUpdate(lastSQL);
			
			lastSQL = "ALTER TABLE relationship_test_child\n" +
			          " ADD CONSTRAINT relationship_test_fk\n" +
			          " FOREIGN KEY (parent_pkcol_1, parent_pkcol_2)\n" +
			          " REFERENCES relationship_test_parent (pkcol_1 , pkcol_2)";
			stmt.executeUpdate(lastSQL);
			
		} catch (SQLException ex) {
			System.out.println("SQL Statement Failed:\n"+lastSQL+"\nStack trace of SQLException follows:");
			ex.printStackTrace();
			fail("SQL statement failed. See system console for details.");
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException e) {
				System.out.println("Couldn't close statement");
				e.printStackTrace();
			}
			try {
				if (con != null) con.close();
			} catch (SQLException e) {
				System.out.println("Couldn't close connection");
				e.printStackTrace();
			}
		}
		
		SQLTable parent = db.getTableByName("relationship_test_parent");
		SQLTable child = db.getTableByName("relationship_test_child");
		
		if (parent == null) {
			parent = db.getTableByName("relationship_test_parent".toUpperCase());
		}
		SQLRelationship rel = (SQLRelationship) parent.getExportedKeys().get(0);
		
		assertEquals("relationship_test_fk", rel.getName().toLowerCase());
		assertSame(parent, rel.getPkTable());
		assertSame(child, rel.getFkTable());
		assertEquals((SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY), rel.getFkCardinality());
		assertEquals(SQLRelationship.ONE, rel.getPkCardinality());
	}

	public void testAllowsChildren() {
		assertTrue(rel1.allowsChildren());
	}

	public void testSQLRelationship() throws ArchitectException {
		SQLRelationship rel = new SQLRelationship();
		assertNotNull(rel.getChildren());
		assertNotNull(rel.getSQLObjectListeners());
	}

	public void testGetMappingByPkCol() throws ArchitectException {
		SQLColumn col = parentTable.getColumnByName("pkcol_1");
		SQLRelationship.ColumnMapping m = rel1.getMappingByPkCol(col);
		assertEquals("pkcol_1", m.getPkColumn().getName());
		assertEquals("child_pkcol_1", m.getFkColumn().getName());

		// check another column (in case it always returns the first mapping or something)
		col = parentTable.getColumnByName("pkcol_2");
		m = rel1.getMappingByPkCol(col);
		assertEquals("pkcol_2", m.getPkColumn().getName());
		assertEquals("child_pkcol_2", m.getFkColumn().getName());
	}
	
	public void testGetNonExistentMappingByPkCol() throws ArchitectException {
		// check a column that's in the PK table but not in the mapping
		SQLColumn col = parentTable.getColumnByName("attribute_1");
		SQLRelationship.ColumnMapping m = rel1.getMappingByPkCol(col);
		assertNull(m);
	}

	/** This was a real regression */
	public void testDeletePkColRemovesFkCol() throws ArchitectException {
		SQLColumn pkcol = parentTable.getColumnByName("pkcol_1");
		assertNotNull("Child col should exist to start", childTable1.getColumnByName("child_pkcol_1"));
		parentTable.removeColumn(pkcol);
		assertNull("Child col should have been removed", childTable1.getColumnByName("child_pkcol_1"));
	}
	
	/**
	 * testing that a column gets hijacked and promoted to the primary key
	 * when the corrisponding pk column is added into the primary key 
	 * 
	 * @throws ArchitectException
	 */
	public void testHijackedColumnGoesToPK() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "hijack", Types.INTEGER, 10, 0);
		SQLColumn fkcol = new SQLColumn(childTable1, "hijack", Types.INTEGER, 10, 0);
		SQLRelationship rel = parentTable.getExportedKeys().get(0);
		childTable1.addColumn(0, fkcol);
		parentTable.addColumn(0, pkcol);
		pkcol.setPrimaryKeySeq(0);
		
		assertNotNull("parent column didn't to go PK", pkcol.getPrimaryKeySeq());
		assertTrue("column didn't get hijacked", rel.containsFkColumn(fkcol));
		
		// this is the point of the test
		assertNotNull("column didn't go to primary key", fkcol.getPrimaryKeySeq());
	}
	
	/**
	 * testing that a column gets hijacked and promoted to the primary key
	 * when the corrisponding pk column is moved into the primary key from further
	 * down in its column list. 
	 * 
	 * @throws ArchitectException
	 */
	public void testHijackedColumnGoesToPK2() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "hijack", Types.INTEGER, 10, 0);
		SQLColumn fkcol = new SQLColumn(childTable1, "hijack", Types.INTEGER, 10, 0);
		SQLRelationship rel = parentTable.getExportedKeys().get(0);
		childTable1.addColumn( fkcol);
		parentTable.addColumn( pkcol);
		assertNull("pkcol already in the primary key",pkcol.getPrimaryKeySeq());
		pkcol.setPrimaryKeySeq(0);
		
		assertNotNull("parent column didn't to go PK", pkcol.getPrimaryKeySeq());
		assertTrue("column didn't get hijacked", rel.containsFkColumn(fkcol));
		
		// this is the point of the test
		assertNotNull("column didn't go to primary key", fkcol.getPrimaryKeySeq());
	}
	
	public void testFKColManagerRemovesImportedKey() throws ArchitectException {
		assertTrue("Parent table should export rel1",parentTable.getExportedKeys().contains(rel1));
		assertTrue("childTable1 should import rel1",childTable1.getImportedKeys().contains(rel1));
		assertEquals("Child's imported count is whacked out", 1, childTable1.getImportedKeys().size());
		
		assertNotNull("Missing imported key", childTable1.getColumnByName("child_pkcol_1"));
		assertNotNull("Missing imported key", childTable1.getColumnByName("child_pkcol_2"));
		int oldChildColCount = childTable1.getColumns().size();
		
		parentTable.removeExportedKey(rel1);

		assertFalse("Parent table should not export rel1 any more", parentTable.getExportedKeys().contains(rel1));
		assertFalse("childTable1 should not import rel1 any more", childTable1.getImportedKeys().contains(rel1));
				
		// the following tests depend on FKColumnManager behaviour, not UndoManager
		assertEquals("Relationship still attached to child", 0, childTable1.getImportedKeys().size());
		assertNull("Orphaned imported key", childTable1.getColumnByName("child_pkcol_1"));
		assertNull("Orphaned imported key", childTable1.getColumnByName("child_pkcol_2"));
		assertEquals("Child column list should have shrunk by 2", oldChildColCount - 2, childTable1.getColumns().size());
		assertNotNull("Missing exported key", parentTable.getColumnByName("pkcol_1"));
		assertNotNull("Missing exported key", parentTable.getColumnByName("pkcol_2"));
	}
	
	public void testRemovedRelationshipsDontInterfere() throws ArchitectException {
		testFKColManagerRemovesImportedKey();
		
		int oldChildColCount = childTable1.getColumns().size();
		
		SQLColumn pk3 = new SQLColumn(parentTable, "pk3", Types.VARCHAR, 10, 0);
		parentTable.addColumn(pk3);
		pk3.setPrimaryKeySeq(0);
		
		assertEquals("Child table got new col!?!", oldChildColCount, childTable1.getColumns().size());
	}
	
	public void testRemoveChildTable() throws ArchitectException {
		
		assertEquals(3,database.getChildCount());
		assertEquals(2,parentTable.getExportedKeys().size());
		
		database.removeChild(childTable1);
		assertEquals(2,database.getChildCount());
		assertEquals(1,parentTable.getExportedKeys().size());
		
		assertNull("Child table not removed from the database",
				database.getTableByName(childTable1.getName()));
		assertFalse("Parent still contains a reference to a deleted table", 
				parentTable.getExportedKeys().contains(rel1));
		
		database.removeChild(childTable2);
		
		assertNull("Child table 2 not removed from the database",
				database.getTableByName(childTable2.getName()));
		assertFalse("Parent still contains a reference to a deleted table", 
				parentTable.getExportedKeys().contains(rel2));
		
		assertEquals(1,database.getChildCount());
		assertEquals(0,parentTable.getExportedKeys().size());
	}
	
	public void testRemoveParentTable() throws ArchitectException {
		database.removeChild(parentTable);
		assertNull("Child table not removed from the database",database.getTableByName(parentTable.getName()));
		assertFalse("Parent still contains a reference to a deleted table", 
				parentTable.getExportedKeys().contains(rel1));
	}
	
	public void testPKColNameChangeGoesToFKColWhenNamesWereSame() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "old name", Types.VARCHAR, 10, 0);
		parentTable.addColumn(pkcol);
		pkcol.setPrimaryKeySeq(0);
		
		SQLColumn fkcol = childTable1.getColumnByName("old name");
		
		pkcol.setName("new name");
		
		assertEquals("fkcol's name didn't update", "new name", fkcol.getName());
	}

	public void testPKColNameChangeDoesntGoToFKColWhenNamesWereDifferent() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "old name", Types.VARCHAR, 10, 0);
		parentTable.addColumn(pkcol);
		pkcol.setPrimaryKeySeq(0);
		
		SQLColumn fkcol = childTable1.getColumnByName("old name");
		
		fkcol.setName("custom fk col name");
		pkcol.setName("new name");
		
		assertEquals("fkcol's name didn't update", "custom fk col name", fkcol.getName());
	}

	public void testPKColTypeChangeGoesToFKCol() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "old name", Types.VARCHAR, 10, 0);
		parentTable.addColumn(pkcol);
		pkcol.setPrimaryKeySeq(0);
		
		SQLColumn fkcol = childTable1.getColumnByName("old name");
		
		pkcol.setType(Types.BINARY);
		
		assertEquals("fkcol's type didn't update", Types.BINARY, fkcol.getType());
	}
    
    public void testCreateIdentifyingRelationship() throws ArchitectException {
        SQLTable parent = new SQLTable(null, "Parent", null, "TABLE", true);
        SQLTable child = new SQLTable(null, "Child", null, "TABLE", true);
        SQLColumn parentCol1 = new SQLColumn(null, "pk1", Types.INTEGER, 10, 0);
        SQLColumn childCol1 = new SQLColumn(null, "child_attr", Types.INTEGER, 10, 0);
        
        parent.addColumn(parentCol1);
        parentCol1.setPrimaryKeySeq(0);
        
        child.addColumn(childCol1);
        childCol1.setPrimaryKeySeq(null);
        
        SQLRelationship rel = new SQLRelationship();
        rel.setIdentifying(true);
        rel.attachRelationship(parent, child, true);
        
        assertEquals("pk1", parent.getColumn(0).getName());
        assertEquals(new Integer(0), parent.getColumn(0).getPrimaryKeySeq());
        
        assertEquals("pk1", child.getColumn(0).getName());
        assertEquals(new Integer(0), child.getColumn(0).getPrimaryKeySeq());
        assertEquals("child_attr", child.getColumn(1).getName());
        assertEquals(null, child.getColumn(1).getPrimaryKeySeq());
    }
    
    public void testCreateNonIdentifyingRelationship() throws ArchitectException {
        SQLTable parent = new SQLTable(null, "Parent", null, "TABLE", true);
        SQLTable child = new SQLTable(null, "Child", null, "TABLE", true);
        SQLColumn parentCol1 = new SQLColumn(null, "pk1", Types.INTEGER, 10, 0);
        SQLColumn childCol1 = new SQLColumn(null, "child_attr", Types.INTEGER, 10, 0);
        
        parent.addColumn(parentCol1);
        parentCol1.setPrimaryKeySeq(0);
        
        child.addColumn(childCol1);
        childCol1.setPrimaryKeySeq(null);
        
        SQLRelationship rel = new SQLRelationship();
        rel.setIdentifying(false);
        rel.attachRelationship(parent, child, true);
        
        assertEquals("pk1", parent.getColumn(0).getName());
        assertEquals(new Integer(0), parent.getColumn(0).getPrimaryKeySeq());
        
        assertEquals("child_attr", child.getColumn(0).getName());
        assertEquals(null, child.getColumn(0).getPrimaryKeySeq());
        assertEquals("pk1", child.getColumn(1).getName());
        assertEquals(null, child.getColumn(1).getPrimaryKeySeq());
    }

	public void testPKColPrecisionChangeGoesToFKCol() throws ArchitectException {
		SQLColumn pkcol = new SQLColumn(parentTable, "old name", Types.VARCHAR, 10, 0);
		parentTable.addColumn(pkcol);
		pkcol.setPrimaryKeySeq(0);
		
		SQLColumn fkcol = childTable1.getColumnByName("old name");
		
		pkcol.setPrecision(20);
		
		assertEquals("fkcol's precision didn't update", 20, fkcol.getPrecision());
	}

	/** This is something the undo manager will attempt when you undo deleting a relationship */
	public void testReconnectOldRelationshipWithCustomMapping() throws ArchitectException {
		List<SQLColumn> origParentCols = new ArrayList<SQLColumn>(parentTable.getColumns()); 
		List<SQLColumn> origChild1Cols = new ArrayList<SQLColumn>(childTable1.getColumns());
        List<ColumnMapping> origMapping = new ArrayList<ColumnMapping>(rel1.getChildren());

		parentTable.removeExportedKey(rel1);
		rel1.attachRelationship(parentTable, childTable1, false);

        assertEquals(origMapping, rel1.getChildren());
        
        assertEquals(Integer.valueOf(0), childTable1.getColumnByName("child_pkcol_1").getPrimaryKeySeq());
        assertEquals(Integer.valueOf(1), childTable1.getColumnByName("child_pkcol_2").getPrimaryKeySeq());

        assertEquals("Exported key columns disappeared", origParentCols, parentTable.getColumns());
		assertEquals("Imported key columns didn't get put back", origChild1Cols, childTable1.getColumns());
		assertEquals("There are multiple copies of this relationship in the parent's exported keys folder",2,parentTable.getExportedKeys().size());
		assertEquals("There are multiple copies of this relationship in the child's imported keys folder",1,childTable1.getImportedKeys().size());
	}
	
	/** This is something the undo manager will attempt when you undo deleting a relationship */
	public void testReconnectOldRelationshipWithAutoMapping() throws ArchitectException {
		SQLTable myParent = new SQLTable(db, true);
		SQLColumn col;
		myParent.addColumn(col = new SQLColumn(myParent, "pkcol1", Types.VARCHAR, 10, 0));
		col.setPrimaryKeySeq(0);
		myParent.addColumn(col = new SQLColumn(myParent, "pkcol2", Types.VARCHAR, 10, 0));
		col.setPrimaryKeySeq(0);
		
		SQLTable myChild = new SQLTable(db, true);
		
		SQLRelationship myRel = new SQLRelationship();
		myRel.attachRelationship(myParent, myChild, true);
		List<SQLColumn> origParentCols = new ArrayList<SQLColumn>(myParent.getColumns()); 
		List<SQLColumn> origChildCols = new ArrayList<SQLColumn>(myChild.getColumns());

		// the next two lines are what the business model sees from undo/redo
		myParent.removeExportedKey(myRel);
		myRel.attachRelationship(myParent, myChild, false);
		
		assertEquals("Exported key columns disappeared", origParentCols, myParent.getColumns());
		assertEquals("Imported key columns didn't get put back", origChildCols, myChild.getColumns());
		assertEquals("There are multiple copies of this relationship in the parent's export keys folder",1,myParent.getExportedKeys().size());
		assertEquals("There are multiple copies of this relationship in the child's import keys folder",1,myChild.getImportedKeys().size());
	}

		
	public void testMovingPKColOutOfPK() throws ArchitectException {
		SQLColumn col = parentTable.getColumnByName("pkcol_1");
		
		col.setPrimaryKeySeq(null);
		assertTrue("pkcol_1 dropped from the parent table", parentTable.getColumns().contains(col));
	}
	public void testMovingPKColOutOfPKByColIndex() throws ArchitectException {
		SQLColumn col = parentTable.getColumnByName("pkcol_2");
		int index = parentTable.getColumnIndex(col);
		parentTable.changeColumnIndex(index,1,false);
		assertTrue("pkcol_1 dropped from the parent table", parentTable.getColumns().contains(col));
	}
    
    /**
     * The relationship manager was detaching from its whole table whenever
     * one relationship (not necessarily the listening one) was removed
     * from its pk table.  This test checks for that problem.
     */
    public void testRelManagerDoesntDetachEarly() {
        assertTrue(parentTable.getSQLObjectListeners().contains(rel1.getRelationshipManager()));
        assertTrue(parentTable.getSQLObjectListeners().contains(rel2.getRelationshipManager()));
        assertTrue(childTable2.getSQLObjectListeners().contains(rel2.getRelationshipManager()));
        
        parentTable.getExportedKeysFolder().removeChild(rel1);
        
        assertFalse(parentTable.getSQLObjectListeners().contains(rel1.getRelationshipManager()));
        
        // and finally, what we're testing for:
        assertTrue(parentTable.getSQLObjectListeners().contains(rel2.getRelationshipManager()));
        assertTrue(childTable2.getSQLObjectListeners().contains(rel2.getRelationshipManager()));
    }
    
    public void testAutoGeneratedColumnGoesIntoPK() throws ArchitectException {
        SQLColumn mycol = new SQLColumn(null, "my_column", Types.CHAR, 1000000, 0);
        parentTable.addColumn(0, mycol);
        
        mycol.setPrimaryKeySeq(0);
        assertTrue(mycol.isPrimaryKey());
        assertTrue(rel1.isIdentifying());
        
        // and the point of the test...
        SQLColumn generatedCol = childTable1.getColumnByName("my_column"); 
        System.out.println("Columns of childTable1: "+childTable1.getColumns());
        System.out.println("Column 0 pk value:"+childTable1.getColumn(0).getPrimaryKeySeq());
        assertNotNull(generatedCol);
        assertTrue(childTable1.getColumnIndex(generatedCol) < childTable1.getPkSize());
        assertTrue(generatedCol.isPrimaryKey());
    }
    
    public void testCreateMappingsFiresEvents() throws ArchitectException {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        rel1.addSQLObjectListener(l);
        SQLRelationship.ColumnMapping columnMapping = new SQLRelationship.ColumnMapping();
        columnMapping.setPkColumn(parentTable.getColumn(0));
        columnMapping.setFkColumn(childTable1.getColumn(0));
        rel1.addChild(0,columnMapping);
        assertEquals(1, l.getInsertedCount());
    }
    
    public void testRemoveMappingsFiresEvents() {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        rel1.addSQLObjectListener(l);
        rel1.removeChild(0);
        assertEquals(1, l.getRemovedCount());
    }

    public void testRelationshipManagerRemoveMappingsFiresEvents() throws ArchitectException {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        rel1.addSQLObjectListener(l);
        parentTable.removeColumn(0);
        assertEquals(1, l.getRemovedCount());
    }
    
    /**
     * This test comes from the post in the forums (post 1670) that foreign keys
     * get left behind when an identifying relationship is removed.
     *
     */
    public void testDeletingIdentifyingRelationshipDoesntStrandKeys() throws ArchitectException {
        database = new SQLDatabase();
        
        SQLTable table1 = new SQLTable(database, "table1", null, "TABLE", true);
        SQLColumn table1PK = new SQLColumn(table1, "pkcol_1", Types.INTEGER, 10, 0);
        table1PK.setPrimaryKeySeq(0);
        table1.addColumn(table1PK);
        
        SQLTable table2 = new SQLTable(database, "table2", null, "TABLE", true);
        SQLColumn table2PK = new SQLColumn(table2, "pkcol_2", Types.INTEGER, 10, 0);
        table2PK.setPrimaryKeySeq(0);
        table2.addColumn(table2PK);
        
        SQLTable table3 = new SQLTable(database, "table3", null, "TABLE", true);
        SQLColumn table3PK = new SQLColumn(table3, "pkcol_3", Types.INTEGER, 10, 0);
        table3PK.setPrimaryKeySeq(0);
        table3.addColumn(table3PK);
        
        SQLRelationship relTable3to2 = new SQLRelationship();
        relTable3to2.setIdentifying(true);
        relTable3to2.attachRelationship(table3,table2,true);
        relTable3to2.setName("relTable3to2");
    
        SQLRelationship relTable2to1 = new SQLRelationship();
        relTable2to1.setName("relTable2to1");
        relTable2to1.attachRelationship(table2,table1,true);
        
        assertTrue("The column pkcol_3 was not added to table1 by the relations correctly", 
                table1.getColumnByName("pkcol_3") != null);
        
        relTable3to2.getPkTable().removeExportedKey(relTable3to2);
        
        //This is what we really want to test
        assertNull("The column created by the relations was not " +
                "removed when the relation was removed", table1.getColumnByName("pkcol_3"));
    }

    /**
     * This is a regression test for the problem where a table's only primary
     * key column has been inherited via a relationship.  The primary key name
     * was coming up null in that case.
     */
    public void testNonNullPrimaryKeyNameWhenInheritingOnlyPKColumn() throws Exception {
        database = new SQLDatabase();
        
        SQLTable table1 = new SQLTable(database, "table1", null, "TABLE", true);
        SQLColumn table1PK = new SQLColumn(table1, "pkcol_1", Types.INTEGER, 10, 0);
        table1PK.setPrimaryKeySeq(0);
        table1.addColumn(table1PK);
        
        SQLTable table2 = new SQLTable(database, "table2", null, "TABLE", true);
        SQLRelationship relTable1to2 = new SQLRelationship();
        relTable1to2.setIdentifying(true);
        relTable1to2.setName("one_to_two_fk");
        relTable1to2.attachRelationship(table1, table2, true);

        assertEquals("pkcol_1", table2.getColumn(0).getName());
        assertTrue(table2.getColumn(0).isPrimaryKey());
        assertNotNull(table2.getColumn(0).getPrimaryKeySeq());

        assertNotNull(table2.getPrimaryKeyIndex());
        assertNotNull(table2.getPrimaryKeyName());
    }
    
    /**
     * We discovered this case was untested while examining the Clover test coverage report.
     */
    public void testAutoMappingHijackWhenTargetColumnExists() throws Exception {
        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);
        
        SQLTable childTable = new SQLTable(database, true);
        database.addChild(childTable);
        SQLColumn childCol = new SQLColumn(childTable, "pkcol_1", Types.INTEGER, 10, 0);
        childTable.addColumn(childCol);
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(parentTable, childTable, true);
        
        assertEquals(1, childTable.getColumns().size());
        assertEquals(2, childCol.getReferenceCount());
    }

    /**
     * We discovered this case was untested while examining the Clover test coverage report.
     */
    public void testAutoMappingNoHijackWhenTargetColumnExistsWithWrongType() throws Exception {
        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);
        
        SQLTable childTable = new SQLTable(database, true);
        database.addChild(childTable);
        SQLColumn childCol = new SQLColumn(childTable, "pkcol_1", Types.VARCHAR, 10, 0);
        childTable.addColumn(childCol);
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(parentTable, childTable, true);
        
        assertEquals(2, childTable.getColumns().size());
        assertEquals(1, childCol.getReferenceCount());
    }
    
    /**
     * Self-referencing auto-mapping regressed at some point and we didn't notice.
     * This test covers one of the many bugs repored in forum posting 1772.
     */
    public void testSelfReferencingAutoMapping() throws Exception {
        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);

        int oldColCount = parentTable.getColumns().size();
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(parentTable, parentTable, true);
        
        assertEquals(1, r.getMappings().size());
        SQLRelationship.ColumnMapping mapping = r.getMappings().get(0);
        assertNotSame(mapping.getFkColumn(), mapping.getPkColumn());
        assertEquals(oldColCount + 1, parentTable.getColumns().size());
    }
    
    /**
     * The original fix for self-referencing tables only works for an existing
     * PK when you attach a new relationship. This test ensures the self-reference
     * also works when a column is added to the PK while the self-referencing
     * relationship already exists.
     */
    public void testSelfReferencingAutoMappingOnPKModification() throws Exception {
        int oldColCount = parentTable.getColumns().size();
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(parentTable, parentTable, true);
        assertEquals(0, r.getMappings().size());

        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);
        
        assertEquals(1, r.getMappings().size());
        SQLRelationship.ColumnMapping mapping = r.getMappings().get(0);
        assertNotSame(mapping.getFkColumn(), mapping.getPkColumn());
        assertEquals(oldColCount + 1, parentTable.getColumns().size());
        assertTrue(parentTable.getColumns().contains(mapping.getPkColumn()));
        assertTrue(parentTable.getColumns().contains(mapping.getFkColumn()));
    }

    /**
     * Self-references set themselves to non-idendifying because that makes sense,
     * and prevents infinite recursion when you add to the primary key. :) But it's
     * easy enough to manually set them back to identifying later on.. so this test
     * makes sure you can still safely modify a PK when it has an identifying self-referencing
     * relationship.
     */
    public void testSelfReferencingInfiniteRecursionOnPKModification() throws Exception {
        int oldColCount = parentTable.getColumns().size();
        
        SQLRelationship r = new SQLRelationship();
        r.attachRelationship(parentTable, parentTable, true);
        assertEquals(0, r.getMappings().size());

        r.setIdentifying(true);
        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);
        
        assertEquals(1, r.getMappings().size());
        SQLRelationship.ColumnMapping mapping = r.getMappings().get(0);
        assertNotSame(mapping.getFkColumn(), mapping.getPkColumn());
        assertEquals(oldColCount + 1, parentTable.getColumns().size());
        assertTrue(parentTable.getColumns().contains(mapping.getPkColumn()));
        assertTrue(parentTable.getColumns().contains(mapping.getFkColumn()));
    }

    /**
     * This tests for uniqueness in the generated column names. It is
     * simple and does not cover all cases of attachRelationship because
     * each case is merely a slight deviation with the same logic. 
     */
    public void testGenerateUniqueColumnNames() throws Exception {
        SQLTable table = new SQLTable(database, true);
        database.addChild(table);
        SQLColumn c1 = new SQLColumn(table, "Col", Types.INTEGER, 10, 0);
        c1.setPrimaryKeySeq(0);
        table.addColumn(c1);
        
        SQLColumn c2 = new SQLColumn(table, "Parent_Col", Types.INTEGER, 10, 0);
        table.addColumn(c2);
        
        SQLRelationship r1 = new SQLRelationship();
        r1.attachRelationship(table, table, true);
        
        SQLRelationship r2 = new SQLRelationship();
        r2.attachRelationship(table, table, true);
        
        List<String> colNames = new ArrayList<String>();
        for (SQLColumn c : table.getColumns()) {
            String colName = c.getName();
            assertFalse("Failed to generate unique column name, duplicated name : " + colName, colNames.contains(colName));
            colNames.add(colName);
        }
    }
    
    /**
     * This checks a new behavior of attachRelationship. It is a case where
     * attaching a relationship to tables that were already related and a 
     * match was found for hijacking should NOT hijack but create a new 
     * column instead.
     */
    public void testAutoMappingNoHijackWhenRelationshipAlreadyExists() throws Exception {
        SQLColumn parentCol = parentTable.getColumnByName("pkcol_1");
        parentCol.setPrimaryKeySeq(0);
        
        SQLTable childTable = new SQLTable(database, true);
        database.addChild(childTable);
        
        SQLRelationship r1 = new SQLRelationship();
        r1.attachRelationship(parentTable, childTable, true);

        // assumes that attaching the relationship caused creation 
        // of a new column that is materially equal
        assertEquals(1, childTable.getColumns().size());
        assertEquals(1, childTable.getColumn(0).getReferenceCount());
        
        SQLRelationship r2 = new SQLRelationship();
        r2.attachRelationship(parentTable, childTable, true);
        
        assertEquals("A new column should have been created.", 2, childTable.getColumns().size());
        assertEquals("Incorrect column mapping.", 1, childTable.getColumn(0).getReferenceCount());
        assertEquals("Incorrect column mapping.", 1, childTable.getColumn(1).getReferenceCount());
    }

    /**
     * Tests determineIdentifyingStatus on rel1 and rel2. rel1 is expected to be
     * identifying, whereas rel2 is expected to be non-identifying.
     * 
     * @throws Exception
     */
    public void testDetermineIdentifyingStatus() throws Exception {
        SQLColumn childPKCol1 = childTable1.getColumnByName("child_pkcol_1");
        SQLColumn childPKCol2 = childTable1.getColumnByName("child_pkcol_2");
        SQLColumn child2PKCol1 = childTable2.getColumnByName("child2_pkcol_1");
        SQLColumn child2PKCol2 = childTable2.getColumnByName("child2_pkcol_2");
        
        childPKCol1.setPrimaryKeySeq(0);
        childPKCol2.setPrimaryKeySeq(1);
        child2PKCol1.setPrimaryKeySeq(0);
        child2PKCol2.setPrimaryKeySeq(1);
        
        assertTrue("Expected rel1 to be identifying", rel1.determineIdentifyingStatus());
        assertFalse("Expected rel2 to be non-identifying", rel2.determineIdentifyingStatus());
    }
    
    /**
     * Regression test: cascading additional PK columns was only working from the
     * parent to the direct children. Grandchild tables were not picking up the new
     * column, which also meant the mapping from the child table to the grandchildren
     * did not cover the entire PK of the child. 
     */
    public void testMultiStepCascade() throws Exception {
        
        // removing rel2 because it makes extra noise in the logs.
        // this is not expected to affect test results.
        parentTable.removeExportedKey(rel2);
        
        parentTable.getColumnByName("pkcol_1").setPrimaryKeySeq(0);
        parentTable.getColumnByName("pkcol_2").setPrimaryKeySeq(1);

        childTable1.getColumnByName("child_pkcol_1").setPrimaryKeySeq(0);
        childTable1.getColumnByName("child_pkcol_2").setPrimaryKeySeq(1);

        SQLTable grandchildTable = new SQLTable(database, "grandchild_1", null, "TABLE", true);
        grandchildTable.addColumn(new SQLColumn(grandchildTable, "grandchild_pkcol_1", Types.INTEGER, 10, 0));
        grandchildTable.addColumn(new SQLColumn(grandchildTable, "grandchild_pkcol_2", Types.INTEGER, 10, 0));
        grandchildTable.addColumn(new SQLColumn(grandchildTable, "grandchild_attribute", Types.INTEGER, 10, 0));
        database.addChild(grandchildTable);

        SQLRelationship rel3 = new SQLRelationship();
        rel3.setIdentifying(true);
        rel3.setName("rel3");
        rel3.attachRelationship(childTable1, grandchildTable, true);
        
        // This is a direct cascade, which was not broken. These assertions are just to make sure.
        assertNotNull(grandchildTable.columnsFolder.getChildByName("child_pkcol_1"));
        assertNotNull(grandchildTable.columnsFolder.getChildByName("child_pkcol_2"));
        
        int oldParentPkSize = parentTable.getPkSize();
        int oldChildPkSize = childTable1.getPkSize();
        int oldGrandchildPkSize = grandchildTable.getPkSize();
        
        SQLColumn newParentPKCol = new SQLColumn(null, "new_parent_pk", Types.INTEGER, 10, 0);
        
        parentTable.addColumn(newParentPKCol);
        newParentPKCol.setPrimaryKeySeq(3);
        
        assertEquals(oldParentPkSize + 1, parentTable.getPkSize());
        assertEquals(oldChildPkSize + 1, childTable1.getPkSize());
        
        // and finally, the point of the test...
        assertEquals(oldGrandchildPkSize + 1, grandchildTable.getPkSize());
    }
    
    /**
     * Regression test: ensures that column rename operations cascade beyond the
     * direct child of the parent whose column was renamed.
     * <p>
     * This test uses a slightly different setup from {@link #testMultiStepCascade()}
     * because the renames are only supposed to cascade through column mappings
     * where the FK column name was the same as the PK column name. The difference is
     * that this test uses childTable2 because its imported columns are named the
     * same as the parent PK.
     */
    public void testCascadeColumnRename() throws Exception {
        // removing rel1 because it makes extra noise in the logs.
        // this is not expected to affect test results.
        parentTable.removeExportedKey(rel1);
        
        rel2.setIdentifying(true);
        
        parentTable.getColumnByName("pkcol_1").setPrimaryKeySeq(0);

        SQLTable grandchildTable = new SQLTable(database, "grandchild_1", null, "TABLE", true);
        database.addChild(grandchildTable);

        SQLRelationship rel3 = new SQLRelationship();
        rel3.setIdentifying(true);
        rel3.setName("rel3");
        rel3.attachRelationship(childTable2, grandchildTable, true);
        
        SQLColumn parentPk = parentTable.getColumnByName("pkcol_1");
        SQLColumn parentPkInChild = childTable2.getColumnByName("pkcol_1");
        SQLColumn parentPkInGrandchild = grandchildTable.getColumnByName("pkcol_1");
        
        assertNotNull(parentPk);
        assertNotNull(parentPkInChild);
        assertNotNull(parentPkInGrandchild);
        
        parentPk.setName("new_name");
        
        // These three tests are the point
        assertEquals("new_name", parentPk.getName());
        assertEquals("new_name", parentPkInChild.getName());
        assertEquals("new_name", parentPkInGrandchild.getName());
    }

    /**
     * This is a bit of a white-box test for an actual problem that we ran into.
     * SQLTable.normalizePrimaryKey() was getting columns to fire primaryKeySeq
     * change events inside its loop, but sometimes (especially in the case of a
     * self-referencing relationship) those events were causing the column list
     * to change.. which causes a concurrent modification exception!
     * 
     * @throws Exception
     */
    public void testComodificationInNormalize() throws Exception {
        parentTable.removeExportedKey(rel1);
        parentTable.removeExportedKey(rel2);
        
        SQLRelationship selfRef = new SQLRelationship();
        selfRef.setName("parent_table_self_ref");
        selfRef.attachRelationship(parentTable, parentTable, true);
        
        parentTable.getColumnByName("pkcol_1").setPrimaryKeySeq(0);
        parentTable.getColumnByName("pkcol_2").setPrimaryKeySeq(1);
        parentTable.getColumnByName("attribute_1").setPrimaryKeySeq(2);

        // This was causing ConcurrentModificationException
        parentTable.getColumnByName("pkcol_2").setPrimaryKeySeq(null);
    }
    
    /**
     * Actual bug: On a table that has a self-reference, If you remove a column
     * from the PK, and there were more PK columns after it, those successive PK
     * columns will end up removed from the PK.
     */
    public void testRemovePkColWithSelfRef() throws Exception {
        parentTable.removeExportedKey(rel1);
        parentTable.removeExportedKey(rel2);

        parentTable.getColumnByName("pkcol_1").setPrimaryKeySeq(0);
        parentTable.getColumnByName("pkcol_2").setPrimaryKeySeq(1);
        parentTable.getColumnByName("attribute_1").setPrimaryKeySeq(2);
        
        SQLRelationship selfRef = new SQLRelationship();
        selfRef.setName("parent_table_self_ref");
        selfRef.attachRelationship(parentTable, parentTable, true);

        assertEquals(Integer.valueOf(0), parentTable.getColumnByName("pkcol_1").getPrimaryKeySeq());
        assertEquals(Integer.valueOf(1), parentTable.getColumnByName("pkcol_2").getPrimaryKeySeq());
        assertEquals(Integer.valueOf(2), parentTable.getColumnByName("attribute_1").getPrimaryKeySeq());
        
        parentTable.getColumnByName("pkcol_2").setPrimaryKeySeq(null);
 
        // The last of these three assertions is the one this test is looking for
        assertEquals(Integer.valueOf(0), parentTable.getColumnByName("pkcol_1").getPrimaryKeySeq());
        assertEquals(null,               parentTable.getColumnByName("pkcol_2").getPrimaryKeySeq());
        assertEquals(Integer.valueOf(1), parentTable.getColumnByName("attribute_1").getPrimaryKeySeq());
    }
}
