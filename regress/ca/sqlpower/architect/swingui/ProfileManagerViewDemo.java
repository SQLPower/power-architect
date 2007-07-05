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
package ca.sqlpower.architect.swingui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.*;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileChangeListener;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileSettings;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileManagerViewDemo {

    static ProfileManagerView view;
    static SQLTable mockTable;
    static TableProfileResult tableProfileResult;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JFrame jf = new JFrame("ProfileManagerViewDemo");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final List<TableProfileResult> mockData = new ArrayList<TableProfileResult>();
        ProfileManager mock = new ProfileManager() {

            public void asynchCreateProfiles(Collection<SQLTable> tables) throws SQLException, ArchitectException {
            }
            
            public List<TableProfileResult> getTableResults() {
                return mockData;
            }

            public boolean removeProfile(TableProfileResult victim) {
                System.out.println("Mock Manager asked to remove " + victim);
                System.out.println("before remove: " + mockData);
                boolean b = mockData.remove(victim);
                System.out.println("after remove: " + mockData);
                view.profilesRemoved(new ProfileChangeEvent(mockTable, tableProfileResult));
                return b;
            }

            public void clear() {
                System.out.println("Mock Manager asked to clear");
                mockData.clear();
            }

            public TableProfileResult createProfile(SQLTable table) throws SQLException, ArchitectException {
                // TODO Auto-generated method stub
                return null;
            }

            public void addProfileChangeListener(ProfileChangeListener listener) {

            }

            public void removeProfileChangeListener(ProfileChangeListener listener) {

            }

            ProfileSettings settings;
            
            public ProfileSettings getProfileSettings() {
                return settings;
            }

            public void setProfileSettings(ProfileSettings settings) {
                this.settings = settings;
            }
        };
        
        mockTable = new SQLTable();
        mockTable.setName("Customers");
        tableProfileResult = new TableProfileResult(mockTable, mock) {
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());
        mockData.add(tableProfileResult);

        mockTable = new SQLTable();
        mockTable.setName("Suppliers");
        tableProfileResult = new TableProfileResult(mockTable, mock);
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());
        mockData.add(tableProfileResult);

        view = new ProfileManagerView(mock);
        jf.setContentPane(view);
        jf.pack();
        jf.setVisible(true);

        Thread.sleep(4000);
        System.out.println("Adding another table");
        mockTable = new SQLTable();
        mockTable.setName("Yet Another Table");
        tableProfileResult = new TableProfileResult(mockTable, mock);
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());
        mockData.add(tableProfileResult);

        view.profilesAdded(new ProfileChangeEvent(mockTable, tableProfileResult));
    }

}
