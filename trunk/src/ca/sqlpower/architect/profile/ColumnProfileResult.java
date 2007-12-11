/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        ColumnValueCount columnValueCount = new ColumnValueCount(value,count);
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
