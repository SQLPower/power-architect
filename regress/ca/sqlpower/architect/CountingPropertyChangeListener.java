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
package ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The CountingPropertyChangeListener class helps with testing beans with
 * bound properties.  It can be registered as a listener on a bean under test,
 * and then queried for the values of its most recently received property change event.
 * 
 * $Id$
 */
public class CountingPropertyChangeListener implements PropertyChangeListener {
    
    /**
     * Individual counts of changes by property name.
     */
    private Map<String, Integer> propertyChangeCounts = new HashMap<String, Integer>();
    
    /**
     * The overall number of property changes (summed across all property names).
     */
	private int propertyChangeCount;
    
    /**
     * The most recent propery name to have changed.
     */
	private String lastPropertyChange;
    
    /**
     * The most recent property change source object.
     */
	private Object lastSource;
    
    /**
     * The most recent "old" property value (the previous value of the <i>lastPropertyChange</i>
     * property of <i>lastSource</i>).
     */
	private Object lastOldValue;

    /**
     * The most recent "new" property value (the current value of the <i>lastPropertyChange</i>
     * property of <i>lastSource</i>).
     */
	private Object lastNewValue;
	
	/**
	 * Copies all the event information into the corresponding instance variables.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		propertyChangeCount++;
		lastPropertyChange = evt.getPropertyName();
		lastSource = evt.getSource();
		lastOldValue = evt.getOldValue();
		lastNewValue = evt.getNewValue();
        
        Integer oldCount = propertyChangeCounts.get(evt.getPropertyName());
        if (oldCount == null) {
            oldCount = 0;
        }
        propertyChangeCounts.put(evt.getPropertyName(), oldCount + 1);
	}

	public Object getLastNewValue() {
		return lastNewValue;
	}

	public Object getLastOldValue() {
		return lastOldValue;
	}

	public String getLastPropertyChange() {
		return lastPropertyChange;
	}

	public Object getLastSource() {
		return lastSource;
	}

    /**
     * Returns the total number of property change events processed by
     * this instance, regardless of property name.
     */
    public int getPropertyChangeCount() {
        return propertyChangeCount;
    }

    /**
     * Returns the number of property change events that applied to the
     * given property name. 
     */
    public int getPropertyChangeCount(String propName) {
        Integer count = propertyChangeCounts.get(propName);
        if (count == null) {
            return 0;
        } else {
            return count.intValue();
        }
    }

}
