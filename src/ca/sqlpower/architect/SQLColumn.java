package ca.sqlpower.architect;

import java.util.Comparator;
import java.util.Collections;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import org.apache.log4j.Logger;

public class SQLColumn extends SQLObject implements java.io.Serializable, Cloneable {

	private static Logger logger = Logger.getLogger(SQLColumn.class);

	// *** REMEMBER *** update the getDerivedInstance method if you add new properties!

	/**
	 * Refers back to the real database-connected SQLColumn that this
	 * column was originally derived from.
	 */
	protected SQLColumn sourceColumn;

	protected SQLObject parent;
	protected String columnName;

	/**
	 * Must be a type defined in java.sql.Types.  Move to enum in 1.5
	 * (we hope!).
	 */
	protected int type;

	/**
	 * This is the native name for the column's type in its source
	 * database.  See {@link #type} for system-independant type.
	 */
	protected String sourceDBTypeName;

	protected int scale;
	protected int precision;
	
	/**
	 * This column's nullability type.  One of:
	 * <ul><li>DatabaseMetaData.columnNoNulls - might not allow NULL values
	 *     <li>DatabaseMetaData.columnNullable - definitely allows NULL values
	 *     <li>DatabaseMetaData.columnNullableUnknown - nullability unknown
	 * </ul>
	 */
	protected int nullable;
	protected String remarks;
	protected String defaultValue;
	protected Integer primaryKeySeq;
	protected boolean autoIncrement;

	// *** REMEMBER *** update the getDerivedInstance method if you add new properties!

	public SQLColumn() {
		logger.debug("NEW COLUMN (noargs) @"+hashCode());
		columnName = "new column";
		type = Types.INTEGER;
		scale = 10;
		nullable = DatabaseMetaData.columnNoNulls;
		autoIncrement = false;
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
					 int scale,
					 int precision,
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
		this.columnName = colName;
		this.type = dataType;
		this.sourceDBTypeName = nativeType;
		this.scale = scale;
		this.precision = precision;
		this.nullable = nullable;
		this.remarks = remarks;
		this.defaultValue = defaultValue;
		this.primaryKeySeq = primaryKeySeq;
		this.autoIncrement = isAutoIncrement;

		this.children = Collections.EMPTY_LIST;
	}

	public SQLColumn(SQLTable parent, String colName, int type, int scale, int precision) {
		this(parent, colName, type, null, scale, precision, DatabaseMetaData.columnNullable, null, null, null, false);
	}

	/**
	 * Makes a near clone of the given source column.  The new column
	 * you get back will have a parent pointer of addTo.columnsFolder,
	 * but will not be attached as a child (you will normally do that
	 * right after calling this).  It will refer to source as its
	 * sourceColumn property, and otherwise be identical to source.
	 */
	public static SQLColumn getDerivedInstance(SQLColumn source, SQLTable addTo) {
		SQLColumn c = new SQLColumn();
		c.sourceColumn = source;
		c.parent = addTo.getColumnsFolder();
		c.columnName = source.columnName;
		c.type = source.type;
		c.sourceDBTypeName = source.sourceDBTypeName;
		c.scale = source.scale;
		c.precision = source.precision;
		c.nullable = source.nullable;
		c.remarks = source.remarks;
		c.defaultValue = source.defaultValue;
		c.primaryKeySeq = source.primaryKeySeq;
		c.autoIncrement = source.autoIncrement;
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
		Connection con = addTo.parentDatabase.getConnection();
		ResultSet rs = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			logger.debug("SQLColumn.addColumnsToTable: catalog="+catalog+"; schema="+schema+"; tableName="+tableName);
			rs = dbmd.getColumns(catalog, schema, tableName, "%");
			while (rs.next()) {
				SQLColumn col = new SQLColumn(addTo,
											  rs.getString(4),  // col name
											  rs.getInt(5), // data type (from java.sql.Types)
											  rs.getString(6), // native type name
											  rs.getInt(7), // column size
											  rs.getInt(9), // decimal size
											  rs.getInt(11), // nullable
											  rs.getString(12), // remarks
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

				logger.debug("Adding column "+col.getColumnName());
				
				if (addTo.getColumnByName(col.getColumnName(), false) != null) {
					throw new DuplicateColumnException(addTo, col.getColumnName());
				}
				addTo.columnsFolder.children.add(col); // don't use addTo.columnsFolder.addColumn() (avoids multiple SQLObjectEvents)

				// XXX: need to find out if column is auto-increment
			}
			rs.close();

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
			return c1.columnName.compareTo(c2.columnName);
		}
	}

	public String toString() {
		return getShortDisplayName();
	}

	// ------------------------- SQLObject support -------------------------

	public void populate() throws ArchitectException {
		// SQLColumn doesn't have children, so populate does nothing!
		return;
	}

	public String getName() {
		return getColumnName();
	}

	public boolean isPopulated() {
		return true;
	}

	public String getShortDisplayName() {
		if (sourceDBTypeName != null) {
			return columnName+": "+sourceDBTypeName+"("+scale+")";
		} else {
			return columnName+": "
				+ca.sqlpower.architect.swingui.SQLType.getTypeName(type) // XXX: replace with TypeDescriptor
				+"("+scale+")";
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
		sourceColumn = col;
		fireDbObjectChanged("sourceColumn");
	}

	/**
	 * Gets the value of name
	 *
	 * @return the value of name
	 */
	public String getColumnName()  {
		return this.columnName;
	}

	/**
	 * Sets the value of name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setColumnName(String argName) {
		this.columnName = argName;
		fireDbObjectChanged("columnName");
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
		if (type != argType) {
			setSourceDBTypeName(null);
			this.type = argType;
			fireDbObjectChanged("type");
		}
	}

	public String getSourceDBTypeName() {
		return sourceDBTypeName;
	}

	public void setSourceDBTypeName(String n) {
		sourceDBTypeName = n;
		fireDbObjectChanged("sourceDBTypeName");
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
		logger.debug("scale changed from "+scale+" to "+argScale);
		this.scale = argScale;
		fireDbObjectChanged("scale");
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
		this.precision = argPrecision;
		fireDbObjectChanged("precision");
	}

	/**
	 * Figures out this column's nullability
	 *
	 * @return true iff this.nullable == DatabaseMetaData.columnNullable.
	 */
	public boolean isNullable()  {
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
		return (SQLTable) parent.getParent();
	}

	/**
	 * Sets the value of parent
	 *
	 * @param argParent Value to assign to this.parent
	 */
	protected void setParent(SQLObject argParent) {
		this.parent = argParent;
		fireDbObjectChanged("parent");
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
		if (this.nullable != argNullable) {
			this.nullable = argNullable;
			fireDbObjectChanged("nullable");
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
		this.remarks = argRemarks;
		fireDbObjectChanged("remarks");
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
		this.defaultValue = argDefaultValue;
		fireDbObjectChanged("defaultValue");
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
		if (argPrimaryKeySeq != null) {
			setNullable(DatabaseMetaData.columnNoNulls);
		}
		if (this.primaryKeySeq != null && this.primaryKeySeq.equals(argPrimaryKeySeq)) return;
		this.primaryKeySeq = argPrimaryKeySeq;
		if (parent != null) {
			Collections.sort(getParentTable().columnsFolder.children, new SortByPKSeq());
			getParentTable().normalizePrimaryKey();
		}
		fireDbObjectChanged("primaryKeySeq");
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
		this.autoIncrement = argAutoIncrement;
		fireDbObjectChanged("autoIncrement");
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
}
