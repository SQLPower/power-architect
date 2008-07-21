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

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.CoreProject;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.UserPrompter;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.OpenProjectAction;
import ca.sqlpower.architect.swingui.action.PreferencesAction;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

public class ArchitectSwingSessionImpl implements ArchitectSwingSession {

    private static final Logger logger = Logger.getLogger(ArchitectSwingSessionImpl.class);

    private final ArchitectSwingSessionContext context;

    /**
     * This is the core session that some tasks are delegated to.
     */
    private final ArchitectSession delegateSession;

    /**
     * The Frame where the main part of the GUI for this session appears.
     */
    private ArchitectFrame frame;

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

    private PlayPen playPen;

    /** the small dialog that lists the profiles */
    private ProfileManagerView profileManagerView;

    private CompareDMSettings compareDMSettings;

    private UndoManager undoManager;

    private boolean savingEntireSource;

    private boolean isNew;

    private DBTree sourceDatabases;

    private KettleJob kettleJob;
    // END STUFF BROUGHT IN FROM SwingUIProject

    private List<SessionLifecycleListener<ArchitectSwingSession>> lifecycleListener;

    private Set<SPSwingWorker> swingWorkers;

    private ProjectModificationWatcher projectModificationWatcher;

    private boolean relationshipLinesDirect;

    private boolean showPkTag = true;
    private boolean showFkTag = true;
    private boolean showAkTag = true;

    protected boolean showPrimary = true;
    protected boolean showForeign = true;
    protected boolean showIndexed = true;
    protected boolean showUnique = true;
    protected boolean showTheRest = true;

