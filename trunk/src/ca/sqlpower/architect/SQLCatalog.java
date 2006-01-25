package ca.sqlpower.architect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * A SQLCatalog is a container for other SQLObjects.  If it is in the
 * containment hierarchy for a given RDBMS, it will be directly under
 * SQLDatabase. SQLCatalogs normally get created and populated by
 * {@link SQLTable#addTablesToDatabase}.
 */
public class SQLCatalog extends SQLObject {
	private static Logger logger = Logger.getLogger(SQLCatalog.class);
	protected SQLObject parent;
	protected String catalogName;

	/**
	 * The term used for catalogs in the native database system.  In
	 * SQLServer2000, this is "database".
	 */
	protected String nativeTerm;

	public SQLCatalog() {
		this(null, null);
	}

	public SQLCatalog(SQLDatabase parent, String name) {
		this.parent = parent;
		this.catalogName = name;
		this.children = new LinkedList();
		this.nativeTerm = "catalog";
	}

	protected SQLTable getTableByName(String tableName) throws ArchitectException {
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLObject child = (SQLObject) childit.next();
			if (child instanceof SQLTable) {
				SQLTable table = (SQLTable) child;
				if (table.getTableName().equalsIgnoreCase(tableName)) {
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

	/**
	 *
	 * @return The schema in this catalog with the given name, or null
	 * if no such schema exists.
	 */
	protected SQLSchema getSchemaByName(String schemaName) throws ArchitectException {
		if (!isSchemaContainer()) {
			return null;
		}
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLSchema schema = (SQLSchema) childit.next();
			if (schema.getName().equalsIgnoreCase(schemaName)) {
				return schema;
			}
		}
		return null;
	}

	/**
	 * Determines whether this catalog object is a container for schemas or for tables.
	 *
	 * @return true (the default) if there are no children; false if
	 * the first child is not of type SQLSchema.
	 */
	public boolean isSchemaContainer() throws ArchitectException {
		if (children == null) {
			populate();
			if (children == null) {
				throw new ArchitectException("catalog.populate");
			}
		}

		// catalog has been populated

		if (children.size() == 0) {
			return true;
		} else {
			return (children.get(0) instanceof SQLSchema);
		}
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ---------------------- SQLObject support ------------------------

	public SQLObject getParent() {
		return parent;
	}

	protected void setParent(SQLObject newParent) {
		parent = newParent;
	}

	public String getName() {
		return getCatalogName();
	}

	public String getShortDisplayName() {
		return catalogName;
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public void populate() throws ArchitectException {
		if (populated) return;
		
		int oldSize = children.size();
		synchronized (parent) {
			
			ResultSet rs = null;
			try {
			
				Connection con = ((SQLDatabase)parent).getConnection();
				DatabaseMetaData dbmd = con.getMetaData();	

				con.setCatalog(catalogName);
				
				rs = dbmd.getSchemas();
				while (rs.next()) {
					String schName = rs.getString(1);
					SQLSchema schema = null;

					if (schName != null) {
						schema = new SQLSchema(this, schName, false);
						children.add(schema);
						schema.setNativeTerm(dbmd.getSchemaTerm());
						logger.debug("Set schema term to "+schema.getNativeTerm());
					}
				}
				rs.close();
				rs = null;
				
				if ( oldSize == children.size() ) {
					rs = dbmd.getTables(catalogName,
										null,
										"%",
										new String[] {"TABLE", "VIEW"});

					while (rs.next()) {
						children.add(new SQLTable(this,
												  rs.getString(3),
												  rs.getString(5),
												  rs.getString(4),
												  false));
					}
					rs.close();
					rs = null;
				}
				
			} catch (SQLException e) {
				throw new ArchitectException("catalog.populate.fail", e);
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
					throw new ArchitectException("catalog.rs.close.fail", e2);
				}
			}
		}
	}


	// ----------------- accessors and mutators -------------------

	public SQLDatabase getParentDatabase() {
		return (SQLDatabase) parent;
	}

	/**
	 * Gets the value of catalogName
	 *
	 * @return the value of catalogName
	 */
	public String getCatalogName()  {
		return this.catalogName;
	}

	/**
	 * Sets the value of catalogName
	 *
	 * @param argCatalogName Value to assign to this.catalogName
	 */
	public void setCatalogName(String argCatalogName) {
		this.catalogName = argCatalogName;
	}

	/**
	 * Gets the value of nativeTerm
	 *
	 * @return the value of nativeTerm
	 */
	public String getNativeTerm()  {
		return this.nativeTerm;
	}

	/**
	 * Sets the value of nativeTerm to a lowercase version of argNativeTerm.
	 *
	 * @param argNativeTerm Value to assign to this.nativeTerm
	 */
	public void setNativeTerm(String argNativeTerm) {
		if (argNativeTerm != null) argNativeTerm = argNativeTerm.toLowerCase();
		this.nativeTerm = argNativeTerm;
	}
}
