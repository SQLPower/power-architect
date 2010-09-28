package ca.sqlpower.architect.swingui.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class FreqValueTable extends JTable {


    public FreqValueTable(TableModel dm) {
        super(dm);
        
        TableColumnModel cm = getColumnModel();
        for (int col = 0; col < cm.getColumnCount(); col++) {
            TableColumn tc = cm.getColumn(col);
            switch(col) {
            case 0:
                tc.setCellRenderer(null);
                break;
            case 1:
                tc.setCellRenderer(new ValueTableCellRenderer());
                break;
            default:
                tc.setCellRenderer(null);
                break;
            }
        }
    }
    
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


}
