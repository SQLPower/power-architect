/*
 * Created on Jun 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect;

/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.util.*;
import java.beans.*;

public class ArchitectDataSource {
	
	protected Map properties;

	protected String name;
	protected String displayName;
	protected String driverClass;
	protected String url;
	protected String user;
	protected String pass;

	protected transient PropertyChangeSupport pcs;
	protected PropertyChangeSupport getPcs() {
		if (pcs == null) pcs = new PropertyChangeSupport(this);
		return pcs;
	}

	public ArchitectDataSource() {
		properties = new HashMap();
	}

	public Object put(String key, String value) {
		return value;
	}
	
	/**
	 * Prints some info from this DBCS.  For use in debugging.
	 */
	public String toString() {
		return "ArchitectDataSource: "+name+", "+displayName+", "+driverClass+", "+url;
	}

	// --------------------- property change ---------------------------
	public void addPropertyChangeListener(PropertyChangeListener l) {
		getPcs().addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		getPcs().removePropertyChangeListener(l);
	}

	// ------------------- accessors and mutators ------------------------

	/**
	 * Gets the value of name
	 *
	 * @return the value of name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the value of name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setName(String argName){
		String oldValue = name;
		this.name = argName;
		getPcs().firePropertyChange("name", oldValue, name);
	}

	/**
	 * Gets the value of displayName
	 *
	 * @return the value of displayName
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Sets the value of displayName
	 *
	 * @param argDisplayName Value to assign to this.displayName
	 */
	public void setDisplayName(String argDisplayName){
		String oldValue = displayName;
		this.displayName = argDisplayName;
		getPcs().firePropertyChange("displayName", oldValue, displayName);
	}

	/**
	 * Gets the value of url
	 *
	 * @return the value of url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Sets the value of url
	 *
	 * @param argUrl Value to assign to this.url
	 */
	public void setUrl(String argUrl) {
		String oldValue = url;
		this.url = argUrl;
		getPcs().firePropertyChange("url", oldValue, url);
	}

	/**
	 * Gets the value of driverClass
	 *
	 * @return the value of driverClass
	 */
	public String getDriverClass() {
		return this.driverClass;
	}

	/**
	 * Sets the value of driverClass
	 *
	 * @param argDriverClass Value to assign to this.driverClass
	 */
	public void setDriverClass(String argDriverClass){
		String oldValue = driverClass;
		this.driverClass = argDriverClass;
		getPcs().firePropertyChange("driverClass", oldValue, driverClass);
	}

	/**
	 * Gets the value of user
	 *
	 * @return the value of user
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Sets the value of user
	 *
	 * @param argUser Value to assign to this.user
	 */
	public void setUser(String argUser){
		String oldValue = user;
		this.user = argUser;
		getPcs().firePropertyChange("user", oldValue, user);
	}

	/**
	 * Gets the value of pass
	 *
	 * @return the value of pass
	 */
	public String getPass() {
		return this.pass;
	}

	/**
	 * Sets the value of pass
	 *
	 * @param argPass Value to assign to this.pass
	 */
	public void setPass(String argPass){
		String oldValue = pass;
		this.pass = argPass;
		getPcs().firePropertyChange("pass", oldValue, pass);
	}
}
