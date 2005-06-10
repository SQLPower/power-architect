package ca.sqlpower.architect;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.architect.jdbc.ConnectionFacade;

public class SQLDatabase extends SQLObject implements java.io.Serializable, PropertyChangeListener {
	private static Logger logger = Logger.getLogger(SQLDatabase.class);

	/**
	 * Caches connections across serialization attempts.  See {@link
	 * #connect()}.
	 */
	private static transient HashMap dbConnections = new HashMap();

	protected DBConnectionSpec connectionSpec;
	protected transient Connection connection;

	protected boolean ignoreReset = false;


	/**
	 * Constructor for instances that connect to a real database by JDBC.
	 */
	public SQLDatabase(DBConnectionSpec connectionSpec) {
		setConnectionSpec(connectionSpec);
		children = new ArrayList();
	}
	
	/**
	 * Constructor for non-JDBC connected instances.
	 */
	public SQLDatabase() {
		children = new ArrayList();
		populated = true;
	}

	public synchronized boolean isConnected() {
		return connection != null;
	}

	/**
	 * Connects to the database if necessary.  It is safe to call this
	 * method many times; it returns quickly if nothing needs to be
	 * done.
	 */
	public synchronized void connect() throws ArchitectException {
		try {
			if (connection != null && !connection.isClosed()) return;
			connection = (Connection) dbConnections.get(connectionSpec);
			if (connection != null && !connection.isClosed()) return;

			if (connectionSpec.getDriverClass() == null
				|| connectionSpec.getDriverClass().trim().length() == 0) {
				throw new ArchitectException("You didn't specify the JDBC Driver class.");
			}

			if (connectionSpec.getUrl() == null
				|| connectionSpec.getUrl().trim().length() == 0) {
				throw new ArchitectException("You didn't specify the JDBC URL.");
			}

			if (connectionSpec.getUser() == null
				|| connectionSpec.getUser().trim().length() == 0) {
				throw new ArchitectException("You didn't specify the JDBC username.");
			}

 			ArchitectSession session = ArchitectSession.getInstance();
 			if (session == null) {
 				throw new ArchitectException
 					("Can't connect to database because ArchitectSession.getInstance()"
 					 +" returned null");
 			}
			if (logger.isDebugEnabled()) {
//				DriverManager.setLogStream(System.err);
				ClassLoader cl = this.getClass().getClassLoader();
				StringBuffer loaders = new StringBuffer();
				loaders.append("Local Classloader chain: ");
				while (cl != null) {
					loaders.append(cl).append(", ");
					cl = cl.getParent();
				}
				logger.debug(loaders);
			}
			Class.forName(connectionSpec.getDriverClass(), true, session.getJDBCClassLoader());
			logger.info("Driver Class "+connectionSpec.getDriverClass()+" loaded without exception");
			connection = ConnectionFacade.createFacade(session.getJDBCClassLoader().getConnection(connectionSpec.getUrl(),
														 	      					connectionSpec.getUser(),
																					connectionSpec.getPass()));
			logger.debug("Connection class is: " + connection.getClass().getName());
			dbConnections.put(connectionSpec, connection);
		} catch (ClassNotFoundException e) {
			logger.warn("Driver Class not found", e);
			throw new ArchitectException("JDBC Driver \""+connectionSpec.getDriverClass()
										 +"\" not found.", e);
		} catch (SQLException e) {
			throw new ArchitectException("Couldn't connect to database:\n"+e.getMessage(), e);
		}
	}

	public void populate() throws ArchitectException {
		if (populated) return;
		int oldSize = children.size();
		
		Connection con = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			DatabaseMetaData dbmd = con.getMetaData();

			logger.debug("MetaData class is: " + dbmd.getClass().getName());

			rs = dbmd.getCatalogs();
		
			while (rs.next()) {
				String catName = rs.getString(1);
				SQLCatalog cat = null;
				if (catName != null) {
					cat = new SQLCatalog(this, catName);
					cat.setNativeTerm(dbmd.getCatalogTerm());
					logger.debug("Set catalog term to "+cat.getNativeTerm());
					children.add(cat);
				}
			}

			if ( children.size() == oldSize ) {
				rs = dbmd.getSchemas();
				while (rs.next()) {
					children.add(new SQLSchema(this, rs.getString(1)));
				}
			}
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
			try {
				if ( rs != null )	rs.close();
			} catch (SQLException e2) {
				throw new ArchitectException("database.rs.close.fail", e2);
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
			if (child.getName().equalsIgnoreCase(catalogName)) {
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
				if (child.getName().equalsIgnoreCase(schemaName)) {
					return (SQLSchema) child;
				}
			} else {
				throw new IllegalStateException("Database contains a mix of schemas or catalogs with other objects");
			}
		}
		return null;
	}

