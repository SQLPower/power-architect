package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * GenericTypeDescriptor is a class that describes a SQL type.  It can
 * be populated from DatabaseMetaData.getTypeInfo results, or you can
 * make up your own types.
 */
public class GenericTypeDescriptor {
	protected String name;
	protected int dataType;
	protected long precision;
	protected String literalPrefix;
	protected String literalSuffix;
	protected int nullable;
	protected boolean hasPrecision;
	protected boolean hasScale;

	/**
	 * Creates a new GenericTypeDescriptor with properties filled in
	 * from the current row of rs, which should have been obtained
	 * from DatabaseMetaData.getTypeInfo().
	 */
	public GenericTypeDescriptor(ResultSet rs) throws SQLException {
		name = rs.getString(1);
		dataType = rs.getInt(2);
		precision = rs.getLong(3);
		literalPrefix = rs.getString(4);
		literalSuffix = rs.getString(5);
		nullable = rs.getInt(7);

		determineScaleAndPrecision();
	}

	public GenericTypeDescriptor(String name, int dataType, long precision, String literalPrefix, String literalSuffix, int nullable, boolean hasPrecision, boolean hasScale) {
		this.name = name;
		this.dataType = dataType;
		this.precision = precision;
		this.literalPrefix = literalPrefix;
		this.literalSuffix = literalSuffix;
		this.nullable = nullable;
		this.hasPrecision = hasPrecision;
		this.hasScale = hasScale;
	}

	/**
	 * This method sets the hasScale and hasPrecision properties based
	 * on the current setting of dataType.  Subclasses for specific
	 * database platforms should override this method if any types for
	 * that database are determined incorrectly by this generic
	 * method.
	 */
	public void determineScaleAndPrecision() {
		switch (dataType) {
		case Types.ARRAY:
		case Types.BINARY:
		case Types.BIT:
		case Types.DATE:
		case Types.DISTINCT:  // UDT: this may or may not have scale or precision
		case Types.JAVA_OBJECT:
		case Types.NULL:
		case Types.OTHER:
		case Types.REF:
		case Types.STRUCT:
		case Types.TIME:
		case Types.TIMESTAMP:	
		default:
			hasScale = false;
			hasPrecision = false;
			break;

		case Types.BIGINT:
		case Types.BLOB:
		case Types.CHAR:
		case Types.CLOB:
		case Types.INTEGER:
		case Types.LONGVARBINARY:
		case Types.LONGVARCHAR:
		case Types.SMALLINT:
		case Types.TINYINT:
		case Types.VARBINARY:
		case Types.VARCHAR:
			hasPrecision = true;
			hasScale = false;
			break;

		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.NUMERIC:
		case Types.REAL:
			hasPrecision = true;
			hasScale = true;
			break;
		}
	}

	public String toString() {
		return getName()+" (type="+getDataType()+")";
	}

	// ------------------- Accessors and Mutators ----------------------

	/**
	 * Gets the value of name
	 *
	 * @return the value of name
	 */
	public String getName()  {
		return this.name;
	}

	/**
	 * Sets the value of name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setName(String argName) {
		this.name = argName;
	}

	/**
	 * Gets the value of dataType
	 *
	 * @return the value of dataType
	 */
	public int getDataType()  {
		return this.dataType;
	}

	/**
	 * Sets the value of dataType
	 *
	 * @param argDataType Value to assign to this.dataType
	 */
	public void setDataType(int argDataType) {
		this.dataType = argDataType;
	}

	/**
	 * Gets the value of precision
	 *
	 * @return the value of precision
	 */
	public long getPrecision()  {
		return this.precision;
	}

	/**
	 * Sets the value of precision
	 *
	 * @param argPrecision Value to assign to this.precision
	 */
	public void setPrecision(long argPrecision) {
		this.precision = argPrecision;
	}

	/**
	 * Gets the value of literalPrefix
	 *
	 * @return the value of literalPrefix
	 */
	public String getLiteralPrefix()  {
		return this.literalPrefix;
	}

	/**
	 * Sets the value of literalPrefix
	 *
	 * @param argLiteralPrefix Value to assign to this.literalPrefix
	 */
	public void setLiteralPrefix(String argLiteralPrefix) {
		this.literalPrefix = argLiteralPrefix;
	}

	/**
	 * Gets the value of literalSuffix
	 *
	 * @return the value of literalSuffix
	 */
	public String getLiteralSuffix()  {
		return this.literalSuffix;
	}

	/**
	 * Sets the value of literalSuffix
	 *
	 * @param argLiteralSuffix Value to assign to this.literalSuffix
	 */
	public void setLiteralSuffix(String argLiteralSuffix) {
		this.literalSuffix = argLiteralSuffix;
	}

	/**
	 * Gets the value of nullable
	 *
	 * @return the value of nullable
	 */
	public int getNullable()  {
		return this.nullable;
	}

	/**
	 * Sets the value of nullable
	 *
	 * @param argNullable Value to assign to this.nullable
	 */
	public void setNullable(int argNullable) {
		this.nullable = argNullable;
	}

	public boolean isNullable() {
		return nullable == DatabaseMetaData.columnNullable;
	}
	
	/**
	 * Gets the value of hasScale
	 *
	 * @return the value of hasScale
	 */
	public boolean getHasScale()  {
		return this.hasScale;
	}

	/**
	 * Sets the value of hasScale
	 *
	 * @param argHasScale Value to assign to this.hasScale
	 */
	public void setHasScale(boolean argHasScale) {
		this.hasScale = argHasScale;
	}

	/**
	 * Gets the value of hasPrecision
	 *
	 * @return the value of hasPrecision
	 */
	public boolean getHasPrecision()  {
		return this.hasPrecision;
	}

	/**
	 * Sets the value of hasPrecision
	 *
	 * @param argHasPrecision Value to assign to this.hasPrecision
	 */
	public void setHasPrecision(boolean argHasPrecision) {
		this.hasPrecision = argHasPrecision;
	}

}
