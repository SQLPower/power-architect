package ca.sqlpower.architect;

/**
 * The DuplicateColumnException represents a failure to add a column
 * to a table because the table already has a column with that name.
 */
public class DuplicateColumnException extends ArchitectException {

	SQLTable table;
    String dupColName;

	public DuplicateColumnException(SQLTable table, String dupColName) {
		super(table.getTableName()+" already has a column named "+dupColName);
			this.table = table;
		this.dupColName = dupColName;
	}

	/**
	 * Gets the value of table
	 *
	 * @return the value of table
	 */
	public SQLTable getTable()  {
		return this.table;
	}

	/**
	 * Sets the value of table
	 *
	 * @param argTable Value to assign to this.table
	 */
	public void setTable(SQLTable argTable) {
		this.table = argTable;
	}

	/**
	 * Gets the value of dupColName
	 *
	 * @return the value of dupColName
	 */
	public String getDupColName()  {
		return this.dupColName;
	}

	/**
	 * Sets the value of dupColName
	 *
	 * @param argDupColName Value to assign to this.dupColName
	 */
	public void setDupColName(String argDupColName) {
		this.dupColName = argDupColName;
	}

}
