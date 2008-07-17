/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.sql.CachedRowSet;

/**
 * The SQLRelationship class represents a foriegn key relationship between
 * two SQLTable objects or two groups of columns within the same table.
 */
public class SQLRelationship extends SQLObject implements java.io.Serializable {

    /**
     * Comparator that orders ColumnMapping objects by FK column position.
     */
	public static class ColumnMappingFKColumnOrderComparator implements Comparator<ColumnMapping> {
        public int compare(ColumnMapping o1, ColumnMapping o2) {
            try {
                int fkPos1 = o1.getFkColumn().getParent().getChildren().indexOf(o1.getFkColumn());
                int fkPos2 = o2.getFkColumn().getParent().getChildren().indexOf(o2.getFkColumn());
                if (fkPos1 == fkPos2) return 0;
                if (fkPos1 < fkPos2) return -1;
                return 1;
            } catch (ArchitectException ex) {
                throw new ArchitectRuntimeException(ex);
            }
        }
    }

    private static Logger logger = Logger.getLogger(SQLRelationship.class);

    /**
     * The enumeration of all referential integrity constraint checking
     * policies.
     */
    public static enum Deferrability {
        
        /**
         * Indicates the constrain is deferrable, and checking is deferred by
         * default unless the current transaction has been set for immediate
         * constraint checking.
         */
        INITIALLY_DEFERRED(5),
        
        /**
         * Indicates the constrain is deferrable, and checking is performed
         * immediately unless the current transaction has been set for deferred
         * constraint checking.
         */
        INITIALLY_IMMEDIATE(6),
        
        /**
         * Indicates that the checking for this constraint must always be immediate
         * regardless of the current transaction setting.
         */
        NOT_DEFERRABLE(7);
        
        /**
         * The JDBC code number for this deferrability policy.
         */
        private final int code;
        
        private Deferrability(int code) {
            this.code = code;
        }
        
        /**
         * Returns the enumeration value associated with the given code number.
         * The code numbers are defined in the JDBC specification.
         * 
         * @throws IllegalArgumentException if the given code number is not valid.
         */
        public static Deferrability ruleForCode(int code) {
            for (Deferrability d : values()) {
                if (d.code == code) return d;
            }
            throw new IllegalArgumentException("No such deferrability code " + code);
        }
        
        /**
         * Returns the enumeration value associated with the given code number,
         * or the given default value if the given code number is not valid.
         * This method exists mainly for backward compatibility with old projects
         * where all the deferrability rules were defaulted to 0, which is an
         * invalid code.  New code should normally be written to use {@link #ruleForCode(int)},
         * which throws an exception when asked for an invalid code.
         */
        public static Deferrability ruleForCode(int code, Deferrability defaultValue) {
            for (Deferrability d : values()) {
                if (d.code == code) return d;
            }
            return defaultValue;
        }
        
        /**
         * Returns the JDBC code number for this deferrability rule.
         */
        public int getCode() {
            return code;
        }
    }
    
    /**
     * Enumeration of the various rules allowed for (foreign/imported/child)
     * columns when their parent value is updated or deleted.
     */
    public static enum UpdateDeleteRule {
        
        /**
         * When parent value changes, child value should be modified to
         * match new parent value.
         */
        CASCADE(DatabaseMetaData.importedKeyCascade),
        
        /**
         * Modifying or deleting the parent value should fail if
         * there are child records. This is different from {@link #NO_ACTION}
         * in that the constraint check will not be deferrable on some
         * platforms.
         */
        RESTRICT(DatabaseMetaData.importedKeyRestrict),
        
        /**
         * The child value will be set to SQL NULL if the parent value
         * is modified or deleted.
         */
        SET_NULL(DatabaseMetaData.importedKeySetNull),
        
        /**
         * Modifying or deleting the parent value should fail if
         * there are child records. This is different from {@link #RESTRICT}
         * in that the constraint checking will be deferrable on some platforms.
         * This is the default update and delete rule on most database platforms.
         */
        NO_ACTION(DatabaseMetaData.importedKeyNoAction),
        
