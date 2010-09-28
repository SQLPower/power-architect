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
