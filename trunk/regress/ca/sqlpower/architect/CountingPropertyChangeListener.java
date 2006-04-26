package ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The CountingPropertyChangeListener class helps with testing beans with
 * bound properties.  It can be registered as a listener on a bean under test,
 * and then queried for the values of its most recently received property change event.
 * 
 * $Id$
 */
public class CountingPropertyChangeListener implements PropertyChangeListener {
	private int propertyChangeCount;
	private String lastPropertyChange;
	private Object lastSource;
	private Object lastOldValue;
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

	public int getPropertyChangeCount() {
		return propertyChangeCount;
	}

}
