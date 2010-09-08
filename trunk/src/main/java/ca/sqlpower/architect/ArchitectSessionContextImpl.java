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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.SPServerInfoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.util.Version;

public class ArchitectSessionContextImpl implements ArchitectSessionContext {
    
    private static final Logger logger = Logger.getLogger(ArchitectSessionContextImpl.class);
    
    private static final String DEFAULT_PATH = "/architect-enterprise";

    /**
     * The preferences node that user-specific preferences are stored in.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(ArchitectSessionContextImpl.class);
    
    /**
     * The parsed list of connections.
     */
    private DataSourceCollection<JDBCDataSource> plDotIni;
    
    /**
     * The location of the PL.INI file.
     */
    private String plDotIniPath;
    
    /**
     * All live sessions that exist in (and were created by) this context.  Sessions
     * will be removed from this list when they fire their sessionClosing lifecycle
     * event.
     */
    private Collection<ArchitectSession> sessions;
    
    private SPServerInfoManager serverManager;

    /**
     * Creates a new session context.  You will normally only need one of these
     * per JVM, but there is no technical barrier to creating multiple contexts.
     * <p>
     * Important note: This constructor must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * @throws SQLObjectException 
     * @throws BackingStoreException 
     */
    public ArchitectSessionContextImpl() throws SQLObjectException, BackingStoreException {
        this((String) null);
    }
    
    public ArchitectSessionContextImpl(boolean checkPlDotIni) throws SQLObjectException, BackingStoreException {
        this(null, checkPlDotIni);
    }
    
    public ArchitectSessionContextImpl(String PlDotIniPath) throws SQLObjectException, BackingStoreException {
        this(PlDotIniPath, true);
    }
    
    /**
     * Similar to the default constructor, but we can specify a pl.ini path
     * ourselves. (This has been created in order to fully automate the JUnit test).
     * @throws BackingStoreException 
     */
    public ArchitectSessionContextImpl(String PlDotIniPath, boolean checkPath) throws SQLObjectException, BackingStoreException {
        if (PlDotIniPath == null) {
            PlDotIniPath = prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null);
        }
        
        setPlDotIniPath(PlDotIniPath);
        
        if (checkPath) {
            setPlDotIniPath(ArchitectUtils.checkForValidPlDotIni(PlDotIniPath, "Architect"));
        }
        init();
    }
    
    /**
     * Similar to the default constructor, but we can specify a data source collection
     * ourselves. (This has been created in order to support immutable dsc's for the server).
     * @throws BackingStoreException 
     */
    public ArchitectSessionContextImpl(DataSourceCollection<JDBCDataSource> dataSources) throws SQLObjectException, BackingStoreException {
        plDotIni = dataSources;
        init();
    }
    
    private void init() throws BackingStoreException {
        sessions = new HashSet<ArchitectSession>();
        
        ArchitectUtils.startup();

        ArchitectUtils.configureLog4j();

        
        SPServerInfo defaultSettings = new SPServerInfo("", "", 8080, DEFAULT_PATH, "", "");
        serverManager = new SPServerInfoManager(getPrefs().node("servers"), new Version(
                ArchitectVersion.APP_FULL_VERSION.toString()), defaultSettings);
        

        
    }
    
    public ArchitectSession createSession() throws SQLObjectException {
        return createSessionImpl("New Project");
    }

    public ArchitectSession createSession(InputStream in) throws SQLObjectException, IOException {
        ArchitectSession session = createSessionImpl("Loading...");
        session.getProjectLoader().load(in, getPlDotIni());
        return session;
    }
    
    /**
     * This is the one createSession() implementation to which all other overloads of
     * createSession() actually delegate their work.
     * <p>
     * This method tracks all sessions that have been successfully created in the
     * {@link #sessions} field.
     * 
     * @param projectName
     * @return
     * @throws SQLObjectException
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    private ArchitectSession createSessionImpl(String projectName) throws SQLObjectException {
        logger.debug("About to create a new session for project \"" + projectName + "\"");
        ArchitectSessionImpl session = new ArchitectSessionImpl(this, projectName);
        sessions.add(session);
        
        return session;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public Collection<ArchitectSession> getSessions() {
        return sessions;
    }

    /**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link #plDotIni}.
     */
    public DataSourceCollection<JDBCDataSource> getPlDotIni() {
        String path = getPlDotIniPath();
        if (path == null && plDotIni == null) return null;
        
        if (plDotIni == null) {
            DataSourceCollection<SPDataSource> newPlDotIni = new PlDotIni();
            try {
                logger.debug("Reading PL.INI defaults");
                newPlDotIni.read(getClass().getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read system resource default_database_types.ini",e));
            }
            try {
                if (newPlDotIni != null) {
                    logger.debug("Reading new PL.INI instance");
                    newPlDotIni.read(new File(path));
                }
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
            
            plDotIni = new SpecificDataSourceCollection<JDBCDataSource>(newPlDotIni, JDBCDataSource.class);
        }
        return plDotIni;
    }
    
    /**
     * See {@link #plDotIniPath}.
     */
    public String getPlDotIniPath() {
        return plDotIniPath;
    }
    
    /**
     * Sets the plDotIniPath property, and nulls out the current plDotIni
     * if the given value differs from the existing one.  See {@link #plDotIniPath}.
     */
    public void setPlDotIniPath(String plDotIniPath) {
        logger.debug("PlDotIniPath changing from \""+this.plDotIniPath+"\" to \""+plDotIniPath+"\"");

        // important to short-circuit when the value is not different
        // (if we don't, the prefs panel doesn't save properly)
        if (this.plDotIniPath != null && this.plDotIniPath.equals(plDotIniPath)) {
            return;
        }
        this.plDotIniPath = plDotIniPath;
        this.plDotIni = null;
    }
    
    public List<JDBCDataSource> getConnections() {
        return getPlDotIni().getConnections();
    }

    public SPServerInfoManager getServerManager() {
        return serverManager;
    }
}
