package ca.sqlpower.architect.swingui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.table.AbstractTableModel;

import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileTableModel extends AbstractTableModel {

    ArrayList<String> columnNames;

    private ProfileManager profileManager;

    private List<ColumnProfileResult> resultList;

    public ProfileTableModel() {
        columnNames = new ArrayList<String>();
        columnNames.add("Database");
        columnNames.add("Catalog");
        columnNames.add("Schema");
        columnNames.add("Table");
        columnNames.add("Column");
        columnNames.add("Run Date");
        columnNames.add("Record Count");
        columnNames.add("Data Type");
        columnNames.add("# Null");
        columnNames.add("% Null");
        columnNames.add("# Unique");
        columnNames.add("% Unique");
        columnNames.add("Min Length");
        columnNames.add("Max Length");
        columnNames.add("Avg. Length");
        columnNames.add("Min Value");
        columnNames.add("Max Value");
        columnNames.add("Avg. Value");

    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public int getRowCount() {
        return resultList.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ColumnProfileResult columnProfile = resultList.get(rowIndex);
        SQLColumn col = columnProfile.getProfiledObject();
        int rowCount = ((TableProfileResult) profileManager.getResult(col.getParentTable())).getRowCount();

        if (columnIndex == 0) {
            return ArchitectUtils.getAncestor(col,SQLDatabase.class);
        } else if (columnIndex == 1) {
            return ArchitectUtils.getAncestor(col,SQLCatalog.class);
        } else if (columnIndex == 2) {
            return ArchitectUtils.getAncestor(col,SQLSchema.class);
        } else if (columnIndex == 3) {
            return ArchitectUtils.getAncestor(col,SQLTable.class);
        } else if (columnIndex == 4) {
            return col;
        } else if (columnIndex == 5) {
            // Run date          
            return columnProfile.getCreateStartTime();
        } else if (columnIndex == 6) {
            // Row Count
            return rowCount;
        } else if (columnIndex == 7) {
            // data type
            DDLGenerator gddl;
            try {
                gddl = DDLUtils.createDDLGenerator(col.getParentTable().getParentDatabase().getDataSource());
                return gddl.columnType(col);
            } catch (Exception e) {
                throw new ArchitectRuntimeException(new ArchitectException(
                        "Unable to get DDL information.  Do we have a valid data source?", e));
            }
        } else if (columnIndex == 8) {
            //  Number of null records
            return columnProfile.getNullCount();
        } else if (columnIndex == 9) {
            //  Percent null records
            return rowCount == 0 ? null :  (double)columnProfile.getNullCount() / rowCount ;
        } else if (columnIndex == 10) {
            //  Number of unique records
            return columnProfile.getDistinctValueCount();
        } else if (columnIndex == 11) {
            //  percent of unique records
            return rowCount == 0 ? null : (double)columnProfile.getDistinctValueCount() / rowCount;
        } else if (columnIndex == 12) {
            //  min Length
            return columnProfile.getMinLength();
        } else if (columnIndex == 13) {
            //  Max Length
            return columnProfile.getMaxLength();
        } else if (columnIndex == 14) {
            //  Avg Length
            return columnProfile.getAvgLength();
        } else if (columnIndex == 15) {
            //  min Value
            return columnProfile.getMinValue();
        } else if (columnIndex == 16) {
            //  Max value
            return columnProfile.getMaxValue();
        } else if (columnIndex == 17) {
            //  Avg Value
            return columnProfile.getAvgValue();
        } else {
            throw new IllegalArgumentException("Column Index out of bounds");
        }
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
    
    public void refresh(){
        resultList = new ArrayList<ColumnProfileResult>();
        for (ProfileResult pr : profileManager.getResults().values()) {
            if (pr instanceof ColumnProfileResult) {
                resultList.add((ColumnProfileResult) pr);
            }
        }
        Collections.sort(resultList);
        fireTableDataChanged();
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
        refresh();
    }

}
