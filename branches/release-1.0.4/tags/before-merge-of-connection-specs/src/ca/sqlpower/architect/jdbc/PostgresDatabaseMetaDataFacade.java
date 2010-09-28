/*
 * Created on Jun 8, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import ca.sqlpower.sql.CachedRowSet;

/**
 * The PostgresDatabaseMetaDataFacade suppresses the list of databases which the Postgres driver
 * reports existing, but does not allow access to.
 *
 * @version $Id$
 */
public class PostgresDatabaseMetaDataFacade extends DatabaseMetaDataFacade {
    
    /**
     * Creates a new facade for PostgreSQL's DatabaseMetaData.
     */
    public PostgresDatabaseMetaDataFacade(DatabaseMetaData delegate) {
        super(delegate);
    }
	/**
     * The Postgres JDBC driver is able to return each Catalog Name, but ignores
     * requests to view schemas that belong to a Catalog other than the one 
     * you are connected to and instead always returns the same set of schenms.
 	 * 
     * To minimize confusion, only return the Catalog for the database you are 
     * connected to.  Use a sqlpower CachedRowSet to ensure resources are freed 
     * up once the query has been run.
     */
	public ResultSet getCatalogs() throws java.sql.SQLException {				
		// if the connection string had a catalog name, it will be set
		String theCatalog = getConnection().getCatalog();
		// if not, the the catalog name is the user name
		if (theCatalog == null || theCatalog.length() == 0) {
			theCatalog = getUserName();
		}
		Statement st = null;
		ResultSet rs = null;
		CachedRowSet crs = new CachedRowSet();
		st = getConnection().createStatement();
		rs = st.executeQuery("SELECT '" + theCatalog + "'");
		crs.populate(rs);
		rs.close();
		st.close();
		return crs;		
	}
}
