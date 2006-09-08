package ca.sqlpower.architect.swingui.event;


public interface SelectionListener {

	/**
	 * Called whenever an item is selected.  The selected item is the
	 * event's source.
	 */
	public void itemSelected(SelectionEvent e);

	/**
	 * Called whenever an item is deselected.  The deselected item is the
	 * event's source.
	 */
	public void itemDeselected(SelectionEvent e);
}
