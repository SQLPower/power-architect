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
            session.getContext().getConnections();
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
