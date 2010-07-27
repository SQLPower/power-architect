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
package ca.sqlpower.architect;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.architect.enterprise.UpstreamTypeUpdaterListener;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.swingui.LiquibaseSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory;

/**
 * The ArchitectSession class represents a single user's session with
 * the architect.  If using the Swing UI (currently this is the only
 * option, but that is subject to change), the ArchitectFrame has a
 * 1:1 relationship with an ArchitectSession.
 *
 * @version $Id$
 * @author fuerth
 */
public class ArchitectSessionImpl implements ArchitectSession {

    private final ArchitectSessionContext context;
    private final ArchitectProject project;
    private String name;

    private final List<SessionLifecycleListener<ArchitectSession>> lifecycleListeners =
        new ArrayList<SessionLifecycleListener<ArchitectSession>>();

    /**
     * The factory that creates user prompters for this session. Defaults to a
     * factory that makes an "always OK" user prompter for headless/embedded use.
     * When this session is being used in a GUI environment, the startup code
     * for the GUI will replace the default factory with one that actually
     * prompts the user.
     */
    private UserPrompterFactory userPrompterFactory = new DefaultUserPrompterFactory();

    /**
     * The project associated with this session.  The project provides save
     * and load functionality, and houses the source database connections.
     */
    private ProjectLoader projectLoader;

    private DDLGenerator ddlGenerator;

	private LiquibaseSettings liquibaseSettings;

    protected boolean isEnterpriseSession;

	public ArchitectSessionImpl(final ArchitectSessionContext context,
	        String name) throws SQLObjectException {
	    this(context, name, new ArchitectProject());
	}
	
	public ArchitectSessionImpl(final ArchitectSessionContext context,
	        String name, ArchitectProject project) throws SQLObjectException {
	    this.context = context;
	    this.project = project;
	    project.setSession(this);
	    ProfileManagerImpl manager = new ProfileManagerImpl();
	    this.project.setProfileManager(manager);
	    this.name = name;
        this.projectLoader = new ProjectLoader(this);
        this.isEnterpriseSession = false;
		this.liquibaseSettings = new LiquibaseSettings();

        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new SQLObjectException("SQL Error in ddlGenerator",e);
        }
        
        project.addSPListener(new UpstreamTypeUpdaterListener(this));
	}

	// --------------- accessors and mutators ------------------

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName()  {
        return this.name;
    }

    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName) {
        this.name = argName;
    }

    public ProfileManager getProfileManager() {
        return project.getProfileManager();
    }

    public SQLDatabase getTargetDatabase() {
        return project.getTargetDatabase();
    }

    public ProjectLoader getProjectLoader() {
        return projectLoader;
    }

    public void setProjectLoader(ProjectLoader project) {
        this.projectLoader = project;
    }

    public SQLObjectRoot getRootObject() {
        return project.getRootObject();
    }

    public ArchitectSessionContext getContext() {
        return context;
    }



    public void setProfileManager(ProfileManagerImpl manager) {
        project.setProfileManager(manager);
    }

    public UserPrompter createUserPrompter(String question, UserPromptType responseType,
            UserPromptOptions optionType, UserPromptResponse defaultResponseType,
            Object defaultResponse, String ... buttonNames) {
        return userPrompterFactory.createUserPrompter(question, responseType,
                optionType, defaultResponseType, defaultResponse, buttonNames);
    }

    /**
     * Changes the user prompter factory in use on this session.
     *
     * @param upFactory The new user prompter factory to use. Must not be null.
     */
    public void setUserPrompterFactory(UserPrompterFactory upFactory) {
        if (upFactory == null) {
            throw new NullPointerException("Null user prompter factory is not allowed!");
        }
        userPrompterFactory = upFactory;
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
        return project.getDatabase(ds);
    }

    public DDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }

    public void setDDLGenerator(DDLGenerator generator) {
        ddlGenerator = generator;
    }

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        project.setSourceDatabaseList(databases);
    }

    public UserPrompter createDatabaseUserPrompter(String question, List<Class<? extends SPDataSource>> dsTypes,
            UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
            DataSourceCollection<SPDataSource> dsCollection, String... buttonNames) {
        return userPrompterFactory.createDatabaseUserPrompter(question, dsTypes, optionType,
                defaultResponseType, defaultResponse, dsCollection, buttonNames);
    }

    public ArchitectProject getWorkspace() {
        return project;
    }

    public boolean isForegroundThread() {
        return true;
    }

    public void runInBackground(Runnable runner) {
        runner.run();
    }

    public void runInForeground(Runnable runner) {
        runner.run();
    }

    public boolean close() {
        //TODO decide what to do with cleanup in the long run. At current this call
        //makes closing Architect extremely slow as it populates everything in the entire
        //collection of databases in the DBTree (painful on a slow Windows laptop with a
        //large Oracle DB).
//        CleanupExceptions cleanupObject = SQLPowerUtils.cleanupSPObject(project);
//        SQLPowerUtils.displayCleanupErrors(cleanupObject, userPrompterFactory);

        SessionLifecycleEvent<ArchitectSession> lifecycleEvent =
            new SessionLifecycleEvent<ArchitectSession>(this);
        for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
            lifecycleListeners.get(i).sessionClosing(lifecycleEvent);
        }

        return true;
    }

    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        lifecycleListeners.add(l);
    }

    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        lifecycleListeners.remove(l);
    }

    public boolean isEnterpriseSession() {
        return isEnterpriseSession;
    }

    public DataSourceCollection<JDBCDataSource> getDataSources() {
        return context.getPlDotIni();
    }

    public List<UserDefinedSQLType> getSQLTypes() {
        return getDataSources().getSQLTypes();
    }

    public UserDefinedSQLType findSQLTypeByUUID(String uuid) {
        List<UserDefinedSQLType> types = getSQLTypes();
        for (UserDefinedSQLType type : types) {
            if (type.getUUID().equals(uuid)) {
                return type;
            }
        }
        return null;
    }

    public UserDefinedSQLType findSQLTypeByJDBCType(int jdbcType) {
        List<UserDefinedSQLType> types = getSQLTypes();
        for (UserDefinedSQLType type : types) {
            if (type.getType() == jdbcType) {
                return type;
            }
        }
        return null;
    }

    public <T> UserPrompter createListUserPrompter(String question, List<T> responses, T defaultResponse) {
        return userPrompterFactory.createListUserPrompter(question, responses, defaultResponse);
    }

    public List<DomainCategory> getDomainCategories() {
        return Collections.emptyList();
    }

	public LiquibaseSettings getLiquibaseSettings() {
		return liquibaseSettings;
	}

	public void setLiquibaseSettings(LiquibaseSettings settings) {
		liquibaseSettings = settings;
	}
}

