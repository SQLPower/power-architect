package ca.sqlpower.architect.swingui;


/**
 * Is a container for POWER*LOADER ODBC Connection stored in PL.ini file
 */
public class PLdbConn {
	protected String  logical;
	protected String  dbType;
    protected String  plsOwner;
	protected String  dbName;
	
	
	public PLdbConn() {
		this.logical  = new String();
		this.dbType   = new String();
		this.plsOwner = new String();
		this.dbName   = new String();
	}

	// ----------------- accessors and mutators -------------------

	
	public String getLogical()  {
		return this.logical;
	}

	public String getDbType()  {
		return this.dbType;
	}

	public String getPlsOwner()  {
		return this.plsOwner;
	}

	public String getDbName()  {
		return this.dbName;
	}


	public void setLogical(String logical)  {
		this.logical = logical;
	}

	public void setDbType(String dbType)  {
		this.dbType = dbType;
	}

	public void setPlsOwner(String plsOwner)  {
		this.plsOwner = plsOwner;
	}

	public void setDbName(String dbName)  {
		this.dbName = dbName;
	}

}
