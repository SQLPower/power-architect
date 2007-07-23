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
package ca.sqlpower.architect.sqlrunner;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.sql.SPDataSource;

import com.darwinsys.database.DataBaseException;
import com.darwinsys.sql.Configuration;
import com.darwinsys.sql.ConfigurationManager;

public class ArchitectSQLRunnerConfigurationManager implements ConfigurationManager {

    private final ArchitectSwingSession session;
    
    static class ArchitectDataSourceConfiguration implements Configuration {

        private SPDataSource ds;

        public ArchitectDataSourceConfiguration(SPDataSource ds) {
            this.ds = ds;
        }

        public SPDataSource getArchitectDataSource() {
            return ds;
        }

        @Override
        public String toString() {
            return ds.getName();
        }

        public String getDbURL() {
            return ds.getUrl();
        }

        public String getDriverName() {
            return ds.getDriverClass();
        }

        public String getName() {
            return ds.getDisplayName();
        }

        public String getPassword() {
            return ds.getPass();
        }

        public String getUserName() {
            return ds.getUser();
        }

        public boolean hasPassword() {
            return getPassword()!=null && getPassword().length() > 0;
        }

        public void setDbURL(String arg0) {
            ds.setUrl(arg0);
        }

        public void setDriverName(String arg0) {
            ds.getParentType().setJdbcDriver(arg0);
        }

        public void setName(String arg0) {
            ds.setDisplayName(arg0);
        }

        public void setPassword(String arg0) {
            ds.setPass(arg0);
        }

        public void setUserName(String arg0) {
            ds.setUser(arg0);
        }
    }
    
    public ArchitectSQLRunnerConfigurationManager(ArchitectSwingSession session) {
        this.session = session;
    }
    
    public List<Configuration> getConfigurations() {
        List<SPDataSource> connections =
            session.getUserSettings().getConnections();
        List<Configuration> results = new ArrayList<Configuration>();
        for (SPDataSource ds : connections) {
            results.add(new ArchitectDataSourceConfiguration(ds));
        }
        return results;
    }

    public Connection getConnection(Configuration conf) {
        ArchitectDataSourceConfiguration config = (ArchitectDataSourceConfiguration) conf;
        try {
            SQLDatabase db = new SQLDatabase(config.getArchitectDataSource());
            return db.getConnection();
        } catch (Exception e) {
            throw new DataBaseException("Could not connect:" + e.toString());
        }
    }

}
