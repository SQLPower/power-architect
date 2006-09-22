package ca.sqlpower.architect.swingui.table;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
/*
 * This class is used to manipulate the sizing on columns within profiling. 
 * By ctrl + selecting the header, the column will automatically resize to fit
 * all the elements under it.  However at the moment, it will not expand fully
 * to its desired size due to the fact that the maximum amount of space a column
 * has is bounded by the other columns as well.  This problem could be fixed
 * if profile table has a horizontal scroll pane as well.
 */
public class TableModelColumnAutofit extends AbstractTableModel{

    private TableModel tableModel;
    private MouseListener mouseListener;
    private JTable table;
    private JTableHeader tableHeader;

    public TableModelColumnAutofit(TableModel tableModel, JTable table){
        this.tableModel = tableModel;
        this.table = table;
        tableHeader = table.getTableHeader();
        mouseListener = new MouseListener();        
    }
    
    public int getRowCount() {
        if (tableModel == null) return 0;
        else return tableModel.getRowCount();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (tableModel == null) return null;
        return tableModel.getColumnClass(columnIndex);
    }
    
    public int getColumnCount() {
        if (tableModel == null) return 0;
        else return tableModel.getColumnCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (tableModel == null) return null;
        else return tableModel.getValueAt(rowIndex, columnIndex);
    }
    @Override
    public String getColumnName(int column) {
        if (tableModel == null) return null;
        return tableModel.getColumnName(column);
    }

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader) {
        this.tableHeader.removeMouseListener(mouseListener);
        this.tableHeader = tableHeader;
        this.tableHeader.addMouseListener(mouseListener);
    }
  
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    public void initColumnSizes() {
        for (int i = 0; i < getColumnCount(); i++) {
            initSingleColumnSize(i);
        }
    }

    public void initSingleColumnSize(int colIndex){
        TableColumn column = null;
        Component comp = null;  
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            getTableHeader().getDefaultRenderer();
            column = table.getColumnModel().getColumn(colIndex);
            
            comp = headerRenderer.getTableCellRendererComponent(
                                 table, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            for (int j = 0; j < getRowCount(); j++) {                
                comp = table.getCellRenderer(j,colIndex).getTableCellRendererComponent(table,
                        table.getValueAt(j, colIndex),false,false,j, colIndex);                                        
                cellWidth = Math.max(cellWidth, comp.getPreferredSize().width);                
            }              
            column.setPreferredWidth((Math.max(headerWidth, cellWidth)));       
    }
    
    private class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            //XXX: Should change to a better condition for size editting
            //     for now, it's just ctrl click on the header
            if (e.isControlDown()){                                                    
                initSingleColumnSize(viewColumn);
            }            
        }
    }
    
}
