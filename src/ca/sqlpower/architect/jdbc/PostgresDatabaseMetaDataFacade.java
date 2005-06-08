/*
 * Created on Jun 8, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.jdbc;

import java.sql.DatabaseMetaData;

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
}