        /**
         * Modifying or deleting the parent value should cause the child
         * value to be set to its default.
         */
        SET_DEFAULT(DatabaseMetaData.importedKeySetDefault);
        
        /**
         * The JDBC code for this update/delete rule.
         */
        private final int code;
        
        private UpdateDeleteRule(int code) {
            this.code = code;
        }
        
        /**
         * Returns the update/delete rule associated with the given code number.
         * The code numbers are defined in the JDBC specification.
         * 
         * @throws IllegalArgumentException if the given code number is not valid.
         */
        public static UpdateDeleteRule ruleForCode(int code) {
            for (UpdateDeleteRule r : values()) {
                if (r.code == code) return r;
            }
            throw new IllegalArgumentException("No such update/delete rule code " + code);
        }
        
        /**
         * Returns the JDBC code number for this update/delete rule.
         */
        public int getCode() {
            return code;
        }
    }
    
	public static final int ZERO = 1;
	public static final int ONE = 2;
	public static final int MANY = 4;
	public static final int PKCOLUMN = 4;
	public static final int FKCOLUMN = 5;

	protected SQLTable pkTable;
	protected SQLTable fkTable;

	/**
	 * The rule for what the DBMS should do to the child (imported) key value when its
	 * parent table (exported) key value changes.
	 */
	protected UpdateDeleteRule updateRule = UpdateDeleteRule.NO_ACTION;
	
    /**
     * The rule for what the DBMS should do to the child (imported) key value when its
     * parent table (exported) row is deleted.
     */
	protected UpdateDeleteRule deleteRule = UpdateDeleteRule.NO_ACTION;
    
    /**
     * The deferrability rule for constraint checking on this relationship.
     * Defaults to NOT_DEFERRABLE.
     */
	protected Deferrability deferrability = Deferrability.NOT_DEFERRABLE;

	protected int pkCardinality;
	protected int fkCardinality;
	
	/**
     * Value should be true if this relationship is identifying, and false if
     * otherwise.
     * <p>
     * Here is our definition of identifying relationships and non-identifying
     * relationships (as discussed in the <a
     * href="http://groups.google.com/group/architect-developers/browse_thread/thread/d70e3e3ee3353f1"/>
     * Architect Developer's mailing list</a>).
     * <p>
     * An 'identifying' relationship is: A foreign key relationship in which the
     * whole primary key of the parent table is entirely contained in the
     * primary key of the child table.
     * <p>
     * A 'non-identifying' relationship is: A foreign key relationship in which
     * the whole primary key of the parent table is NOT entirely contained in
     * the primary key of the child table.
     */
	protected boolean identifying;


	protected String physicalName;

	protected RelationshipManager fkColumnManager;

    /**
     * A counter for {@link #setParent()} to decide when to detach listeners.
     */
    private int parentCount;

	public SQLRelationship() {
		children = new LinkedList();
		pkCardinality = ONE;
		fkCardinality = ZERO | ONE | MANY;
		fkColumnManager = new RelationshipManager();
	}

	/**
     * A copy constructor that returns a copy of the provided SQLRelationship
     * with the following properties copied: 
     * <li> Name </li>
     * <li> Identifying status </li>
     * <li> Update rule </li>
     * <li> Delete rule </li>
     * <li> Deferrability </li>
     * 
     * @param relationshipToCopy
     *            The SQLRelationship object to copy
     */
	public SQLRelationship(SQLRelationship relationshipToCopy) throws ArchitectException {
	    this();
        setName(relationshipToCopy.getName());
        setIdentifying(relationshipToCopy.determineIdentifyingStatus());
        setUpdateRule(relationshipToCopy.getUpdateRule());
        setDeleteRule(relationshipToCopy.getDeleteRule());
        setDeferrability(relationshipToCopy.getDeferrability());
	}
	
