package ca.sqlpower.architect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
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
	
	public SQLSchema() {
		this(null, null);
	}

	public SQLSchema(SQLObject parent, String name) {
		this.parent = parent;
		this.schemaName = name;
		this.children = new LinkedList();
	}

	protected SQLTable getTableByName(String tableName) throws ArchitectException {
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

	public boolean isParentTypeDatabase() {
		return (parent instanceof SQLDatabase);
	}

	// ---------------------- SQLObject support ------------------------

	public SQLObject getParent() {
		return parent;
	}

	protected void setParent(SQLObject newParent) {
		parent = newParent;
	}

	public String getName() {
		return getSchemaName();
	}

	public String getShortDisplayName() {
		return schemaName;
	}
	
	public boolean allowsChildren() {
		return true;
	}

	/**
	 * does nothing because schemas are pre-populated elsewhere.
	 */
	public void populate() {
	}

	public boolean isPopulated() {
		return true;
	}

	// ----------------- accessors and mutators -------------------

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
}
