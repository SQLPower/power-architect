package ca.sqlpower.architect.etl;

import java.util.Properties;

/**
 * The PLConnectionSpec class is a container for POWER*LOADER ODBC
 * Connection information (normally retrieved from the PL.ini file).
 */
public class PLConnectionSpec {

	public static final String CONNECTION_TYPE_ORACLE = "ORACLE";
	public static final String CONNECTION_TYPE_SQLSERVER = "SQL SERVER";
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

	public String getPwd()  {
		return props.getProperty("PWD");
	}

	public String getEngineExeutableName() throws UnknownDatabaseTypeException {
		String type = getDbType();
		if (type == null) {
			throw new UnknownDatabaseTypeException("<unspecified>");
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_SQLSERVER)
				   || type.equalsIgnoreCase(CONNECTION_TYPE_ACCESS)) {
			return "PowerLoader_odbc.exe";
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_ORACLE)) {
			return "PowerLoader_oracle.exe";
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}
}