	/**
	 *  Adds a counter to the end of the default column name until
	 *  it is unique in the given table.
	 */
	private static String generateUniqueColumnName(String colName,
	        SQLTable table) throws ArchitectException {
	    if (table.getColumnByName(colName) == null) return colName;
	    int count = 1;
	    String uniqueName;
	    do {
	        uniqueName = colName + "_" + count; 
	        count++;
	    } while (table.getColumnByName(uniqueName) != null);
	    return uniqueName;
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
        if (pkTable != null)
            ArchitectUtils.unlistenToHierarchy(fkColumnManager,pkTable);
        if (fkTable != null)
            ArchitectUtils.unlistenToHierarchy(fkColumnManager,fkTable);
	}

	/**
     * Associates an {@link SQLRelationship} with the given {@link SQLTable}
     * objects. Also automatically generates the PK to FK column mapping if
     * autoGenerateMapping is set to true.
     * 
     * @param pkTable
     *            The parent table in this relationship.
     * @param fkTable
     *            The child table in this relationship that contains the foreign
     *            key.
     * @param autoGenerateMapping
     *            Automatically generates the PK to FK column mapping if true
     * @throws ArchitectException
     */
	public void attachRelationship(SQLTable pkTable, SQLTable fkTable, boolean autoGenerateMapping) throws ArchitectException {
		if(pkTable == null) throw new NullPointerException("Null pkTable not allowed");
		if(fkTable == null) throw new NullPointerException("Null fkTable not allowed");

		SQLTable oldPkt = this.pkTable;
		SQLTable oldFkt = this.fkTable;

		detachListeners();

		this.pkTable = pkTable;
		this.fkTable = fkTable;

		fireDbObjectChanged("pkTable",oldPkt,pkTable);
		fireDbObjectChanged("fkTable",oldFkt,fkTable);

		try {
		    fkTable.getColumnsFolder().setMagicEnabled(false);
			fkTable.getImportedKeysFolder().setMagicEnabled(false);

			boolean alreadyExists = false;
			
			for (SQLRelationship r : pkTable.getExportedKeys()) {
			    if (r.getFkTable().equals(fkTable)) {
			        alreadyExists = true;
			        break;
			    }
			}
			
			pkTable.addExportedKey(this);
			fkTable.addImportedKey(this);
			if (autoGenerateMapping) {
				// iterate over a copy of pktable's column list to avoid comodification
				// when creating a self-referencing table
				java.util.List<SQLColumn> pkColListCopy = new ArrayList<SQLColumn>(pkTable.getColumns().size());
				pkColListCopy.addAll(pkTable.getColumns());

				for (SQLColumn pkCol : pkColListCopy) {
					if (pkCol.getPrimaryKeySeq() == null) break;

					SQLColumn match = fkTable.getColumnByName(pkCol.getName());
					SQLColumn fkCol = new SQLColumn(pkCol);
					fkCol.setPrimaryKeySeq(null);
                    if (pkTable == fkTable) {
                        // self-reference should never hijack the PK!
                        String colName = "Parent_" + fkCol.getName();
                        fkCol.setName(generateUniqueColumnName(colName, fkTable));
                        setIdentifying(false);
                    } else if (match == null) { 
                        // no match, so we need to import this column from PK table
                        fkCol.setName(generateUniqueColumnName(pkCol.getName(),fkTable));
                    } else {
						// does the matching column have a compatible data type?
						if (!alreadyExists && match.getType() == pkCol.getType() &&
								match.getPrecision() == pkCol.getPrecision() &&
								match.getScale() == pkCol.getScale()) {
							// column is an exact match, so we don't have to recreate it
							fkCol = match;
						} else {
						    String colName = pkCol.getParentTable().getName() + "_" + pkCol.getName();
							fkCol.setName(generateUniqueColumnName(colName,fkTable));
						}
                    }
					this.addMapping(pkCol, fkCol);

				}
			}

			realizeMapping();

            // normally, it wouldn't hurt to normalize anyway, but in order to remain
            // backward compatible with older project files that don't have an indicesFolder
            // for each table, we will only do this when asked to auto-generate mappings.
            // If we ever have time (or more likely, it turns out that there are cases where
            // normalizing the PK is required even when autoGenerateMapping is false), we
            // could modify the SwingUIProject to fix up old tables with no PK folder as soon
            // as they're created, then this normalize could happen unconditionally.
            if (autoGenerateMapping) {
                fkTable.normalizePrimaryKey();
            }
            
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
            if (logger.isDebugEnabled()) {
                logger.debug("realizeMapping: processing " + m);
            }
            SQLColumn fkCol = m.getFkColumn();
            try {
                fkCol.setMagicEnabled(false);
                if (fkCol.getReferenceCount() == 0)
                    fkCol.addReference();

                // since we turned magic off, we have to insert the PK cols in
                // the correct position
                int insertIdx;
                if (identifying) {
                    if (fkCol.getPrimaryKeySeq() == null) {
                        logger.debug("realizeMapping: fkCol PK seq is null. Inserting at end of PK.");
                        insertIdx = fkTable.getPkSize();
                    } else {
                        logger.debug("realizeMapping: using existing fkCol PK seq " + fkCol.getPrimaryKeySeq());
                        insertIdx = fkCol.getPrimaryKeySeq();
                    }
                } else {
                    if (fkCol.getPrimaryKeySeq() != null) {
                        insertIdx = fkCol.getPrimaryKeySeq();
                    } else {
                        insertIdx = fkTable.getColumns().size();
                    }
                }
                
                fkCol.setAutoIncrement(false);

                // This might bump up the reference count (which would be
                // correct)
                fkTable.addColumn(insertIdx, fkCol);
                logger.debug("realizeMapping: Added column '" + fkCol.getName() + "' at index " + insertIdx);
                if (fkCol.getReferenceCount() <= 0)
                    throw new IllegalStateException("Created a column with 0 references!");

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
		CachedRowSet crs = null;
		DatabaseMetaData dbmd = null;
		try {
			con = db.getConnection();
			dbmd = con.getMetaData();
			crs = new CachedRowSet();
	        crs.populate(dbmd.getImportedKeys(table.getCatalogName(),
                     table.getSchemaName(),
                     table.getName()));
		} catch (SQLException e) {
		    throw new ArchitectException("relationship.populate", e);
		} finally {
			// close the connection before it makes the recursive call
            // that could lead to opening more connections
		    try {
		        if (con != null) con.close();
		    } catch (SQLException e) {
		        logger.warn("Couldn't close connection", e);
		    }
		}
		try {
			SQLRelationship r = null;
			int currentKeySeq;
			LinkedList newKeys = new LinkedList();

			logger.debug("search relationship for table:"+table.getCatalogName()+"."+
					table.getSchemaName()+"."+
					table.getName());




			while (crs.next()) {
				currentKeySeq = crs.getInt(9);
				if (currentKeySeq == 1) {
					r = new SQLRelationship();
					newKeys.add(r);
				}
				ColumnMapping m = new ColumnMapping();
				m.parent = r;
				r.children.add(m);
				r.pkTable = db.getTableByName(crs.getString(1),  // catalog
											  crs.getString(2),  // schema
											  crs.getString(3)); // table
				if (r.pkTable == null) {
				    logger.error("addImportedRelationshipsToTable: Couldn't find exporting table "
				            +crs.getString(1)+"."+crs.getString(2)+"."+crs.getString(3)
				            +" in target database!");
				    continue;
				}

				logger.debug("Looking for pk column '"+crs.getString(4)+"' in table '"+r.pkTable+"'");
				m.pkColumn = r.pkTable.getColumnByName(crs.getString(4));
				if (m.pkColumn == null) {
					throw new ArchitectException("relationship.populate.nullPkColumn");
				}

				r.fkTable = db.getTableByName(crs.getString(5),  // catalog
											  crs.getString(6),  // schema
											  crs.getString(7)); // table
				if (r.fkTable != table) {
					throw new IllegalStateException("fkTable did not match requested table");
				}
				m.fkColumn = r.fkTable.getColumnByName(crs.getString(8));
				if (m.fkColumn == null) {
					throw new ArchitectException("relationship.populate.nullFkColumn");
				}
				// column 9 (currentKeySeq) handled above
				r.updateRule = UpdateDeleteRule.ruleForCode(crs.getInt(10));
				r.deleteRule = UpdateDeleteRule.ruleForCode(crs.getInt(11));
				r.setName(crs.getString(12));
                try {
                    r.deferrability = Deferrability.ruleForCode(crs.getInt(14));
                } catch (IllegalArgumentException ex) {
                    logger.warn("Invalid code when reverse engineering" +
                            " relationship. Defaulting to NOT_DEFERRABLE.", ex);
                    r.deferrability = Deferrability.NOT_DEFERRABLE;
                }
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
				if (crs != null) crs.close();
			} catch (SQLException e) {
				logger.warn("Couldn't close resultset", e);
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
				logger.debug("Magic disabled; ignoring children inserted event "+e);
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
				logger.debug("Magic disabled; ignoring children removed event "+e);
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
									r.getFkTable().removeImportedKey(r);
									logger.debug("Removing references for mappings: "+getMappings());

                                    // references to fk columns are removed in reverse order in case
                                    // this relationship is reconnected in the future. (if not removed
                                    // in reverse order, the PK sequence numbers will change as each
                                    // mapping is removed and the subsequent column indexes shift down)
                                    List<ColumnMapping> mappings = new ArrayList<ColumnMapping>(r.getMappings());
                                    Collections.sort(mappings, Collections.reverseOrder(new ColumnMappingFKColumnOrderComparator()));
									for (ColumnMapping cm : mappings) {
										logger.debug("Removing reference to fkcol "+ cm.getFkColumn());
										cm.getFkColumn().removeReference();
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
				logger.debug("Magic disabled; ignoring sqlobject changed event "+e);
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
								|| prop.equals("remarks")
								|| prop.equals("autoIncrement")) {
							// don't care
						} else if (prop.equals("sourceColumn")) {
							m.getFkColumn().setSourceColumn(m.getPkColumn().getSourceColumn());
						} else if (prop.equals("name")) {
							// only update the fkcol name if its name was the same as the old pkcol name
							if (m.getFkColumn().getName().equalsIgnoreCase((String) e.getOldValue())) {
							    m.getFkColumn().setName(m.getPkColumn().getName());
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
			} finally {
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

        // XXX this code serves essentially the same purpose as the loop in realizeMapping().
        //     We should refactor that method to use this one as a subroutine, and at that
        //     time, ensure the special cases in both places are preserved.
        //     (if there is a special case in there that's not here, it's probably a bug)
		protected void ensureInMapping(SQLColumn pkcol) throws ArchitectException {
		    if (!containsPkColumn(pkcol)) {
		        if (logger.isDebugEnabled()) {
		            logger.debug("ensureInMapping("+getName()+"): Adding "
		                    +pkcol.getParentTable().getName()+"."+pkcol.getName()
		                    +" to mapping");
		        }
                
                SQLColumn fkcol;
		        if (pkcol.getParentTable().equals(fkTable)) {
                    // self-reference! must create new column!
                    fkcol = new SQLColumn(pkcol);
                    fkcol.setName(generateUniqueColumnName("Parent_"+pkcol.getName(), pkcol.getParentTable()));
                } else {
                    fkcol = fkTable.getColumnByName(pkcol.getName());
                    if (fkcol == null) fkcol = new SQLColumn(pkcol);
                }
                
                // this either adds the new column or bumps up the refcount on existing col
		        fkTable.addColumn(fkcol);
                
		        if (identifying && pkTable != fkTable) {
		            fkcol.setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
		        } else {
		            // XXX might only want to do this if fkcol was newly created
		            fkcol.setPrimaryKeySeq(null);
		        }
		        logger.debug("ensureInMapping("+getName()+"): added fkcol at pkSeq "+fkcol.getPrimaryKeySeq());
		        fkcol.setAutoIncrement(false);
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
                    // XXX no magic here? this is suspect
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
     * <p>
     * XXX this should return the parent folder of the pk table!
	 */
	public SQLObject getParent() {
		return pkTable.getExportedKeysFolder();
	}

	/**
	 * This method is useful, and has side effects.
	 *
     * setParent detaches the relation manager if newParent is null
     * and reattaches the relation manager if newParent is not null
     *
	 * @param newParent If this is the same as pkTable or fkTable,
	 * this method returns normally.  Otherwise, this method throws
	 * IllegalArgumentException.
	 * @throws ArchitectException
	 * @throws IllegalArgumentException if newParent is anything other
	 * than this relationship's pkTable.exportedKeysFolder or
	 * fkTable.importedKeysFolder
	 */
	protected void setParent(SQLObject newParent) {
	    logger.info("Setting parent of " + this + " to "+ newParent);
	    try {
	        if (newParent == null) {
                if (--parentCount == 0) {
                    detachListeners();
                }
	        } else if ( (pkTable != null && newParent != pkTable.exportedKeysFolder)
	                && (fkTable != null && newParent != fkTable.importedKeysFolder)) {
	            throw new IllegalArgumentException
	            ("You can't change the parent of a SQLRelationship this way");
	        } else {
	            attachListeners();
                parentCount++;
	        }
	    }catch (ArchitectException ae) {
	        throw new ArchitectRuntimeException(ae);
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

	public UpdateDeleteRule getUpdateRule()  {
		return this.updateRule;
	}

	public void setUpdateRule(UpdateDeleteRule rule) {
	    UpdateDeleteRule oldRule = updateRule;
	    updateRule = rule;
		fireDbObjectChanged("updateRule", oldRule, rule);
	}

	public UpdateDeleteRule getDeleteRule()  {
		return this.deleteRule;
	}

	public void setDeleteRule(UpdateDeleteRule rule) {
        UpdateDeleteRule oldRule = deleteRule;
        deleteRule = rule;
        fireDbObjectChanged("deleteRule", oldRule, rule);
	}

	public Deferrability getDeferrability()  {
		return this.deferrability;
	}

	public void setDeferrability(Deferrability argDeferrability) {
        if (argDeferrability == null) {
            throw new NullPointerException("Deferrability policy must not be null");
        }
        Deferrability oldDefferability = this.deferrability;
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
				throw new LockedColumnException(this,col);
			}
		}
	}
	
	/**
     * Some SQLRelationship objects may not have their {@link #identifying}
     * property set properly which is particularly the case then creating
     * SQLRelationships for source database objects and then reverse
     * engineering, so this method will determine for certain if a relationship
     * is identifying or non-identifying. This is currently primarily being used
     * for determining the identifying status of reverse-engineered
     * relationships.
     * 
     * @return True if this SQLRelationship is identifying. False if it is
     *         non-identifying.
     */
	public boolean determineIdentifyingStatus() throws ArchitectException {
	    
	    if (getPkTable().getPkSize() > getFkTable().getPkSize()) return false;
	    
	    List<ColumnMapping> columnMappings = (List<ColumnMapping>)getChildren();
	    SQLIndex pkTablePKIndex = getPkTable().getPrimaryKeyIndex();
	    if (pkTablePKIndex == null) return false;
	    List<Column> pkColumns = pkTablePKIndex.getChildren();
	    
	    for (Column col: pkColumns) {
	        boolean colIsInFKTablePK = false;
	        for (ColumnMapping mapping: columnMappings) {
	            if (mapping.getPkColumn().equals(col.getColumn()) &&
	                    mapping.getFkColumn().isPrimaryKey()) { 
                    colIsInFKTablePK = true;
                    break;
                }
	        }
	        if (colIsInFKTablePK == false) return false;
	    }
	    return true;
	}
}
