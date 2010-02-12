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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.sqlobject.SQLColumn;

public class ColumnProfileResult extends AbstractProfileResult<SQLColumn> {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    @SuppressWarnings("unchecked")
    public static List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
                Arrays.asList(ColumnValueCount.class))); 

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
    @Constructor
    public ColumnProfileResult(@ConstructorParameter(propertyName="profiledObject") SQLColumn profiledObject, 
            @ConstructorParameter(propertyName="parentResult") TableProfileResult parentResult) {
        super(profiledObject);
        this.parentResult = parentResult;
    }

    @Accessor
    public double getAvgLength() {
        return avgLength;
    }

    @Mutator
    public void setAvgLength(double avgLength) {
        double oldLength = this.avgLength;
        this.avgLength = avgLength;
        firePropertyChange("avgLength", oldLength, avgLength);
    }

    /**
     * @return The average value as a Number object, or null if there were
     * 0 values.
     */
    @Accessor
    public Object getAvgValue() {
        return avgValue;
    }

    @Mutator
    public void setAvgValue(Object avgValue) {
        Object oldVal = this.avgValue;
        this.avgValue = avgValue;
        firePropertyChange("avgValue", oldVal, avgValue);
    }

    @Accessor
    public int getDistinctValueCount() {
        return distinctValueCount;
    }

    @Mutator
    public void setDistinctValueCount(int distinctValueCount) {
        int oldVal = this.distinctValueCount;
        this.distinctValueCount = distinctValueCount;
        firePropertyChange("distinctValueCount", oldVal, distinctValueCount);
    }

    @Accessor
    public int getMaxLength() {
        return maxLength;
    }

    @Mutator
    public void setMaxLength(int maxLength) {
        int oldLength = this.maxLength;
        this.maxLength = maxLength;
        firePropertyChange("maxLength", oldLength, maxLength);
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    @Accessor
    public Object getMaxValue() {
        return maxValue;
    }

    @Mutator
    public void setMaxValue(Object maxValue) {
        Object oldVal = this.maxValue;
        this.maxValue = maxValue;
        firePropertyChange("maxValue", oldVal, maxValue);
    }

    @Accessor
    public int getMinLength() {
        return minLength;
    }

    @Mutator
    public void setMinLength(int minLength) {
        int oldLength = this.minLength;
        this.minLength = minLength;
        firePropertyChange("minLength", oldLength, minLength);
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    @Accessor
    public Object getMinValue() {
        return minValue;
    }

    @Mutator
    public void setMinValue(Object minValue) {
        Object oldVal = this.minValue;
        this.minValue = minValue;
        firePropertyChange("minValue", oldVal, this.minValue);
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

    @Accessor
    public int getNullCount() {
        return nullCount;
    }

    @Mutator
    public void setNullCount(int nullCount) {
        int oldCount = this.nullCount;
        this.nullCount = nullCount;
        firePropertyChange("nullCount", oldCount, nullCount);
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
    
    @NonProperty
    public List<ColumnValueCount> getValueCount() {
        return topTen;
    }

    @Accessor
    public TableProfileResult getParentResult() {
        return parentResult;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType.isAssignableFrom(ColumnValueCount.class)) {
            return 0;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() { 
        return allowedChildTypes;
    }

    public List<? extends SPObject> getChildren() {
        List<SPObject> children = new ArrayList<SPObject>();
        children.addAll(topTen);
        return children;
    }
    
}
