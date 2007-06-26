package ca.sqlpower.architect.sqlrunner;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

import com.darwinsys.database.DataBaseException;
import com.darwinsys.sql.Configuration;
import com.darwinsys.sql.ConfigurationManager;

public class ArchitectSQLRunnerConfigurationManager implements ConfigurationManager {

    private final ArchitectSwingSession session;
    
    static class ArchitectDataSourceConfiguration implements Configuration {

        private ArchitectDataSource ds;

        public ArchitectDataSourceConfiguration(ArchitectDataSource ds) {
            this.ds = ds;
        }

        public ArchitectDataSource getArchitectDataSource() {
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
        List<ArchitectDataSource> connections =
            session.getUserSettings().getConnections();
        List<Configuration> results = new ArrayList<Configuration>();
        for (ArchitectDataSource ds : connections) {
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
