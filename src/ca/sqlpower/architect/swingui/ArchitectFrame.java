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
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.action.CloseProjectAction;
import ca.sqlpower.architect.swingui.action.CompareDMAction;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.swingui.action.CreateTableAction;
import ca.sqlpower.architect.swingui.action.DataMoverAction;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
import ca.sqlpower.architect.swingui.action.DeleteSelectedAction;
import ca.sqlpower.architect.swingui.action.EditColumnAction;
import ca.sqlpower.architect.swingui.action.EditIndexAction;
import ca.sqlpower.architect.swingui.action.EditRelationshipAction;
import ca.sqlpower.architect.swingui.action.EditTableAction;
import ca.sqlpower.architect.swingui.action.ExportCSVAction;
import ca.sqlpower.architect.swingui.action.ExportDDLAction;
import ca.sqlpower.architect.swingui.action.ExportPlaypenToPDFAction;
import ca.sqlpower.architect.swingui.action.HelpAction;
import ca.sqlpower.architect.swingui.action.InsertColumnAction;
import ca.sqlpower.architect.swingui.action.InsertIndexAction;
import ca.sqlpower.architect.swingui.action.KettleJobAction;
import ca.sqlpower.architect.swingui.action.OpenProjectAction;
import ca.sqlpower.architect.swingui.action.PreferencesAction;
import ca.sqlpower.architect.swingui.action.PrintAction;
import ca.sqlpower.architect.swingui.action.ProfileAction;
import ca.sqlpower.architect.swingui.action.ProjectSettingsAction;
import ca.sqlpower.architect.swingui.action.RedoAction;
import ca.sqlpower.architect.swingui.action.SQLRunnerAction;
import ca.sqlpower.architect.swingui.action.SearchReplaceAction;
import ca.sqlpower.architect.swingui.action.SelectAllAction;
import ca.sqlpower.architect.swingui.action.UndoAction;
import ca.sqlpower.architect.swingui.action.VisualMappingReportAction;
import ca.sqlpower.architect.swingui.action.ZoomAction;
import ca.sqlpower.architect.swingui.action.ZoomAllAction;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.swingui.SPSUtils;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class ArchitectFrame extends JFrame {

	private static Logger logger = Logger.getLogger(ArchitectFrame.class);

	public static final double ZOOM_STEP = 0.25;

	private ArchitectSwingSession session = null;
	private JToolBar projectBar = null;
	private JToolBar ppBar = null;
	private JMenuBar menuBar = null;
	JSplitPane splitPane = null;
	private PlayPen playpen = null;
	DBTree dbTree = null;
	private CompareDMDialog comapareDMDialog = null;

    private JMenu connectionsMenu;
	private ArchitectLayout autoLayout;
    private UndoAction undoAction;
    private RedoAction redoAction;
    
    private AboutAction aboutAction;
    private Action newProjectAction;
    private OpenProjectAction openProjectAction;
    private Action saveProjectAction;
    private Action saveProjectAsAction;
    private CloseProjectAction closeProjectAction;
    private PreferencesAction prefAction;
    private ProjectSettingsAction projectSettingsAction;
    private PrintAction printAction;
    private ExportPlaypenToPDFAction exportPlaypenToPDFAction;
    private ProfileAction profileAction;
    private ZoomAction zoomInAction;
    private ZoomAction zoomOutAction;
    private Action zoomNormalAction;
    private Action zoomAllAction;
    private AutoLayoutAction autoLayoutAction;
    
    private EditColumnAction editColumnAction;
    private InsertColumnAction insertColumnAction;
    private InsertIndexAction insertIndexAction;
    private EditTableAction editTableAction;
    private EditIndexAction editIndexAction;
    private DeleteSelectedAction deleteSelectedAction;
    private CreateTableAction createTableAction;
    private CreateRelationshipAction createIdentifyingRelationshipAction;
    private CreateRelationshipAction createNonIdentifyingRelationshipAction;
    private EditRelationshipAction editRelationshipAction;
    private SearchReplaceAction searchReplaceAction;
    private SelectAllAction selectAllAction;

    private Action exportDDLAction;
    private Action compareDMAction;
    private Action dataMoverAction;

    /**
     * Closes all sessions and terminates the JVM.
     */
    private Action exitAction = new AbstractAction("Exit") {
        public void actionPerformed(ActionEvent e) {
            session.getContext().closeAll();
        }
    };

    /**
	 * This constructor is used by the session implementation.  To obtain an Architect
     * Frame, you have to create an {@link ArchitectSwingSessionContext} and then call
     * its createSession() method to obtain a Swing session.
	 *
	 * @throws ArchitectException
	 */
	ArchitectFrame(ArchitectSwingSession architectSession, SwingUIProject project) throws ArchitectException {

        session = architectSession;
        ArchitectSwingSessionContext context = session.getContext();
        
        setTitle(session.getName()+" - Power*Architect");
        setIconImage(ASUtils.getFrameIconImage());
	    
        // close is handled by a window listener
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        playpen = session.getPlayPen();
        dbTree = session.getSourceDatabases();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(dbTree));
        splitPane.setRightComponent(new JScrollPane(playpen));

        Preferences prefs = context.getPrefs();
        
        splitPane.setDividerLocation(prefs.getInt(ArchitectSwingUserSettings.DIVIDER_LOCATION,200));

        // Get the size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
        Rectangle bounds = new Rectangle();
        bounds.x = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_X, 40);
        bounds.y = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_Y, 40);
        bounds.width = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_WIDTH, (int) (dim.width * 0.8));
        bounds.height = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_HEIGHT, (int) (dim.height * 0.8));

        setBounds(bounds);
        addWindowListener(new ArchitectFrameWindowListener());
        session.getUserSettings().getSwingSettings().setBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                prefs.getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));
	}

    /**
     * A separate initialization method for setting up the actions in the ArchitectFrame.
     * To be called after the constructor is finished.
     * This method was created because several of the actions require a reference to this ArchitectFrame instance,
     * and we don't want to be passing a reference to the ArchitectFrame while it's still being constructed.
     * 
     * @param context
     * @param sprefs
     * @param accelMask
     * @throws ArchitectException
     */
    void init() throws ArchitectException {
        UserSettings sprefs = session.getUserSettings().getSwingSettings();
        int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        ArchitectSwingSessionContext context = session.getContext();
        
        // Create actions

        aboutAction = new AboutAction(session);
        
        Action helpAction = new HelpAction(session);
        helpAction.putValue(AbstractAction.SHORT_DESCRIPTION, "User Guide");

        newProjectAction = new AbstractAction("New Project",
                SPSUtils.createIcon("new_project","New Project",sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                try {
                    createNewProject();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ArchitectFrame.this,
                            "Can't create new project: "+ex.getMessage());
                    logger.error("Got exception while creating new project", ex);
                }
            }
        };
        newProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, "New");
        newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask));


        openProjectAction = new OpenProjectAction(session);

        saveProjectAction = new AbstractAction("Save Project",
                SPSUtils.createIcon("disk",
                        "Save Project",
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                session.saveOrSaveAs(false, true);
            }
        };
        saveProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Save");
        saveProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));

        saveProjectAsAction = new AbstractAction("Save Project As...",
                SPSUtils.createIcon("save_as",
                        "Save Project As...",
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                session.saveOrSaveAs(true, true);
            }
        };
        saveProjectAsAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Save As");
        
        closeProjectAction = new CloseProjectAction(session);

        prefAction = new PreferencesAction(session);
        projectSettingsAction = new ProjectSettingsAction(session);
        printAction = new PrintAction(session);
        printAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask));

        exportPlaypenToPDFAction = new ExportPlaypenToPDFAction(session);

        zoomInAction = new ZoomAction(session, ZOOM_STEP);
        zoomOutAction = new ZoomAction(session, ZOOM_STEP * -1.0);

        zoomNormalAction
        = new AbstractAction("Reset Zoom",
                SPSUtils.createIcon("zoom_reset",
                        "Reset Zoom",
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                playpen.setZoom(1.0);
            }
        };
        zoomNormalAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Reset Zoom");


        zoomAllAction = new ZoomAllAction(session);
        zoomAllAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom to fit");

        undoAction = new UndoAction(session, session.getUndoManager());
        redoAction = new RedoAction(session, session.getUndoManager());
        autoLayoutAction = new AutoLayoutAction(session, "Auto Layout", "Automatic Layout", "auto_layout");
        autoLayout = new FruchtermanReingoldForceLayout();
        autoLayoutAction.setLayout(autoLayout);
        exportDDLAction = new ExportDDLAction(session);
        comapareDMDialog = new CompareDMDialog(this);
        
        compareDMAction = new CompareDMAction(session,comapareDMDialog);
        dataMoverAction = new DataMoverAction(this, session);
        Action exportCSVAction = new ExportCSVAction(this, playpen);
        Action mappingReportAction = new VisualMappingReportAction(this, session);

        Action kettleETL = new KettleJobAction(session);
        deleteSelectedAction = new DeleteSelectedAction(session);
        createIdentifyingRelationshipAction = new CreateRelationshipAction(session, true);
        createNonIdentifyingRelationshipAction = new CreateRelationshipAction(session, false);
        editRelationshipAction = new EditRelationshipAction(session);
        createTableAction = new CreateTableAction(session);
        editColumnAction = new EditColumnAction(session);
        insertColumnAction = new InsertColumnAction(session);
        insertIndexAction = new InsertIndexAction(session);
        editTableAction = new EditTableAction(session);
        editIndexAction = new EditIndexAction(session);
        searchReplaceAction = new SearchReplaceAction(session);
        searchReplaceAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, accelMask));
        selectAllAction = new SelectAllAction(session);
        selectAllAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask));

        profileAction = new ProfileAction(session, session.getProfileManager());

        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        fileMenu.add(newProjectAction);
        fileMenu.add(openProjectAction);
        fileMenu.add(session.getRecentMenu());
        fileMenu.add(closeProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        fileMenu.add(printAction);
        fileMenu.add(exportPlaypenToPDFAction);
        fileMenu.addSeparator();
        if (!context.isMacOSX()) {
            fileMenu.add(prefAction);
        }
        fileMenu.add(projectSettingsAction);
        if (!context.isMacOSX()) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.addSeparator();
        editMenu.add(selectAllAction);
        editMenu.addSeparator();
        editMenu.add(searchReplaceAction);
        menuBar.add(editMenu);

        // the connections menu is set up when a new project is created (because it depends on the current DBTree)
        connectionsMenu = new JMenu("Connections");
        connectionsMenu.setMnemonic('c');
        menuBar.add(connectionsMenu);
        connectionsMenu.removeAll();
        dbTree.refreshMenu(null);
        connectionsMenu.add(dbTree.connectionsMenu);
        connectionsMenu.add(new JMenuItem(dbTree.dbcsPropertiesAction));
        connectionsMenu.add(new JMenuItem(dbTree.removeDBCSAction));
        connectionsMenu.addSeparator();
        connectionsMenu.add(new DatabaseConnectionManagerAction(session));
        
        JMenu etlMenu = new JMenu("ETL");
        etlMenu.setMnemonic('l');
        etlMenu.add(exportCSVAction);
        etlMenu.add(mappingReportAction);
        etlMenu.add(kettleETL);
        menuBar.add(etlMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('t');
        toolsMenu.add(exportDDLAction);
        toolsMenu.add(compareDMAction);
        toolsMenu.add(new SQLRunnerAction(session));

        toolsMenu.add(dataMoverAction);

        menuBar.add(toolsMenu);

        JMenu profileMenu = new JMenu("Profile");
        profileMenu.setMnemonic('p');
        profileMenu.add(profileAction);

        menuBar.add(profileMenu);

        JMenu windowMenu = new JMenu("Window");
        windowMenu.add(new DatabaseConnectionManagerAction(session));
        windowMenu.add(new AbstractAction("Profile Manager") {
            public void actionPerformed(ActionEvent e) {
                session.getProfileDialog().setVisible(true);
            }
        });
        
        menuBar.add(windowMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        if (!context.isMacOSX()) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
        helpMenu.add(SPSUtils.forumAction);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        projectBar = new JToolBar(JToolBar.HORIZONTAL);
        ppBar = new JToolBar(JToolBar.VERTICAL);

        projectBar.add(newProjectAction);
        projectBar.add(openProjectAction);
        projectBar.add(saveProjectAction);
        projectBar.addSeparator();
        projectBar.add(printAction);
        projectBar.addSeparator();
        projectBar.add(undoAction);
        projectBar.add(redoAction);
        projectBar.addSeparator();
        projectBar.add(exportDDLAction);
        projectBar.add(compareDMAction);
        projectBar.addSeparator();
        projectBar.add(autoLayoutAction);
        projectBar.add(profileAction);
        projectBar.addSeparator();
        projectBar.add(helpAction);
        projectBar.setToolTipText("Project Toolbar");
        projectBar.setName("Project Toolbar");


        JButton tempButton = null; // shared actions need to report where they are coming from
        ppBar.setToolTipText("PlayPen Toolbar");
        ppBar.setName("PlayPen ToolBar");
        ppBar.add(zoomInAction);
        ppBar.add(zoomOutAction);
        ppBar.add(zoomNormalAction);
        ppBar.add(zoomAllAction);
        ppBar.addSeparator();
        tempButton = ppBar.add(deleteSelectedAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton = ppBar.add(createTableAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton  = ppBar.add(insertIndexAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton = ppBar.add(insertColumnAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        tempButton = ppBar.add(editColumnAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        ppBar.add(createNonIdentifyingRelationshipAction);
        ppBar.add(createIdentifyingRelationshipAction);
        tempButton = ppBar.add(editRelationshipAction);
        tempButton.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);


        Container projectBarPane = getContentPane();
        projectBarPane.setLayout(new BorderLayout());
        projectBarPane.add(projectBar, BorderLayout.NORTH);

        JPanel cp = new JPanel(new BorderLayout());
        cp.add(ppBar, BorderLayout.EAST);
        projectBarPane.add(cp, BorderLayout.CENTER);

        cp.add(splitPane, BorderLayout.CENTER);
        logger.debug("Added splitpane to content pane");
    }
    
    /**
     * Creates a new project in the same session context as this one, 
     * and opens it in a new ArchitectFrame instance.
     */
    private void createNewProject() throws ArchitectException {
        session.getContext().createSession();
    }

	public SwingUIProject getProject() {
		return session.getProject();
	}

	/**
     * @deprecated This method should no longer be used (references to a session should be
     * passed around in preference to references to an ArchitectFrame).
     */
	public ArchitectSwingSession getArchitectSession() {
		return session;
	}

	/**
	 * Determine if either create relationship action is currently active.
	 */
	public boolean createRelationshipIsActive () {
		if (createIdentifyingRelationshipAction.isActive()) return true;
		if (createNonIdentifyingRelationshipAction.isActive()) return true;
		return false;
	}

	class ArchitectFrameWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			session.close();
		}
	}

    /**
     * Saves this frame's settings as user prefs.  Settings are frame location,
     * divider location, that kind of stuff.
     */
	public void saveSettings() throws ArchitectException {

	    CoreUserSettings us = session.getUserSettings();

        /* These are saved directly in java.util.Preferences.
         * XXX Eventually we should save almost everything there, except
         * the PL.INI contents that must be shared with other non-Java programs.
         */
	    Preferences prefs = session.getContext().getPrefs();
		prefs.putInt(ArchitectSwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
		prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_X, getLocation().x);
		prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_Y, getLocation().y);
		prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_WIDTH, getWidth());
		prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_HEIGHT, getHeight());
        prefs.putBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                us.getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));

		us.write();

		try {
            us.getPlDotIni().write(new File(us.getPlDotIniPath()));
        } catch (IOException e) {
            logger.error("Couldn't save PL.INI file!", e);
        }
	}

	/**
	 * Creates an ArchitectFrame and sets it visible.  This method is
	 * an acceptable way to launch the Architect application.
	 */
	public static void main(final String args[]) throws ArchitectException {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
                try {
                    
                    // this is one of only two valid uses of the getContext() method.
                    // don't take it as encouragement to call it in other places!
                    // unless you like being attacked by screaming monkeys, that is.
                    ArchitectSwingSessionContext context = ASUtils.getContext();
                    
                    final File openFile;
                    if (args.length > 0) {
                        openFile = new File(args[0]);
                    } else {
                        openFile = null;
                    }

                    if (openFile != null) {
                        InputStream in = new BufferedInputStream(new FileInputStream(openFile));
                        ArchitectSwingSession session = context.createSession(in, true);
                        session.getRecentMenu().putRecentFileName(openFile.getAbsolutePath());
                        session.getProject().setFile(openFile);
                    } else {
                        context.createSession();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //We wish we had a parent component to direct the dialog to
                    //instead of passing a null, but this is being invoked, so 
                    //everything else blew up.
                    ASUtils.showExceptionDialog(null, "Could not launch the Power*Architect", e);
                }
		    }
		});
	}

	public AutoLayoutAction getAutoLayoutAction() {
		return autoLayoutAction;
	}

	public JToolBar getProjectToolBar() {
		return projectBar;
	}

	public JToolBar getPlayPenToolBar() {
		return ppBar;
	}

	public UndoManager getUndoManager() {
		return session.getUndoManager();
	}

	public Action getNewProjectAction() {
		return newProjectAction;
	}

	public void setNewProjectAction(Action newProjectAction) {
		this.newProjectAction = newProjectAction;
	}

	public ZoomAction getZoomInAction() {
		return zoomInAction;
	}

	public ZoomAction getZoomOutAction() {
		return zoomOutAction;
	}

    public PlayPen getPlayPen() {
        return playpen;
    }

    public DBTree getDbTree() {
        return dbTree;
    }

    public AboutAction getAboutAction() {
        return aboutAction;
    }

    public Action getExitAction() {
        return exitAction;
    }

    public PreferencesAction getPrefAction() {
        return prefAction;
    }

    public EditColumnAction getEditColumnAction() {
        return editColumnAction;
    }

    public InsertIndexAction getInsertIndexAction() {
        return insertIndexAction;
    }

    public void setPrefAction(PreferencesAction prefAction) {
        this.prefAction = prefAction;
    }

    public EditTableAction getEditTableAction() {
        return editTableAction;
    }

    public InsertColumnAction getInsertColumnAction() {
        return insertColumnAction;
    }

    public EditIndexAction getEditIndexAction() {
        return editIndexAction;
    }

    public EditRelationshipAction getEditRelationshipAction() {
        return editRelationshipAction;
    }

    public DeleteSelectedAction getDeleteSelectedAction() {
        return deleteSelectedAction;
    }

    public CreateTableAction getCreateTableAction() {
        return createTableAction;
    }

    public CreateRelationshipAction getCreateIdentifyingRelationshipAction() {
        return createIdentifyingRelationshipAction;
    }

    public CreateRelationshipAction getCreateNonIdentifyingRelationshipAction() {
        return createNonIdentifyingRelationshipAction;
    }
    
    public CompareDMDialog getCompareDMDialog() {
        return comapareDMDialog;
    }
}