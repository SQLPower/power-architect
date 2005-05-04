package regress;

import junit.framework.*;
import ca.sqlpower.sql.*;

import ca.sqlpower.architect.*;

/**
 * SQLTestCase is an abstract base class for test cases that require a
 * database connection.
 */
public abstract class SQLTestCase extends TestCase {

	/**
	 * This is the SQLDatabase object.  It will be set up according to
	 * some system properties in the <code>setup()</code> method.
	 *
	 * @see #setup()
	 */
	SQLDatabase db;

	public SQLTestCase(String name) {
		super(name);
	}
	
	/**
	 * Sets up the instance variable <code>db</code>. Uses the system
	 * properties <tt>architect.databaseListFile</tt> and
	 * <tt>architect.databaseListFile.nameToUse</tt> for the location
	 * of the databases.xml file and the database id within that file
	 * to use.
	 */
	protected void setUp() throws Exception {
		String dbXmlFileName = System.getProperty("architect.databaseListFile");
		String dbNameToUse = System.getProperty("architect.databaseListFile.nameToUse");

		DBCSSource source = new XMLFileDBCSSource(dbXmlFileName);
		DBConnectionSpec spec = DBConnectionSpec.searchListForName(source.getDBCSList(),
																   dbNameToUse);
		db = new SQLDatabase(spec);
	}
}
