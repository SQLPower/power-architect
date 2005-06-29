package ca.sqlpower.architect.etl;

import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * The PLConnectionSpec class is a container for POWER*LOADER ODBC
 * Connection information (normally retrieved from the PL.ini file).
 */
public class PLConnectionSpec {

	private static final Logger logger = Logger.getLogger(PLConnectionSpec.class);
	
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
	    return props.getProperty("PWD");
	}
}
