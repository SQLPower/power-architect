package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class SQLRelationship extends SQLObject implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(SQLRelationship.class);
	
	public static final int ZERO = 1;
	public static final int ONE = 2;
	public static final int MANY = 4;
	public static final int PKCOLUMN = 4;
	public static final int FKCOLUMN = 5;
	
	protected SQLTable pkTable;
	protected SQLTable fkTable;

	protected int updateRule;
	protected int deleteRule;
	protected int deferrability;

	protected int pkCardinality;
	protected int fkCardinality;
	protected boolean identifying;


	protected String physicalName;

	protected RelationshipManager fkColumnManager;

	public SQLRelationship() {
		children = new LinkedList();
		pkCardinality = ONE;
		fkCardinality = ZERO | ONE | MANY;
		fkColumnManager = new RelationshipManager();
	}
	
	/*
	 *  Ideally, loop through until you get a unique column name...
	 */
	private static String generateUniqueColumnName(SQLColumn column, SQLTable table) {
		return column.getParentTable().getName() + "_" + column.getName();  // FIXME: still might not be unique
	}
    
    /**
     * This method is for the benefit of the unit tests.  It should never
     * be necessary to use it in the real world.
     */
    public RelationshipManager getRelationshipManager() {
        return fkColumnManager;
    }
	
	private void attachListeners() throws ArchitectException {
		ArchitectUtils.listenToHierarchy(fkColumnManager,pkTable);
		ArchitectUtils.listenToHierarchy(fkColumnManager,fkTable);
		
	}
	
	private void detachListeners() throws ArchitectException {
		ArchitectUtils.unlistenToHierarchy(fkColumnManager,pkTable);
		ArchitectUtils.unlistenToHierarchy(fkColumnManager,fkTable);
	}
	
	public void attachRelationship(SQLTable pkTable, SQLTable fkTable, boolean autoGenerateMapping) throws ArchitectException {
		if(pkTable == null) throw new NullPointerException("Null pkTable not allowed");
		if(fkTable == null) throw new NullPointerException("Null fkTable not allowed");
		
		SQLTable oldPkt = this.pkTable;
		SQLTable oldFkt = this.fkTable;
		if (this.pkTable != null || this.fkTable != null) {
			this.detachListeners();
		}
		try {
			this.pkTable = pkTable;
			this.fkTable = fkTable;
			
			this.fireDbObjectChanged("pkTable",oldPkt,pkTable);
			this.fireDbObjectChanged("fkTable",oldFkt,fkTable);
		
			fkTable.getColumnsFolder().setMagicEnabled(false);
			fkTable.getImportedKeysFolder().setMagicEnabled(false);
			
			pkTable.addExportedKey(this);
			fkTable.addImportedKey(this);
			if (autoGenerateMapping) {
				// iterate over a copy of pktable's column list to avoid comodification
				// when creating a self-referencing table
				java.util.List<SQLColumn> pkColListCopy = new ArrayList<SQLColumn>(pkTable.getColumns().size());
				pkColListCopy.addAll(pkTable.getColumns());
				
				for (SQLColumn pkCol : pkColListCopy) {
					if (pkCol.getPrimaryKeySeq() == null) break;
					
					SQLColumn fkCol;
					SQLColumn match = fkTable.getColumnByName(pkCol.getName());
					if (match != null) {
						// does the matching column have a compatible data type?
						if (match.getType() == pkCol.getType() &&
								match.getPrecision() == pkCol.getPrecision() &&
								match.getScale() == pkCol.getScale()) {
							// column is an exact match, so we don't have to recreate it
							fkCol = match; 
						} else {
							fkCol = new SQLColumn(pkCol);
							fkCol.setName(generateUniqueColumnName(pkCol,fkTable));
						}
					} else {
						// no match, so we need to import this column from PK table
						fkCol = new SQLColumn(pkCol);
					}
					
					this.addMapping(pkCol, fkCol);
					
				}
			}
		
			realizeMapping();
		
			this.attachListeners();
		} finally {
			if ( fkTable != null ) {
				fkTable.getColumnsFolder().setMagicEnabled(true);
				fkTable.getImportedKeysFolder().setMagicEnabled(true);
			}
		}
	}

	/**
	 * Takes the existing ColumnMapping children of this relationship, and ensures
	 * that the FK Columns exist in the FK Table, and that they are in/out of the FK
	 * table's primary key depending on whether or not this is an identifying relationship.
	 * 
	 * @throws ArchitectException If something goes terribly wrong
	 */
	private void realizeMapping() throws ArchitectException {
		for (ColumnMapping m : getMappings()) {
			SQLColumn fkCol = m.getFkColumn();
			try {
				fkCol.setMagicEnabled(false);
				if (fkCol.getReferenceCount() == 0) fkCol.addReference();
				// This might bump up the reference count (which would be correct)
				fkTable.addColumn(fkCol);
				if (fkCol.getReferenceCount() <= 0) throw new IllegalStateException("Created a column with 0 references!");
				
				if (identifying && fkCol.getPrimaryKeySeq() == null) {
					fkCol.setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
				}
			} finally {
				fkCol.setMagicEnabled(true);
			}
		}
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
		Connection con = null;
		ResultSet rs = null;
		try {
			con = db.getConnection();
			DatabaseMetaData dbmd = con.getMetaData();
			SQLRelationship r = null;
			int currentKeySeq;
			LinkedList newKeys = new LinkedList();
			
			logger.debug("scarch relationship for table:"+table.getCatalogName()+"."+
					table.getSchemaName()+"."+
					table.getName());
			
			rs = dbmd.getImportedKeys(table.getCatalogName(),
									  table.getSchemaName(),
									  table.getName());

			
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
				r.setName(rs.getString(12));
				r.deferrability = rs.getInt(14);
				// FIXME: need to determine if the column is identifying or non-identifying!
			}

			// now that all the new SQLRelationship objects are set up, add them to their tables
			Iterator it = newKeys.iterator();
			while (it.hasNext()) {
				r = (SQLRelationship) it.next();
				r.attachRelationship(r.pkTable,r.fkTable,false);
			}

		} catch (SQLException e) {
			throw new ArchitectException("relationship.populate", e);
		} finally {
			try {
				if (rs != null) rs.close();
			} catch (SQLException e) {
				logger.warn("Couldn't close resultset", e);
			}
			try {
				if (con != null) con.close();
			} catch (SQLException e) {
				logger.warn("Couldn't close connection", e);
			}
		}
	}

	public ColumnMapping getMappingByPkCol(SQLColumn pkcol) {
		for (ColumnMapping m : (List<ColumnMapping>) children) {
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
		for (ColumnMapping m : (List<ColumnMapping>) children) {
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
	 * Convenience method that casts children to List&lt;ColumnMapping&gt;.
	 * 
	 * <p>XXX: should be removed when SQLObject API gets generics 
	 */
	public List<ColumnMapping> getMappings() {
		populate(); // doesn't do anything yet, but better safe than sorry
		return Collections.unmodifiableList(children);
	}
	
	public String printKeyColumns(int keyType) {
		StringBuffer s = new StringBuffer();
		int i = 0;
		for (ColumnMapping cm : (List<ColumnMapping>) children) {
			if ( i++ > 0 )
				s.append(",");
			if ( keyType == PKCOLUMN )
				s.append(cm.getPkColumn().getName());
			else
				s.append(cm.getFkColumn().getName());
		}
		return s.toString();
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
		
		logger.debug("add column mapping: "+pkColumn.getParentTable()+"." +
				pkColumn.getName() + " to " +
				fkColumn.getParentTable()+"."+fkColumn.getName() );
		
		addChild(cmap);
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ------------------ SQLObject Listener ---------------------

	/**
	 * Listens to all activity at and under the pkTable and fkTable.  Updates
	 * and maintains the mapping from pkTable to fkTable, and even removes the
	 * whole relationship when necessary.
	 */
	protected class RelationshipManager implements SQLObjectListener {
		public void dbChildrenInserted(SQLObjectEvent e) {
			
			if (!(e.getSQLSource().isMagicEnabled())){
				logger.debug("Magic disabled ignoring sqlobjectEvent "+e);
				return;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("dbChildrenInserted event! parent="+e.getSource()+";" +
						" children="+Arrays.asList(e.getChildren()));
			}
			try {
				startCompoundEdit("Children Inserted Secondary Effect");
				for (SQLObject so : e.getChildren()) {
					ArchitectUtils.listenToHierarchy(this, so);
				}
				
				if (e.getSQLSource() instanceof SQLTable.Folder) {
					SQLTable.Folder f = (SQLTable.Folder) e.getSource();
					if (f == pkTable.getColumnsFolder()) {
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
				}
			} catch (ArchitectException ex) {
				throw new ArchitectRuntimeException(ex);
			} finally {
				endCompoundEdit("End children inserted handler");
			}
		}

		public void dbChildrenRemoved(SQLObjectEvent e) {
			if (!(e.getSQLSource().isMagicEnabled())){
				logger.debug("Magic disabled ignoring sqlobjectEvent "+e);
				return;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("dbChildrenRemoved event! parent="+e.getSource()+";" +
						" children="+Arrays.asList(e.getChildren()));
			}
			try {
				for (SQLObject so : e.getChildren()) {
					ArchitectUtils.unlistenToHierarchy(this, so);
				}
				if (e.getSQLSource() instanceof SQLTable.Folder) {
					SQLTable.Folder f = (SQLTable.Folder) e.getSource();
					if (f == pkTable.getExportedKeysFolder()) {
						SQLObject[] removedRels = e.getChildren();
						int size = removedRels.length;
						for (int i = 0; i < size; i++) {
							SQLRelationship r = (SQLRelationship) removedRels[i];
							if (r == SQLRelationship.this) {
								try {
									startCompoundEdit("Children removed secondary effect");
									try {
										
										r.getFkTable().getImportedKeysFolder().setMagicEnabled(false);
										r.getFkTable().removeImportedKey(r);
									} finally {
										r.getFkTable().getImportedKeysFolder().setMagicEnabled(true);
									}
									logger.debug("Removing references for mappings: "+getMappings());
									
									for (ColumnMapping cm : r.getMappings()) {
										logger.debug("Removing reference to fkcol "+ cm.getFkColumn());
										try {
											fkTable.getColumnsFolder().setMagicEnabled(false);
											cm.getFkColumn().setMagicEnabled(false);
											cm.getFkColumn().removeReference();
										} finally {
											cm.getFkColumn().setMagicEnabled(true);
											fkTable.getColumnsFolder().setMagicEnabled(true);
										}
									}
								} finally {
									endCompoundEdit("End children removed handler");
									detachListeners();
								}
							}
						}
					} else if (f == pkTable.getColumnsFolder()) {
						SQLObject[] cols = e.getChildren();
						try {
							startCompoundEdit("Remove mapped fk columns");
							for (int i = 0; i < cols.length; i++) {
								SQLColumn col = (SQLColumn) cols[i];
								ensureNotInMapping(col);
							}
						} catch (ArchitectException ex) {
							logger.warn("Couldn't remove mapped FK columns", ex);
						} finally {
							endCompoundEdit("End remove mapped fk columns");
						}
					}
				} 
			} catch (ArchitectException ex) {
				throw new ArchitectRuntimeException(ex);
			}
		}

		public void dbObjectChanged(SQLObjectEvent e) {
			if (!(e.getSQLSource().isMagicEnabled())){
				logger.debug("Magic disabled ignoring sqlobjectEvent "+e);
				return;
			}
			String prop = e.getPropertyName();
			if (logger.isDebugEnabled()) {
				logger.debug("Property changed!" +
						"\n source=" + e.getSource() +
						"\n property=" + prop +
						"\n old=" + e.getOldValue() +
						"\n new=" + e.getNewValue());
			}
			try{
				startCompoundEdit("Object change");
				if (e.getSource() instanceof SQLColumn) {
					SQLColumn col = (SQLColumn) e.getSource();
					
					if (col.getParentTable() == pkTable) {
						if (prop.equals("primaryKeySeq")) {
							try {
								if (col.getPrimaryKeySeq() != null) {
									ensureInMapping(col);
								} else {
									ensureNotInMapping(col);
								}
							} catch (ArchitectException ae) {
								throw new ArchitectRuntimeException(ae);
							}
							return;
						}
						
						ColumnMapping m = getMappingByPkCol(col);
						if (m == null) {
							logger.debug("Ignoring change for column "+col+" parent "+col.getParentTable());
							return;
						}
						if (m.getPkColumn() == null) throw new NullPointerException("Missing pk column in mapping");
						if (m.getFkColumn() == null) throw new NullPointerException("Missing fk column in mapping");
						
						if (prop == null
								|| prop.equals("parent")
								|| prop.equals("remarks")) {
							// don't care
						} else if (prop.equals("sourceColumn")) {
							m.getFkColumn().setSourceColumn(m.getPkColumn().getSourceColumn());
						} else if (prop.equals("name")) {
							// only update the fkcol name if its name was the same as the old pkcol name
							if (m.getFkColumn().getName().equalsIgnoreCase((String) e.getOldValue())) {
								try {
									m.getFkColumn().setMagicEnabled(false);
									m.getFkColumn().setName(m.getPkColumn().getName());
								} finally {
									m.getFkColumn().setMagicEnabled(true);
								}
							}
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
				} else if (e.getSource() == fkTable || e.getSource() == pkTable) {
					if (prop.equals("parent") && e.getNewValue() == null) {
						// this will cause a callback to this listener which removes the imported key from fktable
						pkTable.removeExportedKey(SQLRelationship.this);
					}
				}
			}finally {
				endCompoundEdit("End Object change handler");				
			}
		}

		public void dbStructureChanged(SQLObjectEvent e) {
			if (!(e.getSQLSource().isMagicEnabled())){
				logger.debug("Magic disabled ignoring sqlobjectEvent "+e);
				return;
			}
			logger.debug("Received a dbStructure changed event");
			// wow!  let's re-scan the whole table
			// FIXME: This should also check if this relationship is still part of pktable and fktable, and copy properties from pkcol to fkcol in the mappings
			try {
				startCompoundEdit("Structure Change");
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
			} finally {
				endCompoundEdit("End structure changed handler");
			}
			
		}

		protected void ensureInMapping(SQLColumn pkcol) throws ArchitectException {
			logger.debug("Adding "+pkcol.getParentTable()+"."+pkcol+" to mapping");
			if (!containsPkColumn(pkcol)) {
				SQLColumn fkcol = fkTable.getColumnByName(pkcol.getName());
				if (fkcol == null) fkcol = new SQLColumn(pkcol);
				try {
					fkTable.getColumnsFolder().setMagicEnabled(false);
					fkcol.setMagicEnabled(false);
					fkTable.addColumn(fkcol);
					logger.debug("Is the relationship identifying? "+identifying);
					if (identifying) {
						fkcol.setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
					}
				} finally {
					fkTable.getColumnsFolder().setMagicEnabled(true);
					fkcol.setMagicEnabled(true);
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
			logger.debug("Removing "+pkcol.getParentTable()+"."+pkcol+" from mapping");
			if (containsPkColumn(pkcol)) {
				ColumnMapping m = getMappingByPkCol(pkcol);
				removeChild(m);
				try {
					m.getFkColumn().setMagicEnabled(false);
					m.getFkColumn().removeReference();
				} finally {
					m.getFkColumn().setMagicEnabled(true);
				}
			}
		}
		
		@Override
		public String toString() {
			return "RelManager of "+SQLRelationship.this.toString();
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
		return getName();
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
	public void populate() {
		// nothing to do.
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
		int oldUpdateRule = this.updateRule;
		this.updateRule = argUpdateRule;
		fireDbObjectChanged("updateRule",oldUpdateRule,argUpdateRule);
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
		int oldDeleteRule = this.deleteRule;
		this.deleteRule = argDeleteRule;
		fireDbObjectChanged("deleteRule",oldDeleteRule,argDeleteRule);
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
		int oldDefferability = this.deferrability;
		this.deferrability = argDeferrability;
		fireDbObjectChanged("deferrability",oldDefferability,argDeferrability);
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
		int oldPkCardinality = this.pkCardinality;
		this.pkCardinality = argPkCardinality;
		fireDbObjectChanged("pkCardinality",oldPkCardinality,argPkCardinality);
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
		startCompoundEdit("Modify the Foreign key cardinality");
		int oldFkCardinality = this.fkCardinality;
		this.fkCardinality = argFkCardinality;
		fireDbObjectChanged("fkCardinality",oldFkCardinality,argFkCardinality);
		endCompoundEdit("Modify the Foreign key cardinality");
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
		boolean oldIdentifying = this.identifying;
		if (identifying != argIdentifying) {
			identifying = argIdentifying;
			fireDbObjectChanged("identifying",oldIdentifying,argIdentifying);
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

	public void setPkTable(SQLTable pkt) throws ArchitectException {
		SQLTable oldPkt = pkTable;
		if (pkTable != null) {
			detachListeners();
		}
		pkTable = pkt;
		attachListeners();
		fireDbObjectChanged("pkTable",oldPkt,pkt);
	}

	public SQLTable getFkTable() {
		return fkTable;
	}

	public void setFkTable(SQLTable fkt) throws ArchitectException {
		SQLTable oldFkt = fkTable;
		if (fkTable != null) {
			detachListeners();
			
		}
		fkTable = fkt;
		attachListeners();
		fireDbObjectChanged("fkTable",oldFkt,fkt);
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
			return fkColumn.getName()+" - "+
				pkTableName+"."+pkColumn.getName();
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

		@Override
		public Class<? extends SQLObject> getChildType() {		
			return null;
		}
	
	}

	@Override
	public Class<? extends SQLObject> getChildType() { 
		return null;
	}

	/**
	 * Throws a column locked exception if col is in a columnmapping of this relationship
	 * 
	 * @param col
	 * @throws LockedColumnException
	 */
	public void checkColumnLocked(SQLColumn col) throws LockedColumnException {
		for (SQLRelationship.ColumnMapping cm : getMappings()) {
			if (cm.getFkColumn() == col) {
				throw new LockedColumnException(this);
			}
		}
	}
}
