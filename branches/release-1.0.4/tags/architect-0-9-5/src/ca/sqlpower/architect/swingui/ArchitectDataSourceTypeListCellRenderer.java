package ca.sqlpower.architect.swingui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ca.sqlpower.architect.ArchitectDataSourceType;

public class ArchitectDataSourceTypeListCellRenderer extends DefaultListCellRenderer {
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof ArchitectDataSourceType) {
            return super.getListCellRendererComponent(list, ((ArchitectDataSourceType) value).getName(), index, isSelected,
                cellHasFocus);
        } else {
            throw new IllegalArgumentException("Value should only be of type ArchitectDataSourceType not "+value.getClass());
        }
    }

}
