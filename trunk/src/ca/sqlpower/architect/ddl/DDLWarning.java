package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;

/**
 * A DDLWarning object encapsulates the details of a single warning
 * issued by a DDL generator.
 */
public interface DDLWarning {

	/**
	 * The subject of this warning.  For instance, if there is a
	 * duplicte column name, the SQLColumn object with the duplicate
	 * name will be the warning's subject.
	 */
	public SQLObject getSubject();
	
	/**
	 * This warning's category.  Should be only one or two words.
	 * Users may find it useful to sort warnings by category while
	 * fixing problems.
	 */
	public String getCategory();

	/**
	 * Returns a short phrase describing the warning.
	 */
	public String getMessage();
}
