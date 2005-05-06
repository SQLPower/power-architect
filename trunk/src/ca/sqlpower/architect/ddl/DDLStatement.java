package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.SQLObject;

/**
 * The DDLStatement class combines a high-level description of what a
 * certain Data Definition Language statement does, as well as the
 * text of that statement for a particular database.
 */
public class DDLStatement {

	public static class StatementType {
	
		public static final StatementType CREATE = new StatementType("CREATE");
		public static final StatementType DROP = new StatementType("DROP");
		public static final StatementType ALTER = new StatementType("ALTER");
		public static final StatementType ADD_PK = new StatementType("ADD_PK");
		public static final StatementType ADD_FK = new StatementType("ADD_FK");

		private String type;

		private StatementType(String type) {
			this.type = type;
		}

		public boolean equals(Object other) {
			return this.type.equals(((StatementType) other).type);
		}
	}

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
