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

	protected SQLTable pkTable;
	protected SQLTable fkTable;

	protected int updateRule;
	protected int deleteRule;
	protected int deferrability;

	protected String fkName;
	protected String pkName;

	public SQLRelationship() {
		children = new LinkedList();
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
	 * <p>Note that table's database must be fully populated before
	 * you call this method; it requires that all referenced tables
	 * are represented by in-memory SQLTable objects.
	 *
	 * @throws ArchitectException if a database error occurs or if the
	 * given table's parent database is not marked as populated.
	 */
	static void addRelationshipsToTable(SQLTable table) throws ArchitectException {
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
				ColumnMapping m = r.new ColumnMapping();
				r.children.add(m);
				r.pkTable = db.getTableByName(rs.getString(1),  // catalog
											  rs.getString(2),  // schema
											  rs.getString(3)); // table
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
				r.fkName = rs.getString(12);
				r.pkName = rs.getString(13);
				r.deferrability = rs.getInt(14);
			}

			// now that all the new SQLRelationship objects are set up, add them to their tables
			Iterator it = newKeys.iterator();
			while (it.hasNext()) {
				r = (SQLRelationship) it.next();
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

	public String toString() {
		return "[SQLRelationship: "+getShortDisplayName()
			+", pkTable="+pkTable.getName()
			+", fkTable="+fkTable.getName()
			+"]";
	}

	// ---------------------- SQLRelationship SQLObject support ------------------------

	/**
	 * Returns the table that holds the primary keys (the imported table).
	 */
	public SQLObject getParent() {
		return pkTable;
	}

	public String getName() {
		return fkName;
	}

	/**
	 * Returns the foreign key name.
	 */
	public String getShortDisplayName() {
		return fkName;
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
	}

	/**
	 * Gets the value of fkName
	 *
	 * @return the value of fkName
	 */
	public String getFkName()  {
		return this.fkName;
	}

	/**
	 * Sets the value of fkName
	 *
	 * @param argFkName Value to assign to this.fkName
	 */
	public void setFkName(String argFkName) {
		this.fkName = argFkName;
	}

	/**
	 * Gets the value of pkName
	 *
	 * @return the value of pkName
	 */
	public String getPkName()  {
		return this.pkName;
	}

	/**
	 * Sets the value of pkName
	 *
	 * @param argPkName Value to assign to this.pkName
	 */
	public void setPkName(String argPkName) {
		this.pkName = argPkName;
	}

	public SQLTable getPkTable() {
		return pkTable;
	}

	public void setPkTable(SQLTable pkt) {
		pkTable = pkt;
	}

	public SQLTable getFkTable() {
		return fkTable;
	}

	public void setFkTable(SQLTable fkt) {
		fkTable = fkt;
	}

	// -------------------------- COLUMN MAPPING ------------------------

	protected class ColumnMapping extends SQLObject {
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
			return SQLRelationship.this;
		}
		
		/**
		 * Returns the parent relationship's fk name.
		 */
		public String getName() {
			return SQLRelationship.this.fkName;
		}
		
		/**
		 * Returns the table and column name of the pkColumn.
		 */
		public String getShortDisplayName() {
			return fkColumn.getColumnName()+" - "+
				pkColumn.getParent().getName()+"."+pkColumn.getColumnName();
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
