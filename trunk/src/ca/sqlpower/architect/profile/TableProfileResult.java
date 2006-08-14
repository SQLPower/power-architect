package ca.sqlpower.architect.profile;

import java.util.Date;

public class TableProfileResult extends ProfileResult {

    private int rowCount;

    public TableProfileResult() {

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
