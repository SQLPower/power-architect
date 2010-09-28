/**
 * 
 */
package ca.sqlpower.architect.swingui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ca.sqlpower.architect.ArchitectDataSource;

public final class DataSourceRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		ArchitectDataSource ds = (ArchitectDataSource) value;
		String label;
		if (ds == null) {
			label = "(Choose a Connection)";
		} else {
			label = ds.getName();
		}
		return super.getListCellRendererComponent(list, label, index,
				isSelected, cellHasFocus);
	}
}