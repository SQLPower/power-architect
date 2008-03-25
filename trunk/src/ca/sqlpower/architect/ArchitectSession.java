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

import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.profile.ProfileManager;

public interface ArchitectSession extends UserPrompterFactory {

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
     * Returns the top level object in the SQLObject hierarchy.
     * It has no parent and its children are SQLDatabase's.
     */
    public SQLObjectRoot getRootObject();
    
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
    public CoreProject getProject();
    
    /**
     *  This method is only used to create the correct type of project
     *  for an ArchitectSwingSessionImpl and should not be called anywhere
     *  else.
     */
    public void setProject(CoreProject project);
    
    
    /**
     *  Replaces the entire list of source databases for this session.
     *  This method is used reflectively by the code that does loading and saving,
     *  so DON'T DELETE THIS METHOD even if it looks like it's unused.
     * 
     * @param databases
     * @throws ArchitectException
     */
    public void setSourceDatabaseList(List<SQLDatabase> databases) throws ArchitectException;
    
    /**
     * The DDL Generator currently in use for this session.
     */
    public DDLGenerator getDDLGenerator();
    
    /**
     * Sets the new DDL Generator currently in use for this session.
     */
    public void setDDLGenerator(DDLGenerator generator);

    /* docs inherit from interface */
    public UserPrompter createUserPrompter(String question, String okText, String notOkText, String cancelText);

}