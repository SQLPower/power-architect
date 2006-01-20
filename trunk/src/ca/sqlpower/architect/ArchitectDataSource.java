/*
 * Created on Jun 28, 2005
 *
 * This code belongs to SQL Power.
 */
package ca.sqlpower.architect;

/**
 * The ArchitectDataSource represents a database that the Power Loader or
 * the Architect can connect to.  It holds all the information required for
 * making JDBC, ODBC, or native Oracle connections (depending on what type
 * of database the connection is for). 
 * 
 * @see ca.sqlpower.architect.PlDotIni
 * @author jack, jonathan
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ArchitectDataSource {
	
	/**
	 * Compares this data source to the given data source by comparing
	 * the respecitive fields in the following order:
	 * <ol>
	 *  <li>name
	 *  <li>url
	 *  <li>driver class
	 *  <li>user
	 *  <li>password
	 * </ol>
	 * 
	 * <p>Note: the ArchitectDataSource doesn't implement Comparable itself
	 * because it would be difficult and unnecessary to do a complete comparison
	 * which is consistent with equals and hashCode.
	 */
	public static class DefaultComparator implements Comparator<ArchitectDataSource> {
		
		/**
		 * Performs the comparison described in the class comment.
		 * 
		 * @param o the ArchitectDataSource object to compare with.
		 * @return &lt;0 if this data source comes before o; 0 if they
		 *  are equal; &gt;0 otherwise.
		 * @throws NullPointerException if o==null
		 * @throws ClassCastException if o is not an instance of ArchitectDataSource
		 */
		public int compare(ArchitectDataSource ds1, ArchitectDataSource ds2) {
			if (ds1 == ds2) return 0;
			int tmp;
			String v1, v2;
			
			v1 = ds1.getName();
			v2 = ds2.getName();
			if (v1 == null && v2 != null) return -1;
			else if (v1 != null && v2 == null) return 1;
			else if (v1 != null && v2 != null) {
				tmp = v1.compareTo(v2);
			} else {
				tmp = 0;
			}
			if (tmp != 0) return tmp;
			
			v1 = ds1.getUrl();
			v2 = ds2.getUrl();
			if (v1 == null && v2 != null) return -1;
			else if (v1 != null && v2 == null) return 1;
			else if (v1 != null && v2 != null) {
				tmp = v1.compareTo(v2);
			} else {
				tmp = 0;
			}
			if (tmp != 0) return tmp;
			
			v1 = ds1.getDriverClass();
			v2 = ds2.getDriverClass();
			if (v1 == null && v2 != null) return -1;
			else if (v1 != null && v2 == null) return 1;
			else if (v1 != null && v2 != null) {
				tmp = v1.compareTo(v2);
			} else {
				tmp = 0;
			}
			if (tmp != 0) return tmp;
			
			v1 = ds1.getUser();
			v2 = ds2.getUser();
			if (v1 == null && v2 != null) return -1;
			else if (v1 != null && v2 == null) return 1;
			else if (v1 != null && v2 != null) {
				tmp = v1.compareTo(v2);
			} else {
				tmp = 0;
			}
			if (tmp != 0) return tmp;
			
			v1 = ds1.getPass();
			v2 = ds2.getPass();
			if (v1 == null && v2 != null) return -1;
			else if (v1 != null && v2 == null) return 1;
			else if (v1 != null && v2 != null) {
				tmp = v1.compareTo(v2);
			} else {
				tmp = 0;
			}
			if (tmp != 0) return tmp;
			
			return 0;
		}
	}

	private Map<String,String> properties;

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
	public static final String PL_TNS = "TNS Name";
	public static final String PL_DATABASE_NAME = "Database Name";
	public static final String PL_IP = "IP";
	public static final String PL_PORT = "PORT";

	/**
	 * This field is transient; don't access it directly becuase it
	 * will disappear when this instance is serialized.
	 */
	private transient PropertyChangeSupport pcs;
	
	/**
	 * Returns this DataSource's property change support, creating
	 * a new one if necessary.
	 */
	private PropertyChangeSupport getPcs() {
		if (pcs == null) pcs = new PropertyChangeSupport(this);
		return pcs;
	}

	public ArchitectDataSource() {
		properties = new HashMap<String,String>();
	}

	public String put(String key, String value) {		
		String oldValue = get(key);
		properties.put(key,value);
		getPcs().firePropertyChange(key,oldValue,value);
		return oldValue;
	}
	
	public String get(String key) {
		return properties.get(key);
	}
	
	/**
	 * Returns a read-only view of this data source's properties.
	 */
	public Map<String,String> getPropertiesMap() {
		return Collections.unmodifiableMap(properties);
	}
	
	/**
	 * Prints some info from this data source.  For use in debugging.
	 */
	public String toString() {
		return "ArchitectDataSource: "+getDisplayName()+", "+getDriverClass()+", "+getUrl();
	}
	
	/**
	 * Compares all properties of this data source to those of the other.
	 * If there are any differences, returns false.  Otherwise, returns true.
	 */
	@Override
	public boolean equals(Object o) {
		ArchitectDataSource other = (ArchitectDataSource) o;
		return this.properties.equals(other.properties);
	}
	
	/**
	 * Returns a hash that depends on all property values.
	 */
	@Override
	public int hashCode() {
		return properties.hashCode();
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
	
	public String getPlSchema() {
        return properties.get(PL_SCHEMA_OWNER);
    }

    public void setPlSchema(String schema) {
        put(PL_SCHEMA_OWNER, schema);
    }

	public String getPlDbType() {
        return properties.get(PL_TYPE);
    }

	public void setPlDbType(String type) {
        put(PL_TYPE, type);
    }

	public String getOdbcDsn() {
        return properties.get(PL_DSN);
    }

    public void setOdbcDsn(String dsn) {
        put(PL_DSN, dsn);
    }
}
