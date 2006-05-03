/*
 * Created on Jun 8, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import ca.sqlpower.sql.CachedRowSet;

/**
 * The PostgresDatabaseMetaDataDecorator suppresses the list of databases which the Postgres driver
 * reports existing, but does not allow access to.
 *
 * @version $Id$
 */
public class PostgresDatabaseMetaDataDecorator extends DatabaseMetaDataDecorator {
    
    /** XXX Make a Connections Panel extention to let you set this kind of thing. */
    public static final int UGLY_DEFAULT_VARCHAR_SIZE = 1024;
    private static final int DIGITS_IN_INT8 = 20;
    private static final int DIGITS_IN_INT4 = 10;
    private static final int DIGITS_IN_FLOAT4 = 38;
    private static final int DIGITS_IN_FLOAT8 = 308;

    /**
     * Creates a new facade for PostgreSQL's DatabaseMetaData.
     */
    public PostgresDatabaseMetaDataDecorator(DatabaseMetaData delegate) {
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
    
    /** Compensates for unlimited length varchar (which is otherwise reported as VARCHAR(0)
     * by returning a large limit for column_length
     */
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        ResultSet rs = super.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
        CachedRowSet crs = new CachedRowSet();
        crs.populate(rs);
        rs.close();
        while (crs.next()) {
            if (crs.getInt(5) == Types.VARCHAR && crs.getInt(7) <= 0) {
                crs.updateInt(7, UGLY_DEFAULT_VARCHAR_SIZE);
            } else if ("int4".equalsIgnoreCase(crs.getString(6))) {
                crs.updateInt(7, DIGITS_IN_INT4);
            } else if ("int8".equalsIgnoreCase(crs.getString(6))) {
                crs.updateInt(7, DIGITS_IN_INT8);
            } else if ("float4".equalsIgnoreCase(crs.getString(6))) {
                crs.updateInt(7, DIGITS_IN_FLOAT4);
            } else if ("float8".equalsIgnoreCase(crs.getString(6))) {
                crs.updateInt(7, DIGITS_IN_FLOAT8);
            }
        }
        crs.beforeFirst();
        return crs;
    }
}
