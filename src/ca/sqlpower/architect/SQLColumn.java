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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SQL;

public class SQLColumn extends SQLObject implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(SQLColumn.class);

	// *** REMEMBER *** update the copyProperties method if you add new properties!

	/**
	 * Refers back to the real database-connected SQLColumn that this
	 * column was originally derived from.
	 */
	protected SQLColumn sourceColumn;

	protected SQLObject parent;

	/**
	 * Must be a type defined in java.sql.Types.  Move to enum in 1.5
	 * (we hope!).
	 */
	protected int type;

	/**
	 * This is the native name for the column's type in its source
	 * database.  See {@link #type} for system-independant type.
	 */
	private String sourceDataTypeName;

	/*
	 * These were mixed up originally...
	 * 
	 * Some random ideas:
	 * 
	 * 1. hasPrecision and hasScale might be useful here.  They are currently part of 
	 * the GenericTypeDescriptor.  Unfortunately, it is not consulted when the screen
	 * tries to paint itself...
	 * 
	 * 2. nativePrecision and nativeScale might be useful to keep just in case users want
	 * to forward engineer into the same target database as the source.
	 */
    
    /**
     * The maximum length of the field in digits or characters. For numeric types,
     * this value includes all significant digits on both sides of the decimal point.
     */
	protected int precision;
    
    /**
     * The maximum number of digits after the decimal point. For non-numeric data types,
     * this value should be set to 0.
     */
	protected int scale;
	
	/**
	 * This column's nullability type.  One of:
	 * <ul><li>DatabaseMetaData.columnNoNulls - might not allow NULL values
	 *     <li>DatabaseMetaData.columnNullable - definitely allows NULL values
	 *     <li>DatabaseMetaData.columnNullableUnknown - nullability unknown
	 * </ul>
	 */
	protected int nullable;
	// set to empty string so that we don't generate spurious undos
	protected String remarks ="";
	protected String defaultValue;
	
	/**
     * This property is the sort key for this column in primary key index. If
     * the value is null, then it is not a primary key column.
     */
	protected Integer primaryKeySeq;
    
    /**
     * This property indicates that values stored in this column should
     * default to some automatcially-incrementing sequence of values.  Every
     * database platform handles the specifics of this a little differently,
     * but the DDL generators are responsible for taking care of that.
     */
	protected boolean autoIncrement;
    
    /**
     * This property is a hint to the DDL generators to tell them what name
     * to give to the database sequence that generates values for this column.
     * Not all platforms need (or support) sequences, so setting this value
     * doesn't guarantee it will be used.  If the value of this field is left
     * null, the getter method will auto-generate a sequence name based on
     * the table and column names.
     */
    private String autoIncrementSequenceName;

	// *** REMEMBER *** update the copyProperties method if you add new properties!

	
	/**
	 * referenceCount is meant to keep track of how many containers (i.e. 
	 * folders and relationships) have a reference to a column.  A new 
	 * SQLColumn always starts off life with a reference count of 1. (it
	 * is set in the constructors).
	 * 
	 * When creating a new relationship which reuses a column, the 
	 * call addReference() on the column to increment the referenceCount.
	 * 
	 * When removing a relationship, call removeReference() on the column to
	 * decrement the referenceCount.  If the referenceCount falls to zero, it 
	 * removes itself from the containing table (because it imported by 
	 * the creation of a relationship.    
	 */
	protected int referenceCount;
	
	public SQLColumn() {
		logger.debug("NEW COLUMN (noargs) @"+hashCode());
		setName("New_Column");
		type = Types.INTEGER;		
		// scale = 10;
		precision = 10;
		nullable = DatabaseMetaData.columnNoNulls;
		autoIncrement = false;
		logger.debug("SQLColumn() set ref count to 1");
		referenceCount = 1;
		children = Collections.EMPTY_LIST;
	}

	/**
	 * Constructs a SQLColumn that will be a part of the given SQLTable.
	 *
	 * @param parentTable The table that this column will think it belongs to.
	 * @param colName This column's name.
	 * @param dataType The number that represents this column's type. See java.sql.Types.
	 * @param nativeType The type as it is called in the source database.
	 * @param scale The length of this column.  Size is type-dependant.
	 * @param precision The number of places of precision after the decimal place for numeric types.
	 * @param nullable This column's nullability.  One of:
	 * <ul><li>DatabaseMetaData.columnNoNulls - might not allow NULL values
	 *     <li>DatabaseMetaData.columnNullable - definitely allows NULL values
	 *     <li>DatabaseMetaData.columnNullableUnknown - nullability unknown
	 * </ul>
	 * @param remarks User-defined remarks about this column
	 * @param defaultValue The value this column will have if another value is not specified.
	 * @param primaryKeySeq This column's position in the table's primary key.  Null if it is not in the PK.
	 * @param isAutoIncrement Does this column auto-increment?
	 */
	public SQLColumn(SQLTable parentTable,
					 String colName,
					 int dataType,
					 String nativeType,
					 int precision,
					 int scale,
					 int nullable,
					 String remarks,
					 String defaultValue,
					 Integer primaryKeySeq,
					 boolean isAutoIncrement) {
		if (parentTable != null) {
			logger.debug("NEW COLUMN "+colName+"@"+hashCode()+" parent "+parentTable.getName()+"@"+parentTable.hashCode());
		} else {
			logger.debug("NEW COLUMN "+colName+"@"+hashCode()+" (null parent)");
		}
        if (parentTable != null) {
            this.parent = parentTable.getColumnsFolder();
        }
		this.setName(colName);
		this.type = dataType;
		this.sourceDataTypeName = nativeType;
		this.scale = scale;
		this.precision = precision;
		this.nullable = nullable;
		this.remarks = remarks;
		this.defaultValue = defaultValue;
		this.primaryKeySeq = primaryKeySeq;
		this.autoIncrement = isAutoIncrement;

		this.children = Collections.EMPTY_LIST;
		logger.debug("SQLColumn(.....) set ref count to 1");
		this.referenceCount = 1;
	}

	public SQLColumn(SQLTable parent, String colName, int type, int precision, int scale) {
		this(parent, colName, type, null, precision, scale, DatabaseMetaData.columnNullable, null, null, null, false);
	}
	
	/**
	 * Creates a reasonable facsimile of the given column.
	 */
	public SQLColumn(SQLColumn col) {
		super();
		children = Collections.EMPTY_LIST;
		copyProperties(this, col);
		logger.debug("SQLColumn(SQLColumn col ["+col+" "+col.hashCode()+"]) set ref count to 1");
		referenceCount = 1;
	}
	
	/**
	 * Makes a near clone of the given source column.  The new column
	 * you get back will have a parent pointer of addTo.columnsFolder,
	 * but will not be attached as a child (you will normally do that
	 * right after calling this).  It will refer to source as its
	 * sourceColumn property, and otherwise be identical to source.
	 * 
     * getDerivedInstance is used for reverse engineering.  
     * Will not preserve listeners.
	 * 
	 */
	public static SQLColumn getDerivedInstance(SQLColumn source, SQLTable addTo) {
		logger.debug("derived instance SQLColumn constructor invocation.");
		SQLColumn c = new SQLColumn();
		copyProperties(c, source);
		c.sourceColumn = source;
		c.parent = addTo.getColumnsFolder();
		logger.debug("getDerivedInstance set ref count to 1");
		c.referenceCount = 1;
		return c;
	}

	/**
	 * Copies all the interesting properties of source into target.  This is a subroutine of
	 * the copy constructor, and of getDerivedInstance, which is for importing tables from
	 * source databases.
	 * 
	 * @param target The instance to copy properties into.
	 * @param source The instance to copy properties from.
	 */
	private static final void copyProperties(SQLColumn target, SQLColumn source) {
		target.setName(source.getName());
		target.type = source.type;
		target.sourceDataTypeName = source.sourceDataTypeName;
		target.setPhysicalName(source.getPhysicalName());
		target.precision = source.precision;
		target.scale = source.scale;
		target.nullable = source.nullable;
		target.remarks = source.remarks;
		target.defaultValue = source.defaultValue;
		target.primaryKeySeq = source.primaryKeySeq;
		target.autoIncrement = source.autoIncrement;
        target.autoIncrementSequenceName = source.autoIncrementSequenceName;
	}

	/**
	 * Mainly for use by SQLTable's populate method.  Does not cause
	 * SQLObjectEvents to avoid infinite recursion, so you have to
	 * generate them yourself at a safe time.
	 */
	static void addColumnsToTable(SQLTable addTo,
										 String catalog,
										 String schema,
										 String tableName) 
		throws SQLException, DuplicateColumnException, ArchitectException {
		Connection con = null;
		ResultSet rs = null;
		ResultSet typeRs = null;
		DatabaseMetaData dbmd = null;

		try {
		    con = addTo.getParentDatabase().getConnection();
		    dbmd = con.getMetaData();
		    typeRs = con.getMetaData().getTypeInfo();
		    Map<Double, Double> typeToMaxPrecisionMap = new HashMap<Double, Double>();
            while (typeRs.next()) {
                typeToMaxPrecisionMap.put(new Double(typeRs.getDouble(2)), new Double(typeRs.getDouble(3)));
            }
		    
			logger.debug("SQLColumn.addColumnsToTable: catalog="+catalog+"; schema="+schema+"; tableName="+tableName);
			rs = dbmd.getColumns(catalog, schema, tableName, "%");
			
			int autoIncCol = SQL.findColumnIndex(rs, "is_autoincrement");
			logger.debug("Auto-increment info column: " + autoIncCol);
			
			while (rs.next()) {
				logger.debug("addColumnsToTable SQLColumn constructor invocation.");
				
				//Must check precision from the column against the database allowed precision
				//as some jdbc drivers returns a display precision and not an allowed database
				//precision.
				
				int precision = rs.getInt(7);
				Double typePrecision = typeToMaxPrecisionMap.get(new Double(rs.getInt(5)));
				if (typePrecision != null && (rs.getInt(7) < 1 || rs.getInt(7) > typePrecision.intValue())) { //equate across java.sql.Types
				    precision = typePrecision.intValue();
				}
				
				boolean autoIncrement;
                if (autoIncCol > 0) {
                    autoIncrement = "yes".equalsIgnoreCase(rs.getString(autoIncCol));
                } else {
                    autoIncrement = false;
                }
				
				SQLColumn col = new SQLColumn(addTo,
											  rs.getString(4),  // col name
											  rs.getInt(5), // data type (from java.sql.Types)
											  rs.getString(6), // native type name
											  precision, // column size (precision)
											  rs.getInt(9), // decimal size (scale)
											  rs.getInt(11), // nullable
											  rs.getString(12) == null ? "" : rs.getString(12), // remarks
											  rs.getString(13), // default value
											  null, // primaryKeySeq
											  autoIncrement // isAutoIncrement
											  );
				logger.debug("Precision for the column " + rs.getString(4) + " is " + rs.getInt(7));

				// work around oracle 8i bug: when table names are long and similar,
				// getColumns() sometimes returns columns from multiple tables!
				String dbTableName = rs.getString(3);
				if (dbTableName != null) {
					if (!dbTableName.equalsIgnoreCase(tableName)) {
						logger.warn("Got column "+col.getName()+" from "+dbTableName
									+" in metadata for "+tableName+"; not adding this column.");
						continue;
					}
				} else {
					logger.warn("Table name not specified in metadata.  Continuing anyway...");
				}

				logger.debug("Adding column "+col.getName());
				
				if (addTo.getColumnByName(col.getName(), false, true) != null) {
					throw new DuplicateColumnException(addTo, col.getName());
				}
				
				addTo.columnsFolder.children.add(col); // don't use addTo.columnsFolder.addColumn() (avoids multiple SQLObjectEvents)

			}

		} finally {
		    try {
                if (typeRs != null) typeRs.close();
            } catch (SQLException ex) {
                logger.error("Couldn't close result set", ex);
            }
			try {
				if (rs != null) rs.close();
			} catch (SQLException ex) {
				logger.error("Couldn't close result set", ex);
			}
			try {
			    if (con != null) con.close();
			} catch (SQLException ex) {
			    logger.error("Couldn't close connection", ex);
			}
		}
	}

	/**
	 * A comparator for SQLColumns that only pays attention to the
	 * column names.  For example, if <code>column1</code> has
	 * <code>name = "MY_COLUMN"</code> and <code>type =
	 * VARCHAR(20)</code> and <code>column2</code> has <code>name =
	 * "MY_COLUMN"</code> and type <code>VARCHAR(4)</code>,
	 * <code>compare(column1, column2)</code> will return 0.
	 */
	public static class ColumnNameComparator implements Comparator {
		/**
		 * Forwards to {@link #compare(SQLColumn,SQLColumn)}.
		 *
		 * @throws ClassCastException if o1 or o2 is not of class SQLColumn.
		 */
		public int compare(Object o1, Object o2) {
			return compare((SQLColumn) o1, (SQLColumn) o2);
		}

		/**
		 * See class description for behaviour of this method.
		 */
		public int compare(SQLColumn c1, SQLColumn c2) {
			return c1.getName().compareTo(c2.getName());
		}
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ------------------------- SQLObject support -------------------------

	public void populate() throws ArchitectException {
		// SQLColumn: populate is a no-op
	}

	
	public boolean isPopulated() {
		return true;
	}

	public String getShortDisplayName() {
		if (sourceDataTypeName != null) {
			if (precision > 0 && scale > 0) {
				return getName()+": "+sourceDataTypeName+"("+precision+","+scale+")";
			} else if (precision > 0) {
				return  getName()+": "+sourceDataTypeName+"("+precision+")"; // XXX: should we display stuff like (18,0) for decimals?
			} else {
				return  getName()+": "+sourceDataTypeName; 				
			}			
		} else {
			return  getName()+": "
				+SQLType.getTypeName(type) // XXX: replace with TypeDescriptor
				+"("+precision+")";
		}
	}

	public boolean allowsChildren() {
		return false;
	}

	public SQLObject getParent()  {
		return this.parent;
	}	

	// ------------------------- accessors and mutators --------------------------

	public SQLColumn getSourceColumn() {
		return sourceColumn;
	}

	public void setSourceColumn(SQLColumn col) {
		SQLColumn oldCol = this.sourceColumn;
		sourceColumn = col;
		fireDbObjectChanged("sourceColumn",oldCol,col);
	}


	/**
	 * Gets the value of type
	 *
	 * @return the value of type
	 */
	public int getType()  {
		return this.type;
	}

	/**
	 * Sets the value of type
	 *
	 * @param argType Value to assign to this.type
	 */
	public void setType(int argType) {
		int oldType = type;
		this.type = argType;
        startCompoundEdit("Type change");
        setSourceDataTypeName(null);
		fireDbObjectChanged("type",oldType,argType);
        endCompoundEdit("Type change");
	}

	public String getSourceDataTypeName() {
		return sourceDataTypeName;
	}

	public void setSourceDataTypeName(String n) {
		String oldSourceDataTypeName =  sourceDataTypeName;
		sourceDataTypeName = n;
		fireDbObjectChanged("sourceDataTypeName",oldSourceDataTypeName,n);
	}

	/**
	 * Gets the value of scale
	 *
	 * @return the value of scale
	 */
	public int getScale()  {
		return this.scale;
	}

	/**
	 * Sets the value of scale
	 *
	 * @param argScale Value to assign to this.scale
	 */
	public void setScale(int argScale) {
		int oldScale = this.scale;
		logger.debug("scale changed from "+scale+" to "+argScale);
		this.scale = argScale;
		fireDbObjectChanged("scale",oldScale,argScale);
	}

	/**
	 * Gets the value of precision
	 *
	 * @return the value of precision
	 */
	public int getPrecision()  {
		return this.precision;
	}

	/**
	 * Sets the value of precision
	 *
	 * @param argPrecision Value to assign to this.precision
	 */
	public void setPrecision(int argPrecision) {
		int oldPrecision = this.precision;
		this.precision = argPrecision;
		fireDbObjectChanged("precision",oldPrecision,argPrecision);
	}

	/**
	 * Figures out this column's nullability
	 *
	 * @return true iff this.nullable == DatabaseMetaData.columnNullable.
	 */
	public boolean isDefinitelyNullable()  {
		return this.nullable == DatabaseMetaData.columnNullable;
	}

	/**
	 * Is primaryKeySeq defined
	 *
	 * @return whether or not primaryKeySeq is defined
	 */
	public boolean isPrimaryKey()  {
		return this.primaryKeySeq != null;
	}

	/**
	 * Indicates whether this column is a foreign key
	 * 
	 * @return whether this column exists as a foreign key column in any of
	 * its parent table's imported keys
	 */
	public boolean isForeignKey() {
	    if (getParentTable() == null) return false;
	    try {
	        for (SQLRelationship r : getParentTable().getImportedKeys()) {
	            if (r.containsFkColumn(this)) {
	                return true;
	            }
	        }
	        return false;
	    } catch (ArchitectException ex) {
	        throw new RuntimeException(ex);
	    }
	}
	
	/**
	 * Returns whether this column is in an index 
	 */
	public boolean isIndexed() {
	    if (getParentTable() == null) return false;
	    try {
	        for (SQLIndex ind : getParentTable().getIndices()) {
	            for (SQLIndex.Column col : ind.getChildren()) {
	                if (this.equals(col.getColumn())) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    } catch (ArchitectException ex) {
	        throw new RuntimeException(ex);
        }
	}

	/**
	 * Returns whether this column is in an unique index.
	 */
	public boolean isUniqueIndexed() {
	    if (getParentTable() == null) return false;
	    try {
	        for (SQLIndex ind : getParentTable().getIndices()) {
	            if (!ind.isUnique()) continue;
	            for (SQLIndex.Column col : ind.getChildren()) {
	                if (this.equals(col.getColumn())) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    } catch (ArchitectException ex) {
	        throw new RuntimeException(ex);
	    }
	}

	/**
	 * Returns the parent SQLTable object, which is actually a grandparent.
	 */
	public SQLTable getParentTable() {
		if (parent == null) return null;
		else return (SQLTable) parent.getParent();
	}

	/**
	 * Sets the value of parent
	 *
	 * @param argParent Value to assign to this.parent
	 */
	protected void setParent(SQLObject argParent) {
		SQLObject oldParent = this.parent;
		this.parent = argParent;
		fireDbObjectChanged("parent",oldParent,argParent);
	}

	public int getNullable() {
		return nullable;
	}

	/**
	 * Sets the value of nullable
	 *
	 * @param argNullable Value to assign to this.nullable
	 */
	public void setNullable(int argNullable) {
		int oldNullable = this.nullable;
		logger.debug("Changing nullable "+oldNullable+" -> "+argNullable);
		this.nullable = argNullable;
		fireDbObjectChanged("nullable",oldNullable,argNullable);
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
		String oldRemarks = this.remarks;
		this.remarks = argRemarks;
		fireDbObjectChanged("remarks",oldRemarks,argRemarks);
	}

	/**
	 * Gets the value of defaultValue
	 *
	 * @return the value of defaultValue
	 */
	public String getDefaultValue()  {
		return this.defaultValue;
	}

	/**
	 * Sets the value of defaultValue
	 *
	 * @param argDefaultValue Value to assign to this.defaultValue
	 */
	public void setDefaultValue(String argDefaultValue) {
		String oldDefaultValue = this.defaultValue;
		this.defaultValue = argDefaultValue;
		fireDbObjectChanged("defaultValue",oldDefaultValue,argDefaultValue);
	}

	/**
	 * Gets the value of primaryKeySeq
	 *
	 * @return the value of primaryKeySeq
	 */
	public Integer getPrimaryKeySeq()  {
		return this.primaryKeySeq;
	}

    /**
     * Sets the value of primaryKeySeq, and moves the column to the appropriate location in the
     * parent table's column folder.  However, if magic is disabled on this column, this method
     * simply sets the PrimaryKeySeq property to the given value, fires the change event, and
     * returns without trying to re-order the columns. 
     * 
     * If there is no primary key on this column's table it will create a new key
     * with default values.
     */
	public void setPrimaryKeySeq(Integer argPrimaryKeySeq) {
	    // do nothing if there's no change
	    if ( (primaryKeySeq == null && argPrimaryKeySeq == null) ||
	         (primaryKeySeq != null && primaryKeySeq.equals(argPrimaryKeySeq)) ) {
            return;
        }

        Integer oldPrimaryKeySeq = primaryKeySeq;
        //The check for setNullable() is not moved out is because it is
        //needed to be part of the compound edit if isMagicEnabled is true
        
        if (!isMagicEnabled()) {
            if (argPrimaryKeySeq != null && !this.autoIncrement) {
                setNullable(DatabaseMetaData.columnNoNulls);    
            }
            this.primaryKeySeq = argPrimaryKeySeq;
            fireDbObjectChanged("primaryKeySeq",oldPrimaryKeySeq,argPrimaryKeySeq);
        } else try {
            startCompoundEdit("Starting PrimaryKeySeq compound edit");
 
	        if (argPrimaryKeySeq != null && !this.autoIncrement) {
	            setNullable(DatabaseMetaData.columnNoNulls);	
	        }
            
	        // consider delaying this event until after the column has been put in place,
            // because firing the event at this point causes relationship managers to update the
            // child's PK before the parent's PK is properly formed
            // (such a change would require thorough testing of course!)
            this.primaryKeySeq = argPrimaryKeySeq;
            fireDbObjectChanged("primaryKeySeq",oldPrimaryKeySeq,argPrimaryKeySeq);

            SQLObject p = parent;
            if (p != null) {
                try {
                    p.setMagicEnabled(false);
                    p.removeChild(this);
                    int idx = 0;
                    int targetPKS = primaryKeySeq == null ? Integer.MAX_VALUE : primaryKeySeq.intValue();
                    logger.debug("Parent = "+p);
                    logger.debug("Parent.children = "+p.children);
                    for (SQLColumn col : (List<SQLColumn>) p.children) {
                        if (col.getPrimaryKeySeq() == null ||
                                col.getPrimaryKeySeq() > targetPKS) {
                            logger.debug("idx is " + idx);
                            break;
                        }
                        idx++;
                    }                
                    p.addChild(idx, this);
                } finally {
                    p.setMagicEnabled(true);
                }
	            getParentTable().normalizePrimaryKey();
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
            endCompoundEdit("Ending PrimaryKeySeq compound edit");
        }
	}

	/**
	 * Gets the value of autoIncrement
	 *
	 * @return the value of autoIncrement
	 */
	public boolean isAutoIncrement()  {
		return this.autoIncrement;
	}

	/**
	 * Sets the value of autoIncrement
	 *
	 * @param argAutoIncrement Value to assign to this.autoIncrement
	 */
	public void setAutoIncrement(boolean argAutoIncrement) {
	    boolean oldAutoIncrement = autoIncrement;
	    this.autoIncrement = argAutoIncrement;
	    fireDbObjectChanged("autoIncrement",oldAutoIncrement,argAutoIncrement);
	}
    
    /**
     * Returns the auto-increment sequence name, or a made-up
     * default (<code><i>parentTableName</i>_<i>columnName</i>_seq</code>) if the sequence name
     * has not been set explicitly.  The auto-increment sequence
     * name is a hint to DDL generators for platforms that need
     * sequence objects to support auto-incrementing column values.
     */
    public String getAutoIncrementSequenceName() {
        if (autoIncrementSequenceName == null) {
            String tableName = getParentTable() == null ? "" : getParentTable().getName() + "_";
            return tableName + getName() + "_seq";
        } else {
            return autoIncrementSequenceName;
        }
    }
    
    /**
     * Only sets the name if it is different from the default name.  This is important
     * in case the table name changes; the name should be expected to update.
     */
    public void setAutoIncrementSequenceName(String autoIncrementSequenceName) {

        // have to use getter because it produces the default value
        String oldName = getAutoIncrementSequenceName();
        
        if (!oldName.equals(autoIncrementSequenceName)) {
            this.autoIncrementSequenceName = autoIncrementSequenceName;
            fireDbObjectChanged("autoIncrementSequenceName", oldName, autoIncrementSequenceName);
        }
    }

    /**
     * Returns true if the auto-increment sequence name of this column has
     * been changed from its default value.  Code that loads and saves this
     * SQLColumn will want to know if the value is a default or not.
     */
    public boolean isAutoIncrementSequenceNameSet() {
        return autoIncrementSequenceName != null;
    }
    
	/**
	 * This comparator helps you sort a list of columns so that the
	 * primary key columns come first in their correct order, and all
	 * the other columns come after.
	 */
	public static class CompareByPKSeq implements Comparator {
		public int compare(Object o1, Object o2) {
			SQLColumn c1 = (SQLColumn) o1;
			SQLColumn c2 = (SQLColumn) o2;
			if (c1.primaryKeySeq == null && c2.primaryKeySeq == null) {
				return 0;
			} else if (c1.primaryKeySeq == null && c2.primaryKeySeq != null) {
				return 1;
			} else if (c1.primaryKeySeq != null && c2.primaryKeySeq == null) {
				return -1;
			} else {
				return c1.primaryKeySeq.intValue() - c2.primaryKeySeq.intValue();
			}
		}
	}
	
	public void addReference() {
        int oldReference = referenceCount;
		referenceCount++;
		logger.debug("incremented reference count to: " + referenceCount);
        fireDbObjectChanged("referenceCount", oldReference, referenceCount);
	}
	
	public void removeReference() {
		if (logger.isDebugEnabled()) {
			String parentName = "<no parent table>";
			if (getParent() != null && getParentTable() != null) {
				parentName = getParentTable().getName();
			}
			logger.debug("Trying to remove reference from "+parentName+"."+getName()+" "+hashCode());
			
		}
		if (referenceCount == 0) {
		    logger.debug("Reference count of "+ this.getParentTable() +"."+this+" was already 0");
			throw new IllegalStateException("Reference count of is already 0; can't remove any references!");
		}
        int oldReference = referenceCount;
		referenceCount--;
		logger.debug("decremented reference count to: " + referenceCount);
		if (referenceCount == 0) {
			// delete from the parent (columnsFolder) 
			if (getParent() != null){
				logger.debug("reference count is 0, deleting column from parent.");
				getParent().removeChild(this);
			} else {
				logger.debug("Already removed from parent");
			}
		}
        fireDbObjectChanged("referenceCount", oldReference, referenceCount);
	}
	
	/**
	 * @return Returns the referenceCount.
	 */
	public int getReferenceCount() {
		return referenceCount;
	}

    /**
     * WARNING this should not be used by hand coded objects
     * @param referenceCount
     * @deprecated This method exists only to be called reflectively by the undo manager.  You should use addReference() and removeReference().
     */
	@Deprecated
    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    @Override
	public Class<? extends SQLObject> getChildType() {
		return null;
	}
}