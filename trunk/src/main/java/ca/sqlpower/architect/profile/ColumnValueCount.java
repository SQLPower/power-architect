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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;

/**
 * A simple class for keeping track of a value, the number of occurrences
 * associated with it, and the percentage of occurences in the table.  
 * Instances of this class are used in the "Top N most
 * frequent values" property of a column's profile.
 */
public class ColumnValueCount extends AbstractSPObject {

    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();

    /**
     * String name that identifies this column value count as the count of all
     * other columns in the system not including the top n in the column
     * profile. The flag in this class for representing other values should be
     * checked for the official decision if this value count is all of the other
     * values.
     * 
     * This is used the the TableModelSortDecorator in the library in comparing table
     * rows so if you update this here, update it there as well.
     */
    public static final String OTHER_VALUE_OBJECT = "Other Values";
    
    private final Object value;
    private final int count;
    private final double percent;

    /**
     * If true this value count is the sum and precent of the values in the
     * column that does not match with the rest of values already counted for
     * the column profile. Each column profile should have only one value count
     * with this flag set to true.
     */
    private final boolean otherValues;

    /**
     * Creates a new ColumnValueCount instance that associates the given value
     * with the given count. Instances of this class are meant to be immtuable,
     * so if the give value object is mutable, you must not modify it.
     * 
     * @param value
     *            The value to associate the count with. Null is allowed.
     * @param count
     *            The number of occurrences of <tt>value</tt> in the column
     *            being profiled.
     * @param percent
     *            The percentage of occurrences in the table.
     * @param otherValues
     *            If true this value count represents the count of all other
     *            values in the column not accounted for by the rest of the
     *            value counts under the column profile. The column profile
     *            should have only one value count object with this flag set to
     *            true. If true the value should be set to
     *            {@link #OTHER_VALUE_OBJECT}, either way the value will be set
     *            to {@link #OTHER_VALUE_OBJECT}.
     * 
     */
    @Constructor
    public ColumnValueCount(@ConstructorParameter(propertyName="value") Object value, 
            @ConstructorParameter(propertyName="count") int count, 
            @ConstructorParameter(propertyName="percent") double percent,
            @ConstructorParameter(propertyName="otherValues") boolean otherValues) {
        setName("New Column Value Count");
        this.count = count;
        this.percent = percent;
        //Checking with .equals to reduce complexity in the persistence layer.
        if (otherValues && !value.equals(OTHER_VALUE_OBJECT)) {
            throw new IllegalArgumentException("The other values object should equal the static value in this class.");
        }
        if (otherValues) {
            this.value = OTHER_VALUE_OBJECT;
        } else {
            this.value = value;
        }
        this.otherValues = otherValues;
    }
    
    public ColumnValueCount(ColumnValueCount cvcToCopy) {
        setName(cvcToCopy.getName());
        this.value = cvcToCopy.value;
        this.count = cvcToCopy.count;
        this.percent = cvcToCopy.percent;
        this.otherValues = cvcToCopy.isOtherValues();
    }
    
    @Accessor
    public int getCount() {
        return count;
    }
    
    @Accessor
    public Object getValue() {
        return value;
    }
    
    @Accessor
    public double getPercent() {
        return percent;
    }
    
    /**
     * Compares this ColumnValueCount to the other object based on the
     * value, the count, and the percent.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ColumnValueCount)) {
            return false;
        }
        ColumnValueCount other = (ColumnValueCount) obj;
        
        if ((value == null ? other.value == null : value.equals(other.value)) 
                && count == other.count && percent == other.percent)  {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        if (value != null) {
            result = 37 * result + value.hashCode();
        }
        result = 37 * result + count;
        return result;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getChildren() {     
        return Collections.emptyList();
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        
    }
    
    @Override
    @Accessor
    public ColumnProfileResult getParent() {
        return (ColumnProfileResult) super.getParent();
    }
    
    @Override
    @Mutator
    public void setParent(SPObject parent) {
        if (!(parent instanceof ColumnProfileResult || parent == null)) {
            throw new IllegalArgumentException("The parent of " + this + " must be a " + 
                    ColumnProfileResult.class + " object.");
        }
        super.setParent(parent);
    }

    @Accessor
    public boolean isOtherValues() {
        return otherValues;
    }
}
