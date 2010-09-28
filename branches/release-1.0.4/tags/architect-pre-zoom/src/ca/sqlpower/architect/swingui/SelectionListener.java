package ca.sqlpower.architect.swingui;

public interface SelectionListener {

	/**
	 * Called whenever an item is selected.  The selected item is the
	 * event's source.
	 */
	public void itemSelected(SelectionEvent e);
}
