package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

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
		setName(name);
		this.children = new LinkedList();
		this.nativeTerm = "catalog";
	}

	protected SQLTable getTableByName(String tableName) throws ArchitectException {
		populate();
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLObject child = (SQLObject) childit.next();
			if (child instanceof SQLTable) {
				SQLTable table = (SQLTable) child;
				if (table.getName().equalsIgnoreCase(tableName)) {
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
	public SQLSchema getSchemaByName(String schemaName) throws ArchitectException {
		populate();
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

	
	public String getShortDisplayName() {
		return getName();
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public void populate() throws ArchitectException {
		if (populated) return;

		logger.debug("SQLCatalog: populate starting");
	
		int oldSize = children.size();
		synchronized (parent) {
			
			Connection con = null;
			ResultSet rs = null;
			try {
			
				con = ((SQLDatabase) parent).getConnection();
				DatabaseMetaData dbmd = con.getMetaData();	

                // This can fail in SS2K because of privelege problems.  There is
                // apparently no way to check if it will fail; you just have to try.
                try {
                    con.setCatalog(getName());
                } catch (SQLException ex) {
                    logger.info("populate: Could not setCatalog("+getName()+"). Assuming it's a permission problem.  Stack trace:", ex);
                    return;
                }
				
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
				
				if (oldSize == children.size()) {
					rs = dbmd.getTables(getName(),
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
					if (rs != null) rs.close();
				} catch (SQLException e2) {
					throw new ArchitectException("catalog.rs.close.fail", e2);
				}
				try {
					if (con != null) con.close();
				} catch (SQLException e2) {
					throw new ArchitectException("Couldn't close connection", e2);
				}
			}
		}
		
		logger.debug("SQLCatalog: populate finished");

	}


	// ----------------- accessors and mutators -------------------

	public SQLDatabase getParentDatabase() {
		return (SQLDatabase) parent;
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
		String oldValue = nativeTerm;
		this.nativeTerm = argNativeTerm;
		fireDbObjectChanged("nativeTerm", oldValue, nativeTerm);
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
}
