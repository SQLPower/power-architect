package ca.sqlpower.architect;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ca.sqlpower.sql.DBConnectionSpec;

public class SQLDatabase extends SQLObject {

	protected DBConnectionSpec connectionSpec;
	protected Connection connection;
	protected boolean populated = false;

	public SQLDatabase(DBConnectionSpec connectionSpec) {
		this.connectionSpec = connectionSpec;
		children = new ArrayList();
	}

	public synchronized boolean isConnected() {
		return connection != null;
	}

	public synchronized void connect() throws ArchitectException {
		if (connection != null) return;
		try {
			Class.forName(connectionSpec.getDriverClass());
			System.out.println("Driver Class "+connectionSpec.getDriverClass()+" loaded without exception");
			connection = DriverManager.getConnection(connectionSpec.getUrl(),
													 connectionSpec.getUser(),
													 connectionSpec.getPass());
		} catch (ClassNotFoundException e) {
			System.out.println("Driver Class not found");
			throw new ArchitectException("dbconnect.noDriver", e);
		} catch (SQLException e) {
			throw new ArchitectException("dbconnect.connectionFailed", e);
		}
	}

	protected synchronized void populateTables() throws ArchitectException {
		if (populated) return;
		int oldSize = children.size();
		try {
			SQLTable.addTablesToDatabase(this);
		} catch (SQLException e) {
			throw new ArchitectException("database.populate.fail", e);
		} finally {
			populated = true;
			int newSize = children.size();
			if (newSize > oldSize) {
				int[] changedIndices = new int[newSize - oldSize];
				for (int i = 0, n = newSize - oldSize; i < n; i++) {
					changedIndices[i] = oldSize + i;
				}
				fireDbChildrenInserted(changedIndices, children.subList(oldSize, newSize));			
			}
		}
	}

	/**
	 * Searches this database's list of tables for one with the given
	 * name, ignoring case because SQL isn't (usually) case sensitive.
	 *
	 * @return the first SQLTable with the given name, or null if no
	 * such table exists.
	 */
	public SQLTable getTableByName(String tableName) throws ArchitectException {
		if (!populated) populate();
		SQLTable table = null;
		Iterator tables = children.iterator();
		while (tables.hasNext()) {
			table = (SQLTable) tables.next();
			if (table.getTableName().equalsIgnoreCase(tableName)) {
				return table;
			}
		}
		return null;
	}

	// ---------------------- SQLObject support ------------------------

	/**
	 * SQLDatabase objects don't have parents.
	 *
	 * @return <code>null</code>
	 */
	public SQLObject getParent() {
		return null;
	}

	public String getShortDisplayName() {
		return connectionSpec.getDisplayName();
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public void populate() throws ArchitectException {
		populateTables();
	}

	public boolean isPopulated() {
		return populated;
	}

	// ----------------- accessors and mutators -------------------
	
	/**
	 * Gets the value of tables
	 *
	 * @return the value of tables
	 */
	public List getTables() throws ArchitectException {
		return getChildren();
	}

// 	public void setTables(List argTables) {
// 		setChildren(argTables);
// 	}

	/**
	 * Gets the value of connectionSpec
	 *
	 * @return the value of connectionSpec
	 */
	public DBConnectionSpec getConnectionSpec()  {
		return this.connectionSpec;
	}

	/**
	 * Sets the value of connectionSpec
	 *
	 * @param argConnectionSpec Value to assign to this.connectionSpec
	 */
	public void setConnectionSpec(DBConnectionSpec argConnectionSpec) {
		// tear down old connection stuff
		List old = children;
		int[] oldIndices = new int[old.size()];
		for (int i = 0, n = old.size(); i < n; i++) {
			oldIndices[i] = i;
		}
		children = new ArrayList();
		fireDbChildrenRemoved(oldIndices, old);
		connection = null;
		populated = false;

		connectionSpec = argConnectionSpec;
		fireDbObjectChanged("connectionSpec");
	}

	/**
	 * Returns a reference to the JDBC connection to this database.
	 * Calls connect() if necessary.
	 *
	 * @return the value of con
	 */
	public Connection getConnection() throws ArchitectException {
		if (connection == null) connect();
		return this.connection;
	}

	public String toString() {
		return connectionSpec.getDisplayName();
	}
}
