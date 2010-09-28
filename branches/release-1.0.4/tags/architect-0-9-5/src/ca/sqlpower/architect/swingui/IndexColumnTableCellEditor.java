package ca.sqlpower.architect.swingui;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
/**
 *  Creates an editor for the  
 *
 *  Note: This class only works with a table with a IndexColumnTableModel
 */
public class IndexColumnTableCellEditor extends DefaultCellEditor implements TableCellEditor {
    Component component;
    
    public IndexColumnTableCellEditor(JCheckBox checkBox) {
        super(checkBox);
    }

    public IndexColumnTableCellEditor(JComboBox comboBox) {
        super(comboBox);
    }

    public IndexColumnTableCellEditor(JTextField textField) {
        super(textField);
    }
    
    public IndexColumnTableCellEditor() {
        super(new JComboBox());
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        IndexColumnTableModel tm = (IndexColumnTableModel) table.getModel();
        try {
            SQLColumnComboBoxModel columnComboBoxModel = new SQLColumnComboBoxModel(tm.getTable(), tm.getUsedColumns());
            component = new JComboBox(columnComboBoxModel);
            columnComboBoxModel.setSelectedItem(value);
            return component;
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }

    public Object getCellEditorValue() {
       return ((JComboBox)component).getModel().getSelectedItem();
    }
    
    @Override
    public Component getComponent() {
        return component;
    }
}
