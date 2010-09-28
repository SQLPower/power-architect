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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.ProjectLoader;
import ca.sqlpower.architect.ProjectSettings;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.ProjectSettings.ColumnVisibility;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.NetworkConflictResolver;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.AddDataSourceAction;
import ca.sqlpower.architect.swingui.action.NewDataSourceAction;
import ca.sqlpower.architect.swingui.action.OpenProjectAction;
import ca.sqlpower.architect.swingui.action.PreferencesAction;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.OLAPSchemaManager;
import ca.sqlpower.architect.undo.ArchitectUndoManager;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.DialogUserPrompter;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory;
import ca.sqlpower.swingui.db.DataSourceDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectSwingSessionImpl implements ArchitectSwingSession {

    private static final Logger logger = Logger.getLogger(ArchitectSwingSessionImpl.class);
    
    private static final Executor saveExecutor = new ScheduledThreadPoolExecutor(1);
    
    private final ArchitectSwingSessionContext context;

    /**
     * This is the core session that some tasks are delegated to.
     */
    private final ArchitectSession delegateSession;

    /**
     * The frame that this project may appear in
     */
    private ArchitectFrame frame;
    
    /**
     * The panel where the GUI for this session appears
     */
    private JComponent projectPanel;
    
    private JScrollPane playPenScrollPane;

    private Saver saveBehaviour = new Saver() {
        public boolean save(ArchitectSwingSession session, boolean showChooser, boolean separateThread) {
            final boolean finalSeparateThread = separateThread;
            final ProgressMonitor pm = new ProgressMonitor
            (frame, Messages.getString("ArchitectSwingSessionImpl.saveProgressDialogTitle"), "", 0, 100); //$NON-NLS-1$ //$NON-NLS-2$

            class SaverTask implements Runnable {
                boolean success;

                public void run() {
                    SwingUIProjectLoader project = getProjectLoader();
                    try {
                        success = false;
                        if (finalSeparateThread) {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    getArchitectFrame().setEnableSaveOption(false);
                                }
                            });
                        }
                        project.setSaveInProgress(true);
                        project.save(finalSeparateThread ? pm : null);
                        success = true;
                    } catch (Exception ex) {
                        success = false;
                        ASUtils.showExceptionDialog(
                                ArchitectSwingSessionImpl.this,
                                Messages.getString("ArchitectSwingSessionImpl.cannotSaveProject")+ex.getMessage(), ex); //$NON-NLS-1$
                    } finally {
                        project.setSaveInProgress(false);
                        if (finalSeparateThread) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    getArchitectFrame().setEnableSaveOption(true);
                                }
                            });
                        } 
                    }
                }
            }
            SaverTask saveTask = new SaverTask();
            if (separateThread) {
                saveExecutor.execute(saveTask);
                return true; // this is an optimistic lie
            } else {
                saveTask.run();
                return saveTask.success;
            }
        }

        @Override
        public void saveToStream(ArchitectSwingSession session, OutputStream out) throws IOException {
            session.getProjectLoader().save(out, "utf-8");
        }
    };
    
    /**
     * The menu of recently-opened project files on this system.
     */
    private RecentMenu recent;

    /** the dialog that contains the small ProfileManagerView */
    private JDialog profileDialog;

    /**
     * Keeps track of whether or not the profile manager dialog has been packed yet.
     * We only want to do this the first time we make it visible, since doing it over
     * and over will annoy users.
     */
    private boolean profileDialogPacked = false;

    private final PlayPen playPen;

    /** the small dialog that lists the profiles */
    private ProfileManagerView profileManagerView;

    private CompareDMSettings compareDMSettings;

    private ArchitectUndoManager undoManager;

    private boolean isNew;    

    private final DBTree dbTree;

    private KettleJob kettleJob;
    // END STUFF BROUGHT IN FROM SwingUIProject

    private final List<SessionLifecycleListener<ArchitectSession>> lifecycleListeners;

    private Set<SPSwingWorker> swingWorkers;

    private ProjectModificationWatcher projectModificationWatcher;

    private List<OLAPEditSession> olapEditSessions;
    
    /**
     * A GUI for adding, removing, or opening the OLAP schema edit sessions
     * that belong to this architect session.
     */
    private final OLAPSchemaManager olapSchemaManager;

    /**
     * This will store the properties of the print panel.
     */
    private final PrintSettings printSettings;
    
    /**
     * This user prompter factory will create all the necessary GUI user prompts
     * for Architect.
     */
    private UserPrompterFactory swinguiUserPrompterFactory;

    /**
     * A colour chooser used by the {@link RelationshipEditPanel}, and possibly
     * others, to set custom colours. It has been created within a swing session
     * to share recent colours amongst different objects.
     */
    private static final JColorChooser colourChooser = new JColorChooser();

    /**
     * The database connection manager GUI for this session (because all sessions
     * do not share the same set of connections, some get theirs locally, and others
     * get theirs from a server).
     */
    private final DatabaseConnectionManager dbConnectionManager;
    
    /**
     * The Preferences editor for this application.
     */
    private final PreferencesEditor prefsEditor;
    
    /**
     * This factory just passes the request through to the {@link ASUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceDialogFactory dsDialogFactory = new DataSourceDialogFactory() {

        public JDialog showDialog(Window parentWindow, JDBCDataSource dataSource, Runnable onAccept) {
            return ASUtils.showDbcsDialog(parentWindow, dataSource, onAccept);
        }

        public JDialog showDialog(Window parentWindow, Olap4jDataSource dataSource,
                DataSourceCollection<? super JDBCDataSource> dsCollection, Runnable onAccept) {
            throw new UnsupportedOperationException("There is no editor dialog for Olap4j connections in Architect.");
        }
        
    };
    
    /**
     * This factory just passes the request through to the {@link ASUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {
        public Window showDialog(Window owner) {
            return prefsEditor.showJDBCDriverPreferences(owner, ArchitectSwingSessionImpl.this);
        }
    };

    /**
     * Creates a new, swing ready, architect session. It will not be displayed
     * anywhere until it is added to an {@link ArchitectFrame}.
     * 
     * @param context The {@link ArchitectSwingSessionContext} that owns this session.
     * @param name The User visible name of this session.
     * @throws SQLObjectException
     */
    ArchitectSwingSessionImpl(final ArchitectSwingSessionContext context, String name)
    throws SQLObjectException {        
        this(context, new ArchitectSessionImpl(context, name, new ArchitectSwingProject()));
    }

    /**
     * Creates a new, swing ready, architect session. It will not be displayed
     * anywhere until it is added to an {@link ArchitectFrame}.
     * 
     * @param context
     *            The {@link ArchitectSwingSessionContext} that owns this
     *            session.
     * @param delegateSession
     *            An {@link ArchitectSession} that this session will delegate
     *            functionality to. This session must have a swing project in
     *            order to properly maintain the state of the swing components.
     */
    ArchitectSwingSessionImpl(final ArchitectSwingSessionContext context, ArchitectSession delegateSession) {

        if (!(delegateSession.getWorkspace() instanceof ArchitectSwingProject)) {
            throw new IllegalStateException("The delegate session must have a swing project" +
            		"as its workspace. If there is no way to pass in a delegate with a swing" +
            		"project we may need to make the reference non-final.");
        }
        swinguiUserPrompterFactory = new DefaultUserPrompterFactory();
        
        this.isNew = true;
        this.context = context;
        this.delegateSession = delegateSession;
        getWorkspace().setSession(this);
        ProfileManagerImpl profileManager = new ProfileManagerImpl();
        ((ArchitectSessionImpl)delegateSession).setProfileManager(profileManager);
        ((ArchitectSessionImpl)delegateSession).setUserPrompterFactory(this);
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                File f = new File(fileName);
                try {
                    OpenProjectAction.getFileLoader().openAsynchronously(getContext().createSession(), f, ArchitectSwingSessionImpl.this);
                } catch (SQLObjectException ex) {
                    SPSUtils.showExceptionDialogNoReport(getArchitectFrame(), Messages.getString("ArchitectSwingSessionImpl.openProjectFileFailed"), ex); //$NON-NLS-1$
                }
            }
        };
        
        dbConnectionManager = new DatabaseConnectionManager(
                delegateSession.getDataSources(), dsDialogFactory, dsTypeDialogFactory);

        setProjectLoader(new SwingUIProjectLoader(this));

        compareDMSettings = new CompareDMSettings();

        kettleJob = new KettleJob(this);

        olapSchemaManager = new OLAPSchemaManager(this);
        
        this.dbTree = new DBTree(this);
        
        if (isEnterpriseSession()) {
            ((DBTreeCellRenderer) dbTree.getCellRenderer()).addIconFilter(new DomainCategorySnapshotIconFilter());
        }

        playPen = RelationalPlayPenFactory.createPlayPen(this, dbTree);
        this.getWorkspace().setPlayPenContentPane(playPen.getContentPane());
        UserSettings sprefs = getUserSettings().getSwingSettings();
        if (sprefs != null) {
            playPen.setRenderingAntialiased(sprefs.getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));
        }
        projectModificationWatcher = new ProjectModificationWatcher(playPen);
        
        getRootObject().addSPListener(new AbstractPoolingSPListener() {
            @Override
            public void propertyChangeImpl(PropertyChangeEvent e) {
                isNew = false;        
            }
            @Override
            public void childRemovedImpl(SPChildEvent e) {
                isNew = false;
            }
            @Override
            public void childAddedImpl(SPChildEvent e) {
                isNew = false;
            }
        });
        undoManager = new ArchitectUndoManager(playPen);
        final PropertyChangeListener undoListener = undoManager.getEventAdapter();
        
        playPen.getContentPane().addComponentPropertyListener(
                new String[] {
                        "bounds",
                        "pkConnectionPoint",
                        "fkConnectionPoint",
                        "backgroundColor",
                        "foregroundColor",
                        "dashed",
                        "rounded"
                }, 
                // Simple conversion from PropertyChangeListener to SPListener
                new AbstractSPListener() {
                    public void propertyChanged(PropertyChangeEvent evt) {
                        undoListener.propertyChange(evt);
                    }
                }
        );
        
        getWorkspace().setPlayPenContentPane(playPen.getContentPane());

        lifecycleListeners = new ArrayList<SessionLifecycleListener<ArchitectSession>>();

        swingWorkers = new HashSet<SPSwingWorker>();
        
        olapEditSessions = new ArrayList<OLAPEditSession>();
        
        printSettings = new PrintSettings();
        
        prefsEditor = new PreferencesEditor();
           
        // Ensure the this swing session is listening to the project settings
        // so it can repaint and update the UI when it is changed.
        
        final SPListener settingsListener = new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                getPlayPen().updateTablePanes();
                getPlayPen().repaint();
            }
        };
        getWorkspace().addSPListener(new AbstractSPListener() {
            public void childAdded(SPChildEvent evt) {
                if (evt.getChildType() == ProjectSettings.class) {
                    evt.getChild().addSPListener(settingsListener);
                }
            }
            public void childRemoved(SPChildEvent evt) {
                if (evt.getChildType() == ProjectSettings.class) {
                    evt.getChild().removeSPListener(settingsListener);
                }
            }
        });
        getWorkspace().getProjectSettings().addSPListener(settingsListener);       
        
        getWorkspace().getCriticManager().registerStartingCritics();
        
    }

    /**
     * Creates the GUI components for this session, and parents them to the
     * given ArchitectFrame. This should only be called by the frame itself, and
     * then only if you need a GUI.
     */
    public void initGUI(ArchitectFrame parentFrame) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called on the Swing Event Dispatch Thread."); //$NON-NLS-1$
        }
        
        this.frame = parentFrame;
        
        ((ArchitectSessionImpl)delegateSession).setStatusInfo(getStatusInformation());

        // makes the tool tips show up on these components 
        ToolTipManager.sharedInstance().registerComponent(playPen);
        ToolTipManager.sharedInstance().registerComponent(dbTree);

        swinguiUserPrompterFactory = new SwingUIUserPrompterFactory(parentFrame);
        
        if (projectPanel == null) {
            playPenScrollPane = new JScrollPane(playPen);
            projectPanel = new JPanel();
            projectPanel.setLayout(new BorderLayout());
            projectPanel.add(playPenScrollPane, BorderLayout.CENTER);
        }
        
        profileDialog = new JDialog(frame, Messages.getString("ArchitectSwingSessionImpl.profilesDialogTitle")); //$NON-NLS-1$
        profileManagerView = new ProfileManagerView(delegateSession.getProfileManager());
        delegateSession.getProfileManager().addProfileChangeListener(profileManagerView);
        profileDialog.add(profileManagerView);


        // This has to be called after frame.init() because playPen gets the keyboard actions from frame,
        // which only get set up after calling frame.init().
        RelationalPlayPenFactory.setupKeyboardActions(playPen, this);
        dbTree.setupKeyboardActions();

        macOSXRegistration(frame);

        profileDialog.setLocationRelativeTo(frame);
    }
    
    public SwingUIProjectLoader getProjectLoader() {
        return (SwingUIProjectLoader) delegateSession.getProjectLoader();
    }

    public void setProjectLoader(ProjectLoader project) {
        delegateSession.setProjectLoader(project);
    }

    public CoreUserSettings getUserSettings() {
        return context.getUserSettings();
    }

    public ArchitectSwingSessionContext getContext() {
        return context;
    }

    public ArchitectFrame getArchitectFrame() {
        return frame;
    }

    /**
     * Registers this application in Mac OS X if we're running on that platform.
     *
     * <p>This code came from Apple's "OS X Java Adapter" example.
     */
    private void macOSXRegistration(ArchitectFrame frame) {


        Action exitAction = frame.getExitAction();
        PreferencesAction prefAction = frame.getPrefAction();
        AboutAction aboutAction = frame.getAboutAction();
        Action openProjectAction = frame.getOpenProjectAction();

        // Whether or not this is OS X, the three actions we're referencing must have been initialized by now.
        if (exitAction == null) throw new IllegalStateException("Exit action has not been initialized"); //$NON-NLS-1$
        if (prefAction == null) throw new IllegalStateException("Prefs action has not been initialized"); //$NON-NLS-1$
        if (aboutAction == null) throw new IllegalStateException("About action has not been initialized"); //$NON-NLS-1$
        if (openProjectAction == null) throw new IllegalStateException("Open Project action has not been initialized"); //$NON-NLS-1$

        if (context.isMacOSX()) {
            try {
                Class<?> osxAdapter = ArchitectSwingSessionImpl.class.getClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter"); //$NON-NLS-1$

                // The main registration method.  Takes quitAction, prefsAction, aboutAction, openAction.
                Class<?>[] defArgs = { Action.class, Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs); //$NON-NLS-1$
                Object[] args = { exitAction, prefAction, aboutAction, openProjectAction };
                registerMethod.invoke(osxAdapter, args);

                // The enable prefs method.  Takes a boolean.
                defArgs = new Class[] { boolean.class };
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs); //$NON-NLS-1$
                args = new Object[] {Boolean.TRUE};
                prefsEnableMethod.invoke(osxAdapter, args);
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the project is modified, and if so presents the user with the option to save
     * the existing project.  This is useful to use in actions that are about to get rid of
     * the currently open project.
     *
     * @return True if the project can be closed; false if the project should remain open.
     */
    protected boolean promptForUnsavedModifications() {
        if (getProjectLoader().isModified()) {
            int response = JOptionPane.showOptionDialog(frame,
                    Messages.getString("ArchitectSwingSessionImpl.projectHasUnsavedChanges"), Messages.getString("ArchitectSwingSessionImpl.unsavedChangesDialogTitle"), //$NON-NLS-1$ //$NON-NLS-2$
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] {Messages.getString("ArchitectSwingSessionImpl.doNotSaveOption"), Messages.getString("ArchitectSwingSessionImpl.cancelOption"), Messages.getString("ArchitectSwingSessionImpl.saveOption")}, Messages.getString("ArchitectSwingSessionImpl.saveOption")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (response == 0) {
                return true;
            } else if (response == JOptionPane.CLOSED_OPTION || response == 1) {
                return false;
            } else {
                return saveOrSaveAs(false, false);
            }
        } else {
            return true;
        }
    }

    /**
     * Condition the Model to save the project, showing a file chooser when appropriate.
     *
     * @param showChooser If true, a chooser will always be shown; otherwise a
     * chooser will only be shown if the project has no file associated with it
     * (this is usually because it has never been saved before).
     * @param separateThread If true, the (possibly lengthy) save operation
     * will be executed in a separate thread and this method will return immediately.
     * Otherwise, the save operation will proceed on the current thread.
     * @return True if the project was saved successfully; false otherwise.  If saving
     * on a separate thread, a result of <code>true</code> is just an optimistic guess,
     * and there is no way to discover if the save operation has eventually succeeded or
     * failed.
     */
    public boolean saveOrSaveAs(boolean showChooser, boolean separateThread) {
        SwingUIProjectLoader project = getProjectLoader();

        if (project.getFile() == null || showChooser) {
            JFileChooser chooser = new JFileChooser(project.getFile());
            chooser.addChoosableFileFilter(SPSUtils.ARCHITECT_FILE_FILTER);
            int response = chooser.showSaveDialog(frame);
            if (response != JFileChooser.APPROVE_OPTION) {
                return false;
            } else {
                File file = chooser.getSelectedFile();
                if (!file.getPath().endsWith(".architect")) { //$NON-NLS-1$
                    file = new File(file.getPath()+".architect"); //$NON-NLS-1$
                }
                if (file.exists()) {
                    response = JOptionPane.showConfirmDialog(
                            frame,
                            Messages.getString("ArchitectSwingSessionImpl.fileAlreadyExists", file.getPath()), //$NON-NLS-1$
                            Messages.getString("ArchitectSwingSessionImpl.fileAlreadyExistsDialogTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
                    if (response == JOptionPane.NO_OPTION) {
                        return saveOrSaveAs(true, separateThread);
                    }
                }

                //creates an empty file if "file" does not exist 
                //so that the new file can be found by the recent menu
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    ASUtils.showExceptionDialog(this, Messages.getString("ArchitectSwingSessionImpl.couldNotCreateFile"), e); //$NON-NLS-1$
                    return false;
                }

                getRecentMenu().putRecentFileName(file.getAbsolutePath());
                project.setFile(file);
                project.clearFileVersion();
                String projName = file.getName().substring(0, file.getName().length()-".architect".length()); //$NON-NLS-1$
                setName(projName);
            }
        }
        return saveBehaviour.save(this, showChooser, separateThread);
    }
    
    public Executor getSaveExecutor() {
        return saveExecutor;
    }

    // STUFF BROUGHT IN FROM SwingUIProject

    /**
     * This is a common handler for all actions that must occur when switching
     * projects, e.g., prompting to save any unsaved changes, disposing dialogs,
     * shutting down running threads, and so on.
     */
    public boolean close() {
     // IMPORTANT NOTE: If the GUI hasn't been initialized, frame will be null.
        
        if (!delegateSession.isEnterpriseSession()) {
            // Only prompt to save if it is not an enterprise session
            if (getProjectLoader().isSaveInProgress()) {
                // project save is in progress, don't allow exit
                JOptionPane.showMessageDialog(frame,
                        Messages.getString("ArchitectSwingSessionImpl.cannotExitWhileSaving"), //$NON-NLS-1$
                        Messages.getString("ArchitectSwingSessionImpl.cannotExitWhileSavingDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
                return false;
            }
            
            if (!promptForUnsavedModifications()) {
                return false;
            }
        } else {
            getEnterpriseSession().putPref("zoom", playPen.getZoom());
        }
        
        if (!delegateSession.close()) {
            return false;
        }

        // If we still have ArchitectSwingWorker threads running, 
        // tell them to cancel, and then ask the user to try again later.
        // Note that it is not safe to force threads to stop, so we will
        // have to wait until the threads stop themselves.
        if (swingWorkers.size() > 0) {
            for (SPSwingWorker currentWorker : swingWorkers) {
                currentWorker.setCancelled(true);
            }


            Object[] options = {Messages.getString("ArchitectSwingSessionImpl.waitOption"), Messages.getString("ArchitectSwingSessionImpl.forceQuiteOption")}; //$NON-NLS-1$ //$NON-NLS-2$
            int n = JOptionPane.showOptionDialog(frame, 
                    Messages.getString("ArchitectSwingSessionImpl.unfinishedTasksRemaining"),  //$NON-NLS-1$
                    Messages.getString("ArchitectSwingSessionImpl.unfinishedTasksDialogTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,  //$NON-NLS-1$
                    null, options, options[0]);

            if (n == 0) {
                return false;
            } else {
                for (SPSwingWorker currentWorker : swingWorkers) {
                    currentWorker.kill();
                    currentWorker.setCancelled(true);
                }
            }
        }

        if (profileDialog != null) {
            // XXX this could/should be done by the profile dialog with a session closing listener
            profileDialog.dispose();
        }
        
        // Write pl.ini data back
        try {
            if (!isEnterpriseSession())
                if (context.getPlDotIniPath() != null) {
                    getDataSources().write(new File(context.getPlDotIniPath()));
                } else {
                    getDataSources().write();
                }
        } catch (IOException e) {
            logger.error("Couldn't save PL.INI file!", e); //$NON-NLS-1$
        }

        // close connections
        for (SQLDatabase db : getRootObject().getChildren(SQLDatabase.class)) {
            logger.debug ("closing connection: " + db.getName()); //$NON-NLS-1$
            db.disconnect();
        }

        // Clear the profile manager (the effect we want is just to cancel running profiles.. clearing is a harmless side effect)
        // XXX this could/should be done by the profile manager with a session closing listener
        delegateSession.getProfileManager().clear();

        fireSessionClosing();
        
        return true;
    }

    /**
     * Gets the value of sourceDatabases
     *
     * @return the value of sourceDatabases
     */
    public DBTree getDBTree()  {
        return this.dbTree;
    }

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        delegateSession.setSourceDatabaseList(databases);
    }

    /**
     * Gets the target database in the playPen.
     */
    public SQLDatabase getTargetDatabase()  {
        return delegateSession.getTargetDatabase();
    }

    /**
     * The ProjectModificationWatcher watches a PlayPen's components and
     * business model for changes.  When it detects any, it marks the
     * project dirty.
     *
     * <p>Note: when we implement proper undo/redo support, this class should
     * be replaced with a hook into that system.
     */
    class ProjectModificationWatcher extends AbstractSPListener {

        /**
         * Sets up a new modification watcher on the given playpen.
         */
        public ProjectModificationWatcher(PlayPen pp) {
            SQLPowerUtils.listenToHierarchy(getTargetDatabase(), this);
            PlayPenContentPane ppcp = pp.getContentPane();
            ppcp.addComponentPropertyListener(this);
        }

        /** Marks project dirty, and starts listening to new kids. */
        public void childAdded(SPChildEvent e) {
            getProjectLoader().setModified(true);
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            isNew = false;
        }

        /** Marks project dirty, and stops listening to removed kids. */
        public void childRemoved(SPChildEvent e) {
            getProjectLoader().setModified(true);
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
            isNew = false;
        }

        /** Marks project dirty. */
        public void propertyChanged(PropertyChangeEvent e) {
            getProjectLoader().setModified(true);
            isNew = false;
        }
    }

    public String getServerName() {
        if (delegateSession instanceof ArchitectClientSideSession) {
            return ((ArchitectClientSideSession) delegateSession).getProjectLocation().getServiceInfo().getName();
        } else {
            return null;
        }
    }
    
    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName()  {
        return delegateSession.getName();
    }

    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName) {
        delegateSession.setName(argName);
    }

    /**
     * Gets the value of playPen
     *
     * @return the value of playPen
     */
    public PlayPen getPlayPen()  {
        return this.playPen;
    }
    
    /**
     * Gets the recent menu list
     * 
     * @return the recent menu
     */
    public RecentMenu getRecentMenu()  {
        return this.recent;    
    }

	public LiquibaseSettings getLiquibaseSettings() {
		return delegateSession.getLiquibaseSettings();
	}

	public void setLiquibaseSettings(LiquibaseSettings settings) {
		delegateSession.setLiquibaseSettings(settings);
	}

    public CompareDMSettings getCompareDMSettings() {
        return compareDMSettings;
    }

    public void setCompareDMSettings(CompareDMSettings compareDMSettings) {
        this.compareDMSettings = compareDMSettings;
    }

    public ArchitectUndoManager getUndoManager() {
        return undoManager;
    }

    public ProfileManager getProfileManager() {
        return delegateSession.getProfileManager();
    }

    public JDialog getProfileDialog() {
        if (!profileDialogPacked) {
            profileDialog.pack();
            profileDialogPacked = true;
        }
        return profileDialog;
    }
    
    public void setSaveBehaviour(Saver saveBehaviour) {
        this.saveBehaviour = saveBehaviour;
    }
    
    public Saver getSaveBehaviour() {
        return saveBehaviour;
    }

    /**
     * See {@link #savingEntireSource}.
     *
     * @return the value of savingEntireSource
     */
    public boolean isSavingEntireSource()  {
        return getProjectSettings().isSavingEntireSource();
    }

    /**
     * See {@link #savingEntireSource}.
     *
     * @param argSavingEntireSource Value to assign to this.savingEntireSource
     */
    public void setSavingEntireSource(boolean argSavingEntireSource) {
        getProjectSettings().setSavingEntireSource(argSavingEntireSource);
    }

    public KettleJob getKettleJob() {
        return kettleJob;
    }

    public void setKettleJob(KettleJob kettleJob) {
        this.kettleJob = kettleJob;
    }
    // END STUFF BROUGHT IN FROM SwingUIProject

    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        lifecycleListeners.add(l);
    }
    
    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> l) {
        lifecycleListeners.remove(l);
    }

    public void fireSessionClosing() {
        SessionLifecycleEvent<ArchitectSession> evt = new SessionLifecycleEvent<ArchitectSession>(this);
        for (SessionLifecycleListener<ArchitectSession> listener: lifecycleListeners) {
            listener.sessionClosing(evt);
        }
    }

    public void registerSwingWorker(SPSwingWorker worker) {
        swingWorkers.add(worker);
    }

    public void removeSwingWorker(SPSwingWorker worker) {
        swingWorkers.remove(worker);
    }

    public boolean isNew() {
        return isNew;
    }

    /**
     * A package-private getter for the projectModificationWatcher.
     * This is currently used to run the event handler methods in
     * the unit tests.
     */
    ProjectModificationWatcher getProjectModificationWatcher() {
        return projectModificationWatcher;
    }

    public void setRelationshipLinesDirect(boolean relationshipLinesDirect) {
        getProjectSettings().setRelationshipLinesDirect(relationshipLinesDirect);
    }

    public boolean getRelationshipLinesDirect() {
        return getProjectSettings().isRelationshipLinesDirect();
    }
    
    public boolean isUsingLogicalNames() {
        return getProjectSettings().isUsingLogicalNames();
    }
    
    public void setUsingLogicalNames(boolean usingLogicalNames) {
        getProjectSettings().setUsingLogicalNames(usingLogicalNames);
    }

    public SQLObjectRoot getRootObject() {
        return delegateSession.getRootObject();
    }

    public DDLGenerator getDDLGenerator() {
        return delegateSession.getDDLGenerator();
    }

    public void setDDLGenerator(DDLGenerator generator) {
        delegateSession.setDDLGenerator(generator);
    }
    
    public OLAPRootObject getOLAPRootObject() {
        return getWorkspace().getOlapRootObject();
    }
    
    /**
     * Creates a new user prompter that uses a modal dialog to pose the given question.
     * 
     * @see DialogUserPrompter
     */
    public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType, UserPromptResponse defaultResponseType,
            Object defaultResponse, String ... buttonNames) {
        return swinguiUserPrompterFactory.createUserPrompter(question,
                responseType, optionType, defaultResponseType, defaultResponse, buttonNames);
        
    }
    
    public ProjectSettings getProjectSettings() {
        return getWorkspace().getProjectSettings();
    }

    public boolean isShowPkTag() {
        return getProjectSettings().isShowPkTag();
    }

    public void setShowPkTag(boolean showPkTag) {
        getProjectSettings().setShowPkTag(showPkTag);
    }

    public boolean isShowFkTag() {
        return getProjectSettings().isShowFkTag();
    }

    public void setShowFkTag(boolean showFkTag) {
        getProjectSettings().setShowFkTag(showFkTag);
    }

    public boolean isShowAkTag() {
        return getProjectSettings().isShowAkTag();
    }

    public void setShowAkTag(boolean showAkTag) {
        getProjectSettings().setShowAkTag(showAkTag);
    }

    /**
     * Sets the visibility of columns in the playpen of this session.
     * 
     * @param option The new column visibility setting. If null, all columns will
     * be shown (equivalent to specifying {@link ColumnVisibility#ALL}).
     */
    public void setColumnVisibility(ColumnVisibility option) {
        getProjectSettings().setColumnVisibility(option);
    }
    
    public ColumnVisibility getColumnVisibility() {
        return getProjectSettings().getColumnVisibility();
    }
    
    public void showOLAPSchemaManager(Window owner) {
        olapSchemaManager.showDialog(owner);
    }

    public List<OLAPEditSession> getOLAPEditSessions() {
        return olapEditSessions;
    }
    
    public OLAPEditSession getOLAPEditSession(OLAPSession olapSession) {
        if (olapSession == null) {
            throw new NullPointerException(Messages.getString("ArchitectSwingSessionImpl.nullOlapSession")); //$NON-NLS-1$
        }
        for (OLAPEditSession editSession : getOLAPEditSessions()) {
            if (editSession.getOlapSession() == olapSession) {
                return editSession;
            }
        }
        return new OLAPEditSession(this, olapSession);
    }
    
    // docs inherit from interface
    public JMenu createDataSourcesMenu() {
        JMenu dbcsMenu = new JMenu(Messages.getString("DBTree.addSourceConnectionMenuName")); //$NON-NLS-1$
        dbcsMenu.add(new JMenuItem(new NewDataSourceAction(this)));
        dbcsMenu.addSeparator();

        // populate
        for (SPDataSource dbcs : getDataSources().getConnections()) {
            dbcsMenu.add(new JMenuItem(new AddDataSourceAction(dbTree, dbcs)));
        }
        SPSUtils.breakLongMenu(getArchitectFrame(), dbcsMenu);
        
        return dbcsMenu;
    }
    
    public PrintSettings getPrintSettings() {
        return printSettings;
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
        return delegateSession.getDatabase(ds);
    }

    public boolean isDisplayRelationshipLabel() {
        return getProjectSettings().isDisplayRelationshipLabel();
    }
    
    public void setDisplayRelationshipLabel(boolean displayRelationshipLabel) {
        getProjectSettings().setDisplayRelationshipLabel(displayRelationshipLabel);
    }
    
    /**
     * This method will let users select a custom colour from a colour chooser
     * and then return the colour.
     * 
     * @param initial
     *            The initial colour to have selected in the colour chooser.
     * @return The colour selected or created by the user.
     */
    public static Color getCustomColour(Color initial, JComponent parent) {
        if (initial == null) {
            initial = Color.BLACK;
        }
        colourChooser.setColor(initial);
        ColorTracker ok = new ColorTracker(colourChooser);
        JDialog dialog = JColorChooser.createDialog(parent, "Choose a custom colour", true, colourChooser, ok, null);

        dialog.setVisible(true); 

        return ok.getColor();
    }
    
    /**
     * Action Listener used by the custom colour dialog created in the
     * getCustomColour method.
     */
    private static class ColorTracker implements ActionListener, Serializable {
        JColorChooser chooser;
        Color color;

        public ColorTracker(JColorChooser c) {
            chooser = c;
        }

        public void actionPerformed(ActionEvent e) {
            color = chooser.getColor();
        }

        public Color getColor() {
            return color;
        }
    }

    public UserPrompter createDatabaseUserPrompter(String question, List<Class<? extends SPDataSource>> dsTypes,
            UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
            DataSourceCollection<SPDataSource> dsCollection, String... buttonNames) {
        return swinguiUserPrompterFactory.createDatabaseUserPrompter(question, dsTypes, optionType,
                defaultResponseType, defaultResponse, dsCollection, buttonNames);
    }

    public ArchitectSwingProject getWorkspace() {
        return (ArchitectSwingProject) delegateSession.getWorkspace();
    }

    public boolean isForegroundThread() {
        //Until the GUI is initialized we may be running headless in which case
        //we will not be using the EDT.
        if (frame != null) {
            return SwingUtilities.isEventDispatchThread();
        } else {
            return true;
        }
    }

    public void runInBackground(Runnable runner) {
        runInBackground(runner, "worker");
    }
    
    public void runInBackground(final Runnable runner, String name) {
        new Thread(runner, name).start();       
    }

    public void runInForeground(Runnable runner) {
        //Until the GUI is initialized we may be running headless in which case
        //we will not be using the EDT.
        if (frame == null || SwingUtilities.isEventDispatchThread()) {
            runner.run();
        } else {
            SwingUtilities.invokeLater(runner);
        }     
    }
    
    public boolean isEnterpriseSession() {
        return delegateSession.isEnterpriseSession();
    }

    public ArchitectClientSideSession getEnterpriseSession() {
        if (isEnterpriseSession()) {
            return (ArchitectClientSideSession) delegateSession;
        } else {
            throw new RuntimeException("This swing session is not an enterprise session");
        }
    }

    public DataSourceCollection<JDBCDataSource> getDataSources() {
        return delegateSession.getDataSources();
    }

    public void showConnectionManager(Window owner) {
        dbConnectionManager.showDialog(owner);
    }

    public void showPreferenceDialog(Window owner) {
        prefsEditor.showPreferencesDialog(owner, ArchitectSwingSessionImpl.this);
    }

    /**
     * Protected method for setting the user prompter factory delegate to
     * something other than the UI user prompter factory. This allows
     * the tests to define a user prompter factory that does not block 
     * waiting for user response.
     */
    void setUserPrompterFactory(UserPrompterFactory newUPF) {
        swinguiUserPrompterFactory = newUPF;
    }

    public void refresh() {
        if (isEnterpriseSession()) {
            try {
                ArchitectSwingSession newSession = ((ArchitectSwingSessionContextImpl) getContext())
                    .createServerSession(((ArchitectClientSideSession) delegateSession).getProjectLocation(), false);
                
                frame.addSession(newSession);

                JLabel messageLabel = new JLabel("Refreshing");
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                
                final JDialog dialog = new JDialog(frame, "Refreshing");
                DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref"));
                builder.setDefaultDialogBorder();
                builder.append(messageLabel, 3);
                builder.nextLine();
                builder.append(progressBar, 3);
                dialog.add(builder.getPanel());
                
                dialog.pack();
                dialog.setLocation(frame.getX() + (frame.getWidth() - dialog.getWidth())/2, 
                                   frame.getY() + (frame.getHeight() - dialog.getHeight())/2);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                
                close();

                ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) newSession).getDelegateSession())
                    .getUpdater().addListener(new NetworkConflictResolver.UpdateListener() {
                    public boolean updatePerformed(NetworkConflictResolver updater) {
                       dialog.dispose();
                       return true; // true indicates that the listener should be removed
                    }
                    
                    public boolean updateException(NetworkConflictResolver resolver, Throwable t) {return false;}

                    public void preUpdatePerformed(NetworkConflictResolver resolver) {
                        //do nothing
                    }
                    
                    public void workspaceDeleted() {
                        // do nothing
                    }
                });
                
                ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) newSession).getDelegateSession()).startUpdaterThread();
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to refresh", e);
            }
        } 
    }
    
    public ArchitectSession getDelegateSession() {
        return delegateSession;
    }

    public List<UserDefinedSQLType> getSQLTypes() {
        return delegateSession.getSQLTypes();
    }
    
    public UserDefinedSQLType findSQLTypeByUUID(String uuid) {
        return delegateSession.findSQLTypeByUUID(uuid);
    }
    
    public UserDefinedSQLType findSQLTypeByJDBCType(int type) {
        return delegateSession.findSQLTypeByJDBCType(type);
    }

    public <T> UserPrompter createListUserPrompter(String question, List<T> responses, T defaultResponse) {
        return swinguiUserPrompterFactory.createListUserPrompter(question, responses, defaultResponse);
    }

    public List<UserDefinedSQLType> getDomains() {
        return delegateSession.getDomains();
    }

    public JScrollPane getPlayPenScrollPane() {
        return playPenScrollPane;
    }
    
    public void setPlayPenScrollPane(JScrollPane ppScrollPane) {
        playPenScrollPane = ppScrollPane;
    }

    public JComponent getProjectPanel() {
        return projectPanel;
    }
    
    public void setProjectPanel(JComponent panel) {
        projectPanel = panel;
    }

    @Override
    public ArchitectStatusBar getStatusInformation() {
        if (frame == null || frame.getStatusBar() == null) return null;
        return frame.getStatusBar();
    }

    @Override
    public Runnable createUpdateSnapshotRunnable(SPObjectSnapshot<?> snapshot) {
        return delegateSession.createUpdateSnapshotRunnable(snapshot);
    }
    
}
