package ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.BaseObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

public class SQLDatabase extends SQLObject implements java.io.Serializable, PropertyChangeListener {
	private static Logger logger = Logger.getLogger(SQLDatabase.class);

	/**
	 * This ArchitectDataSource describes how to connect to the 
	 * physical database that backs this SQLDatabase object.
	 */
	private ArchitectDataSource dataSource;

	/**
	 * A pool of JDBC connections backed by a Jakarta Commons DBCP pool.
	 * You should access it only via the getConnectionPool() method.
	 */
	private transient BaseObjectPool connectionPool;
	
	/**
	 * Tells this database that it is being used to back the PlayPen.  Also 
	 * stops removal of children and the closure of connection when properties
	 * change.
	 */
	private boolean playPenDatabase = false;

	/**
	 * Constructor for instances that connect to a real database by JDBC.
	 */
	public SQLDatabase(ArchitectDataSource dataSource) {
		setDataSource(dataSource);
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
		return connectionPool != null;
	}

	public synchronized void populate() throws ArchitectException {
		if (populated) return;
		int oldSize = children.size();
		
		logger.debug("SQLDatabase: populate starting");
		
		Connection con = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			DatabaseMetaData dbmd = con.getMetaData();
			
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
			rs.close();
			rs = null;

			// if we tried to get Catalogs, and there were none, then I guess
			// we should look for Schemas instead (i.e. this database has no
			// catalogs, and schemas attached directly to the database)
			if ( children.size() == oldSize ) {
				rs = dbmd.getSchemas();
				while (rs.next()) {
					children.add(new SQLSchema(this, rs.getString(1),false));
				}
				rs.close();
				rs = null;
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
				if (rs != null ) rs.close();
			} catch (SQLException e2) {
				throw new ArchitectException("database.rs.close.fail", e2);
			}
			try {
				if (con != null ) con.close();
			} catch (SQLException e2) {
				throw new ArchitectException("Couldn't close connection", e2);
			}
		}
		
		logger.debug("SQLDatabase: populate finished");
	}
	



	public SQLCatalog getCatalogByName(String catalogName) throws ArchitectException {
		populate();
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
		populate();
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
				boolean match = (child.getName() == null ?
								 schemaName == null :
								 child.getName().equalsIgnoreCase(schemaName)); 
				if (match) {
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

		this.populate();

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
		target.populate();		
		Iterator childit = target.children.iterator();
		while (childit.hasNext()) {
			SQLObject child = (SQLObject) childit.next();
			if (child instanceof SQLTable) {
				SQLTable table = (SQLTable) child;
				if (table.getName().equalsIgnoreCase(tableName)) {
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

	@Override
	public String getName() {
		if (dataSource != null) {
			return dataSource.getDisplayName();
		} else {
			return "PlayPen Database";
		}
	}
	/**
	 * Sets the data source name if the data source is not null
	 */
	@Override
	public void setName(String argName)
	{
		if (dataSource != null) {
			dataSource.setName(argName);
		}
		
	}

	public String getShortDisplayName() {
		return getName();
	}
	
	public boolean allowsChildren() {
		return true;
	}

	/**
	 * Determines whether this SQL object is a container for catalog
	 *
	 * @return true (the default) if there are no children; false if
	 * the first child is not of type SQLCatlog.
	 */
	public boolean isCatalogContainer() throws ArchitectException {
		if (getParent() != null){
			populate();
		}
			
		if (children.size() == 0) {
			return true;
		} else {
			return (children.get(0) instanceof SQLCatalog);
		}
	}
	
	/**
	 * Determines whether this SQL object is a container for schemas
	 *
	 * @return true (the default) if there are no children; false if
	 * the first child is not of type SQLSchema.
	 */
	public boolean isSchemaContainer() throws ArchitectException {
		if (getParent() != null){
			populate();
		}
	
		// catalog has been populated
	
		if (children.size() == 0) {
			return true;
		} else {
			return (children.get(0) instanceof SQLSchema);
		}
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
	 * Gets the value of dataSource
	 *
	 * @return the value of dataSource
	 */
	public ArchitectDataSource getDataSource()  {
		return this.dataSource;
	}

	/**
	 * Sets the value of dataSource
	 *
	 * @param argDataSource Value to assign to this.dataSource
	 */
	public void setDataSource(ArchitectDataSource argDataSource) {
		ArchitectDataSource oldDataSource = this.dataSource;
		if (dataSource != null) {
			dataSource.removePropertyChangeListener(this);
			reset();
		}
		dataSource = argDataSource;
		dataSource.addPropertyChangeListener(this);		
		fireDbObjectChanged("dataSource",oldDataSource,argDataSource);
	}

	public void setPlayPenDatabase(boolean v) {
		boolean oldValue = playPenDatabase;
		playPenDatabase = v;

		if (oldValue != v) {
			fireDbObjectChanged("playPenDatabase", oldValue, v);
		}
	}

	public boolean isPlayPenDatabase() {
		return playPenDatabase;
	}

	/**
	 * Removes all children, closes and discards the JDBC connection.  
	 * Unless {@link #playPenDatabase} is true
	 */
	protected synchronized void reset() {
		
		if (playPenDatabase) {
			// preserve the objects that are in the Target system when
            // the connection spec changes
			logger.debug("Ignoring Reset request for: " + getDataSource());
			populated = true;
		} else {
			// discard everything and reload (this is generally for source systems)
			logger.debug("Resetting: " + getDataSource() );
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
		
		// destroy connection pool in either case (it still points to the old data source)
		if (connectionPool != null) {
			try {
				connectionPool.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		connectionPool = null;
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
			if ("url".equals(pn) || "driverClass".equals(pn) || "user".equals(pn)) {
				reset();
			} else if ("name".equals(pn)) {				
				fireDbObjectChanged("shortDisplayName",e.getOldValue(),e.getNewValue());
			}
		}
	}

	/**
	 * Returns a JDBC connection to the backing database, if there
	 * is one.  The connection that you get will be yours and only yours
	 * until you call close() on it.  To maximize efficiency of the pool,
	 * try to call close() as soon as you are done with the connection.
	 *
	 * @return an open connection if this database has a valid
	 * dataSource; null if this is a dummy database (such as the
	 * playpen instance).
	 */
	public Connection getConnection() throws ArchitectException {
		if (dataSource == null) {
			return null;
		} else {
			try {
				return (Connection) getConnectionPool().borrowObject();
			} catch (Exception e) {
				throw new ArchitectException(
						"Couldn't connect to database: "+e.getMessage(), e);
			}
		}
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
		
		/**
		 * Returns null, which will keep the monitor in indeterminate mode.
		 */
		public Integer getJobSize() throws ArchitectException {
			return null;
		}
		
		public int getProgress() {
			return 0;
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
		try {
			if (connectionPool != null){
				connectionPool.close();
			}
		} catch (Exception ex) {
			logger.error("Error closing connection pool", ex);
		} finally {
			connectionPool = null;
		}
	}
	
	private synchronized BaseObjectPool getConnectionPool() {
		if (connectionPool == null) {
			connectionPool = new GenericObjectPool();			
			ConnectionFactory cf = new ArchitectConnectionFactory(dataSource);			
			new PoolableConnectionFactory(cf, connectionPool, null,
					null, false, true);
		}
		return connectionPool;
	}

	@Override
	public Class<? extends SQLObject> getChildType() {
		if (children.size() == 0){
			return null;
		}
		else{
			return ((SQLObject)children.get(0)).getClass();
		}
	}
	
}
