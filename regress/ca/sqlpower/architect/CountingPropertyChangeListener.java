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
