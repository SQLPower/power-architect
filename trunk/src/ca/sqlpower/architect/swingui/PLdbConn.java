package ca.sqlpower.architect.swingui;

import java.util.Properties;

/**
 * Is a container for POWER*LOADER ODBC Connection stored in PL.ini file
 */
public class PLdbConn {
	Properties props;
	
	public PLdbConn() {
		props = new Properties();
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	// ----------------- accessors and mutators -------------------
// 		String label[] = new String[]{"Logical=",
// 									  "Type=",
// 									  "PL Schema Owner=",
// 									  "UID=",
// 									  "PWD=",
// 									  "TNS Name=",
// 									  "Database Name="};
	
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
		} else if (type.equalsIgnoreCase("SQL SERVER")) {
			return "PowerLoader_odbc.exe";
		} else if (type.equalsIgnoreCase("ORACLE")) {
			return "PowerLoader_oracle.exe";
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}
}
