package ca.sqlpower.architect.swingui.table;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;



public class FreqValueCountTableModel extends AbstractTableModel {

    private ColumnProfileResult profile;    
    
    private static final String COUNT="COUNT";
    private static final String VALUE="VALUE";
    
    public FreqValueCountTableModel(ColumnProfileResult profile) {
        super();
        this.profile = profile;
    }



    @Override
    public String getColumnName(int column) {
        if ( column == 0 ) {
            return COUNT;
        } else if ( column == 1 ) {
            return VALUE;
        } else {
            throw new IllegalStateException("Unknown Column Index:"+column);
        }
    }
    
    
    
    public int getRowCount() {
        return profile==null?0:profile.getValueCount().size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( profile == null )
            return null;
        
        if ( columnIndex == 0 ) {
            return profile.getValueCount().get(rowIndex).getCount();
        } else if ( columnIndex == 1 ) {
            return profile.getValueCount().get(rowIndex).getValue();
        } else {
            throw new IllegalStateException("Unknown Column Index:"+columnIndex);
        }
    }

}
