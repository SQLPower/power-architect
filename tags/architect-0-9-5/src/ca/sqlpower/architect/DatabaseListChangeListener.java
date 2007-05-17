package ca.sqlpower.architect;

public interface DatabaseListChangeListener {
	void databaseAdded(DatabaseListChangeEvent e);
	void databaseRemoved(DatabaseListChangeEvent e);
}
