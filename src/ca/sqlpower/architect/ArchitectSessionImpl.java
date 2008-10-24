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
import java.util.List;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.profile.ProfileManagerImpl;

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
    private ProfileManagerImpl profileManager;
    private SQLDatabase db;
    private String name;
    private SQLObjectRoot rootObject;
    private OLAPRootObject olapRootObject;
    
    /**
     * The factory that creates user prompters for this session. Defaults to a
     * factory that makes an "always OK" user prompter for headless/embedded use.
     * When this session is being used in a GUI environment, the startup code
     * for the GUI will replace the default factory with one that actually
     * prompts the user.
     */
    private UserPrompterFactory userPrompterFactory = new AlwaysOKUserPrompterFactory();
    
    private DDLGenerator ddlGenerator;
    
    /**
     * The project associated with this session.  The project provides save
     * and load functionality, and houses the source database connections.
     */
    private CoreProject project;

	public ArchitectSessionImpl(final ArchitectSessionContext context,
	        String name) throws ArchitectException {
	    this.context = context;
	    this.name = name;
	    this.rootObject = new SQLObjectRoot();
	    this.olapRootObject = new OLAPRootObject(this); // XXX wrong reference direction--core shouldn't depend on OLAP
        this.profileManager = new ProfileManagerImpl(this);
        this.project = new CoreProject(this);
        this.db = new SQLDatabase();
        
        rootObject.addSQLObjectPreEventListener(new SourceObjectIntegrityWatcher(this));
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new ArchitectException("SQL Error in ddlGenerator",e);
        }
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

    public ProfileManagerImpl getProfileManager() {
        return profileManager;
    }

    public SQLDatabase getTargetDatabase() {
        return db;
    }

    public CoreProject getProject() {
        return project;
    }
    
    public void setProject(CoreProject project) {
        this.project = project;
    }

    public SQLObjectRoot getRootObject() {
        return rootObject;
    }

    public ArchitectSessionContext getContext() {
        return context;
    }
    public void setSourceDatabaseList(List<SQLDatabase> databases) throws ArchitectException {
        SQLObject root = getRootObject();
        while (root.getChildCount() > 0) {
            root.removeChild(root.getChildCount() - 1);
        }
        for (SQLDatabase db : databases) {
            root.addChild(db);
        }
    }
    
    public DDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }

    public void setDDLGenerator(DDLGenerator generator) {
        ddlGenerator = generator;
    }
    
    public void setProfileManager(ProfileManagerImpl manager) {
        profileManager = manager;
    }
    
    public UserPrompter createUserPrompter(String question, String okText, String notOkText, String cancelText) {
        return userPrompterFactory.createUserPrompter(question, okText, notOkText, cancelText);
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
    
    public OLAPRootObject getOLAPRootObject() {
        return olapRootObject;
    }
}

