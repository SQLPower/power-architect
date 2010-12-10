/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect;

import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.LiquibaseSettings;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

public class StubArchitectSession implements ArchitectSession {

    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        // TODO Auto-generated method stub

    }

    public boolean close() {
        // TODO Auto-generated method stub
        return false;
    }

    public ArchitectSessionContext getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public DDLGenerator getDDLGenerator() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public ProfileManager getProfileManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public ProjectLoader getProjectLoader() {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLObjectRoot getRootObject() {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLDatabase getTargetDatabase() {
        // TODO Auto-generated method stub
        return null;
    }

    public ArchitectProject getWorkspace() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        // TODO Auto-generated method stub

    }

    public void setDDLGenerator(DDLGenerator generator) {
        // TODO Auto-generated method stub

    }

    public void setName(String argName) {
        // TODO Auto-generated method stub

    }

    public void setProjectLoader(ProjectLoader project) {
        // TODO Auto-generated method stub

    }

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        // TODO Auto-generated method stub

    }

    public UserPrompter createDatabaseUserPrompter(String question, List<Class<? extends SPDataSource>> dsTypes,
            UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
            DataSourceCollection<SPDataSource> dsCollection, String... buttonNames) {
        // TODO Auto-generated method stub
        return null;
    }

    public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse, String... buttonNames) {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isForegroundThread() {
        return true;
    }

    public void runInBackground(Runnable runner) {
        // TODO Auto-generated method stub

    }

    public void runInForeground(Runnable runner) {
        // TODO Auto-generated method stub

    }

    public boolean isEnterpriseSession() {
        // TODO Auto-generated method stub
        return false;
    }

    public DataSourceCollection<JDBCDataSource> getDataSources() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<UserDefinedSQLType> getSQLTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    public UserDefinedSQLType findSQLTypeByUUID(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public UserDefinedSQLType findSQLTypeByJDBCType(int type) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> UserPrompter createListUserPrompter(String question, List<T> responses, T defaultResponse) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public List<UserDefinedSQLType> getDomains() {
        // TODO Auto-generated method stub
        return null;
    }

	public LiquibaseSettings getLiquibaseSettings() {
		return null;
	}

	public void setLiquibaseSettings(LiquibaseSettings settings) {
	}

    @Override
    public ArchitectStatusInformation getStatusInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Runnable createUpdateSnapshotRunnable(SPObjectSnapshot<?> snapshot) {
        // TODO Auto-generated method stub
        return null;
    }
}
