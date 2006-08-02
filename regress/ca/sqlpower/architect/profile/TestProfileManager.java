package ca.sqlpower.architect.profile;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;

public class TestProfileManager extends TestCase {
    
    public void testProfileManager() throws IOException {
        
        ArchitectFrame.getMainInstance();  // creates an ArchitectFrame, which loads settings
        //FIXME: a better approach would be to have an initialsation method
        // in the business model, which does not depend on the init routine in ArchitectFrame.
        PlDotIni plini = new PlDotIni();
        plini.read(new File("pl.regression.ini"));
        
        ArchitectDataSource ds = plini.getDataSource("regression_test");
        
        SQLDatabase mydb = new SQLDatabase(ds);
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
                System.out.println("+++ TestProfile exception should be for dropping a non-existant table");
                sqle.printStackTrace();
            }

            lastSQL = "CREATE TABLE PROFILE_TEST1 (t1_c1 char(100), t1_c2 date, t1_c4 decimal, t1_c5 float, t1_c6 int, t1_c8 long,       t1_c10 raw(300), t1_c11 number(12,2), t1_c13 numeric(10), t1_c14 real, t1_c15 smallint, t1_c16 varchar(200), t1_c17 varchar2(120))";
            stmt.executeUpdate(lastSQL);
            
            lastSQL = "CREATE TABLE PROFILE_TEST2 (t2_c1 char(100), t2_c2 date, t2_c4 decimal, t2_c5 float, t2_c6 int, t2_c9 long raw,   t2_c10 raw(400), t2_c11 number(12,2), t2_c13 numeric(10), t2_c14 real, t2_c15 smallint, t2_c16 varchar(200), t2_c17 varchar2(120))";
            stmt.executeUpdate(lastSQL);
            
            lastSQL = "CREATE TABLE PROFILE_TEST3 (t3_c1 char(100), t3_c2 date, t3_c4 decimal, t3_c5 float, t3_c6 int, t3_c9 long raw,   t3_c10 raw(500), t3_c11 number(12,2), t3_c13 numeric(10), t3_c14 real, t3_c15 smallint, t3_c16 varchar(200), t3_c17 varchar2(120))";
            stmt.executeUpdate(lastSQL);
            
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890a', to_date('26-jul-2006 11:22:33','dd-mon-yyyy hh24:mi:ss'), 12345.6789, 23456.789, 987654321, 1234567890, '5B5261775F446174615D',    1234567.89, 123456789,  567.89, 321,   'column of varchar 200 aaa', 'column of varchar2 200 xxx')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890b', to_date('26-jul-2006 11:22:34','dd-mon-yyyy hh24:mi:ss'), 22345.6789, 33456.789, 987654322, 1234567891, '6B5261775F446174615D',   2234567.89, 1234567890, 667.89, 3212,  'column of varchar 200 bbb', 'column of varchar2 200 yyy')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890c', to_date('26-jul-2006 11:22:35','dd-mon-yyyy hh24:mi:ss'), 32345.6789, 43456.789, 987654323, 1234567892, '8B526146174615D',  3234567.89, 1234567891, 767.89, 32123, 'column of varchar 200 ccc', 'column of varchar2 200 zzz')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890d', to_date('26-jul-2006 11:22:36','dd-mon-yyyy hh24:mi:ss'), 42345.6789, 53456.789, 987654324, 1234567893, '8B526146174615D', 4234567.89, 1234567892, 867.89, 32124, 'column of varchar 200 ddd', 'column of varchar2 200 sss')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST1 values ('abc12345678901234567890e', to_date('26-jul-2006 11:22:37','dd-mon-yyyy hh24:mi:ss'), 52345.6789, 63456.789, 987654325, 1234567894, '9B52616174615D',5234567.89, 1234567893, 967.89, 32125, 'column of varchar 200 eee', 'column of varchar2 200 ddd')";
            stmt.executeUpdate(lastSQL);
            
            lastSQL = "Insert into PROFILE_TEST2 values ('abc12345678901234567890a', to_date('26-jul-2006 11:22:33','dd-mon-yyyy hh24:mi:ss'), 12345.6789, 23456.789, 987654321, '1234567890', '5B5261775F446174615D',    1234567.89, 123456789,  567.89, 321,   'column of varchar 200 aaa', 'column of varchar2 200 xxx')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('',                         to_date('26-jul-2006 11:22:34','dd-mon-yyyy hh24:mi:ss'), 22345.6789, 33456.789, 987654322, NULL,         '6B5261775F446174615D',    2234567.89, 1234567890, 667.89, 3212,  'column of varchar 200 bbb', 'column of varchar2 200 yyy')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values (NULL,                       to_date('26-jul-2006 11:22:35','dd-mon-yyyy hh24:mi:ss'), 32345.6789, 43456.789, 987654323, '1234567892', '6B5261775F446174615D',    3234567.89, NULL,       767.89, 32123, NULL,                        'column of varchar2 200 zzz')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('abcd',                     NULL,                                                     NULL,       NULL,      NULL,      '1234567',    '8B5261775F446174615D',    4234567.89, 1234567892, 867.89, 32124, 'column of var',             'column of varchar2 200 sss')";
            stmt.executeUpdate(lastSQL);
            lastSQL = "Insert into PROFILE_TEST2 values ('1234567890',               to_date('26-jul-2006 11:22:37','dd-mon-yyyy hh24:mi:ss'), 52345.6789, NULL,      987654325, NULL,         null,                      5234567.89, 1234567893, 967.89, NULL,  'col',                       'column of varchar2 200 ddd')";
            stmt.executeUpdate(lastSQL);
            

            conn.commit();
            
            List tableList = new ArrayList();
            for ( int i=1; i<4; i++ ) {
                SQLTable t = mydb.getTableByName("PROFILE_TEST"+i);
                tableList.add(t);
            }
            ProfileManager pm = new ProfileManager();
            pm.setFindingAvg(true);
            pm.setFindingMin(true);
            pm.setFindingMax(true);
            pm.setFindingMinLength(true);
            pm.setFindingMaxLength(true);
            pm.setFindingAvgLength(true);
            pm.setFindingDistinctCount(true);
            pm.setFindingNullCount(true);
            
            pm.createProfiles(tableList);
            
            for ( int i=1; i<4; i++ ) {
                SQLTable t = mydb.getTableByName("PROFILE_TEST"+i);
                ProfileResult pr = pm.getResult(t);
                System.out.println(t.getName()+"  "+pr.toString());
                for ( SQLColumn c : t.getColumns() ) {
                    pr = pm.getResult(c);
                    System.out.println(c.getName()+"["+c.getSourceDataTypeName()+"]   "+pr);
                }
            }
           
            
        } catch (ArchitectException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error in SQL query: "+lastSQL);
            e.printStackTrace();
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
            mydb.disconnect();
        }
        
        
    }
}
