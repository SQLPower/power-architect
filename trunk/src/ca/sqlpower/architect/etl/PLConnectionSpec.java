package ca.sqlpower.architect.etl;

import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * The PLConnectionSpec class is a container for POWER*LOADER ODBC
 * Connection information (normally retrieved from the PL.ini file).
 */
public class PLConnectionSpec {

	private static final Logger logger = Logger.getLogger(PLConnectionSpec.class);


	public static final String CONNECTION_TYPE_ORACLE = "ORACLE";
	public static final String CONNECTION_TYPE_SQLSERVER = "SQL SERVER";
	public static final String CONNECTION_TYPE_POSTGRES = "POSTGRES";
	public static final String CONNECTION_TYPE_ACCESS = "ACCESS";
	
	Properties props;
	
	public PLConnectionSpec() {
		props = new Properties();
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	// ----------------- accessors and mutators -------------------
	
	public String getLogical()  {
		return props.getProperty("Logical");
	}

	public String getDbType()  {
		return props.getProperty("Type");
	}

	public String getPlsOwner()  {
		return props.getProperty("PL Schema Owner");
	}

	public String getTNSName() {
		return props.getProperty("TNS Name");
	}

	public String getUid()  {
		return props.getProperty("UID");
	}

	public String getDSN() {
		return props.getProperty("DSN");
	}		

	public String getPwd()  {
		if (props.getProperty("PWD") == null) {
			return null;
		} else {
			return PLUtils.decryptPlIniPassword(9, props.getProperty("PWD"));
		}
	}

	public String getEngineExecutableName() throws UnknownDatabaseTypeException {
		String type = getDbType();
		if (type == null) {
			throw new UnknownDatabaseTypeException("<unspecified>");
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_SQLSERVER)
				   || type.equalsIgnoreCase(CONNECTION_TYPE_ACCESS)
                   || type.equalsIgnoreCase(CONNECTION_TYPE_POSTGRES)) {
			return "PowerLoader_odbc.exe";
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_ORACLE)) {
			return "PowerLoader_oracle.exe";
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}

	/**
	 * Returns the correct argument for USER= when running the PL engine.
	 *
	 * Use the same rules as engine executable name to decide what 
	 * kind of connection string to return.
	 */
	public String getEngineConnectString() throws UnknownDatabaseTypeException {
		logger.debug("get engine connect String PWD: " + getPwd());
		String type = getDbType();
		if (type == null) {
			throw new UnknownDatabaseTypeException("<unspecified>");
		}
		if (type.equalsIgnoreCase(CONNECTION_TYPE_SQLSERVER) 
		    || type.equalsIgnoreCase(CONNECTION_TYPE_ACCESS)
		    || type.equalsIgnoreCase(CONNECTION_TYPE_POSTGRES)) {
				return getUid()+"/"+getPwd()+"@"+getLogical();
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_ORACLE)) {
				return getUid()+"/"+getPwd()+"@"+getTNSName();
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}
}
