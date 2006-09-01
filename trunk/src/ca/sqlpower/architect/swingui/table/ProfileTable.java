package ca.sqlpower.architect.swingui.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileColumn;

/**
 * Override JTable methods that control cell formatting, because we want
 * to always use particular format subclasses (from c.s.a.swingui.table)
 * to format particular columns.
 *
 */
public class ProfileTable extends JTable implements TableTextConverter {

    public ProfileTable(TableModel model) {
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
    
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    public void initColumnSizes() {
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            getTableHeader().getDefaultRenderer();

        for (int i = 0; i < getColumnCount(); i++) {

            column = getColumnModel().getColumn(i);
            cellWidth = 0;
            comp = headerRenderer.getTableCellRendererComponent(
                                 this, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            for (int j = 0; j < getRowCount(); j++) {
                comp = getCellRenderer(j,i).getTableCellRendererComponent(this,
                        getModel().getValueAt(j,i),false,false,j,i);

                cellWidth = Math.max(cellWidth, comp.getPreferredSize().width);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public String getTextForCell(int row, int col) {
        // note: this will only work because we know all the renderers are jlabels
        JLabel renderer = (JLabel) getCellRenderer(row, col).getTableCellRendererComponent(this, getModel().getValueAt(row, col), false, false, row, col);
        return renderer.getText();
    }
}
