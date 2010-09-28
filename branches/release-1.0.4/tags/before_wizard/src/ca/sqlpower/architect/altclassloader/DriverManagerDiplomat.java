package ca.sqlpower.architect.altclassloader;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import ca.sqlpower.architect.JDBCClassLoader;

/**
 * This class works around the antique security notions of the
 * java.sql.DriverManager class.  In order to get a connection from
 * the DriverManager, the ClassLoader of the calling object must be
 * able to come up with the same Class object as is registered in the
 * DriverManager's list.  Therefore, for this "diplomatic manoeuver"
 * to be successful, this class must ONLY be loadable by the
 * JDBCClassLoader.
 */
public class DriverManagerDiplomat {
	public DriverManagerDiplomat() {
		if ( ! (this.getClass().getClassLoader() instanceof JDBCClassLoader) ) {
			throw new IllegalStateException("This DriverManagerDiplomat's class loader is not a JDBCClassLoader.");
		}
	}

	public Connection getConnection(String url, String user, String password) 
		throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
}
