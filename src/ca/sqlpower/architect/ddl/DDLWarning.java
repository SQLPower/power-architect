package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;

/**
 * A DDLWarning object encapsulates the details of a single warning
 * message issued by a DDL generator.
 */
public interface DDLWarning {

	/**
	 * The subject of this warning.  For instance, if there is a
	 * duplicte column name, the SQLColumn object with the duplicate
	 * name will be the warning's subject.
	 */
	public SQLObject getSubject();
    
	/**
	 * This warning's category/reason.  Should be only one or two words.
	 * Users may find it useful to sort warnings by reason while
	 * fixing problems.
	 */
	public String getReason();
	
	/**
	 * The problematic value, which is a property of the warning's
	 * subject, and is described by getReason.
	 */
	public Object getOldValue();

	/**
	 * The new value assigned by the DDL Generator in order to
	 * continue with the DDL Generation.  The user should be given the
	 * chance to see and possibly modify this value before executing
	 * the DDL script.
	 */
	public Object getNewValue();

	/**
	 * Modifies the value set by the DDL Generator to something else.
	 */
	public void setNewValue(Object newValue);
}
