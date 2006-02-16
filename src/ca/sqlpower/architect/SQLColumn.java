package ca.sqlpower.architect;

import java.util.Comparator;
import java.util.Collections;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.TypeMap;

public class SQLColumn extends SQLObject implements java.io.Serializable, Cloneable {

	private static Logger logger = Logger.getLogger(SQLColumn.class);

	// *** REMEMBER *** update the getDerivedInstance method if you add new properties!

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
	protected int precision; // the length of the field in digits or characters
	protected int scale; // the placement of a decimal point, counting from the far right
	
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
	protected Integer primaryKeySeq;
	protected boolean autoIncrement;

	// *** REMEMBER *** update the getDerivedInstance method if you add new properties!

	
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
		setName("new column");
		type = Types.INTEGER;		
		// scale = 10;
		precision = 10;
		nullable = DatabaseMetaData.columnNoNulls;
		autoIncrement = false;
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
		this.parent = parentTable.getColumnsFolder();
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
		
		this.referenceCount = 1;
	}

	public SQLColumn(SQLTable parent, String colName, int type, int precision, int scale) {
		this(parent, colName, type, null, precision, scale, DatabaseMetaData.columnNullable, null, null, null, false);
	}
	
	/**
	 * Makes a near clone of the given source column.  The new column
	 * you get back will have a parent pointer of addTo.columnsFolder,
	 * but will not be attached as a child (you will normally do that
	 * right after calling this).  It will refer to source as its
	 * sourceColumn property, and otherwise be identical to source.
	 * 
	 * 
	 */
	public static SQLColumn getDerivedInstance(SQLColumn source, SQLTable addTo) {
		logger.debug("derived instance SQLColumn constructor invocation.");
		SQLColumn c = new SQLColumn();
		c.sourceColumn = source;
		c.parent = addTo.getColumnsFolder();
		c.setName(source.getName());
		c.type = source.type;
		c.sourceDataTypeName = source.sourceDataTypeName;
		c.setPhysicalName(source.getPhysicalName());
		c.precision = source.precision;
		c.scale = source.scale;
		c.nullable = source.nullable;
		c.remarks = source.remarks;
		c.defaultValue = source.defaultValue;
		c.primaryKeySeq = source.primaryKeySeq;
		c.autoIncrement = source.autoIncrement;
		c.referenceCount = source.referenceCount; 
		return c;
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
		Connection con = addTo.getParentDatabase().getConnection();
		ResultSet rs = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			logger.debug("SQLColumn.addColumnsToTable: catalog="+catalog+"; schema="+schema+"; tableName="+tableName);
			rs = dbmd.getColumns(catalog, schema, tableName, "%");
			while (rs.next()) {
				logger.debug("addColumnsToTable SQLColumn constructor invocation.");				
				SQLColumn col = new SQLColumn(addTo,
											  rs.getString(4),  // col name
											  rs.getInt(5), // data type (from java.sql.Types)
											  rs.getString(6), // native type name
											  rs.getInt(7), // column size (precision)
											  rs.getInt(9), // decimal size (scale)
											  rs.getInt(11), // nullable
											  rs.getString(12) == null ? "" : rs.getString(12), // remarks
											  rs.getString(13), // default value
											  null, // primaryKeySeq
											  false // isAutoIncrement
											  );

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
				
				if (addTo.getColumnByName(col.getName(), false) != null) {
					throw new DuplicateColumnException(addTo, col.getName());
				}
				
				// do any database specific transformations required for this column
				/* TODO This is not used to be replace with an XML format later
				  
				 
				if(TypeMap.getInstance().applyRules(col)) {
					logger.debug("Applied mapppings to column: " + col);
				}
				*/
				
				addTo.columnsFolder.children.add(col); // don't use addTo.columnsFolder.addColumn() (avoids multiple SQLObjectEvents)

				// XXX: need to find out if column is auto-increment
			}
			rs.close();
			rs = null;

			rs = dbmd.getPrimaryKeys(catalog, schema, tableName);
			while (rs.next()) {
				SQLColumn col = addTo.getColumnByName(rs.getString(4), false);
				col.primaryKeySeq = new Integer(rs.getInt(5));
				addTo.setPrimaryKeyName(rs.getString(6));
			}
			rs.close();
			rs = null;
		} finally {
			if (rs != null) rs.close();
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
		logger.debug("SQLColumn: populate is a no-op");
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
				+ca.sqlpower.architect.swingui.SQLType.getTypeName(type) // XXX: replace with TypeDescriptor
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
		if (type != argType) {
			setSourceDataTypeName(null);
			this.type = argType;
			fireDbObjectChanged("type",oldType,argType);
		}
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
	 * Gets the value of primaryKey
	 *
	 * @return the value of primaryKey
	 */
	public boolean isPrimaryKey()  {
		return this.primaryKeySeq != null;
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
		if (this.nullable != argNullable) {
			this.nullable = argNullable;
			fireDbObjectChanged("nullable",oldNullable,argNullable);
		}
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
	 * Sets the value of primaryKeySeq
	 *
	 * @param argPrimaryKeySeq Value to assign to this.primaryKeySeq
	 */
	public void setPrimaryKeySeq(Integer argPrimaryKeySeq) {
		Integer oldPrimaryKeySeq = primaryKeySeq;
		if (argPrimaryKeySeq != null && !this.autoIncrement) {
			setNullable(DatabaseMetaData.columnNoNulls);
		}
		if (this.primaryKeySeq != null && this.primaryKeySeq.equals(argPrimaryKeySeq)) return;
		this.primaryKeySeq = argPrimaryKeySeq;
		if (parent != null) {
			Collections.sort(getParentTable().columnsFolder.children, new SortByPKSeq());
			getParentTable().normalizePrimaryKey();
		}
		
		fireDbObjectChanged("primaryKeySeq",oldPrimaryKeySeq,argPrimaryKeySeq);
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
		boolean oldAutoIncrement = this.autoIncrement;
		this.autoIncrement = argAutoIncrement;
		fireDbObjectChanged("autoIncrement",oldAutoIncrement,argAutoIncrement);
	}

	/**
	 * This comparator helps you sort a list of columns so that the
	 * primary key columns come first in their correct order, and all
	 * the other columns come after.
	 */
	public static class SortByPKSeq implements Comparator {
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

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("Clone not supported !?!?");
			return null;
		}
	}
	
	public void addReference() {
		logger.debug("(inc) old reference count: " + referenceCount);
		referenceCount++;
		logger.debug("incremented reference count to: " + referenceCount);
	}
	public void removeReference() {
		logger.debug("(dec) old reference count: " + referenceCount);
		referenceCount--;
		logger.debug("decremented reference count to: " + referenceCount);
		if (referenceCount == 0) {
			// delete from the parent (columnsFolder) 
			logger.debug("reference count is 0, deleting column from parent.");
			getParent().removeChild(this);
		}
	}


	
	
	/**
	 * @return Returns the referenceCount.
	 */
	public int getReferenceCount() {
		return referenceCount;
	}

	@Override
	public Class<? extends SQLObject> getChildType() {
		return null;
	}
}