	public SQLTable getTableByName(String tableName) throws ArchitectException {
		return getTableByName(null, null, tableName);
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
		    if (logger.isDebugEnabled())
		        logger.debug("getTableByName("+catalogName+","+schemaName+","+tableName+"): no such catalog!");
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
		    if (logger.isDebugEnabled())
		        logger.debug("getTableByName("+catalogName+","+schemaName+","+tableName+"): no such schema!");
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

		if (logger.isDebugEnabled())
	        logger.debug("getTableByName("+catalogName+","+schemaName+","+tableName+"): catalog and schema ok; no such table!");
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

	protected void setParent(SQLObject newParent) {
		// no parent
	}

	public String getName() {
		if (connectionSpec != null) {
			return connectionSpec.getDisplayName();
		} else {
			return "PlayPen Database";
		}
	}

	public String getShortDisplayName() {
		return getName();
	}
	
	public boolean allowsChildren() {
		return true;
	}

	

	// ----------------- accessors and mutators -------------------
	
	/**
	 * Recursively searches this database for SQLTable descendants,
	 * compiles a list of those that were found, and returns that
	 * list.
	 *
	 * <p>WARNING: Calling this method will populate the entire
	 * database!  Think carefully about using it on lazy-loading
	 * source databases (it is safe to use on the playpen database).
	 *
	 * @return the value of tables
	 */
	public List getTables() throws ArchitectException {
		return getTableDescendants(this);
	}

	/**
	 * This is the recursive subroutine used by {@link #getTables}.
	 */
	private static List getTableDescendants(SQLObject o) throws ArchitectException {

		// this seemingly redundant short-circuit is required because
		// we don't want o.getChildren() to be null
		if (!o.allowsChildren()) return Collections.EMPTY_LIST;

		LinkedList tables = new LinkedList();
		Iterator it = o.getChildren().iterator();
		while (it.hasNext()) {
			SQLObject c = (SQLObject) it.next();
			if (c instanceof SQLTable) {
				tables.add(c);
			} else {
				tables.addAll(getTableDescendants(c));
			}
		}
		return tables;
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
		if (connectionSpec != null) {
			connectionSpec.removePropertyChangeListener(this);
			reset();
		}
		connectionSpec = argConnectionSpec;
		connectionSpec.addPropertyChangeListener(this);
		fireDbObjectChanged("connectionSpec");
	}

	/**
	 * Lets outside users modify the internal flag that says whether
	 * or not the list of child objects has already been loaded from
	 * the source database.  Users of this class should not normally
	 * call this method, but it needs to be public for the
	 * SwingUIProject load implementation.
	 */
	public void setPopulated(boolean v) {
		populated = v;
	}

	public void setIgnoreReset(boolean v) {
		ignoreReset = v;
	}

	public boolean getIgnoreReset() {
		return ignoreReset;
	}

	/**
	 * Removes all children, closes and discards the JDBC connection.
	 */
	protected void reset() {
		if (ignoreReset) {
			// preserve the objects that are in the Target system when
            // the connection spec changes
			logger.debug("Ignoring Reset request for: " + getConnectionSpec());
			populated = true;
		} else {
			// discard everything and reload (this is generally for source systems)
			logger.debug("Resetting: " + getConnectionSpec());
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
			populated = false;
		}
		
		// reset connection in either case
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		connection = null;
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
		if (connectionSpec != null && connection == null) {
			connect();
		}
		return this.connection;
	}

	public String toString() {
		return getName();
	}
	
	private PopulateProgressMonitor progressMonitor;
	public synchronized PopulateProgressMonitor getProgressMonitor() throws ArchitectException {
		if (progressMonitor == null) {
			progressMonitor = new PopulateProgressMonitor();
		}
		return progressMonitor;
	}
	
	// -------------- Small class for monitoring populate progress -----------------
	
	public class PopulateProgressMonitor {
		
		private Integer jobSize;
		
		/**
		 * Returns the number of children this database will have when
		 * it is populated.  If the database connection has not been made
		 * yet, it returns null.  Otherwise it counts schemas, catalogs,
		 * or tables (depending on DBMS type).
		 */
		public Integer getJobSize() throws ArchitectException {
			if (connection == null) {
				return null;
			} else if (jobSize == null) {
				jobSize = new Integer(getChildCount());  // this will probably do network io
			}
			return jobSize;
		}
		
		public int getProgress() {
			if (children == null) {
				return 0;
			} else {
				return children.size();
			}
		}
		
		public boolean isFinished() {
			return isPopulated();
		}
	}

	/**
	 * Closes all connections and other resources that were allocated
	 * by the connect() method.  Logs, but does not propogate, SQL exceptions.
	 */
	public void disconnect() {
//		Iterator it = dbConnections.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry ent = (Map.Entry) it.next();
//			Connection c = (Connection) ent.getValue();
//			try {
//				if (c != null && !c.isClosed()) c.close();
//			} catch (SQLException ex) {
//				logger.error("Error disconnecting a dbConnections connection in disconnect()");
//			} finally {
//				it.remove();
//			}
//		}
		try {
			if (connection != null && !connection.isClosed()) connection.close();
		} catch (SQLException ex) {
			logger.error("Error disconnecting main connection in disconnect()");
		} finally {
			connection = null;
		}
	}
	
}
