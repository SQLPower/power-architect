package ca.sqlpower.architect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;

/**
 * A SQLCatalog is a container for other SQLObjects.  If it is in the
 * containment hierarchy for a given RDBMS, it will be directly under
 * SQLDatabase. SQLCatalogs normally get created and populated by
 * {@link SQLTable#addTablesToDatabase}.
 */
public class SQLCatalog extends SQLObject {
	protected SQLDatabase parent;
	protected String catalogName;

	public SQLCatalog(SQLDatabase parent, String name) {
		this.parent = parent;
		this.catalogName = name;
		this.children = new LinkedList();
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
	}

	public boolean isPopulated() {
		return true;
	}

	// ----------------- accessors and mutators -------------------

	
	/**
	 * Don't use.
	 */
	private void setParent(SQLDatabase argParent) {
		throw new UnsupportedOperationException("You can't set the parent on a catalog");
	}

	public SQLDatabase getParentDatabase() {
		return parent;
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
	 * Gets the value of children
	 *
	 * @return the value of children
	 */
	public List getChildren()  {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Sets the value of children
	 *
	 * @param argChildren Value to assign to this.children
	 */
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
