package ca.sqlpower.architect.swingui.table;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.swingui.table.TableTextConverter;

/**
 * Override JTable methods that control cell formatting, because we want
 * to always use particular format subclasses (from our swingui.table package)
 * to format particular columns.
 */
public class ProfileJTable extends JTable implements TableTextConverter {

    public ProfileJTable(TableModel model) {
        super(model);
        
        TableColumnModel cm = getColumnModel();
        for (int col = 0; col < cm.getColumnCount(); col++) {
            TableColumn tc = cm.getColumn(col);
            switch(ProfileColumn.values()[col]) {
            case DATABASE: case SCHEMA: case CATALOG: case TABLE: case COLUMN:
                tc.setCellRenderer(new SQLObjectTableCellRenderer());
                break;
            case RUNDATE:
                tc.setCellRenderer(new DateTableCellRenderer());
                break;
            case PERCENT_UNIQUE:
            case PERCENT_NULL:
                tc.setCellRenderer(new PercentTableCellRenderer());
                break;
            case AVERAGE_LENGTH:
                tc.setCellRenderer(new DecimalTableCellRenderer());
                break;
            case MIN_VALUE:
            case MAX_VALUE:
            case AVERAGE_VALUE:
            case TOP_VALUE:
                tc.setCellRenderer(new ValueTableCellRenderer());
                break;
            default:
                tc.setCellRenderer(null);
                break;
            }
        }
    }


    /**
     * Returns the ColumnProfileResult object that is associated with the
     * given row number.
     * 
     * @param row The visible row number as displayed in the table
     * @return The ColumnProfileResult object for that row
     */
    public ColumnProfileResult getColumnProfileResultForRow(int row) {
        return (ColumnProfileResult) 
                (getModel().getValueAt(
                        row, ProfileTableModel.CPR_PSEUDO_COLUMN_INDEX));
    }
    
    public TableModel getDataTableModel() {
        TableModelSortDecorator m1 = (TableModelSortDecorator) getModel();
        TableModelSearchDecorator m2 = (TableModelSearchDecorator) m1.getTableModel();
        ProfileTableModel m3 = (ProfileTableModel) m2.getTableModel();
        return m3;
    }
    
    public int modelIndex(int viewIndex) {
        TableModelSortDecorator m1 = (TableModelSortDecorator) getModel();        
        return m1.modelIndex(viewIndex);
    }
      
    public String getTextForCell(int row, int col) {
        // note: this will only work because we know all the renderers are jlabels
        JLabel renderer = (JLabel) getCellRenderer(row, col).getTableCellRendererComponent(this, getModel().getValueAt(row, getColumnModel().getColumn(col).getModelIndex()), false, false, row, col);
        return renderer.getText();
    }


}
