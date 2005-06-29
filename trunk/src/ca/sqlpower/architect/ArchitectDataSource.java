/*
 * Created on Jun 28, 2005
 *
 * This code belongs to SQL Power.
 */
package ca.sqlpower.architect;

/**
 * The ArchitectDataSource represents a connection that the Power Loader or
 * the Architect can connect to.  It holds all the information required for
 * making JDBC, ODBC, or native Oracle connections (depending on what type
 * of database the connection is for). 
 * 
 * @see ca.sqlpower.architect.PlDotIni
 * @author jack
 */

import java.util.*;
import java.beans.*;
public class ArchitectDataSource {
	
	protected Map properties;

	/*
	 * constants used as keys to get into the properties
	 * map.  the shared heritage of this class explains why
	 * some constants use the prefix DBCS_ while others use
	 * the prefix PL_.
	 */
	public static final String DBCS_DRIVER_CLASS = "JDBC Driver Class";
	public static final String DBCS_URL = "JDBC URL";
	
	public static final String PL_LOGICAL = "Logical";
	public static final String PL_TYPE = "Type";
	public static final String PL_DSN = "DSN";
	public static final String PL_SCHEMA_OWNER = "PL Schema Owner";
	public static final String PL_UID = "UID";
	public static final String PL_PWD = "PWD";
	public static final String PL_TNS = "TNS";
	public static final String PL_DATABASE_NAME = "Database Name";
	public static final String PL_IP = "IP";
	public static final String PL_PORT = "PORT";

	protected transient PropertyChangeSupport pcs;
	protected PropertyChangeSupport getPcs() {
		if (pcs == null) pcs = new PropertyChangeSupport(this);
		return pcs;
	}

	public ArchitectDataSource() {
		properties = new HashMap();
	}

	public String put(String key, String value) {		
		String oldValue = get(key);
		properties.put(key,value);
		getPcs().firePropertyChange(key,oldValue,value);
		return oldValue;
	}
	
	public String get(String key) {
		return (String) properties.get(key);
	}
	
	public Map getPropertiesMap() {
		return properties;
	}
	
	/**
	 * Prints some info from this DBCS.  For use in debugging.
	 */
	public String toString() {
		return "ArchitectDataSource: "+getDisplayName()+", "+getDriverClass()+", "+getUrl();
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
		return get(PL_LOGICAL);
	}

	/**
	 * Sets the value of name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setName(String argName){
		put(PL_LOGICAL, argName);
	}

	/**
	 * Gets the value of displayName
	 *
	 * @return the value of displayName
	 */
	public String getDisplayName() {
		return get(PL_LOGICAL);
	}

	/**
	 * Sets the value of displayName
	 *
	 * @param argDisplayName Value to assign to this.displayName
	 */
	public void setDisplayName(String argDisplayName){
		put(PL_LOGICAL, argDisplayName);
	}

	/**
	 * Gets the value of url
	 *
	 * @return the value of url
	 */
	public String getUrl() {
		return get(DBCS_URL);
	}

	/**
	 * Sets the value of url
	 *
	 * @param argUrl Value to assign to this.url
	 */
	public void setUrl(String argUrl) {
		put(DBCS_URL, argUrl);
	}

	/**
	 * Gets the value of driverClass
	 *
	 * @return the value of driverClass
	 */
	public String getDriverClass() {
		return get(DBCS_DRIVER_CLASS);
	}

	/**
	 * Sets the value of driverClass
	 *
	 * @param argDriverClass Value to assign to this.driverClass
	 */
	public void setDriverClass(String argDriverClass){
		put(DBCS_DRIVER_CLASS, argDriverClass);
	}

	/**
	 * Gets the value of user
	 *
	 * @return the value of user
	 */
	public String getUser() {
		return get(PL_UID);
	}

	/**
	 * Sets the value of user
	 *
	 * @param argUser Value to assign to this.user
	 */
	public void setUser(String argUser){
		put(PL_UID, argUser);
	}

	/**
	 * Gets the value of pass
	 *
	 * @return the value of pass
	 */
	public String getPass() {
		return get(PL_PWD);
	}

	/**
	 * Sets the value of pass
	 *
	 * @param argPass Value to assign to this.pass
	 */
	public void setPass(String argPass){
		put(PL_PWD, argPass);
	}
}
