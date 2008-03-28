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

/**
 * A simple class for keeping track of a value, the number of occurrences
 * associated with it, and the percentage of occurences in the table.  
 * Instances of this class are used in the "Top N most
 * frequent values" property of a column's profile.
 */
public class ColumnValueCount {

    private Object value;
    private int count;
    private double percent;

    /**
     * Creates a new ColumnValueCount instance that associates the given value
     * with the given count.  Instances of this class are meant to be immtuable,
     * so if the give value object is mutable, you must not modify it.
     * 
     * @param value The value to associate the count with.  Null is allowed.
     * @param count The number of occurrences of <tt>value</tt> in the column
     * being profiled.
     * @param percent The percentage of occurrences in the table.
     */
    public ColumnValueCount(Object value, int count, double percent) {
        this.value = value;
        this.count = count;
        this.percent = percent;
    }
    
    public int getCount() {
        return count;
    }
    
    public Object getValue() {
        return value;
    }
    
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
}
