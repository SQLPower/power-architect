/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.architect.profile;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

/**
 * Tests for the RemoteDatabaseProfileCreator implementation.
 */
public class RemoteDatabateProfileCreatorTest extends TestCase {

    /**
     * A pl.ini instance initialized from the "pl.regression.ini" file.
     * Gets created by setUp().
     */
    private DataSourceCollection plini;
    
    /**
     * The testing data source "regression_test" from the plini.
     * Gets created by setUp().
     */
    private SPDataSource ds; 
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plini = new PlDotIni();
        plini.read(new File("pl.regression.ini"));
        ds = plini.getDataSource("regression_test");
    }
    
    /**
     * Regression test: discovering profile functions was mysteriously failing,
     * and the cause turned out to be an inadequate stub object created by the discovery
     * method. This is just a general "black box" blanket test that discovering a
     * profile function descriptor can succeed.
     */
    public void testDiscoverVarcharFunctionDescriptor() throws Exception {
        SPDataSourceType dsType = ds.getParentType();
        clearProfileFunctionDescriptors(dsType);
        
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.createConnection();
            stmt = con.createStatement();
            stmt.execute("create table test_table (col1 varchar (50))");
            stmt.execute("insert into test_table (col1) values ('hello')");
            RemoteDatabaseProfileCreator rdpc = new RemoteDatabaseProfileCreator(new ProfileSettings());
            SQLDatabase db = new SQLDatabase(ds);
            SQLTable table = db.getTableByName("test_table");
            TableProfileResult tpr = new TableProfileResult(table, new ProfileSettings());
            rdpc.doProfile(tpr);
            Collection<ColumnProfileResult> cprCollection = tpr.getColumnProfileResult(table.getColumn(0));
            assertEquals(1, cprCollection.size());
            for(ColumnProfileResult cpr : cprCollection) {
                assertEquals(5.0, cpr.getAvgLength());
                assertEquals(null, cpr.getAvgValue());
                assertEquals("hello", cpr.getMaxValue());
                assertEquals("hello", cpr.getMinValue());
                assertEquals(1, cpr.getDistinctValueCount());
                assertEquals(5, cpr.getMaxLength());
                assertEquals(5, cpr.getMinLength());
                assertEquals(null, cpr.getException());
                assertEquals(0, cpr.getNullCount());
            }
        } finally {
            if (con != null) {
                con.close();
            }
        }
        
    }

    /**
     * Removes all ProfileFunctionDescriptor entries from the given database type.
     */
    private void clearProfileFunctionDescriptors(SPDataSourceType dsType) {
        for (int i = 0; ; i++) {
            String key = ProfileFunctionDescriptor.class.getName() + "_" + i;
            String pfd = dsType.getProperty(key);
            if (pfd == null) break;
            dsType.putProperty(key, null);
        }
    }
}
