package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;

/**
 * A NameChangeWarning object represents a warning about a SQLObject
 * whose name was substituted for a different one because it has the
 * same name as another SQLObject in the same scope, or contained
 * non-alphanumeric characters, etc.
 */
public class NameChangeWarning implements DDLWarning {

	protected SQLObject subject;
	protected String reason;
	protected String oldName;

	public NameChangeWarning(SQLObject subject, String reason, String oldName) {
		this.subject = subject;
		this.reason = reason;
		this.oldName = oldName;
	}

	/**
	 * The subject of this warning.  For instance, if there is a
	 * duplicte column name, the SQLColumn object with the duplicate
	 * name will be the warning's subject.
	 */
	public SQLObject getSubject() {
		return subject;
	}
	
	public String getReason() {
		return reason;
	}

	/**
	 * Returns the original name (before the change).  If the subject
	 * is a SQLColumn, the string returned is
	 * <code>table_name.old_column_name</code>.
	 */
	public Object getOldValue() {
		if (getSubject() instanceof SQLColumn) {
			SQLTable parent = ((SQLColumn) getSubject()).getParentTable();
			return parent.getName()+"."+oldName;
		} else {
			return oldName;
		}
	}

	public Object getNewValue() {
		return subject.getName();
	}
}
