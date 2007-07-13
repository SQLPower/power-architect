/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.etl;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;

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
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ArchitectDataSourceType dsType = ds.getParentType();
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
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ds.put(KettleOptions.KETTLE_HOSTNAME_KEY, "Hostname");
        ds.put(KettleOptions.KETTLE_PORT_KEY, "1234");
        ds.put(KettleOptions.KETTLE_DATABASE_KEY, "Database");
        ArchitectDataSourceType dsType = ds.getParentType();
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
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setName("DataSource for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ArchitectDataSourceType dsType = ds.getParentType();
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
