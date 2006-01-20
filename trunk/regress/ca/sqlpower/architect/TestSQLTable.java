package regress.ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanUtils;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

public class TestSQLTable extends SQLTestCase {
	
		public TestSQLTable(String name) throws Exception
		{
			super(name);
		}
		
		protected void setUp() throws Exception {
			super.setUp();
			SQLDatabase mydb = new SQLDatabase(db.getDataSource());
			Connection con = mydb.getConnection();
			
			
			/*
			 * Setting up a clean db for each of the tests
			 */
			Statement stmt = con.createStatement();
			try {
				stmt.executeUpdate("DROP TABLE REGRESSION_TEST1");
				stmt.executeUpdate("DROP TABLE REGRESSION_TEST2");
			}
			catch (SQLException sqle ){
				System.out.println("+++ TestSQLDatabase exception should be for dropping a non-existant table");
				sqle.printStackTrace();
			}
			
			stmt.executeUpdate("CREATE TABLE REGRESSION_TEST1 (t1_c1 numeric(10), t1_c2 numeric(5))");
			stmt.executeUpdate("CREATE TABLE REGRESSION_TEST2 (t2_c1 char(10))");
			
			stmt.close();
			mydb.disconnect();
			
		}
		
		public void testGetDerivedInstance() throws Exception
		{
			SQLTable derivedTable;
			SQLTable table1;
			assertNotNull(table1 = db.getTableByName("REGRESSION_TEST1"));			
			derivedTable = SQLTable.getDerivedInstance(table1,table1.getParentDatabase());
		
			TreeMap derivedPropertyMap = new TreeMap( BeanUtils.describe(derivedTable));
			TreeMap table1PropertyMap = new TreeMap(BeanUtils.describe(table1));
			
			derivedPropertyMap.remove("parent");
			derivedPropertyMap.remove("schemaName");
			derivedPropertyMap.remove("schema");
			derivedPropertyMap.remove("shortDisplayName");
			table1PropertyMap.remove("parent");
			table1PropertyMap.remove("schemaName");
			table1PropertyMap.remove("schema");
			table1PropertyMap.remove("shortDisplayName");	
			assertEquals("Derived table not properly copied", derivedPropertyMap.toString(),table1PropertyMap.toString());
			
		}
		
		public void testInherit() throws ArchitectException
		{
			SQLTable table1;
			SQLTable table2;
			table1 = db.getTableByName("REGRESSION_TEST1");
			table2 = db.getTableByName("REGRESSION_TEST2");
		
			table2.inherit(table1);
			assertEquals("The wrong 1st column was inherited",table2.getColumn(0).toString(),table1.getColumn(0).toString());
			assertEquals("The wrong 2nd column was inherited",table2.getColumn(1).toString(),table1.getColumn(1).toString());
			assertEquals("The wrong number of columns were inherited",table2.getColumns().size(), 3);
			try {
				table2.inherit(table2);
			}
			catch (ArchitectException ae)
			{
				if ("Cannot inherit from self".equals(ae.getMessage()))
				{
					System.out.println("Expected Behaviour is to not be able to inherit from self");
					
				}
				else
				{
					throw ae;
				}
			}
		}
		
		public void testGetColumnByName() throws ArchitectException
		{
			SQLTable table1;
			SQLColumn col1;
			SQLColumn col2;
			table1 = db.getTableByName("REGRESSION_TEST1");
			col2 = table1.getColumnByName("t1_c2");
			assertNotNull(col2);
			assertEquals("The wrong colomn us returned",col2, table1.getColumn(1));
			
			col1= table1.getColumnByName("t1_c1");
			assertNotNull(col1);
			assertEquals("The wrong colomn us returned",col1, table1.getColumn(0));
			
			assertNull(col1 = table1.getColumnByName("This_is_a_non_existant_column"));
			assertNull("Invalid column name", col1 = table1.getColumnByName("$#  #$%#%"));
		}
		
		public void testAddColumn() throws ArchitectException
		{
			SQLTable table1;
			SQLColumn col1;
			SQLColumn col2;
			SQLColumn newColumn = db.getTableByName("REGRESSION_TEST2").getColumn(0);
			String newName = "This is a new name changed for testAddColumn";
			table1 = db.getTableByName("REGRESSION_TEST1");
			col1 = table1.getColumnByName("t1_c2");
			table1.addColumn(2,col1);
			col2 = table1.getColumn(2);
			
			if (col2 != null)
				col2.setName(newName);
			assertFalse("Multiple columns where modified",newName.equals(table1.getColumn(1).getName()));
		
			table1.addColumn(1,newColumn);
			assertEquals("Column inserted into wrong position or did not insert",newColumn,table1.getColumn(1));
			
			
		}
		
		public void testNormalizePrimaryKey() throws ArchitectException
		{
			SQLTable table1;
			SQLColumn col2;
			SQLColumn col1 = db.getTableByName("REGRESSION_TEST2").getColumn(0);
			col1.setPrimaryKeySeq(new Integer(5));
			table1 = db.getTableByName("REGRESSION_TEST1");
			col2=(SQLColumn) col1.clone();
			col2.setPrimaryKeySeq(new Integer(16));
			table1.addColumn(2,col1);
			table1.addColumn(3,col2);
			table1.normalizePrimaryKey();
			assertEquals("Wrong number of primary keys",table1.getPkSize(),0);
			
			col1.setPrimaryKeySeq(new Integer(5));
			col2.setPrimaryKeySeq(new Integer(16));
			
			assertEquals("Invalid key order",table1.getColumn(0),col1);
			assertEquals("2nd key out of order", table1.getColumn(1),col2);
			assertEquals("Too many or too few primary keys",table1.getPkSize(),2);
			
		}

}
