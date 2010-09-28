package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.SQLTable;

public class TestProfileManagerListeners extends TestProfileBase {

    int addedEvents = 0;
    int removedEvents = 0;
    private int changedEvents;

    public void testListeners() throws Exception {
        ProfileChangeListener listener = new ProfileChangeListener() {


            public void profilesAdded(ProfileChangeEvent e) {
                addedEvents++;
            }

            public void profilesRemoved(ProfileChangeEvent e) {
                removedEvents++;
            }

            public void profileListChanged(ProfileChangeEvent event) {
                ++changedEvents;
            }
        };
        pm.addProfileChangeListener(listener);
        assertEquals(0, addedEvents);
        assertEquals(0, removedEvents);
        List<SQLTable> tableList = new ArrayList<SQLTable>();
        Connection conn = mydb.getConnection();
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("DROP TABLE PROFILE_TEST5");
        } catch (SQLException e) {
            // nothing to do, first time...
        }

        String cmd  = "CREATE TABLE PROFILE_TEST5 (id integer)";
        stmt.executeUpdate(cmd);
        for (int i = 0; i < 10; i++) {
            cmd = String.format("insert into PROFILE_TEST5 values(%d)", i);
            stmt.executeUpdate(cmd);
        }
        SQLTable t = mydb.getTableByName("PROFILE_TEST5");
        tableList.add(t);
        for (SQLTable table : tableList) {
            pm.createProfile(table);
        }
        assertEquals("table and 1 column got added", 2, addedEvents);
        assertEquals(0, removedEvents);

        for (SQLTable table : tableList) {
            pm.createProfile(table);
        }
        
        assertEquals("Table added", tableList.size() + 2, addedEvents);
        Collection<TableProfileResult> tableResults = pm.getTableResult(t);
        for (TableProfileResult tpr : tableResults) {
            pm.removeProfile(tpr);
        }
        assertEquals("Table Removed", tableResults.size(), removedEvents);


    }
}
