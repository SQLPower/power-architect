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

import java.beans.PropertyChangeListener;
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.WorkspaceContainer;

public interface ArchitectSession extends UserPrompterFactory, SQLDatabaseMapping, WorkspaceContainer, RunnableDispatcher {

    public static final String PREFS_PL_INI_PATH = "PL.INI.PATH";
    
    public ProfileManager getProfileManager();
    
    /**
     * Returns the context that created this session.
     */
    public ArchitectSessionContext getContext();
    
    /**
     * Returns the database in use for this session. In a 
     * gui session, this would be the playpen database. 
     */
    public SQLDatabase getTargetDatabase();
    
    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName);
    
    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName();
    
    /**
     * Returns the project associated with this session.  The project
     * holds the playpen objects, and can save and load itself in an
     * XML format.
     */
    public ProjectLoader getProjectLoader();
    
    /**
     *  This method is only used to create the correct type of project
     *  for an ArchitectSwingSessionImpl and should not be called anywhere
     *  else.
     */
    public void setProjectLoader(ProjectLoader project);
    
    
    /**
     *  Replaces the entire list of source databases for this session.
     *  This method is used reflectively by the code that does loading and saving,
     *  so DON'T DELETE THIS METHOD even if it looks like it's unused.
     * 
     * @param databases
     * @throws SQLObjectException
     */
    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException;
    
    /**
     * The DDL Generator currently in use for this session.
     */
    public DDLGenerator getDDLGenerator();
    
    /**
     * Sets the new DDL Generator currently in use for this session.
     */
    public void setDDLGenerator(DDLGenerator generator);    

    /**
     * Returns the workspace object which is the root object of the ArchitectSession, the ArchitectProject.  
     */
    public ArchitectProject getWorkspace();    
    
    /**
     * Returns the root SQL object of the session, which is the tree that contains Columns, Databases, etc.
     */
    public SQLObjectRoot getRootObject();
    
    public DataSourceCollection<JDBCDataSource> getDataSources();
    
    public boolean isEnterpriseSession();

    void addPropertyChangeListener(PropertyChangeListener l);
    
    void removePropertyChangeListener(PropertyChangeListener l);
    
    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l);

    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l);        
    
    /**
     * Ends this session, disposing its frame and releasing any system
     * resources that were obtained explicitly by this session. Also
     * fires a sessionClosing lifecycle event, so any resources used up
     * by subsystems dependent on this session can be freed by the appropriate
     * parties.
     * 
     * @return True if the session was successfully closed. False if the
     * session did not close due to an error or user intervention.
     */
    public boolean close();
    
}