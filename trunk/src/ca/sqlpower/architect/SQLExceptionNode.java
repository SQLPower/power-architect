package ca.sqlpower.architect;

import java.util.Collections;

/**
 * A SQLExceptionNode exists for reporting failures in the SQLObject
 * hierarchy.  For example, when the DBTree tries to expand a node and
 * that results in failure, it adds one of these at the failure point.
 */
public class SQLExceptionNode extends SQLObject {

	protected Throwable exception;
	protected String message;
	protected SQLObject parent;

	public SQLExceptionNode(Throwable exception, String message) {
		this.exception = exception;
		this.message = message;
        this.children = Collections.EMPTY_LIST;
	}
	
	/**
	 * If you wanna show the exception to the user later on, get it
	 * here.  But don't kid yourself: users don't read error messages.
	 */
	public Throwable getException() {
		return exception;
	}
	

	// ------------- SQLObject Methods -------------
	
	/**
	 * Returns the message.
	 */
	public String getName() {
		return message;
	}
	
	public SQLObject getParent() {
		return parent;
	}
	
	/**
	 * Because these nodes get added just as they are needed, it is
	 * sometimes necessary for users of the class (DBTreeModel) to set
	 * the parent directly.
	 */
	public void setParent(SQLObject parent) {
        SQLObject oldParent = this.parent;
		this.parent = parent;
        fireDbObjectChanged("parent", oldParent, parent);
	}

	public void populate() {
        // nothing to populate
	}

	protected void addChildImpl(int index, SQLObject child) {
		throw new UnsupportedOperationException("SQLExceptionNodes can't have children");
	}

	public boolean isPopulated() {
		return true;
	}
	
	public String getShortDisplayName() {
		return "Error: "+message;
	}
	
	public boolean allowsChildren() {
		return false;
	}
	
	public String toString() {
		return getShortDisplayName();
	}

	public String getMessage() {
	    return message;
	}
	
	public void setMessage(String v) {
        String oldMessage = message;
	    message = v;
        fireDbObjectChanged("message", oldMessage, message);
	}

	@Override
	public Class<? extends SQLObject> getChildType() {
		return null;
	}
}
