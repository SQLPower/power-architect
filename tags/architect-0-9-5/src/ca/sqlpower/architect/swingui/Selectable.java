package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.swingui.event.SelectionListener;

public interface Selectable {
	public void setSelected(boolean v,int multiSelectionType);
	public boolean isSelected();

	public void addSelectionListener(SelectionListener l);
	public void removeSelectionListener(SelectionListener l);
}
