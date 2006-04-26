package ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectFrame;

public class JDBCClassLoaderTest extends TestCase {

	private static final String PGSQL_DRIVER = "org.postgresql.Driver";
	
	/*
	 * Test method for 'ca.sqlpower.architect.JDBCClassLoader.JDBCClassLoader(ArchitectSession)'
	 */
	public void testJDBCClassLoader() throws Exception {
				
		// Side effect is to load my prefs; needed to avoid having to manually add jar to session.
		ArchitectFrame.getMainInstance();
		
		ArchitectSession sess = ArchitectSession.getInstance();
		
		JDBCClassLoader cl = sess.getJDBCClassLoader();
		
		System.out.println("If this throws a ClassNotFoundException, update your Architect Preferences to include the PostGreSQL driver jar");
		Class clazz = cl.findClass(PGSQL_DRIVER);
		
		// If we get here, this test has passed.
		assertNotNull("Loaded JDBC class %s%n", clazz);
	}
}
