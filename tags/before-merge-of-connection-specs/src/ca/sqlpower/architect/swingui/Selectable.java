package ca.sqlpower.architect.swingui;

public interface Selectable {
	public void setSelected(boolean v);
	public boolean isSelected();

	public void addSelectionListener(SelectionListener l);
	public void removeSelectionListener(SelectionListener l);
}
