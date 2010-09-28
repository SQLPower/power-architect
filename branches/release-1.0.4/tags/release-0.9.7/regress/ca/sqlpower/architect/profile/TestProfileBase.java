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
package ca.sqlpower.architect.profile;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

/**
 * Base class for various tests on profiling.  The setUp method sets up three
 * tables, adds rows of data to them, and then uses a new profile manager to
 * create profiles of each table.  Each table has 13 columns, one of each data
 * type we currently support for profiling.  The differences between the tables
 * are:
 * 
 * <ul>
 *  <li>PROFILE_TEST1: Saved as protected member variable <tt>t1</tt>.
 *                     Has 5 rows with different data in each row and no NULL values.
 *  <li>PROFILE_TEST2: Saved as protected member variable <tt>t2</tt>.
 *                     Has 5 rows of data similar to PROFILE_TEST1, but some values are NULL.
 *  <li>PROFILE_TEST3: Saved as protected member variable <tt>t3</tt>.
 *                     Has no data.
 * </ul>
 */
public abstract class TestProfileBase extends TestCase {

    /**
     * The database where the profile tables are created then profiled.
     */
    SQLDatabase mydb;
    
    /**
     * Profile manager instance that contains profiles of the three tables
     * described in this class's comment.
     */
    protected TableProfileManager pm;

    /**
     * SQLTable version of TEST_TABLE1.
     */
    protected SQLTable t1;
    
    /**
     * SQLTable version of TEST_TABLE2.
     */
    protected SQLTable t2;

    /**
     * SQLTable version of TEST_TABLE3.
     */
    protected SQLTable t3;
    
    /**
     * When true, the setUp method will print a summary of what it did.
     */
    protected boolean debug = false;
    
    /**
     * Creates new tables in mydb then profiles them, as described in this class's comment.
     */
    @Override
    public void setUp() throws Exception {
        System.out.println("TestProfileBase.testProfileManager()");

        DataSourceCollection plini = new PlDotIni();
        plini.read(new File("pl.regression.ini"));

        SPDataSource ds = plini.getDataSource("regression_test");

        mydb = new SQLDatabase(ds);
        Connection conn = null;
        Statement stmt = null;
        String lastSQL = null;

        /*
         * Setting up a clean db for each of the tests
         */
        try {
            conn = mydb.getConnection();
            stmt = conn.createStatement();
            try {
                stmt.executeUpdate("DROP TABLE PROFILE_TEST1");
                stmt.executeUpdate("DROP TABLE PROFILE_TEST2");
                stmt.executeUpdate("DROP TABLE PROFILE_TEST3");
            } catch (SQLException sqle) {
                if (debug) {
                    System.out.println("+++ TestProfile exception should be for dropping a non-existant table");
                    sqle.printStackTrace();
                }
            }

            lastSQL = "CREATE TABLE PROFILE_TEST1 (t1_c1 char(100), t1_c2 date, t1_c4 decimal, t1_c5 float, t1_c6 int, t1_c7 bigint, t1_c10 varchar(300), t1_c11 numeric(12,2), t1_c13 numeric(10), t1_c14 real, t1_c15 smallint, t1_c16 varchar(200), t1_c17 varchar(120))";
            stmt.executeUpdate(lastSQL);

            lastSQL = "CREATE TABLE PROFILE_TEST2 (t2_c1 char(100), t2_c2 date, t2_c4 decimal, t2_c5 float, t2_c6 int, t2_c7 bigint, t2_c10 varchar(400), t2_c11 numeric(12,2), t2_c13 numeric(10), t2_c14 real, t2_c15 smallint, t2_c16 varchar(200), t2_c17 varchar(120))";
            stmt.executeUpdate(lastSQL);

            lastSQL = "CREATE TABLE PROFILE_TEST3 (t3_c1 char(100), t3_c2 date, t3_c4 decimal, t3_c5 float, t3_c6 int, t3_c7 bigint, t3_c10 varchar(500), t3_c11 numeric(12,2), t3_c13 numeric(10), t3_c14 real, t3_c15 smallint, t3_c16 varchar(200), t3_c17 varchar(120))";
            stmt.executeUpdate(lastSQL);

            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890a', {ts '2006-07-26 11:22:33'}, 12345.6789, 23456.789, 1, 1234567890, '5B5261775F446174615D',    1234567.89, 123456789,  567.89, 321,   'column of varchar 200 aaa', 'column of varchar 200 xxx')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890b', {ts '2006-07-26 11:22:34'}, 22345.6789, 33456.789, 2, 1234567891, '6B5261775F446174615D',   2234567.89, 1234567890, 667.89, 3212,  'column of varchar 200 bbb', 'column of varchar 200 yyy')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890c', {ts '2006-07-26 11:22:35'}, 32345.6789, 43456.789, 3, 1234567892, '8B526146174615D',  3234567.89, 1234567891, 767.89, 32123, 'column of varchar 200 ccc', 'column of varchar 200 zzz')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890d', {ts '2006-07-26 11:22:36'}, 42345.6789, 53456.789, 4, 1234567893, '8B526146174615D', 4234567.89, 1234567892, 867.89, 32124, 'column of varchar 200 ddd', 'column of varchar 200 sss')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890e', {ts '2006-07-26 11:22:37'}, 52345.6789, 63456.789, 5, 1234567894, '9B52616174615D',5234567.89, 1234567893, 967.89, 32125, 'column of varchar 200 eee', 'column of varchar 200 ddd')";
            stmt.executeUpdate(lastSQL);

