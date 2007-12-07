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

/**
 * A simple class for keeping track of a value and the number of occurrences
 * associated with it.  Instances of this class are used in the "Top N most
 * frequent values" property of a column's profile.
 */
public class ColumnValueCount {

    private Object value;
    private int count;

    /**
     * Creates a new ColumnValueCount instance that associates the given value
     * with the given count.  Instances of this class are meant to be immtuable,
     * so if the give value object is mutable, you must not modify it.
     * 
     * @param value The value to associate the count with.  Null is allowed.
     * @param count The number of occurrences of <tt>value</tt> in the column
     * being profiled.
     */
    public ColumnValueCount(Object value, int count) {
        this.value = value;
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
    
    public Object getValue() {
        return value;
    }
    
    /**
     * Compares this ColumnValueCount to the other object based on both the
     * value and the count.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ColumnValueCount)) {
            return false;
        }
        ColumnValueCount other = (ColumnValueCount) obj;
        
        if ((value == null ? other.value == null : value.equals(other.value)) && count == other.count)  {
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
