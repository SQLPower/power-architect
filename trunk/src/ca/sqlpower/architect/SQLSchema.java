package ca.sqlpower.architect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

/**
 * A SQLSchema is a container for SQLTables.  If it is in the
 * containment hierarchy for a given RDBMS, it will be directly above
 * SQLTables.  Its parent could be either a SQLDatabase or a SQLCatalog.
 */
public class SQLSchema extends SQLObject {
	protected SQLObject parent;
	protected String schemaName;

	protected boolean populated;

	public SQLSchema(SQLObject parent, String name) {
		this.parent = parent;
		this.schemaName = name;
		this.children = new LinkedList();
		this.populated = false;
	}

	/**
	 * This one is a bit funny, so read the documentation.
	 *
	 * <p>Some RDBMSes have a Database-Catalog-Schema-(Table|View|...) 
	 * hierarchy ("type 1", SQLServer), and others have
	 * Database-Catalog-(Table|View|...) ("type 2", Access).  Still
	 * others have Database-Schema-(Table|View|...) ("type 3",
	 * Oracle). This method populates the catalogs and schemas for
	 * types 1 and 3.
	 *
	 * <p>For type 1 databases, a new set of SQLCatalog objects will
	 * be created.  The schemas will be added to the various catalogs,
	 * and then the catalogs will be added to the database.
	 *
	 * <p>For type 3 databases, all schemas will be added directly to
	 * the database.
	 */
	protected static boolean addSchemasToDatabase(SQLDatabase addTo)
		throws SQLException, ArchitectException {

		boolean addedSome = false;
		HashMap catalogs = new HashMap();
		ResultSet rs = null;
		try {
			rs = addTo.getConnection().getMetaData().getSchemas();
			while (rs.next()) {
				String schName = rs.getString(1);
				String catName = "fake"; //rs.getString(2);
				if (catName != null) {
					SQLCatalog cat = (SQLCatalog) catalogs.get(catName);
					if (cat == null) {
						cat = new SQLCatalog(addTo, catName);
						addTo.children.add(cat);
						catalogs.put(catName, cat);
					}
					cat.children.add(new SQLSchema(cat, schName));
					addedSome = true;
				} else {
					// for catalogless RDBMS (type 3)
					addTo.children.add(new SQLSchema(addTo, schName));
					addedSome = true;
				}
			}
		} finally {
			if (rs != null) rs.close();
		}
		return addedSome;
	}

	protected synchronized void populateTables() throws ArchitectException {
		if (populated) return;
		int oldSize = children.size();
		try {
			SQLTable.addTablesToSchema(this);
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
	

	protected SQLTable getTableByName(String tableName) throws ArchitectException {
		if (!populated) populate();
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLTable child = (SQLTable) childit.next();
			if (child.getTableName().equalsIgnoreCase(tableName)) {
				return child;
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

	public String getShortDisplayName() {
		return schemaName;
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
	 * Don't use.
	 */
	protected void setParent(SQLObject argParent) {
		throw new UnsupportedOperationException("You can't do that");
	}

	/**
	 * Gets the value of schemaName
	 *
	 * @return the value of schemaName
	 */
	public String getSchemaName()  {
		return this.schemaName;
	}

	/**
	 * Sets the value of schemaName
	 *
	 * @param argSchemaName Value to assign to this.schemaName
	 */
	public void setSchemaName(String argSchemaName) {
		this.schemaName = argSchemaName;
	}

	/**
	 * Gets an unmodifiable view to this schema's children.
	 */
	public List getChildren()  {
		return Collections.unmodifiableList(this.children);
	}

	protected void setChildren(List argChildren) {
		this.children = argChildren;
	}

	/**
	 * Don't use.
	 */
	private void setPopulated(boolean argPopulated) {
		throw new UnsupportedOperationException("You can't do that");
	}

}