            lastSQL = "Insert into PROFILE_TEST2 values ('abc12345678901234567890a', {ts '2006-07-26 11:22:33'}, 12345.6789, 23456.789, 987654321, '1234567890', '5B5261775F446174615D',    1234567.89, 123456789,  567.89, 321,   'column of varchar 200 aaa', 'column of varchar 200 xxx')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('',                         {ts '2006-07-26 11:22:34'}, 22345.6789, 33456.789, 987654322, NULL,         '6B5261775F446174615D',    2234567.89, 1234567890, 667.89, 3212,  'column of varchar 200 bbb', 'column of varchar 200 yyy')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values (NULL,                       {ts '2006-07-26 11:22:35'}, 32345.6789, 43456.789, 987654323, '1234567892', '6B5261775F446174615D',    3234567.89, NULL,       767.89, 32123, NULL,                        'column of varchar 200 zzz')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('abcd',                     NULL,                                                     NULL,       NULL,      NULL,      '1234567',    '8B5261775F446174615D',    4234567.89, 1234567892, 867.89, 32124, 'column of var',             'column of varchar 200 sss')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('1234567890',               {ts '2006-07-26 11:22:37'}, 52345.6789, NULL,      987654325, NULL,         null,                      5234567.89, 1234567893, 967.89, NULL,  'col',                       'column of varchar 200 ddd')";
            stmt.executeUpdate(lastSQL);

            conn.commit();

            List<SQLTable> tableList = new ArrayList<SQLTable>();
            for ( int i=1; i<4; i++ ) {
                SQLTable t = mydb.getTableByName("PROFILE_TEST"+i);
                
                if (i == 1) t1 = t;
                else if (i == 2) t2 = t;
                else if (i == 3) t3 = t;
                
                tableList.add(t);
            }
            
            assertNotNull(t1);
            assertNotNull(t2);
            assertNotNull(t3);
            
            pm = new TableProfileManager();
            pm.getDefaultProfileSettings().setFindingAvg(true);
            pm.getDefaultProfileSettings().setFindingMin(true);
            pm.getDefaultProfileSettings().setFindingMax(true);
            pm.getDefaultProfileSettings().setFindingMinLength(true);
            pm.getDefaultProfileSettings().setFindingMaxLength(true);
            pm.getDefaultProfileSettings().setFindingAvgLength(true);
            pm.getDefaultProfileSettings().setFindingDistinctCount(true);
            pm.getDefaultProfileSettings().setFindingNullCount(true);

            for (SQLTable t : tableList) {
                pm.createProfile(t);
            }
            
            List<TableProfileResult> tableResults = pm.getResults();
            // Dump the results in case somebody wants to read them
            if (debug) {
                for (int i = 0; i < tableResults.size(); i++) {
                    SQLTable t = mydb.getTableByName("PROFILE_TEST"+ (i+1) ); // +1 to keep table names same
                    TableProfileResult tpr = tableResults.get(i);
                    System.out.println(t.getName() + "  " + tpr.toString());
                    for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                        SQLColumn c = cpr.getProfiledObject();
                        System.out.println(c.getName() + "[" + c.getSourceDataTypeName() + "]   "+  cpr);
                    }
                }
            }
        } catch (SQLException e) {
            // print the SQL while we still have it
            System.out.println("Error in SQL query: "+lastSQL);
            throw e;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close statement");
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close connection");
            }
        }
    }

    /**
     * Disconnects from the database. Does not drop the tables created by
     * setUp().
     */
    @Override
    protected void tearDown() throws Exception {
        mydb.disconnect();
    }
}
