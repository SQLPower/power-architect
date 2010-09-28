package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;

public class TestDBTree extends TestCase {

	DBTree dbTree;
	ArchitectDataSource ds;
	ArchitectDataSource db2ds;
	protected void setUp() throws Exception {
		ds = new ArchitectDataSource();
		db2ds = new ArchitectDataSource();
		SQLDatabase ppdb = new SQLDatabase(ds);
		
		List dbList = new ArrayList();
		dbList.add(0,ppdb);
		dbList.add(1,new SQLDatabase(db2ds));
		dbTree = new DBTree(dbList);
	}
	
	public void testdbcsAlreadyExists() throws ArchitectException {
		ArchitectDataSource ds2 = new ArchitectDataSource();
		assertTrue("ds2 must .equals ds for this test to work", ds.equals(ds2));
		assertFalse("dbcsAlreadyExists Should not find ds2", dbTree.dbcsAlreadyExists(ds2));
		assertTrue("db2ds should be in the list",dbTree.dbcsAlreadyExists(db2ds));
	}

}
