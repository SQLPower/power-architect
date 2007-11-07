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
package ca.sqlpower.architect;

import java.util.List;

import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ProfileManager;

public interface ArchitectSession {

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
    
    public GenericDDLGenerator getDDLGenerator();
    
    
    public void setDDLGenerator(GenericDDLGenerator generator);
}