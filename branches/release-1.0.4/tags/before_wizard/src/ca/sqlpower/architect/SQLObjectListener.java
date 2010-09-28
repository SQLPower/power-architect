package ca.sqlpower.architect;

/**
 * Our own version of the javax.swing.event.TreeModelListener.
 *
 * @see javax.swing.event.TreeModelListener
 */
public interface SQLObjectListener {
	public void dbChildrenInserted(SQLObjectEvent e);
	public void dbChildrenRemoved(SQLObjectEvent e);
	public void dbObjectChanged(SQLObjectEvent e);
	public void dbStructureChanged(SQLObjectEvent e);
}