    /**
     * Creates a new swing session, including a new visible architect frame, with
     * the given parent context and the given name.
     * 
     * @param context
     * @param name
     * @throws ArchitectException
     */
    ArchitectSwingSessionImpl(final ArchitectSwingSessionContext context, String name)
    throws ArchitectException {
        this.isNew = true;
        this.context = context;
        this.delegateSession = new ArchitectSessionImpl(context, name);
        ((ArchitectSessionImpl)delegateSession).setProfileManager(new ProfileManagerImpl(this));
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                File f = new File(fileName);
                try {
                    OpenProjectAction.openAsynchronously(getContext().createSession(false), f, ArchitectSwingSessionImpl.this);
                } catch (ArchitectException ex) {
                    SPSUtils.showExceptionDialogNoReport(getArchitectFrame(), Messages.getString("ArchitectSwingSessionImpl.openProjectFileFailed"), ex); //$NON-NLS-1$
                }
            }
        };

        // Make sure we can load the pl.ini file so we can handle exceptions
        // XXX this is probably redundant now, since the context owns the pl.ini
        getContext().getPlDotIni();

        setProject(new SwingUIProject(this));

        compareDMSettings = new CompareDMSettings();

        kettleJob = new KettleJob(this);

        playPen = new PlayPen(this);
        UserSettings sprefs = getUserSettings().getSwingSettings();
        if (sprefs != null) {
            playPen.setRenderingAntialiased(sprefs.getBoolean(ArchitectSwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));
        }
        projectModificationWatcher = new ProjectModificationWatcher(playPen);

        delegateSession.getRootObject().addChild(getTargetDatabase());
        this.sourceDatabases = new DBTree(this);

        undoManager = new UndoManager(playPen);
        playPen.getPlayPenContentPane().addPropertyChangeListener("location", undoManager.getEventAdapter());
        playPen.getPlayPenContentPane().addPropertyChangeListener("connectionPoints", undoManager.getEventAdapter());
        playPen.getPlayPenContentPane().addPropertyChangeListener("backgroundColor", undoManager.getEventAdapter());
        playPen.getPlayPenContentPane().addPropertyChangeListener("foregroundColor", undoManager.getEventAdapter());
        playPen.getPlayPenContentPane().addPropertyChangeListener("dashed", undoManager.getEventAdapter());
        playPen.getPlayPenContentPane().addPropertyChangeListener("rounded", undoManager.getEventAdapter());

        lifecycleListener = new ArrayList<SessionLifecycleListener<ArchitectSwingSession>>();

        swingWorkers = new HashSet<SPSwingWorker>();
    }

    public void initGUI() throws ArchitectException {
        initGUI(null);
    }

    public void initGUI(ArchitectSwingSession openingSession) throws ArchitectException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called on the Swing Event Dispatch Thread."); //$NON-NLS-1$
        }

        // makes the tool tips show up on these components 
        ToolTipManager.sharedInstance().registerComponent(playPen);
        ToolTipManager.sharedInstance().registerComponent(sourceDatabases);

        if (openingSession != null) {
            Rectangle bounds = openingSession.getArchitectFrame().getBounds();
            if (!openingSession.isNew()) {
                bounds.x += 20;
                bounds.y += 20;
            }
            frame = new ArchitectFrame(this, bounds);
        } else {
            frame = new ArchitectFrame(this, null);
        }

        // MUST be called after constructed to set up the actions
        frame.init(); 
        frame.setVisible(true);

        if (openingSession != null && openingSession.isNew()) {
            openingSession.close();
        }

        profileDialog = new JDialog(frame, Messages.getString("ArchitectSwingSessionImpl.profilesDialogTitle")); //$NON-NLS-1$
        profileManagerView = new ProfileManagerView(delegateSession.getProfileManager());
        delegateSession.getProfileManager().addProfileChangeListener(profileManagerView);
        profileDialog.add(profileManagerView);


        // This has to be called after frame.init() because playPen gets the keyboard actions from frame,
        // which only get set up after calling frame.init().
        playPen.setupKeyboardActions();
        sourceDatabases.setupKeyboardActions();

        macOSXRegistration(frame);

        profileDialog.setLocationRelativeTo(frame);
    }

    public SwingUIProject getProject() {
        return (SwingUIProject) delegateSession.getProject();
    }

    public void setProject(CoreProject project) {
        delegateSession.setProject(project);
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

        // Whether or not this is OS X, the three actions we're referencing must have been initialized by now.
        if (exitAction == null) throw new IllegalStateException("Exit action has not been initialized"); //$NON-NLS-1$
        if (prefAction == null) throw new IllegalStateException("Prefs action has not been initialized"); //$NON-NLS-1$
        if (aboutAction == null) throw new IllegalStateException("About action has not been initialized"); //$NON-NLS-1$

        if (context.isMacOSX()) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter"); //$NON-NLS-1$

                // The main registration method.  Takes quitAction, prefsAction, aboutAction.
                Class[] defArgs = { Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs); //$NON-NLS-1$
                Object[] args = { exitAction, prefAction, aboutAction };
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
        if (getProject().isModified()) {
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
        SwingUIProject project = getProject();

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
                String projName = file.getName().substring(0, file.getName().length()-".architect".length()); //$NON-NLS-1$
                setName(projName);
                frame.setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", projName)); //$NON-NLS-1$
            }
        }
        final boolean finalSeparateThread = separateThread;
        final ProgressMonitor pm = new ProgressMonitor
        (frame, Messages.getString("ArchitectSwingSessionImpl.saveProgressDialogTitle"), "", 0, 100); //$NON-NLS-1$ //$NON-NLS-2$

        class SaverTask implements Runnable {
            boolean success;

            public void run() {
                SwingUIProject project = getProject();
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
            new Thread(saveTask).start();
            return true; // this is an optimistic lie
        } else {
            saveTask.run();
            return saveTask.success;
        }
    }

    // STUFF BROUGHT IN FROM SwingUIProject

    /**
     * This is a common handler for all actions that must occur when switching
     * projects, e.g., prompting to save any unsaved changes, disposing dialogs,
     * shutting down running threads, and so on.
     */
    public void close() {

        // IMPORTANT NOTE: If the GUI hasn't been initialized, frame will be null.

        if (getProject().isSaveInProgress()) {
            // project save is in progress, don't allow exit
            JOptionPane.showMessageDialog(frame,
                    Messages.getString("ArchitectSwingSessionImpl.cannotExitWhileSaving"), //$NON-NLS-1$
                    Messages.getString("ArchitectSwingSessionImpl.cannotExitWhileSavingDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
            return;
        }

        if (!promptForUnsavedModifications()) {
            return;
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
                return;
            } else {
                for (SPSwingWorker currentWorker : swingWorkers) {
                    currentWorker.kill();
                }
            }
        }

        try {
            if (frame != null) {
                // XXX this could/should be done by the frame with a session closing listener
                frame.saveSettings();
            }
        } catch (ArchitectException e) {
            logger.error("Couldn't save settings: "+e); //$NON-NLS-1$
        }

        if (profileDialog != null) {
            // XXX this could/should be done by the profile dialog with a session closing listener
            profileDialog.dispose();
        }

        // It is possible this method will be called again via indirect recursion
        // because the frame has a windowClosing listener that calls session.close().
        // It should be harmless to have this close() method invoked a second time.
        if (frame != null) {
            // XXX this could/should be done by the frame with a session closing listener
            frame.dispose();
        }

        // close connections
        try {
            for (SQLDatabase db : (List<SQLDatabase>) getRootObject().getChildren()) {
                logger.debug ("closing connection: " + db.getName()); //$NON-NLS-1$
                db.disconnect();
            }
        } catch (ArchitectException ex) {
            throw new AssertionError("Got impossible ArchitectException from root object"); //$NON-NLS-1$
        }

        // Clear the profile manager (the effect we want is just to cancel running profiles.. clearing is a harmless side effect)
        // XXX this could/should be done by the profile manager with a session closing listener
        delegateSession.getProfileManager().clear();

        fireSessionClosing();
    }

    /**
     * Gets the value of sourceDatabases
     *
     * @return the value of sourceDatabases
     */
    public DBTree getSourceDatabases()  {
        return this.sourceDatabases;
    }

    /**
     * Sets the value of sourceDatabases
     *
     * @param argSourceDatabases Value to assign to this.sourceDatabases
     */
    public void setSourceDatabases(DBTree argSourceDatabases) {
        this.sourceDatabases = argSourceDatabases;
    }

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws ArchitectException {
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
    class ProjectModificationWatcher implements SQLObjectListener, PropertyChangeListener {

        /**
         * Sets up a new modification watcher on the given playpen.
         */
        public ProjectModificationWatcher(PlayPen pp) {
            try {
                ArchitectUtils.listenToHierarchy(this, getTargetDatabase());
            } catch (ArchitectException e) {
                logger.error("Can't listen to business model for changes", e); //$NON-NLS-1$
            }
            PlayPenContentPane ppcp = pp.contentPane;
            ppcp.addPropertyChangeListener(this);
        }

        /** Marks project dirty, and starts listening to new kids. */
        public void dbChildrenInserted(SQLObjectEvent e) {
            getProject().setModified(true);
            SQLObject[] newKids = e.getChildren();
            for (int i = 0; i < newKids.length; i++) {
                try {
                    ArchitectUtils.listenToHierarchy(this, newKids[i]);
                } catch (ArchitectException e1) {
                    logger.error("Couldn't listen to SQLObject hierarchy rooted at "+newKids[i], e1); //$NON-NLS-1$
                }
            }
            isNew = false;
        }

        /** Marks project dirty, and stops listening to removed kids. */
        public void dbChildrenRemoved(SQLObjectEvent e) {
            getProject().setModified(true);
            SQLObject[] oldKids = e.getChildren();
            for (int i = 0; i < oldKids.length; i++) {
                oldKids[i].removeSQLObjectListener(this);
            }
            isNew = false;
        }

        /** Marks project dirty. */
        public void dbObjectChanged(SQLObjectEvent e) {
            getProject().setModified(true);
            isNew = false;
        }

        /** Marks project dirty and listens to new hierarchy. */
        public void dbStructureChanged(SQLObjectEvent e) {
            try {
                ArchitectUtils.listenToHierarchy(this, e.getSQLSource());
            } catch (ArchitectException e1) {
                logger.error("dbStructureChanged listener: Failed to listen to new project hierarchy", e1); //$NON-NLS-1$
            }
            isNew = false;
        }

        /**
         * Marks the project dirty when property change event fires.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            getProject().setModified(true);
            isNew = false;

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

    public CompareDMSettings getCompareDMSettings() {
        return compareDMSettings;
    }
    public void setCompareDMSettings(CompareDMSettings compareDMSettings) {
        this.compareDMSettings = compareDMSettings;
    }

    public UndoManager getUndoManager() {
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

    /**
     * See {@link #savingEntireSource}.
     *
     * @return the value of savingEntireSource
     */
    public boolean isSavingEntireSource()  {
        return this.savingEntireSource;
    }

    /**
     * See {@link #savingEntireSource}.
     *
     * @param argSavingEntireSource Value to assign to this.savingEntireSource
     */
    public void setSavingEntireSource(boolean argSavingEntireSource) {
        this.savingEntireSource = argSavingEntireSource;
    }

    public KettleJob getKettleJob() {
        return kettleJob;
    }

    public void setKettleJob(KettleJob kettleJob) {
        this.kettleJob = kettleJob;
    }
    // END STUFF BROUGHT IN FROM SwingUIProject

    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSwingSession> listener) {
        lifecycleListener.add(listener);
    }

    public void fireSessionClosing() {
        SessionLifecycleEvent<ArchitectSwingSession> evt = new SessionLifecycleEvent<ArchitectSwingSession>(this);
        for (SessionLifecycleListener<ArchitectSwingSession> listener: lifecycleListener) {
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

    public boolean isRelationshipLinesDirect() {
        return relationshipLinesDirect;
    }

    public void setRelationshipLinesDirect(boolean relationshipLinesDirect) {
        this.relationshipLinesDirect = relationshipLinesDirect;
        getPlayPen().repaint();
    }

    public boolean getRelationshipLinesDirect() {
        return relationshipLinesDirect;
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

    /**
     * Creates a new user prompter that uses a modal dialog to pose the given question.
     * 
     * @see ModalDialogUserPrompter
     */
    public UserPrompter createUserPrompter(String question, String okText, String notOkText, String cancelText) {
        return new ModalDialogUserPrompter(frame, question, okText, notOkText, cancelText);
    }

    public boolean isShowPkTag() {
        return showPkTag;
    }

    public void setShowPkTag(boolean showPkTag) {
        this.showPkTag = showPkTag;
        for (TablePane tp : getPlayPen().getTablePanes()) {
            tp.revalidate();
        }
    }

    public boolean isShowFkTag() {
        return showFkTag;
    }

    public void setShowFkTag(boolean showFkTag) {
        this.showFkTag = showFkTag;
        for (TablePane tp : getPlayPen().getTablePanes()) {
            tp.revalidate();
        }
    }

    public boolean isShowAkTag() {
        return showAkTag;
    }

    public void setShowAkTag(boolean showAkTag) {
        this.showAkTag = showAkTag;
        for (TablePane tp : getPlayPen().getTablePanes()) {
            tp.revalidate();
        }
    }

    public boolean isShowPrimary() {
        return showPrimary;
    }

    public void setShowPrimary(boolean showPrimary) {
        this.showPrimary = showPrimary;
    }

    public boolean isShowForeign() {
        return showForeign;
    }

    public void setShowForeign(boolean showForeign) {
        this.showForeign = showForeign;
    }

    public boolean isShowIndexed() {
        return showIndexed;
    }

    public void setShowIndexed(boolean showIndexed) {
        this.showIndexed = showIndexed;
    }

    public boolean isShowUnique() {
        return showUnique;
    }

    public void setShowUnique(boolean showUnique) {
        this.showUnique = showUnique;
    }

    public boolean isShowTheRest() {
        return showTheRest;
    }

    public void setShowTheRest(boolean showTheRest) {
        this.showTheRest = showTheRest;
    }

}
