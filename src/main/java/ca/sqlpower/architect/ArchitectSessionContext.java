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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.enterprise.client.SPServerInfoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;

/**
 * The ArchitectSessionContext interface specifies a set of
 * properties and methods for creating new Architect Sessions.
 * Additionally, the session context is the gateway to information
 * that is specific to the current user's environment (as opposed
 * to information that is attached to specific projects, which is
 * stored in the session).
 */
public interface ArchitectSessionContext {

    /**
     * The URL where there is more information about finding and configuring
     * JDBC drivers.
     */
    static final String DRIVERS_URL = "http://www.sqlpower.ca/forum/posts/list/401.page";
    
    /**
     * Creates a new session within this parent context. 
     * @throws SQLObjectException 
     */
    public abstract ArchitectSession createSession() throws SQLObjectException;
    
    /**
     * Creates a new session by loading the Architect XML project description
     * from the given input stream.
     * 
     * @param in The input stream to read the XML data from
     * @return The new session
     * @throws SQLObjectException
     * @throws IOException
     */
     public abstract ArchitectSession createSession(InputStream in) throws SQLObjectException, IOException;

    /**
     * Returns the user preferences node associated with this context.
     * 
     * @return the preferences
     */
    public abstract Preferences getPrefs();

    /**
     * Returns a collection containing all the sessions from this context. 
     * 
     * @return collections of Architect Session
     */
    public Collection<ArchitectSession> getSessions();
    
    /**
     * 
     * @return the PlDotIni path
     */
    public String getPlDotIniPath();
    
    /**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link ca.sqlpower.architect.ArchitectSessionContextImpl#plDotIni}.
     * @return JDBCDataSource Collection
     */
    public DataSourceCollection<JDBCDataSource> getPlDotIni();
    
    public List<JDBCDataSource> getConnections();
    
    /**
     * Sets the plDotIniPath property, and nulls out the current plDotIni
     * if the given value differs from the existing one.  See {@link ca.sqlpower.architect.ArchitectSessionContextImpl#plDotIniPath}.
     */
    public void setPlDotIniPath(String plDotIniPath);
    
    /**
     * Returns the {@link SPServerInfoManager} for this application instance.
     */
    public SPServerInfoManager getServerManager();
}
