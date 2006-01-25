package ca.sqlpower.architect;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import org.apache.log4j.Logger;

public class SQLRelationship extends SQLObject implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(SQLRelationship.class);
	
	public static final int ZERO = 1;
	public static final int ONE = 2;
	public static final int MANY = 4;
	
	protected SQLTable pkTable;
	protected SQLTable fkTable;

	protected int updateRule;
	protected int deleteRule;
	protected int deferrability;

	protected int pkCardinality;
	protected int fkCardinality;
	protected boolean identifying;

	protected String name;
	protected String physicalName;

	protected FKColumnManager fkColumnManager;

	public SQLRelationship() {
		children = new LinkedList();
		pkCardinality = ONE;
		fkCardinality = ZERO | ONE | MANY;
		fkColumnManager = new FKColumnManager();
	}

	/**
	 * Fetches all imported keys for the given table.  (Imported keys
	 * are the PK columns of other tables that are referenced by the
	 * given table).
	 *
	 * <p>Mainly for use by SQLTable's populate method.  Does not cause
	 * SQLObjectEvents (to avoid infinite recursion), so you have to
	 * generate them yourself at a safe time.
	 *
	 * <p>Note that <code>table</code>'s database must be fully
	 * populated up to the table level (the tables themselves can be
	 * unpopulated) before you call this method; it requires that all
	 * referenced tables are represented by in-memory SQLTable
	 * objects.
	 *
	 * @throws ArchitectException if a database error occurs or if the
	 * given table's parent database is not marked as populated.
	 */
	static void addImportedRelationshipsToTable(SQLTable table) throws ArchitectException {
		SQLDatabase db = table.getParentDatabase();
		if (!db.isPopulated()) {
			throw new ArchitectException("relationship.unpopulatedTargetDatabase");
		}
		Connection con = db.getConnection();
		ResultSet rs = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			SQLRelationship r = null;
			int currentKeySeq;
			LinkedList newKeys = new LinkedList();
			rs = dbmd.getImportedKeys(table.getCatalogName(),
									  table.getSchemaName(),
									  table.getTableName());
			while (rs.next()) {
				currentKeySeq = rs.getInt(9);
				if (currentKeySeq == 1) {
					r = new SQLRelationship();
					newKeys.add(r);
				}
				ColumnMapping m = new ColumnMapping();
				m.parent = r;
				r.children.add(m);
				r.pkTable = db.getTableByName(rs.getString(1),  // catalog
											  rs.getString(2),  // schema
											  rs.getString(3)); // table
				if (r.pkTable == null) {
				    logger.error("addImportedRelationshipsToTable: Couldn't find exporting table "
				            +rs.getString(1)+"."+rs.getString(2)+"."+rs.getString(3)
				            +" in target database!");
				    continue;
				}

				logger.debug("Looking for pk column '"+rs.getString(4)+"' in table '"+r.pkTable+"'");
				m.pkColumn = r.pkTable.getColumnByName(rs.getString(4));
				if (m.pkColumn == null) {
					throw new ArchitectException("relationship.populate.nullPkColumn");
				}

				r.fkTable = db.getTableByName(rs.getString(5),  // catalog
											  rs.getString(6),  // schema
											  rs.getString(7)); // table
				if (r.fkTable != table) {
					throw new IllegalStateException("fkTable did not match requested table");
				}
				m.fkColumn = r.fkTable.getColumnByName(rs.getString(8));
				if (m.fkColumn == null) {
					throw new ArchitectException("relationship.populate.nullFkColumn");
				}
				// column 9 (currentKeySeq) handled above
				r.updateRule = rs.getInt(10);
				r.deleteRule = rs.getInt(11);
				r.name = rs.getString(12);
				r.deferrability = rs.getInt(14);
				// FIXME: need to determine if the column is identifying or non-identifying!
			}

			// now that all the new SQLRelationship objects are set up, add them to their tables
			Iterator it = newKeys.iterator();
			while (it.hasNext()) {
				r = (SQLRelationship) it.next();
				r.pkTable.columnsFolder.addSQLObjectListener(r.fkColumnManager);
				r.pkTable.addExportedKey(r);
				logger.debug("Added exported key to "+r.pkTable.getName());
				r.fkTable.addImportedKey(r);
				logger.debug("Added imported key to "+r.fkTable.getName());
			}

		} catch (SQLException e) {
			throw new ArchitectException("relationship.populate", e);
		} finally {
			try {
				if (rs != null) rs.close();
			} catch (SQLException e) {
				logger.warn("Couldn't close resultset", e);
			}
		}
	}

	public ColumnMapping getMappingByPkCol(SQLColumn pkcol) {
		Iterator it = children.iterator();
		while (it.hasNext()) {
			ColumnMapping m = (ColumnMapping) it.next();
			if (m.pkColumn == pkcol) {
				return m;
			}
		}
		return null;
	}

	public boolean containsPkColumn(SQLColumn col) {
		return getMappingByPkCol(col) != null;
	}

	public ColumnMapping getMappingByFkCol(SQLColumn fkcol) {
		Iterator it = children.iterator();
		while (it.hasNext()) {
			ColumnMapping m = (ColumnMapping) it.next();
			if (m.fkColumn == fkcol) {
				return m;
			}
		}
		return null;
	}

	public boolean containsFkColumn(SQLColumn col) {
		return getMappingByFkCol(col) != null;
	}

	/**
	 * Convenience method for adding a SQLRelationship.ColumnMapping
	 * child to this relationship.
	 * @throws ArchitectException 
	 */
	public void addMapping(SQLColumn pkColumn, SQLColumn fkColumn) throws ArchitectException {
		ColumnMapping cmap = new ColumnMapping();
		cmap.setPkColumn(pkColumn);
		cmap.setFkColumn(fkColumn);
		addChild(cmap);
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ------------------ SQLObject Listener ---------------------

	/**
	 * Listens for new or removed PK columns in the exporting table,
	 * and updates the list of column mappings as well as the set of
	 * columns that are being pushed onto the FK table.
	 */
	protected class FKColumnManager implements SQLObjectListener {
		public void dbChildrenInserted(SQLObjectEvent e) {
			logger.debug("dbChildrenInserted event! children="+e.getChildren());
			SQLObject[] cols = e.getChildren();
			for (int i = 0; i < cols.length; i++) {
				SQLColumn col = (SQLColumn) cols[i];
				try {
					if (col.getPrimaryKeySeq() != null) {
						ensureInMapping(col);
					} else {
						ensureNotInMapping(col);
					}
				} catch (ArchitectException ex) {
					logger.warn("Couldn't add/remove mapped FK columns", ex);
				}
			}
		}

		public void dbChildrenRemoved(SQLObjectEvent e) {
			logger.debug("dbChildrenRemoved event! children="+e.getChildren());
			SQLObject[] cols = e.getChildren();
			for (int i = 0; i < cols.length; i++) {
				SQLColumn col = (SQLColumn) cols[i];
				try {
					ensureNotInMapping(col);
				} catch (ArchitectException ex) {
					logger.warn("Couldn't remove mapped FK columns", ex);
				}
			}
		}

		public void dbObjectChanged(SQLObjectEvent e) {
			// a column changed.. should update FK type/size/precision and maybe name
			if (e.getSource() instanceof SQLColumn) {
				ColumnMapping m = getMappingByPkCol((SQLColumn) e.getSource());
				String prop = e.getPropertyName();
				if (prop == null
					|| prop.equals("parent")
					|| prop.equals("primaryKeySeq")
					|| prop.equals("remarks")) {
					// don't care
				} else if (prop.equals("sourceColumn")) {
					m.getFkColumn().setSourceColumn(m.getPkColumn().getSourceColumn());
				} else if (prop.equals("columnName")) {
					m.getFkColumn().setColumnName(m.getPkColumn().getColumnName());
				} else if (prop.equals("type")) {
					m.getFkColumn().setType(m.getPkColumn().getType());
				} else if (prop.equals("sourceDataTypeName")) {
					m.getFkColumn().setSourceDataTypeName(m.getPkColumn().getSourceDataTypeName());
				} else if (prop.equals("scale")) {
					m.getFkColumn().setScale(m.getPkColumn().getScale());
				} else if (prop.equals("precision")) {
					m.getFkColumn().setPrecision(m.getPkColumn().getPrecision());
				} else if (prop.equals("nullable")) {
					m.getFkColumn().setNullable(m.getPkColumn().getNullable());
				} else if (prop.equals("defaultValue")) {
					m.getFkColumn().setDefaultValue(m.getPkColumn().getDefaultValue());
				} else if (prop.equals("autoIncrement")) {
					m.getFkColumn().setAutoIncrement(m.getPkColumn().isAutoIncrement());
				} else {
					logger.warn("Warning: unknown column property "+prop
								+" changed while monitoring pkTable");
				}
			}
		}

		public void dbStructureChanged(SQLObjectEvent e) {
			// wow!  let's re-scan the whole table
			try {
				Iterator it = pkTable.getColumns().iterator();
				while (it.hasNext()) {
					SQLColumn col = (SQLColumn) it.next();
					if (col.getPrimaryKeySeq() != null) {
						ensureInMapping(col);
					} else {
						ensureNotInMapping(col);
					}
				}
			} catch (ArchitectException ex) {
				logger.warn("Coulnd't re-scan table as a result of dbStructureChanged", ex);
			}
		}

		protected void ensureInMapping(SQLColumn pkcol) throws ArchitectException {
			if (!containsPkColumn(pkcol)) {
				SQLColumn fkcol = fkTable.getColumnByName(pkcol.getName());
				if (fkcol == null) {
					fkcol = (SQLColumn) pkcol.clone();
					fkTable.addColumn(fkcol);
					if (identifying) {
						fkcol.setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
					}
				}
				addMapping(pkcol, fkcol);
			}
		}

		/**
		 * Ensures there is no mapping for pkcol in this relationship.
		 * If there was, it is removed along with the column that may
		 * have been pushed into the relationship's fkTable.
		 */
		protected void ensureNotInMapping(SQLColumn pkcol) throws ArchitectException {
			if (containsPkColumn(pkcol)) {
				ColumnMapping m = getMappingByPkCol(pkcol);
				List fkTies = fkTable.keysOfColumn(m.getFkColumn());
				if (fkTies == null || fkTies.size() <= 1) {
					fkTable.removeColumn(m.getFkColumn());
				}
				removeChild(m);
			}
		}
	}

	// ---------------------- SQLRelationship SQLObject support ------------------------

	/**
	 * Returns the table that holds the primary keys (the imported table).
	 */
	public SQLObject getParent() {
		return pkTable;
	}

	/**
	 * This method is not useful, and has no side effects.
	 *
	 * @param newParent If this is the same as pkTable or fkTable,
	 * this method returns normally.  Otherwise, this method throws
	 * IllegalArgumentException.
	 * @throws IllegalArgumentException if newParent is anything other
	 * than this relationship's pkTable.exportedKeysFolder or
	 * fkTable.importedKeysFolder
	 */
	protected void setParent(SQLObject newParent) {
		if (newParent != null
			&& (pkTable != null && newParent != pkTable.exportedKeysFolder)
			&& (fkTable != null && newParent != fkTable.importedKeysFolder)) {
			throw new IllegalArgumentException
				("You can't change the parent of a SQLRelationship this way");
		}
	}

	/**
	 * Returns the foreign key name.
	 */
	public String getShortDisplayName() {
		return name;
	}
	
	/**
	 * Relationships have ColumnMapping children.
	 *
	 * @return true
	 */
	public boolean allowsChildren() {
		return true;
	}

	/**
	 * This class is not a lazy-loading class.  This call does nothing.
	 */
	public void populate() throws ArchitectException {
		return;
	}

	/**
	 * Returns true.
	 */
	public boolean isPopulated() {
		return true;
	}
	/*
	 * Make sure, among other things, that the PK Listener is removed.
	 */
	public void removeDependencies() {
		if (pkTable != null) {
			pkTable.columnsFolder.removeSQLObjectListener(fkColumnManager);
		}		
	}
	
	
	// ----------------- accessors and mutators -------------------
	
	/**
	 * Gets the value of updateRule
	 *
	 * @return the value of updateRule
	 */
	public int getUpdateRule()  {
		return this.updateRule;
	}

	/**
	 * Sets the value of updateRule
	 *
	 * @param argUpdateRule Value to assign to this.updateRule
	 */
	public void setUpdateRule(int argUpdateRule) {
		this.updateRule = argUpdateRule;
		fireDbObjectChanged("updateRule");
	}

	/**
	 * Gets the value of deleteRule
	 *
	 * @return the value of deleteRule
	 */
	public int getDeleteRule()  {
		return this.deleteRule;
	}

	/**
	 * Sets the value of deleteRule
	 *
	 * @param argDeleteRule Value to assign to this.deleteRule
	 */
	public void setDeleteRule(int argDeleteRule) {
		this.deleteRule = argDeleteRule;
		fireDbObjectChanged("deleteRule");
	}

	/**
	 * Gets the value of deferrability
	 *
	 * @return the value of deferrability
	 */
	public int getDeferrability()  {
		return this.deferrability;
	}

	/**
	 * Sets the value of deferrability
	 *
	 * @param argDeferrability Value to assign to this.deferrability
	 */
	public void setDeferrability(int argDeferrability) {
		this.deferrability = argDeferrability;
		fireDbObjectChanged("deferrability");
	}

	public String getName()  {
		return this.name;
	}

	public void setName(String argName) {
		this.name = argName;
		fireDbObjectChanged("name");
	}


	public String getPhysicalName()  {
		return this.physicalName;
	}

	/**
	 * Changes the physical name of this relationship.  Fires a dbObjectChanged event if
	 * the new name differs from the old one.
	 */
	public void setPhysicalName(String argName) {
		boolean changed = (argName == null ? physicalName != null : !argName.equals(physicalName));
		if (changed) {
			this.physicalName = argName;
			fireDbObjectChanged("physicalName");
		}
	}

	/**
	 * Gets the value of pkCardinality
	 *
	 * @return the value of pkCardinality
	 */
	public int getPkCardinality()  {
		return this.pkCardinality;
	}

	/**
	 * Sets the value of pkCardinality
	 *
	 * @param argPkCardinality Value to assign to this.pkCardinality
	 */
	public void setPkCardinality(int argPkCardinality) {
		this.pkCardinality = argPkCardinality;
		fireDbObjectChanged("pkCardinality");
	}

	/**
	 * Gets the value of fkCardinality
	 *
	 * @return the value of fkCardinality
	 */
	public int getFkCardinality()  {
		return this.fkCardinality;
	}

	/**
	 * Sets the value of fkCardinality
	 *
	 * @param argFkCardinality Value to assign to this.fkCardinality
	 */
	public void setFkCardinality(int argFkCardinality) {
		this.fkCardinality = argFkCardinality;
		fireDbObjectChanged("fkCardinality");
	}

	/**
	 * Gets the value of identifying
	 *
	 * @return the value of identifying
	 */
	public boolean isIdentifying()  {
		return this.identifying;
	}

	/**
	 * Sets the value of identifying, and moves the FK columns into or
	 * out of the FK Table's primary key as appropriate.
	 *
	 * @param argIdentifying Value to assign to this.identifying
	 */
	public void setIdentifying(boolean argIdentifying) throws ArchitectException {
		if (identifying != argIdentifying) {
			identifying = argIdentifying;
			fireDbObjectChanged("identifying");
			if (identifying) {
				Iterator mappings = getChildren().iterator();
				while (mappings.hasNext()) {
					ColumnMapping m = (ColumnMapping) mappings.next();
					if (m.getFkColumn().getPrimaryKeySeq() == null) {
						m.getFkColumn().setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
					}
				}
			} else {
				Iterator mappings = getChildren().iterator();
				while (mappings.hasNext()) {
					ColumnMapping m = (ColumnMapping) mappings.next();
					if (m.getFkColumn().getPrimaryKeySeq() != null) {
						m.getFkColumn().setPrimaryKeySeq(null);
					}
				}
			}
		}
	}


	public SQLTable getPkTable() {
		return pkTable;
	}

	public void setPkTable(SQLTable pkt) {
		if (pkTable != null) {
			pkTable.columnsFolder.removeSQLObjectListener(fkColumnManager);
		}
		pkTable = pkt;
		pkTable.columnsFolder.addSQLObjectListener(fkColumnManager);
		// XXX: fire event?
	}

	public SQLTable getFkTable() {
		return fkTable;
	}

	public void setFkTable(SQLTable fkt) {
		fkTable = fkt;
		// XXX: fire event?
	}
	
	// -------------------------- COLUMN MAPPING ------------------------

	public static class ColumnMapping extends SQLObject {
		protected SQLRelationship parent;
		protected SQLColumn pkColumn;
		protected SQLColumn fkColumn;

		public ColumnMapping() {
			children = Collections.EMPTY_LIST;
		}
		
		/**
		 * Gets the value of pkColumn
		 *
		 * @return the value of pkColumn
		 */
		public SQLColumn getPkColumn()  {
			return this.pkColumn;
		}

		/**
		 * Sets the value of pkColumn
		 *
		 * @param argPkColumn Value to assign to this.pkColumn
		 */
		public void setPkColumn(SQLColumn argPkColumn) {
			this.pkColumn = argPkColumn;
		}

		/**
		 * Gets the value of fkColumn
		 *
		 * @return the value of fkColumn
		 */
		public SQLColumn getFkColumn()  {
			return this.fkColumn;
		}

		/**
		 * Sets the value of fkColumn
		 *
		 * @param argFkColumn Value to assign to this.fkColumn
		 */
		public void setFkColumn(SQLColumn argFkColumn) {
			this.fkColumn = argFkColumn;
		}

		public String toString() {
			return getShortDisplayName();
		}

		// ---------------------- ColumnMapping SQLObject support ------------------------
		
		/**
		 * Returns the table that holds the primary keys (the imported table).
		 */
		public SQLObject getParent() {
			return (SQLRelationship) parent;
		}

		protected void setParent(SQLObject newParent) {
			parent = (SQLRelationship) newParent;
		}
		
		public String getName() {
			return "Column Mapping";
		}

		/**
		 * Returns the table and column name of the pkColumn.
		 */
		public String getShortDisplayName() {
			String pkTableName = null;
			if (pkColumn.getParentTable() != null) {
				pkTableName = pkColumn.getParentTable().getName();
			}
			return fkColumn.getColumnName()+" - "+
				pkTableName+"."+pkColumn.getColumnName();
		}
		
		/**
		 * Mappings do not contain other SQLObjects.
		 *
		 * @return false
		 */
		public boolean allowsChildren() {
			return false;
		}
		
		/**
		 * This class is not a lazy-loading class.  This call does nothing.
		 */
		public void populate() throws ArchitectException {
			return;
		}
		
		/**
		 * Returns true.
		 */
		public boolean isPopulated() {
			return true;
		}
	
	}
}
