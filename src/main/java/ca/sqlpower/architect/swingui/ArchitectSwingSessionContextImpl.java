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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.NetworkConflictResolver;
import ca.sqlpower.architect.enterprise.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.SPServerInfoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory.NonModalSwingUIUserPrompterFactory;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

/**
 * Instances of this class provide the basic global (non-project-specific) settings
 * and facilities to an invocation of the Architect's Swing user interface.  You
 * need an instance of one of these in order to start the Architect's Swing UI.
 * <p>
 * It may one day be desirable for this to be an interface, but there didn't seem
 * to be a need for it when we first created this class.
 */
public class ArchitectSwingSessionContextImpl implements ArchitectSwingSessionContext, ClipboardOwner {
    
    private static final Logger logger = Logger.getLogger(ArchitectSwingSessionContextImpl.class);
    
    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x")); //$NON-NLS-1$ //$NON-NLS-2$
    
    private final List<ArchitectFrame> frames = new ArrayList<ArchitectFrame>();
    
    /**
     * This dummy transferable is placed on the local clipboard if the system 
     * clipboard is lost. This is used instead of a null value as setting
     * the local clipboard to have null as its content causes an NPE.
     */
    private class DummyTransferable implements Transferable {

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public DataFlavor[] getTransferDataFlavors() {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    /**
     * The dummy transferable to place on the local clipboard to avoid an
     * NPE when setting the contents to null.
     */
    private final Transferable dummyTransferable = new DummyTransferable();
    
    /**
     * A more structured interface to the prefs node.  Might be going away soon.
     */
    CoreUserSettings userSettings;
    
    /**
     * This is the context that some work delegates to.
     */
    private ArchitectSessionContext delegateContext;
   
    /**
     * This internal clipboard allows copying and pasting objects within
     * the app to stay as objects. The system clipboard throws modification
     * exceptions when it is used with SQLObjects.
     */
    private final Clipboard clipboard = new Clipboard("Internal clipboard");
    
    /**
     * Creates a new session context.  You will normally only need one of these
     * per JVM, but there is no technical barrier to creating multiple contexts.
     * <p>
     * Important note: This constructor must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * 
     * @throws SQLObjectException 
     * @throws BackingStoreException 
     */
    public ArchitectSwingSessionContextImpl() throws SQLObjectException, BackingStoreException {
        this(new ArchitectSessionContextImpl());
    }
    
    public ArchitectSwingSessionContextImpl(String plIniPath, boolean checkPath) 
            throws SQLObjectException, BackingStoreException {
        this(new ArchitectSessionContextImpl(plIniPath, checkPath));
    }
    
    public ArchitectSwingSessionContextImpl(DataSourceCollection<JDBCDataSource> dsCollection)
            throws SQLObjectException, BackingStoreException {
        this(new ArchitectSessionContextImpl(dsCollection));
    }
    
    private ArchitectSwingSessionContextImpl(ArchitectSessionContextImpl delegate) 
            throws SQLObjectException, BackingStoreException {
        delegateContext = delegate;
        
        System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        
        userSettings = new CoreUserSettings(getPrefs());

        // this doesn't appear to have any effect on the motion threshold
        // in the Playpen, but it does seem to work on the DBTree...
        logger.debug("current motion threshold is: " + System.getProperty("awt.dnd.drag.threshold")); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("awt.dnd.drag.threshold","10"); //$NON-NLS-1$ //$NON-NLS-2$
        logger.debug("new motion threshold is: " + System.getProperty("awt.dnd.drag.threshold")); //$NON-NLS-1$ //$NON-NLS-2$

        // sets the icon so exception dialogs handled by SPSUtils instead
        // of ASUtils can still have the correct icon
        SPSUtils.setMasterIcon(new ImageIcon(ASUtils.getFrameIconImage()));
        
        logger.debug("toolkit has system clipboard " + Toolkit.getDefaultToolkit().getSystemClipboard());
        clipboard.setContents(dummyTransferable, this);
        
    }

    /**
     * Loads the XML project description from an input stream. If frame is
     * non-null, a GUI will be created inside the given frame.
     * <p>
     * <b>Important Note:</b> If you wish to show a GUI, this method must be
     * called on the Swing Event Dispatch Thread. If this is not possible or
     * practical, call {@link #createSession(InputStream)}, then add this
     * session to an ArchitectFrame on the EDT later on.
     * 
     * @throws IOException
     *             If the file is not found or can't be read.
     * @throws SQLObjectException
     *             if there is some problem with the file
     * @throws IllegalStateException
     *             if frame is not null and this method was not called on the Event
     *             Dispatch Thread.
     */
    public ArchitectSwingSession createSession(InputStream in) throws SQLObjectException, IOException {
        ArchitectSwingSession session = createSessionImpl(Messages.getString("ArchitectSwingSessionContextImpl.projectLoadingDialogTitle")); //$NON-NLS-1$
        
        try {
            session.getProjectLoader().load(in, session.getDataSources());

            return session;
        } catch (SQLObjectException ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e); //$NON-NLS-1$
            }
            throw ex;
        } catch (IOException ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e); //$NON-NLS-1$
            }
            throw ex;
        } catch (Exception ex) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Session cleanup failed after botched read. Eating this secondary exception:", e); //$NON-NLS-1$
            }
            throw new RuntimeException(ex);
        }
    }
    
    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession() throws SQLObjectException {
        return createSessionImpl(Messages.getString("ArchitectSwingSessionContextImpl.defaultNewProjectName"));
    }

    public ArchitectSwingSession createServerSession(ProjectLocation projectLocation, boolean autoStartUpdater) throws SQLObjectException {

        final ArchitectClientSideSession clientSession = new ArchitectClientSideSession(this, projectLocation.getName(), projectLocation);
        final ArchitectSwingSession swingSession = new ArchitectSwingSessionImpl(this, clientSession);
        clientSession.getUpdater().setUserPrompterFactory(new NonModalSwingUIUserPrompterFactory(swingSession.getArchitectFrame()));
        
        clientSession.getUpdater().addListener(new NetworkConflictResolver.UpdateListener() {
            
            boolean loading = true;
            
            public void preUpdatePerformed(NetworkConflictResolver resolver) {
                if (loading) {
                    swingSession.getUndoManager().setLoading(true);
                }
            }
        
            public boolean updatePerformed(NetworkConflictResolver resolver) {
                //On the first update from the server we must discard all edits
                //as it is equivalent to loading from a file and undoing does not
                //make sense.
                if (loading) {
                    swingSession.getUndoManager().setLoading(false);
                    loading = false;
                }
                return false;
            }
        
            public boolean updateException(NetworkConflictResolver resolver, Throwable t) {                
                if (loading) {
                    swingSession.getUndoManager().setLoading(false);
                    loading = false;
                }
                return false;
            }
            
            public void workspaceDeleted() {
                int response = JOptionPane.showConfirmDialog(swingSession.getArchitectFrame(),
                        "This project has been deleted from the server. " +
                        "\nWould you like to save it to a file before it closes?",
                        "Workspace deleted...", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    swingSession.saveOrSaveAs(true, false);
                }                
                exitAfterAllSessionsClosed = false;
                swingSession.close();
                exitAfterAllSessionsClosed = true;
                if (getSessions().size() == 0) {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                try {
                                    createSession();
                                } catch (Exception e) {
                                    throw new RuntimeException("Error occurred when trying to open new project", e);
                                }
                            }
                        });
                    }  catch (Exception e) {
                        throw new RuntimeException("Error occurred when trying to open new project", e);
                    }
                }
            }
        });
        
        if (autoStartUpdater) {
            clientSession.startUpdaterThread();
        }
        
        getSessions().add(swingSession);
        swingSession.addSessionLifecycleListener(sessionLifecycleListener);

        if (externalLifecycleListener != null) {
            externalLifecycleListener.sessionOpening(
                    new SessionLifecycleEvent<ArchitectSwingSession>(swingSession));
        }
        
        return swingSession;
    }

    
    public ArchitectClientSideSession createSecuritySession(final SPServerInfo serverInfo) {
        ArchitectClientSideSession session = null;
        
        if (ArchitectClientSideSession.getSecuritySessions().get(serverInfo.getServerAddress()) == null) {
            ProjectLocation securityLocation = new ProjectLocation("system", "system", serverInfo);
             
            try {
                final ArchitectClientSideSession newSecuritySession = new ArchitectClientSideSession(this, serverInfo.getServerAddress(), securityLocation);
            
                newSecuritySession.getUpdater().addListener(new NetworkConflictResolver.UpdateListener() {
                    public boolean updatePerformed(NetworkConflictResolver resolver) {return false;}
                
                    public boolean updateException(NetworkConflictResolver resolver, Throwable t) {
                        if (t instanceof AccessDeniedException) return false;
                        
                        newSecuritySession.close();
                        ArchitectClientSideSession.getSecuritySessions().remove(serverInfo.getServerAddress());
                        final String errorMessage = "Error accessing security session.";
                        logger.error(errorMessage, t);
                        SPSUtils.showExceptionDialogNoReport(frames.get(0), errorMessage, t);
                        //If you try to create a new security session here because creating the first
                        //one failed the same error message can continue to repeat. 
                        return true;
                    }

                    public void preUpdatePerformed(NetworkConflictResolver resolver) {
                        //do nothing
                    }
                    
                    public void workspaceDeleted() {
                        // do nothing
                    }
                });
            
                newSecuritySession.startUpdaterThread();
                ArchitectClientSideSession.getSecuritySessions().put(serverInfo.getServerAddress(), newSecuritySession);
                session = newSecuritySession;
            } catch (AccessDeniedException e) {
                throw e;
            } catch (SQLObjectException e) {
                throw new RuntimeException("Unable to create security session!!!", e);
            }
        } else {
            session = ArchitectClientSideSession.getSecuritySessions().get(serverInfo.getServerAddress());
        }
        
        return session;
    }
    
    /**
     * This is the one createSession() implementation to which all other
     * overloads of createSession() actually delegate their work.
     * <p>
     * This method tracks all sessions that have been successfully created in
     * the {@link #sessions} field.
     * 
     * @param projectName
     *            The name of the project being opened in the new session
     * @param showGUI
     *            If true, then displays the GUI. If false, do not show the GUI
     * @param openingSession
     *            If showGUI is true, then positions the new session window
     *            relative to the openingSession's window. If null, then just
     *            positions the new windows according to the most recently
     *            stored user preference.
     * @return An new ArchitectSwingSession with the given project name.
     * @throws SQLObjectException
     * @throws IllegalStateException
     *             if showGUI==true and this method was not called on the Event
     *             Dispatch Thread.
     */
    private ArchitectSwingSession createSessionImpl(String projectName) throws SQLObjectException {
        logger.debug("About to create a new session for project \"" + projectName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ArchitectSwingSession session = new ArchitectSwingSessionImpl(this, projectName);
        getSessions().add(session);
        session.addSessionLifecycleListener(sessionLifecycleListener);

        if (externalLifecycleListener != null) {
            externalLifecycleListener.sessionOpening(
                    new SessionLifecycleEvent<ArchitectSwingSession>(session));
        }
        
        return session;
    }
    
    /**
     * Removes the closed session from the list, and terminates the VM
     * if there are no more sessions.
     */
    private SessionLifecycleListener<ArchitectSession> sessionLifecycleListener =
        new SessionLifecycleListener<ArchitectSession>() {
        public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
            getSessions().remove(e.getSource());
            
            boolean closeSecuritySessions = true;
            for (ArchitectSession session : getSessions()) {
                if (session.isEnterpriseSession()) {
                    closeSecuritySessions = false;
                }
            }
            
            if (closeSecuritySessions) {
                for (ArchitectSession session : ArchitectClientSideSession.getSecuritySessions().values()) {
                    session.close();
                }
                ArchitectClientSideSession.getSecuritySessions().clear();
            }
            
            if (getSessions().isEmpty() && exitAfterAllSessionsClosed) {
                for (ArchitectFrame frame : frames) {
                    try {
                        frame.saveSettings();
                    } catch (SQLObjectException ex) {
                        logger.error("Exception occurred while saving settings", ex);
                    }
                }
                System.exit(0);
            }
        }

        public void sessionOpening(SessionLifecycleEvent<ArchitectSession> e) {
        }
    };
    
    public void registerFrame(final ArchitectFrame frame) {
        frames.add(frame);
        
        if (frames.size() == 1) {
            frame.addWindowListener(new WindowListener() {
                @Override
                public void windowActivated(WindowEvent e) {
                    showWelcomeScreen(frame);
                    frame.removeWindowListener(this);
                }
                @Override
                public void windowClosed(WindowEvent e) {
                }
                @Override
                public void windowClosing(WindowEvent e) {
                }
                @Override
                public void windowDeactivated(WindowEvent e) {
                }
                @Override
                public void windowDeiconified(WindowEvent e) {
                }
                @Override
                public void windowIconified(WindowEvent e) {
                }
                @Override
                public void windowOpened(WindowEvent e) {
                }
                
            });
        }
        
        if (windowLifecycleListener != null) {
            windowLifecycleListener.sessionOpening(new SessionLifecycleEvent<ArchitectFrame>(frame));
        }
        
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent e) {
            }
            public void windowClosed(WindowEvent e) {
                if (windowLifecycleListener != null) {
                    frames.remove(frame);
                    windowLifecycleListener.sessionClosing(new SessionLifecycleEvent<ArchitectFrame>(frame));
                }
            }
            public void windowClosing(WindowEvent e) {
            }
            public void windowDeactivated(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowOpened(WindowEvent e) {
            }
            
        });
    }

    /**
     * Provides a way for architect enterprise to have at the sessions and
     * frames as they are created and distroyed.
     */
    private SessionLifecycleListener<ArchitectFrame> windowLifecycleListener = null;
    private SessionLifecycleListener<ArchitectSwingSession> externalLifecycleListener = null;
    
    public void setExternalLifecycleListener(SessionLifecycleListener<ArchitectSwingSession> externalLifecycleListener) {
        this.externalLifecycleListener = externalLifecycleListener;
    }
    
    public void setWindowLifecycleListener(SessionLifecycleListener<ArchitectFrame> l) {
        windowLifecycleListener = l;
    }
    
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
        if (getUserSettings().getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true)) {
            WelcomeScreen ws = new WelcomeScreen(this);
            ws.showWelcomeDialog(dialogOwner);
        }
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
            if (s instanceof ArchitectSwingSession) {
                ((ArchitectSwingSession) s).close();
            } else {
                s.close();
            }
        }
    }

    public boolean getExitAfterAllSessionsClosed() {
        return exitAfterAllSessionsClosed;
    }

    public void setExitAfterAllSessionsClosed(boolean allowExit) {
        exitAfterAllSessionsClosed = allowExit;
    }

    public List<JDBCDataSource> getConnections() {
        return delegateContext.getConnections();
    }

    public DataSourceCollection<JDBCDataSource> getPlDotIni() {
        return delegateContext.getPlDotIni();
    }

    public String getPlDotIniPath() {
        return delegateContext.getPlDotIniPath();
    }

    public void setPlDotIniPath(String plDotIniPath) {
        delegateContext.setPlDotIniPath(plDotIniPath);
    }
    
    public Transferable getClipboardContents() {
        logger.debug("local clipboard contents are " + clipboard.getContents(null));
        if (clipboard.getContents(null) != dummyTransferable) {
            logger.debug("Getting clipboard contents from local clipboard");
            return clipboard.getContents(null);
        }
        logger.debug("Getting clipboard contents from system");
        return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    }
    
    public void setClipboardContents(Transferable t) {
        clipboard.setContents(t, this);
        logger.debug("Setting local clipboard contents");
        if (t instanceof SQLObjectSelection) {
            ((SQLObjectSelection) t).setLocal(false);
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, this);
        logger.debug("toolkit pasting to system clipboard " + Toolkit.getDefaultToolkit().getSystemClipboard());
        if (t instanceof SQLObjectSelection) {
            ((SQLObjectSelection) t).setLocal(true);
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        this.clipboard.setContents(dummyTransferable, this);
        logger.debug("Context lost clipboard ownership");
    }

    public SPServerInfoManager getServerManager() {
        return delegateContext.getServerManager();
    }
}
