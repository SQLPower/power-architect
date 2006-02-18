package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectDataSource;
import junit.framework.TestCase;

public class TestCompareDMProcess extends TestCase {

	
	ArchitectDataSource tables;
	ArchitectDataSource catalogs;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		tables= new ArchitectDataSource();
		tables.setDisplayName("Schemaless Database");
		tables.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		tables.setUser("fake");
		tables.setPass("fake");
		//this creates a mock jdbc database with only catalogs
		tables.setUrl("jdbc:mock:tables=cow,moo,chicken,cluck,duck,quack");
		
		catalogs= new ArchitectDataSource();
		catalogs.setDisplayName("Schemaless Database");
		catalogs.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		catalogs.setUser("fake");
		catalogs.setPass("fake");
		//this creates a mock jdbc database with only catalogs
		catalogs.setUrl("jdbc:mock:tables=cow,moo,chicken,cluck,duck,quack");
		
	}
	
	public void testTwoTableComparison(){
		
		
		
		
	}

}
