package regress;

import junit.framework.*;
import java.sql.*;
import ca.sqlpower.sql.*;
import java.util.*;

import ca.sqlpower.architect.*;

public class TestSQLColumn extends SQLTestCase {

	SQLTable table;

	public TestSQLColumn(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		db.populate();
		table = db.getTableByName("batting");
	}

	public void testPopulateTable() throws ArchitectException {
		assertFalse("Table should not have been populated already", table.isPopulated());
		table.populate();
		assertTrue("Table should been populated", table.isPopulated());

		// spot-check that expected columns exist
		SQLColumn yearCol, teamCol, strikeoutsCol;
		assertNotNull("year column not found", yearCol = table.getColumnByName("year"));
		assertNotNull("team column not found", teamCol = table.getColumnByName("team"));
		assertNotNull("league column not found", table.getColumnByName("league"));
		assertNotNull("strikeouts column not found", strikeoutsCol = table.getColumnByName("strikeouts"));
		
		// check that all columns are owned by the correct table
		assertEquals("column doesn't belong to correct parent!", table, yearCol.getParent());
		assertEquals("column doesn't belong to correct parent!", table, teamCol.getParent());
		assertEquals("column doesn't belong to correct parent!", table, strikeoutsCol.getParent());

		// check for PK vs non PK attributes
		assertTrue("year should have been flagged as PK", yearCol.isPrimaryKey());
		assertEquals("year nullability incorrect", yearCol.getNullable(), DatabaseMetaData.columnNoNulls);
		assertFalse("year isDefinitelyNullable incorrect", yearCol.isDefinitelyNullable());

		assertFalse("strikeouts should have been flagged as PK", strikeoutsCol.isPrimaryKey());
		assertEquals("strikeouts nullability incorrect", strikeoutsCol.getNullable(), DatabaseMetaData.columnNullable);
		assertTrue("strikeouts isDefinitelyNullable incorrect", strikeoutsCol.isDefinitelyNullable());

		// check column name comparator
		Comparator nameComp = new SQLColumn.ColumnNameComparator();
		assertTrue(nameComp.compare(yearCol, strikeoutsCol) > 0);
		assertTrue(nameComp.compare(teamCol, strikeoutsCol) > 0);
		assertTrue(nameComp.compare(strikeoutsCol, yearCol) < 0);
		assertTrue(nameComp.compare(strikeoutsCol, teamCol) < 0);
		assertTrue(nameComp.compare(yearCol, yearCol) == 0);
		teamCol.setColumnName("year");
		assertTrue(nameComp.compare(yearCol, teamCol) == 0);
	}
}
