package ca.sqlpower.architect;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class SQLDatabase extends SQLObject implements java.io.Serializable, PropertyChangeListener {
	private static Logger logger = Logger.getLogger(SQLDatabase.class);

	private static SQLDatabase playPenInstance;

	/**
	 * Caches connections across serialization attempts.  See {@link
	 * #connect()}.
	 */
	private static transient HashMap dbConnections = new HashMap();

	protected DBConnectionSpec connectionSpec;
	protected transient Connection connection;
	protected boolean populated = false;

	public SQLDatabase(DBConnectionSpec connectionSpec) {
		setConnectionSpec(connectionSpec);
		children = new ArrayList();
	}
	
	/**
	 * Empty constructor for static factory methods.
	 */
	private SQLDatabase() {
	}

	public static synchronized SQLDatabase getPlayPenInstance() {
		if (playPenInstance == null) {
			playPenInstance = new SQLDatabase();
			playPenInstance.populated = true;
		}
		return playPenInstance;
	}

	public synchronized boolean isConnected() {
		return connection != null;
	}

	public synchronized void connect() throws ArchitectException {
		if (connection != null) return;
		connection = (Connection) dbConnections.get(connectionSpec);
		if (connection != null) return;
		try {
			Class.forName(connectionSpec.getDriverClass());
			logger.info("Driver Class "+connectionSpec.getDriverClass()+" loaded without exception");
			connection = DriverManager.getConnection(connectionSpec.getUrl(),
													 connectionSpec.getUser(),
													 connectionSpec.getPass());
			dbConnections.put(connectionSpec, connection);
		} catch (ClassNotFoundException e) {
			logger.warn("Driver Class not found");
			throw new ArchitectException("dbconnect.noDriver", e);
		} catch (SQLException e) {
			throw new ArchitectException("dbconnect.connectionFailed", e);
		}
	}

	/**
	 * This hands off all the real work to {@link SQLTable#addTablesToDatabase},
	 * which will add either SQLCatalog or SQLSchema or SQLTable objects 
	 * to this table's children list.
	 */
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

	public SQLCatalog getCatalogByName(String catalogName) throws ArchitectException {
		if (!populated) populate();
		if (children == null || children.size() == 0) {
			return null;
		}
		if (! (children.get(0) instanceof SQLCatalog) ) {
			// this database doesn't contain catalogs!
			return null;
		}
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLCatalog child = (SQLCatalog) childit.next();
			if (child.getName().equals(catalogName)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Searches for the named schema as a direct child of this
	 * database, or as a child of any catalog of this database.
	 *
	 * <p>Note: there may be more than one schema with the given name,
	 * if your RDBMS supports catalogs.  In that case, use {@link
	 * SQLCatalog#getSchemaByName} or write another version of this
	 * method that return an array of SQLSchema.
	 *
	 * @return the first SQLSchema whose name matches the given schema
	 * name.
	 */
	public SQLSchema getSchemaByName(String schemaName) throws ArchitectException {
		if (!populated) populate();
		if (children == null || children.size() == 0) {
			return null;
		}
		if (! (children.get(0) instanceof SQLSchema || children.get(0) instanceof SQLCatalog) ) {
			// this database doesn't contain schemas or catalogs!
			return null;
		}
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLObject child = (SQLObject) childit.next();
			if (child instanceof SQLCatalog) {
				// children are tables or schemas
				SQLSchema schema = ((SQLCatalog) child).getSchemaByName(schemaName);
				if (schema != null) {
					return schema;
				}
			} else if (child instanceof SQLSchema) {
				if (child.getName().equals(schemaName)) {
					return (SQLSchema) child;
				}
			} else {
				throw new IllegalStateException("Database contains a mix of schemas or catalogs with other objects");
			}
		}
		return null;
	}

	/**
	 * Searches this database's list of tables for one with the given
	 * name, ignoring case because SQL isn't (usually) case sensitive.
	 *
	 * @param catalogName The name of the catalog to search, or null
	 * if you want to search all catalogs.
	 * @param schemaName The name of the schema to search (in this
	 * database or in the given catalog) or null to search all
	 * schemas.
	 * @param tableName The name of the table to look for (null is not
	 * allowed).
	 * @return the first SQLTable with the given name, or null if no
	 * such table exists.
	 */
	public SQLTable getTableByName(String tableName) throws ArchitectException {
		return getTableByName(null, null, tableName);
	}

	public SQLTable getTableByName(String catalogName, String schemaName, String tableName)
		throws ArchitectException {

		if (!populated) populate();

		if (tableName == null) {
			throw new NullPointerException("Table Name must be specified");
		}
		
		// we will recursively search a target (database, catalog, or schema)
		SQLObject target = this;
		
		if (catalogName != null) {
			target = getCatalogByName(catalogName);
		}

		// no such catalog?
		if (target == null) {
			return null;
		}

		if (schemaName != null) {
			if (target instanceof SQLDatabase) {
				target = ((SQLDatabase) target).getSchemaByName(schemaName);
			} else if (target instanceof SQLCatalog) {
				target = ((SQLCatalog) target).getSchemaByName(schemaName);
			} else {
				throw new IllegalStateException("Oops, somebody forgot to update this!");
			}
		}

		// no such schema or catalog.schema?
		if (target == null) {
			return null;
		}

		Iterator childit = target.children.iterator();
		while (childit.hasNext()) {
			SQLObject child = (SQLObject) childit.next();
			if (child instanceof SQLTable) {
				SQLTable table = (SQLTable) child;
				if (table.getTableName().equalsIgnoreCase(tableName)) {
					return table;
				}
			} else if (child instanceof SQLCatalog) {
				SQLTable table = ((SQLCatalog) child).getTableByName(tableName);
				if (table != null) {
					return table;
				}
			} else if (child instanceof SQLSchema) {
				SQLTable table = ((SQLSchema) child).getTableByName(tableName);
				if (table != null) {
					return table;
				}
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

	public String getName() {
		return connectionSpec.getDisplayName();
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
		reset();
		if (connectionSpec != null) connectionSpec.removePropertyChangeListener(this);
		connectionSpec = argConnectionSpec;
		connectionSpec.addPropertyChangeListener(this);
		fireDbObjectChanged("connectionSpec");
	}

	/**
	 * Removes all children, closes and discards the JDBC connection.
	 */
	protected void reset() {
		// tear down old connection stuff
		List old = children;
		if (old != null && old.size() > 0) {
			int[] oldIndices = new int[old.size()];
			for (int i = 0, n = old.size(); i < n; i++) {
				oldIndices[i] = i;
			}
			fireDbChildrenRemoved(oldIndices, old);
			
		}
		children = new ArrayList();
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		connection = null;
		populated = false;
	}

	/**
	 * Listens for changes in DBCS properties, and resets this
	 * SQLDatabase if a critical property (url, driver, username)
	 * changes.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String pn = e.getPropertyName();
		if ( (e.getOldValue() == null && e.getNewValue() != null)
			 || (e.getOldValue() != null && e.getNewValue() == null)
			 || (e.getOldValue() != null && e.getNewValue() != null 
				 && !e.getOldValue().equals(e.getNewValue())) ) {
			if (pn.equals("url") || pn.equals("driverClass") || pn.equals("user")) {
				reset();
			} else if (pn.equals("displayName")) {
				fireDbObjectChanged("shortDisplayName");
			}
		}
	}

	/**
	 * Returns a reference to the JDBC connection to this database.
	 * Calls connect() if necessary.
	 *
	 * @return an open connection if this database has a valid
	 * connectionSpec; null if this is a dummy database (such as the
	 * playpen instance).
	 */
	public Connection getConnection() throws ArchitectException {
		if (connection == null && connectionSpec != null) connect();
		return this.connection;
	}

	public String toString() {
		if (connectionSpec != null) {
			return connectionSpec.getDisplayName();
		} else {
			return "PlayPen Database";
		}
	}
}
