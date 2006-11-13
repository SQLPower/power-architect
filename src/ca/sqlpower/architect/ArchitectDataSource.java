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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
     * This is just for debugging purposes and prints out the difference between
     * two ArchitectDataSource
     * @param o2 the object containing the properties
     */
    public void diff(ArchitectDataSource o2) {
        Map<String,String> p2 = o2.properties;
        dump(properties, p2, DBCS_DRIVER_CLASS);
        dump(properties, p2, DBCS_URL);
        dump(properties, p2, PL_LOGICAL);
        dump(properties, p2, PL_TYPE);
        dump(properties, p2, PL_DSN);
        dump(properties, p2, PL_SCHEMA_OWNER);
        dump(properties, p2, PL_UID);
        dump(properties, p2, PL_PWD);
        dump(properties, p2, PL_TNS);
        dump(properties, p2, PL_DATABASE_NAME);
        dump(properties, p2, PL_IP);
        dump(properties, p2, PL_PORT); 
        Iterator<String> iter = properties.keySet().iterator();
        while (iter.hasNext()) {
            System.out.print(iter.next() + ", ");
        }
        System.out.println();
        Iterator<String> iter2 = p2.keySet().iterator();
        while (iter2.hasNext()) {
            System.out.print(iter2.next() + ", ");
        }
        System.out.println();
    }
    
    /**
     * This method is for debugging purposes to print out the differences between two maps
     * @param myMap first map to compare
     * @param otherMap second map for comparaing
     * @param key the property to compare
     */
	private void dump(Map<String, String> myMap, Map<String, String> otherMap, String key) {
        String val1 = myMap.get(key);
        String val2 = otherMap.get(key);
        System.out.print("Property=" + key + "; me=" + val1 + "; other=" + val2 + ';');
        if (val1 != null && val2 != null && !val1.equals(val2))
            System.out.print(" <-- DIFFERENT");
        System.out.println();
    }
    
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

    /**
     * Creates a new ArchitectDataSource with all properties set to null.
     */
	public ArchitectDataSource() {
		properties = new HashMap<String,String>();
	}

    /**
     * Copy constructor. Creates an independent copy of the given data source.
     * 
     * @param copyMe the ArchitectDataSource to make a copy of.
     */
    public ArchitectDataSource(ArchitectDataSource copyMe) {
        properties = new HashMap<String, String>(copyMe.properties);
    }
    
	/**
	 * The method that actually modifies the property map.
	 *
	 * @param key The key to use in the map (this will be a PL.INI property name)
	 * @param value The value that corresponds with the key
	 * @param propertyName The name of the Java Beans property that changed.  This will
	 * be the property name in the resulting PropertyChangeEvent.
	 * @return The old value of the property.
	 */
	private String putImpl(String key, String value, String propertyName) {
		String oldValue = get(key);
		properties.put(key, value);
		getPcs().firePropertyChange(propertyName, oldValue, value);
		return oldValue;
	}

	/**
	 * Adds the given key to the map.
	 *
	 * @param key The key to use.
	 * @param value The value to associate with key.
	 * @return The old value of the property.
	 */
	public String put(String key, String value) {
		return putImpl(key, value, key);
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
		return getDisplayName();
	}

	/**
	 * Compares all properties of this data source to those of the other.
	 * If there are any differences, returns false.  Otherwise, returns true.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
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

	/**
	 * Registers the given object as a listener to property changes on this
	 * ArchitectDataSource.
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		getPcs().addPropertyChangeListener(l);
	}

	/**
	 * Removes the given object from the listener list, if it was on that list.
	 * Does nothing if l is not a property change listener of this data source.
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		getPcs().removePropertyChangeListener(l);
	}

	/**
	 * Returns an unmodifiable view of the list of property change listeners.
	 */
	public List getPropertyChangeListeners() {
		return Collections.unmodifiableList(Arrays.asList(pcs.getPropertyChangeListeners()));
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
		putImpl(PL_LOGICAL, argName, "name");
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
		putImpl(PL_LOGICAL, argDisplayName, "name");
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
		putImpl(DBCS_URL, argUrl, "url");
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
		putImpl(DBCS_DRIVER_CLASS, argDriverClass, "driverClass");
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
	public void setUser(String argUser) {
		putImpl(PL_UID, argUser, "user");
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
		putImpl(PL_PWD, argPass, "pass");
	}

	public String getPlSchema() {
        return properties.get(PL_SCHEMA_OWNER);
    }

    public void setPlSchema(String schema) {
        putImpl(PL_SCHEMA_OWNER, schema, "plSchema");
    }

	public String getPlDbType() {
        return properties.get(PL_TYPE);
    }

	public void setPlDbType(String type) {
        putImpl(PL_TYPE, type, "plDbType");
    }

	public String getOdbcDsn() {
        return properties.get(PL_DSN);
    }

    public void setOdbcDsn(String dsn) {
        putImpl(PL_DSN, dsn, "odbcDsn");
    }
}
