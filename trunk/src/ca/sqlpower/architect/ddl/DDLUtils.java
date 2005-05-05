package ca.sqlpower.architect.ddl;

import org.apache.log4j.*;
import ca.sqlpower.architect.*;
import java.sql.*;
import java.util.*;

/**
 * DDLUtils is a collection of utilities related to creating and
 * executing Data Definition Language (DDL) statements.
 */
public class DDLUtils {

	private static final Logger logger = Logger.getLogger(DDLUtils.class);

	/**
	 * DDLUtils is a container for static methods.  You can't make an instance of it.
	 */
	private DDLUtils() {
	}

	/**
	 * Searches for objects in the database pointed to by con that
	 * would conflict with the execution of the given DDL statement.
	 *
	 * @return a list of object names that need to be removed before
	 * ddlStmt will succeed.
	 */
	public static List findConflicting(Connection con, DDLStatement ddlStmt) throws SQLException {
		List conflicts = new ArrayList();

		SQLObject so = ddlStmt.getObject();
		Class clazz = so.getClass();
		if (clazz.equals(SQLTable.class)) {
			SQLTable t = (SQLTable) so;
			DatabaseMetaData dbmd = con.getMetaData();
			if (logger.isDebugEnabled()) {
				logger.debug("Finding conflicts for TABLE '"+t.getCatalogName()+"'.'"+t.getSchemaName()+"'.'"+t.getName()+"'");
			}
			ResultSet rs = dbmd.getTables(t.getCatalogName(), t.getSchemaName(), t.getName(), null);
			while (rs.next()) {
				StringBuffer qualName = new StringBuffer();
				if (rs.getString("TABLE_CAT") != null) {
					qualName.append(rs.getString("TABLE_CAT"));
				}
				if (rs.getString("TABLE_SCHEM") != null) {
					if (qualName.length() > 0) qualName.append(".");
					qualName.append(rs.getString("TABLE_SCHEM"));
				}
				if (qualName.length() > 0) qualName.append(".");
				qualName.append(rs.getString("TABLE_NAME"));
				qualName.insert(0, rs.getString("TABLE_TYPE"));
				conflicts.add(qualName);
			}
		} else if (clazz.equals(SQLRelationship.class)) {
			logger.error("Relationship conflicts are not supported yet!");
		} else {
			throw new IllegalArgumentException("Unknown subclass of SQLObject: "+clazz.getName());
		}

		return conflicts;
	}

	public static void dropConflicting(Connection con, List objectNames) throws SQLException {
		Iterator it = objectNames.iterator();
		Statement stmt = null;
		try {
			while (it.hasNext()) {
				String objectName = (String) it.next();
				stmt = con.createStatement();
				stmt.executeUpdate("DROP "+objectName);
			}
		} finally {
			if (stmt != null) stmt.close();
		}
	}
}
