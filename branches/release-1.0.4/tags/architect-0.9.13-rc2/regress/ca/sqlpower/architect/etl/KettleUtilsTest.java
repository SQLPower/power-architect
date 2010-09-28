/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.etl;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;

import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

public class KettleUtilsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testRetrieveKettleConnectionTypes() {
        List<String> utilsConnectionTypes = KettleUtils.retrieveKettleConnectionTypes();
        DatabaseInterface[] dbConnectionArray = DatabaseMeta.getDatabaseInterfaces();
        for (int i = 0; i < dbConnectionArray.length; i++) {
            assertEquals(utilsConnectionTypes.get(i), dbConnectionArray[i].getDatabaseTypeDescLong());
        }
    }
    
    public void testCreateDatabaseMetaUsingURL() {
        SPDataSource ds = new SPDataSource(new PlDotIni());
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        SPDataSourceType dsType = ds.getParentType();
        dsType.setJdbcUrl("<Hostname>:<Port>:<Database>");
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        ds.setUrl("hostname:1234:database");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("hostname", dbMeta.getHostname());
        assertEquals("database", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
    
    public void testCreateDatabaseMetaUsingProperties() {
        SPDataSource ds = new SPDataSource(new PlDotIni());
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ds.put(KettleOptions.KETTLE_HOSTNAME_KEY, "Hostname");
        ds.put(KettleOptions.KETTLE_PORT_KEY, "1234");
        ds.put(KettleOptions.KETTLE_DATABASE_KEY, "Database");
        SPDataSourceType dsType = ds.getParentType();
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("Hostname", dbMeta.getHostname());
        assertEquals("Database", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
    
    public void testCreateDatabaseMetaUsingNulls() {
        SPDataSource ds = new SPDataSource(new PlDotIni());
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        SPDataSourceType dsType = ds.getParentType();
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        DatabaseMeta dbMeta = null;
        // If something goes wrong this will throw a RuntimeException
        dbMeta = KettleUtils.createDatabaseMeta(ds);
        assertEquals(ds.getName(), dbMeta.getName());
        assertEquals(ds.getUser(), dbMeta.getUsername());
        assertEquals(ds.getPass(), dbMeta.getPassword());
        assertEquals("", dbMeta.getHostname());
        assertEquals("", dbMeta.getDatabaseName());
        assertEquals("oracle", dbMeta.getDatabaseTypeDesc().toLowerCase());
        assertEquals("Native", dbMeta.getAccessTypeDesc());
    }
}
