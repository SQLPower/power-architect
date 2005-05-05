package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.sql.DBConnectionSpec;

/**
 * The DDLStatement class combines a high-level description of what a
 * certain Data Definition Language statement does, as well as the
 * text of that statement for a particular database.
 */
public class DDLStatement {

	public enum StatementType { create, drop, alter, addPk, addFk };

	private SQLObject object;
	private StatementType type;
	private String sqlText;

	public DDLStatement(SQLObject object, StatementType type, String sqlText) {
		this.object = object;
		this.type = type;
		this.sqlText = sqlText;
	}

	// ------------------------ Accessors and Mutators -------------------------
	
	/**
	 * See {@link #object}.
	 *
	 * @return the value of object
	 */
	public SQLObject getObject()  {
		return this.object;
	}

	/**
	 * See {@link #object}.
	 *
	 * @param argObject Value to assign to this.object
	 */
	public void setObject(SQLObject argObject) {
		this.object = argObject;
	}

	/**
	 * See {@link #type}.
	 *
	 * @return the value of type
	 */
	public StatementType getType()  {
		return this.type;
	}

	/**
	 * See {@link #type}.
	 *
	 * @param argType Value to assign to this.type
	 */
	public void setType(StatementType argType) {
		this.type = argType;
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @return the value of sqlText
	 */
	public String getSQLText()  {
		return this.sqlText;
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @param v Value to assign to this.sqlText
	 */
	public void setSQLText(String v) {
		this.sqlText = v;
	}

}
