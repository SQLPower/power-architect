/*
 * Created on Jun 8, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * The PostgresConnectionDecorator makes sure that the special PostgresDatabaseMetaDataDecorator
 * class wraps the database metadata returned by the PostgreSQL driver.
 *
 * @author fuerth
 * @version $Id$
 */
public class PostgresConnectionDecorator extends ConnectionDecorator {
    
    /**
     * Creates a new PostgresConnectionDecorator.
     * 
     * @param delegate an instance of the PostgreSQL Connection object.
     */
    public PostgresConnectionDecorator(Connection delegate) {
        super(delegate);
    }
    
    public DatabaseMetaData getMetaData() throws SQLException {
        return new PostgresDatabaseMetaDataDecorator(super.getMetaData());
    }
}
