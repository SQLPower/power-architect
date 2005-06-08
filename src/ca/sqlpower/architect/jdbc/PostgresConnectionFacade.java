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
 * The PostgresConnectionFacade makes sure that the special PostgresDatabaseMetaDataFacade
 * class wraps the database metadata returned by the PostgreSQL driver.
 *
 * @author fuerth
 * @version $Id$
 */
public class PostgresConnectionFacade extends ConnectionFacade {
    
    /**
     * Creates a new PostgresConnectionFacade.
     * 
     * @param delegate an instance of the PostgreSQL Connection object.
     */
    public PostgresConnectionFacade(Connection delegate) {
        super(delegate);
    }
    
    public DatabaseMetaData getMetaData() throws SQLException {
        return new PostgresDatabaseMetaDataFacade(super.getMetaData());
    }
}
