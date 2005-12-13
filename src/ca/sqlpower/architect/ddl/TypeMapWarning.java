package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;

/**
 * A TypeMapWarning is generated when the DDL Generator tries to make
 * a column definition but the target datbase's type map does not
 * contain information about the column's type.  The user will have to
 * pick a type manually.
 */
public class TypeMapWarning implements DDLWarning {

	protected SQLColumn subject;
	protected String reason;
	protected GenericTypeDescriptor newType;
	protected GenericTypeDescriptor oldType;

	public TypeMapWarning(SQLColumn subject, String reason, GenericTypeDescriptor oldType, GenericTypeDescriptor newType) {
		this.subject = subject;
		this.reason = reason;
		this.oldType = oldType;
		this.newType = newType;
	}

	/**
	 * The column whose type is unknown in the target database.
	 */
	public SQLObject getSubject() {
		return subject;
	}
	
	public String getReason() {
		return reason;
	}

	/**
	 * Returns the original type (before the change).
	 *
	 * @return an object of class GenericTypeDescriptor.
	 */
	public Object getOldValue() {
		return oldType;
	}

	/**
	 * Returns the new type (after the change).
	 *
	 * @return an object of class GenericTypeDescriptor.
	 */
	public Object getNewValue() {
		return newType;
	}

    public void setNewValue(Object newValue) {
        newType = (GenericTypeDescriptor) newValue;
        subject.setType(newType.getDataType());
    }
}
