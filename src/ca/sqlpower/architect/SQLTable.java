package ca.sqlpower.architect;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class SQLTable extends SQLObject {

	protected SQLDatabase parentDatabase;
	protected String catalogName;
	protected String schemaName;
	protected String tableName;
	protected String remarks;
	protected String objectType;

	/**
	 * A List of SQLRelationship objects describing keys that this
	 * table imports.
	 */
	protected List importedKeys;
	protected boolean columnsPopulated;
	protected String primaryKeyName;

	public SQLTable(SQLDatabase parent, String catalog, String schema, String name, String remarks, String objectType) {
		this.parentDatabase = parent;
		this.catalogName = catalog;
		this.schemaName = schema;
		this.tableName = name;
		this.remarks = remarks;
		this.columnsPopulated = false;
		this.objectType = objectType;

		this.children = new ArrayList();
	}

	protected static void addTablesToDatabase(SQLDatabase addTo) 
		throws SQLException, ArchitectException {
		synchronized (addTo) {
			Connection con = addTo.getConnection();
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet mdTables = null;
			try {
				mdTables = dbmd.getTables(null, null, "%", new String[] {"SYSTEM TABLE", "TABLE", "VIEW"});
				while (mdTables.next()) {
					addTo.children.add(new SQLTable(addTo,
													mdTables.getString(1),
													mdTables.getString(2),
													mdTables.getString(3),
													mdTables.getString(5),
													mdTables.getString(4)
													));
				}
			} finally {
				if (mdTables != null) mdTables.close();
			}
		}
	}

	/**
	 * Calls to this method should be synchronized, and should check
	 * that columnsPopulated == false.
	 */
	protected synchronized void populateColumns() throws ArchitectException {
		if (columnsPopulated) return;
		int oldSize = children.size();
		try {
			SQLColumn.addColumnsToTable(this, catalogName, schemaName, tableName);
			columnsPopulated = true;
		} catch (SQLException e) {
			throw new ArchitectException("table.populate", e);
		} finally {
			columnsPopulated = true;
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
	 * XXX: This could be speeded up with a hashset if needed.
	 */
	public SQLColumn getColumnByName(String colName) {
		Iterator it = children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getColumnName().equals(colName)) {
				return col;
			}
		}
		return null;
	}

	public void addColumn(SQLColumn col) {
		addChild(col);
	}
	
	public String toString() {
		return getShortDisplayName();
	}

	// ---------------------- SQLObject support ------------------------

	/**
	 * SQLDatabase objects don't have parents.
	 *
	 * @return <code>null</code>
	 */
	public SQLObject getParent() {
		return parentDatabase;
	}

	/**
	 * The table's name.
	 */
	public String getShortDisplayName() {
		if (schemaName != null) {
			return schemaName+"."+tableName+" ("+objectType+")";
		} else {
			return tableName+" ("+objectType+")";
		}
	}

	/**
	 * Just calls populateColumns.
	 */
	public void populate() throws ArchitectException {
		populateColumns();
	}
	
	public boolean isPopulated() {
		return columnsPopulated;
	}

	/**
	 * Returns true (tables have columns as children).
	 */
	public boolean allowsChildren() {
		return true;
	}


	// ------------------ Accessors and mutators below this line ------------------------

	/**
	 * Gets the value of parentDatabase
	 *
	 * @return the value of parentDatabase
	 */
	public SQLDatabase getParentDatabase()  {
		return this.parentDatabase;
	}

	/**
	 * Sets the value of parentDatabase
	 *
	 * @param argParentDatabase Value to assign to this.parentDatabase
	 */
	public void setParentDatabase(SQLDatabase argParentDatabase) {
		this.parentDatabase = argParentDatabase;
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
	 * Gets the value of tableName
	 *
	 * @return the value of tableName
	 */
	public String getTableName()  {
		return this.tableName;
	}

	/**
	 * Sets the value of tableName
	 *
	 * @param argTableName Value to assign to this.tableName
	 */
	public void setTableName(String argTableName) {
		this.tableName = argTableName;
	}

	/**
	 * Gets the value of remarks
	 *
	 * @return the value of remarks
	 */
	public String getRemarks()  {
		return this.remarks;
	}

	/**
	 * Sets the value of remarks
	 *
	 * @param argRemarks Value to assign to this.remarks
	 */
	public void setRemarks(String argRemarks) {
		this.remarks = argRemarks;
	}

	/**
	 * Gets the value of columns
	 *
	 * @return the value of columns
	 */
	public synchronized List getColumns() throws ArchitectException {
		return getChildren();
	}

	// 	/**
	// 	 * Sets the value of columns
	// 	 *
	// 	 * @param argColumns Value to assign to this.columns
	// 	 */
	// 	public synchronized void setColumns(List argColumns) {
	// 		List old = columns;
	// 		this.columns = argColumns;
	// 		columnsPopulated = true;
	// 		firePropertyChange("children", old, columns);
	// 	}

	/**
	 * Gets the value of importedKeys
	 *
	 * @return the value of importedKeys
	 */
	public List getImportedKeys()  {
		return this.importedKeys;
	}

	/**
	 * Sets the value of importedKeys
	 *
	 * @param argImportedKeys Value to assign to this.importedKeys
	 */
	public void setImportedKeys(List argImportedKeys) {
		this.importedKeys = argImportedKeys;
	}

	/**
	 * Gets the value of columnsPopulated
	 *
	 * @return the value of columnsPopulated
	 */
	public boolean isColumnsPopulated()  {
		return this.columnsPopulated;
	}

	/**
	 * Sets the value of columnsPopulated
	 *
	 * @param argColumnsPopulated Value to assign to this.columnsPopulated
	 */
	protected void setColumnsPopulated(boolean argColumnsPopulated) {
		this.columnsPopulated = argColumnsPopulated;
	}

	/**
	 * Gets the value of primaryKeyName
	 *
	 * @return the value of primaryKeyName
	 */
	public String getPrimaryKeyName()  {
		return this.primaryKeyName;
	}

	/**
	 * Sets the value of primaryKeyName
	 *
	 * @param argPrimaryKeyName Value to assign to this.primaryKeyName
	 */
	public void setPrimaryKeyName(String argPrimaryKeyName) {
		this.primaryKeyName = argPrimaryKeyName;
	}
	
	
	/**
	 * Gets the value of objectType
	 *
	 * @return the value of objectType
	 */
	public String getObjectType()  {
		return this.objectType;
	}

	/**
	 * Sets the value of objectType
	 *
	 * @param argObjectType Value to assign to this.objectType
	 */
	public void setObjectType(String argObjectType) {
		this.objectType = argObjectType;
	}

}
