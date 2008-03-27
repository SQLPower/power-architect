/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.profile;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;

public class ColumnProfileResult extends AbstractProfileResult<SQLColumn> {

    private static final Logger logger = Logger.getLogger(ColumnProfileResult.class);

    private int distinctValueCount;
    private Object minValue;
    private Object maxValue;
    private Object avgValue;
    private int minLength;
    private int maxLength;
    private double avgLength;
    private int nullCount;
    private List<ColumnValueCount> topTen = new ArrayList<ColumnValueCount>();
    
    private final TableProfileResult parentResult;

    /**
     * This creates a column profile result which stores information about a profiled column.
     */
    public ColumnProfileResult(SQLColumn profiledObject, TableProfileResult parentResult) {
        super(profiledObject);
        this.parentResult = parentResult;
    }

    public double getAvgLength() {
        return avgLength;
    }

    public void setAvgLength(double avgLength) {
        this.avgLength = avgLength;
    }

    /**
     * @return The average value as a Number object, or null if there were
     * 0 values.
     */
    public Object getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Object avgValue) {
        this.avgValue = avgValue;
    }

    public int getDistinctValueCount() {
        return distinctValueCount;
    }

    public void setDistinctValueCount(int distinctValueCount) {
        this.distinctValueCount = distinctValueCount;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    @Override
    public String toString() {
        return "[ColumnProfileResult:" +
        "; distinctValues: "+distinctValueCount+
        "; minLength: "+minLength+
        "; maxLength: "+maxLength+
        "; avgLength: "+avgLength+
        "; minValue: "+getMinValue()+
        "; maxValue: "+getMaxValue()+
        "; avgValue: "+avgValue+
        "; nullCount: "+getNullCount()+ "]";
    }

    public int getNullCount() {
        return nullCount;
    }

    public void setNullCount(int nullCount) {
        this.nullCount = nullCount;
    }

    public void addValueCount(Object value, int count) {
        double per =  count/(double)parentResult.getRowCount();

        ColumnValueCount columnValueCount = new ColumnValueCount(value,count, per);
        if (!topTen.contains(columnValueCount)) {
            topTen.add(columnValueCount);
            logger.debug("Added Value Count: Value: " + value + " Count: " + count);
        }
    }

    public void addValueCount(ColumnValueCount value) {
        topTen.add(value);
    }
    public List<ColumnValueCount> getValueCount() {
        return topTen;
    }

    public TableProfileResult getParentResult() {
        return parentResult;
    }
}
