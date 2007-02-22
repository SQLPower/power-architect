package ca.sqlpower.architect.ddl;

import java.util.List;

import ca.sqlpower.architect.SQLObject;

/**
 * A DDLWarning object encapsulates the details of a single warning
 * message issued by a DDL generator.
 */
public interface DDLWarning {

	/**
	 * Get the message associated with this warning, e.g., a string
     * like "Primary Key Name is already in use"
	 */
	public String getMessage();

	/**
	 * The subject(s) of this warning.  For instance, if there is a
	 * duplicate table names, the SQLTable objects with the duplicate
	 * names will be the "involved objects".
	 */
	public List<SQLObject> getInvolvedObjects();

	/** Return true if the user has repaired or quickfixed the problem */
	public boolean isFixed();

    public void setFixed(boolean fixed);

    /** Tell whether the user can "quick fix" this problem */
    public boolean isQuickFixable();

    /** If isQuickFixable(), then this gives the message about what
     * will be done.
     */
    public String getQuickFixMessage();

    /** If isQuickFixable(), then this applies the quick fix */
    public boolean quickFix();

}
