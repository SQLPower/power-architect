package ca.sqlpower.architect.profile;

import java.util.Date;

import ca.sqlpower.architect.SQLTable;

public class TableProfileResult extends ProfileResult<SQLTable> {

    private int rowCount;

    public TableProfileResult(SQLTable profiledObject) {
        super(profiledObject);
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public String toString() {
        return "RowCount:" + rowCount +
                "   Run Date:[" + new Date(getCreateStartTime()) + "]" +
                "   Time To Create:" + getTimeToCreate() + "ms";
    }
}
