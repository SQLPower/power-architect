package ca.sqlpower.architect.swingui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableModelColumnAutofit extends AbstractTableModel{

    private TableModel tableModel;
    private MouseListener mouseListener;
    private ProfileTable table;
    private JTableHeader tableHeader;

    public TableModelColumnAutofit(TableModel tableModel, ProfileTable table){
        this.tableModel = tableModel;
        this.table = table;
        mouseListener = new MouseListener();        
    }
    
    public int getRowCount() {
        if (tableModel == null) return 0;
        else return tableModel.getRowCount();
    }

    public int getColumnCount() {
        if (tableModel == null) return 0;
        else return tableModel.getColumnCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (tableModel == null) return null;
        else return tableModel.getValueAt(rowIndex, columnIndex);
    }
    

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader) {
        this.tableHeader = tableHeader;
        this.tableHeader.addMouseListener(mouseListener);
    }
  
    
    private class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            //XXX: Change this condition for size editting            
            if (e.isControlDown()){                                                    
                table.initSingleColumnSize(viewColumn);
            }            
        }
    }
    
}
