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
package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;

/**
 * Instances of this class provide the basic global (non-project-specific) settings
 * and facilities to an invocation of the Architect's Swing user interface.  You
 * need an instance of one of these in order to start the Architect's Swing UI.
 * <p>
 * It may one day be desirable for this to be an interface, but there didn't seem
 * to be a need for it when we first created this class.
 */
public class ArchitectSwingSessionContextImpl implements ArchitectSwingSessionContext {
    
    private static final Logger logger = Logger.getLogger(ArchitectSwingSessionContextImpl.class);
    
    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    
    /**
     * A more structured interface to the prefs node.  Might be going away soon.
     */
    CoreUserSettings userSettings;
    
    /**
     * This is the context that some work delegates to.
     */
    private ArchitectSessionContext delegateContext;
   
    /**
     * The database connection manager GUI for this session context (because all sessions
     * share the same set of database connections).
     */
    private final DatabaseConnectionManager dbConnectionManager;

    /**
     * The Preferences editor for this application context.
     */
    private final PreferencesEditor prefsEditor;
    
    /**
     * This factory just passes the request through to the {@link ASUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceDialogFactory dsDialogFactory = new DataSourceDialogFactory() {

        public JDialog showDialog(Window parentWindow, SPDataSource dataSource, Runnable onAccept) {
            return ASUtils.showDbcsDialog(parentWindow, dataSource, onAccept);
        }
        
    };
    
    /**
     * This factory just passes the request through to the {@link ASUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {
        public Window showDialog(Window owner) {
            return prefsEditor.showJDBCDriverPreferences(owner, ArchitectSwingSessionContextImpl.this);
        }
    };

    /**
     * Creates a new session context.  You will normally only need one of these
     * per JVM, but there is no technical barrier to creating multiple contexts.
     * <p>
     * Important note: This constructor must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * @throws ArchitectException 
     */
    ArchitectSwingSessionContextImpl() throws ArchitectException {
        delegateContext = new ArchitectSessionContextImpl();
        
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        userSettings = new CoreUserSettings(getPrefs());

        // this doesn't appear to have any effect on the motion threshold
        // in the Playpen, but it does seem to work on the DBTree...
        logger.debug("current motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));
        System.setProperty("awt.dnd.drag.threshold","10");
        logger.debug("new motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        
        dbConnectionManager = new DatabaseConnectionManager(getPlDotIni(), dsDialogFactory,dsTypeDialogFactory);
        prefsEditor = new PreferencesEditor();

        // sets the icon so exception dialogs handled by SPSUtils instead
        // of ASUtils can still have the correct icon
        SPSUtils.setMasterIcon(new ImageIcon(ASUtils.getFrameIconImage()));
    }
    
    /**
     * Loads the XML project description from the input stream,
     * optionally creating the GUI for you.
     * <p>
     * <b>Important Note:</b> If you set showGUI to true, this method
     * must be called on the Swing Event Dispatch Thread.  If this is
     * not possible or practical, call this method with showGUI false,
     * then call {@link ArchitectSwingSession#initGUI()} on the returned
     * session using the event dispatch thread some time later on.
     * @throws IOException If the file is not found or can't be read.
     * @throws ArchitectException if there is some problem with the file
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    public ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws ArchitectException, IOException {
        ArchitectSwingSession session = createSessionImpl("Loading...", false, null);
        
        try {
            session.getProject().load(in, getPlDotIni());

            if (showGUI) {
                session.initGUI();
            }
        
            return session;
        } catch (ArchitectException ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e);
            }
            throw ex;
        } catch (IOException ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e);
            }
            throw new RuntimeException(ex);
        }
    }
    
    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession() throws ArchitectException {
        return createSession(true);
    }

    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException {
        return createSessionImpl("New Project", showGUI, null);
    }
    
    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession(InputStream in) throws ArchitectException, IOException {
        return createSession(in, true);
    }

    public ArchitectSwingSession createSession(ArchitectSwingSession openingSession) throws ArchitectException {
        return createSessionImpl("New Project", true, openingSession);
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
     * @throws ArchitectException
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    private ArchitectSwingSession createSessionImpl(String projectName, boolean showGUI, ArchitectSwingSession openingSession) throws ArchitectException {
        logger.debug("About to create a new session for project \"" + projectName + "\"");
        ArchitectSwingSessionImpl session = new ArchitectSwingSessionImpl(this, projectName);
        getSessions().add(session);
        session.addSessionLifecycleListener(sessionLifecycleListener);
        
        if (showGUI) {
            logger.debug("Creating the Architect frame...");
            session.initGUI(openingSession);
        
            if (getSessions().size() == 1) {
                showWelcomeScreen(session.getArchitectFrame());
            }
        }
        
        return session;
    }
    
    /**
     * Removes the closed session from the list, and terminates the VM
     * if there are no more sessions.
     */
    private SessionLifecycleListener sessionLifecycleListener = new SessionLifecycleListener() {
        public void sessionClosing(SessionLifecycleEvent e) {
            getSessions().remove(e.getSource());
            if (getSessions().isEmpty() && exitAfterAllSessionsClosed) {
                System.exit(0);
            }
        }
    };

    /**
     * Defaults to false, which is required by the interface spec.
     */
    private boolean exitAfterAllSessionsClosed = false;
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#isMacOSX()
     */
    public boolean isMacOSX() {
        return MAC_OS_X;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#getPrefs()
     */
    public Preferences getPrefs() {
        return delegateContext.getPrefs();
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#getUserSettings()
     */
    public CoreUserSettings getUserSettings() {
        return userSettings;
    }

    public Collection<ArchitectSession> getSessions() {
        return delegateContext.getSessions();
    }
    
    private void showWelcomeScreen(Component dialogOwner) {
        // should almost certainly move this into the swing context
        if (getUserSettings().getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true)) {
            WelcomeScreen ws = new WelcomeScreen(this);
            ws.showWelcomeDialog(dialogOwner);
        }
    }

    public void showConnectionManager(Window owner) {
        dbConnectionManager.showDialog(owner);
    }

    public void showPreferenceDialog(Window owner) {
        prefsEditor.showPreferencesDialog(owner, ArchitectSwingSessionContextImpl.this);
    }
    
    /**
     * Attempts to close all sessions that were created by this context.  The
     * user might abort some or all of the session closes by choosing to cancel
     * when the "prompt for unsaved modifications" step happens.
     */
    public void closeAll() {
        List<ArchitectSession> doomedSessions =
            new ArrayList<ArchitectSession>(getSessions());
        
        for (ArchitectSession s : doomedSessions) {
            ((ArchitectSwingSession) s).close();
        }
    }

    public boolean getExitAfterAllSessionsClosed() {
        return exitAfterAllSessionsClosed;
    }

    public void setExitAfterAllSessionsClosed(boolean allowExit) {
        exitAfterAllSessionsClosed = allowExit;
    }

    public List<SPDataSource> getConnections() {
        return delegateContext.getConnections();
    }

    public DataSourceCollection getPlDotIni() {
        return delegateContext.getPlDotIni();
    }

    public String getPlDotIniPath() {
        return delegateContext.getPlDotIniPath();
    }

    public void setPlDotIniPath(String plDotIniPath) {
        delegateContext.setPlDotIniPath(plDotIniPath);
    }
}
