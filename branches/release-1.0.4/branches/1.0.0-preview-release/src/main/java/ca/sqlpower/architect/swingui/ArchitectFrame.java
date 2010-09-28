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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Types;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
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
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.ProjectLocation;
import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.AlignTableAction;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.action.CheckForUpdateAction;
import ca.sqlpower.architect.swingui.action.CloseProjectAction;
import ca.sqlpower.architect.swingui.action.CompareDMAction;
import ca.sqlpower.architect.swingui.action.CopySelectedAction;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.swingui.action.CreateTableAction;
import ca.sqlpower.architect.swingui.action.CutSelectedAction;
import ca.sqlpower.architect.swingui.action.DataMoverAction;
import ca.sqlpower.architect.swingui.action.DataSourcePropertiesAction;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
import ca.sqlpower.architect.swingui.action.DeleteSelectedAction;
import ca.sqlpower.architect.swingui.action.EditColumnAction;
import ca.sqlpower.architect.swingui.action.EditRelationshipAction;
import ca.sqlpower.architect.swingui.action.EditSelectedAction;
import ca.sqlpower.architect.swingui.action.EditSelectedIndexAction;
import ca.sqlpower.architect.swingui.action.EditSpecificIndexAction;
import ca.sqlpower.architect.swingui.action.EditTableAction;
import ca.sqlpower.architect.swingui.action.ExportCSVAction;
import ca.sqlpower.architect.swingui.action.ExportDDLAction;
import ca.sqlpower.architect.swingui.action.ExportHTMLReportAction;
import ca.sqlpower.architect.swingui.action.ExportPlaypenToPDFAction;
import ca.sqlpower.architect.swingui.action.FocusToChildOrParentTableAction;
import ca.sqlpower.architect.swingui.action.InsertColumnAction;
import ca.sqlpower.architect.swingui.action.InsertIndexAction;
import ca.sqlpower.architect.swingui.action.InvadersAction;
import ca.sqlpower.architect.swingui.action.KettleJobAction;
import ca.sqlpower.architect.swingui.action.OpenProjectAction;
import ca.sqlpower.architect.swingui.action.PasteSelectedAction;
import ca.sqlpower.architect.swingui.action.PreferencesAction;
import ca.sqlpower.architect.swingui.action.PrintAction;
import ca.sqlpower.architect.swingui.action.ProfileAction;
import ca.sqlpower.architect.swingui.action.ProjectSettingsAction;
import ca.sqlpower.architect.swingui.action.RedoAction;
import ca.sqlpower.architect.swingui.action.RemoveSourceDBAction;
import ca.sqlpower.architect.swingui.action.ReverseRelationshipAction;
import ca.sqlpower.architect.swingui.action.SQLQueryAction;
import ca.sqlpower.architect.swingui.action.SearchReplaceAction;
import ca.sqlpower.architect.swingui.action.SelectAllAction;
import ca.sqlpower.architect.swingui.action.UndoAction;
import ca.sqlpower.architect.swingui.action.VisualMappingReportAction;
import ca.sqlpower.architect.swingui.action.ZoomAction;
import ca.sqlpower.architect.swingui.action.ZoomResetAction;
import ca.sqlpower.architect.swingui.action.ZoomToFitAction;
import ca.sqlpower.architect.swingui.action.enterprise.RefreshProjectAction;
import ca.sqlpower.architect.swingui.enterprise.ProjectSecurityPanel;
import ca.sqlpower.architect.swingui.enterprise.RevisionListPanel;
import ca.sqlpower.architect.swingui.enterprise.SecurityPanel;
import ca.sqlpower.architect.swingui.enterprise.ServerProjectsManagerPanel;
import ca.sqlpower.architect.swingui.olap.action.ImportSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPEditAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPSchemaManagerAction;
import ca.sqlpower.enterprise.client.ConnectionTestAction;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.undo.NotifyingUndoManager;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.action.OpenUrlAction;
import ca.sqlpower.swingui.enterprise.client.SPServerInfoManagerPanel;

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
    private JScrollPane playpenScrollPane;
    DBTree dbTree = null;
    private Navigator navigatorDialog;
    private CompareDMDialog comapareDMDialog = null;
    private int oldWidth;
    private int oldHeight;
    private int prefWidth;
    private int prefHeight;

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
    private ZoomResetAction zoomNormalAction;
    private ZoomToFitAction zoomToFitAction;
    private AutoLayoutAction autoLayoutAction;
    
    private EditSelectedAction editSelectedAction;
    private EditColumnAction editColumnAction;
    private InsertColumnAction insertColumnAction;
    private InsertIndexAction insertIndexAction;
    private EditTableAction editTableAction;
    
    /**
     * Edits the index which is currently selected in the DBTree.
     * For PlayPen purposes, see {@link EditSpecificIndexAction}.
     */
    private EditSelectedIndexAction editIndexAction;
    
    private DeleteSelectedAction deleteSelectedAction;
    private CreateTableAction createTableAction;
    private CreateRelationshipAction createIdentifyingRelationshipAction;
    private CreateRelationshipAction createNonIdentifyingRelationshipAction;
    private EditRelationshipAction editRelationshipAction;
    private SearchReplaceAction searchReplaceAction;
    private SelectAllAction selectAllAction;
    private ReverseRelationshipAction reverseRelationshipAction;
    private AlignTableAction alignTableHorizontalAction;
    private AlignTableAction alignTableVerticalAction;
    private FocusToChildOrParentTableAction focusToChildAction;
    private FocusToChildOrParentTableAction focusToParentAction;
    
    private Action exportDDLAction;
    private Action compareDMAction;
    private Action dataMoverAction;
    private Action sqlQueryAction;
    private CopySelectedAction copyAction;
    private CutSelectedAction cutAction;
    private PasteSelectedAction pasteAction;
    
    private RefreshProjectAction refreshProjectAction;
    
    /**
     * Closes all sessions and terminates the JVM.
     */
    private Action exitAction = new AbstractAction(Messages.getString("ArchitectFrame.exitActionName")) { //$NON-NLS-1$
        public void actionPerformed(ActionEvent e) {
            session.getContext().closeAll();
        }
    };
    
    private Action openServerManagerAction = new AbstractAction("Configure Server Connections...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Server Connections");
            
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            final SPServerInfoManagerPanel sim = new SPServerInfoManagerPanel(session.getContext().getServerManager(),
                    ArchitectFrame.this, closeAction);
            sim.setLoginAction(new AbstractAction("Login") {
                public void actionPerformed(ActionEvent e) {
                    SPServerInfo si = sim.getSelectedServer();
                    if (si != null) {
                        
                        boolean accessible = true;
                        
                        if (accessible) {
                            final JDialog dialog = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Projects");
                            Action closeAction = new AbstractAction("Close") {
                                public void actionPerformed(ActionEvent e) {
                                    dialog.dispose();
                                }
                            };
                            
                            ServerProjectsManagerPanel spm = new ServerProjectsManagerPanel(si, session, session.getContext(),
                                    ArchitectFrame.this, closeAction);
                            if (spm.isConnected()) {
                                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                dialog.setContentPane(spm.getPanel());
                                
                                SPSUtils.makeJDialogCancellable(dialog, null);
                                dialog.pack();
                                dialog.setLocationRelativeTo(ArchitectFrame.this);
                                dialog.setVisible(true);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(ArchitectFrame.this, "Please select a server", "", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            
            sim.setTestAction(new ConnectionTestAction("Test Server") {
                public void actionPerformed(ActionEvent e) {
                    String msg = "Unable to connect to server";
                    try {
                        List<ProjectLocation> l = 
                            ArchitectClientSideSession.getWorkspaceNames(
                                findPanel((JButton) e.getSource()).getServerInfo(), session);
                        if (l != null) {
                            msg = "Successfully connected to server";
                        }
                    } catch (Exception ex) {
                    }
                    
                    JOptionPane.showMessageDialog(ArchitectFrame.this, msg);
                }
            });
            
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(sim.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openProjectManagerAction = new AbstractAction("Projects...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Projects");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            ServerProjectsManagerPanel spm = new ServerProjectsManagerPanel(session, session.getContext(),
                    ArchitectFrame.this, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openSecurityManagerPanelAction = new AbstractAction("Users & Groups...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Security Manager");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) session).getDelegateSession()).getSystemSession().getUpdater().setPromptSession(session);
            SecurityPanel spm = new SecurityPanel(((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) session).getDelegateSession()).getProjectLocation().getServiceInfo(), closeAction, d, session);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getSplitPane());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openProjectSecurityPanelAction = new AbstractAction("Project Settings...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Security Manager");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) session).getDelegateSession()).getSystemSession().getUpdater().setPromptSession(session);
            ProjectSecurityPanel spm = new ProjectSecurityPanel(((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) session).getDelegateSession()).getSystemWorkspace(), 
                    session.getWorkspace(), ArchitectProject.class, ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) session).getDelegateSession()).getProjectLocation().getServiceInfo().getUsername(), d, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openRevisionListAction = new AbstractAction("Revisions...") {                       
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Revision List");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            final RevisionListPanel p = new RevisionListPanel(session, ArchitectFrame.this, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(p.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
            
        }        
    };
    
    /**
     * This constructor is used by the session implementation. To obtain an
     * Architect Frame, you have to create an
     * {@link ArchitectSwingSessionContext} and then call its createSession()
     * method to obtain a Swing session.
     * 
     * @param architectSession
     *            The ArchitectSwingSession related to this frame.
     * @param bounds
     *            A Rectangle whose x and y properties will be used to determine
     *            the position of newly created ArchitectFrame
     * 
     * @throws SQLObjectException
     */
    ArchitectFrame(ArchitectSwingSession architectSession, Rectangle bounds) throws SQLObjectException {

        session = architectSession;
        ArchitectSwingSessionContext context = session.getContext();
        
        setTitle(session.getName()+" - SQL Power Architect"); //$NON-NLS-1$
        setIconImage(ASUtils.getFrameIconImage());
        
        // close is handled by a window listener
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        playpen = session.getPlayPen();
        dbTree = session.getSourceDatabases();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(SPSUtils.getBrandedTreePanel(dbTree)));
        playpenScrollPane = new JScrollPane(playpen);
        
        splitPane.setRightComponent(playpenScrollPane);
        playpen.setInitialViewPosition();

        final Preferences prefs = context.getPrefs();
        
        splitPane.setDividerLocation(prefs.getInt(ArchitectSwingUserSettings.DIVIDER_LOCATION,200));
        splitPane.setOneTouchExpandable(true);

        // Get the size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
        if (bounds == null) {
            bounds = new Rectangle();
            bounds.x = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_X, 40);
            bounds.y = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_Y, 40);
        } 
        
        bounds.width = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_WIDTH, (int) (dim.width * 0.8));
        bounds.height = prefs.getInt(ArchitectSwingUserSettings.MAIN_FRAME_HEIGHT, (int) (dim.height * 0.8));
        prefWidth = bounds.width;
        prefHeight = bounds.height;
        oldWidth = prefWidth;
        oldHeight = prefHeight;
        
        setBounds(bounds);
        setPreferredSize(new Dimension(bounds.width, bounds.height));
        addWindowListener(new ArchitectFrameWindowListener());
        session.getUserSettings().getSwingSettings().setBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                prefs.getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));
        
        SQLColumn.setDefaultName(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_NAME, "New Column"));
        SQLColumn.setDefaultType(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_TYPE, Types.INTEGER));
        SQLColumn.setDefaultPrec(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_PREC, 10));
        SQLColumn.setDefaultScale(prefs.getInt(DefaultColumnUserSettings.DEFAULT_COLUMN_SCALE, 0));
        SQLColumn.setDefaultInPK(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_INPK, false));
        SQLColumn.setDefaultNullable(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_NULLABLE, false));
        SQLColumn.setDefaultAutoInc(prefs.getBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_AUTOINC, false));
        SQLColumn.setDefaultRemarks(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_REMARKS, ""));
        SQLColumn.setDefaultForDefaultValue(prefs.get(DefaultColumnUserSettings.DEFAULT_COLUMN_DEFAULT_VALUE, ""));
        
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
                oldWidth = prefWidth;
                oldHeight = prefHeight;
                prefWidth = getWidth();
                prefHeight = getHeight();
            }

            public void componentShown(ComponentEvent e) {
            }
        });
        
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState() == ArchitectFrame.MAXIMIZED_BOTH) {
                    prefWidth = oldWidth;
                    prefHeight = oldHeight;
                }
            }
        });
        
        refreshProjectAction = new RefreshProjectAction(session);
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
     * @throws SQLObjectException
     */
    void init() throws SQLObjectException {
        UserSettings sprefs = session.getUserSettings().getSwingSettings();
        int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        // Create actions

        aboutAction = new AboutAction(session);

        newProjectAction = new AbstractAction(Messages.getString("ArchitectFrame.newProjectActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("new_project",Messages.getString("ArchitectFrame.newProjectActionIconDescription"),sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) { //$NON-NLS-1$ //$NON-NLS-2$
            public void actionPerformed(ActionEvent e) {
                try {
                    ArchitectSwingSession newSession = createNewProject();
                    if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                        ArchitectFrame newFrame = newSession.getArchitectFrame();
                        JMenuBar mb = newFrame.menuBar;
                        for (int i = 0; i < mb.getMenuCount(); i++) {
                            if ("TOOLS_MENU".equals(mb.getMenu(i).getName())) {
                                mb.getMenu(i).add(new InvadersAction(newSession));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(session, Messages.getString("ArchitectFrame.projectCreationFailed"), ex); //$NON-NLS-1$
                    logger.error("Got exception while creating new project", ex); //$NON-NLS-1$
                }
            }
        };
        newProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.newProjectActionDescription")); //$NON-NLS-1$
        newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask));


        openProjectAction = new OpenProjectAction(session);

        saveProjectAction = new AbstractAction(Messages.getString("ArchitectFrame.saveProjectActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("disk", //$NON-NLS-1$
                        Messages.getString("ArchitectFrame.saveProjectActionIconDescription"), //$NON-NLS-1$
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                session.saveOrSaveAs(false, true);
            }
        };
        saveProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.saveProjectActionDescription")); //$NON-NLS-1$
        saveProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));

        saveProjectAsAction = new AbstractAction(Messages.getString("ArchitectFrame.saveProjectAsActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("save_as", //$NON-NLS-1$
                        Messages.getString("ArchitectFrame.saveProjectAsActionIconDescription"), //$NON-NLS-1$
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                session.saveOrSaveAs(true, true);
            }
        };
        saveProjectAsAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.saveProjectAsActionDescription")); //$NON-NLS-1$
        
        closeProjectAction = new CloseProjectAction(session);

        prefAction = new PreferencesAction(session);
        projectSettingsAction = new ProjectSettingsAction(session);
        printAction = new PrintAction(session);
        printAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask));

        exportPlaypenToPDFAction = new ExportPlaypenToPDFAction(session, session.getPlayPen());

        zoomInAction = new ZoomAction(session, session.getPlayPen(), ZOOM_STEP);
        zoomOutAction = new ZoomAction(session, session.getPlayPen(), ZOOM_STEP * -1.0);

        zoomNormalAction = new ZoomResetAction(session, session.getPlayPen());

        zoomToFitAction = new ZoomToFitAction(session, session.getPlayPen());
        zoomToFitAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.zoomToFitActionDescription")); //$NON-NLS-1$

        undoAction = new UndoAction(session, session.getUndoManager());
        redoAction = new RedoAction(session, session.getUndoManager());
        autoLayoutAction = new AutoLayoutAction(session, session.getPlayPen(), Messages.getString("ArchitectFrame.autoLayoutActionName"), Messages.getString("ArchitectFrame.autoLayoutActionDescription"), "auto_layout"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        autoLayout = new FruchtermanReingoldForceLayout();
        autoLayoutAction.setLayout(autoLayout);
        exportDDLAction = new ExportDDLAction(session);
        comapareDMDialog = new CompareDMDialog(session);
        
        compareDMAction = new CompareDMAction(session,comapareDMDialog);
        dataMoverAction = new DataMoverAction(this, session);
        sqlQueryAction = new SQLQueryAction(session);

        deleteSelectedAction = new DeleteSelectedAction(session);
        createIdentifyingRelationshipAction = new CreateRelationshipAction(session, true, playpen.getCursorManager());
        createNonIdentifyingRelationshipAction = new CreateRelationshipAction(session, false, playpen.getCursorManager());
        editRelationshipAction = new EditRelationshipAction(session);
        createTableAction = new CreateTableAction(session);
        editColumnAction = new EditColumnAction(session);
        editSelectedAction = new EditSelectedAction(session);
        insertColumnAction = new InsertColumnAction(session);
        insertIndexAction = new InsertIndexAction(session);
        editTableAction = new EditTableAction(session);
        editIndexAction = new EditSelectedIndexAction(session);
        searchReplaceAction = new SearchReplaceAction(session);
        searchReplaceAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, accelMask));
        selectAllAction = new SelectAllAction(session);
        selectAllAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask));

        profileAction = new ProfileAction(session, session.getProfileManager());
        reverseRelationshipAction = new ReverseRelationshipAction(session);
        alignTableHorizontalAction = new AlignTableAction(session, Messages.getString("ArchitectFrame.alignTablesHorizontallyActionName"), Messages.getString("ArchitectFrame.alignTablesHorizontallyActionDescription"), true); //$NON-NLS-1$ //$NON-NLS-2$
        alignTableVerticalAction = new AlignTableAction(session, Messages.getString("ArchitectFrame.alignTablesVerticallyActionName"), Messages.getString("ArchitectFrame.alignTablesVerticallyActionDescription"), false); //$NON-NLS-1$ //$NON-NLS-2$
        focusToParentAction = new FocusToChildOrParentTableAction(session, Messages.getString("ArchitectFrame.setFocusToParentTableActionName"), Messages.getString("ArchitectFrame.setFocusToParentTableActionDescription"), true); //$NON-NLS-1$ //$NON-NLS-2$
        focusToChildAction = new FocusToChildOrParentTableAction(session, Messages.getString("ArchitectFrame.setFocusToChildTableActionName"), Messages.getString("ArchitectFrame.setFocusToChildTableActionDescription"), false); //$NON-NLS-1$ //$NON-NLS-2$
        
        copyAction = new CopySelectedAction(session);
        cutAction = new CutSelectedAction(session);
        pasteAction = new PasteSelectedAction(session);
        
        menuBar = createNewMenuBar();        
        setJMenuBar(menuBar);

        projectBar = new JToolBar(JToolBar.HORIZONTAL);
        ppBar = new JToolBar(JToolBar.VERTICAL);

        projectBar.add(newProjectAction);
        projectBar.add(openProjectAction);
        projectBar.add(saveProjectAction);
        projectBar.addSeparator();
        
        projectBar.add(refreshProjectAction);
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
        projectBar.setToolTipText(Messages.getString("ArchitectFrame.projectToolbarToolTip")); //$NON-NLS-1$
        projectBar.setName(Messages.getString("ArchitectFrame.projectToolbarName")); //$NON-NLS-1$
        
        projectBar.setFocusable(false);
        for (Component c : projectBar.getComponents()) {
            c.setFocusable(false);
        }
        
        JButton tempButton = null; // shared actions need to report where they are coming from
        ppBar.setToolTipText(Messages.getString("ArchitectFrame.playPenToolbarToolTip")); //$NON-NLS-1$
        ppBar.setName(Messages.getString("ArchitectFrame.playPenToolbarName")); //$NON-NLS-1$
        ppBar.add(zoomInAction);
        ppBar.add(zoomOutAction);
        ppBar.add(zoomNormalAction);
        ppBar.add(zoomToFitAction);
        ppBar.addSeparator();
        tempButton = ppBar.add(deleteSelectedAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton = ppBar.add(createTableAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton  = ppBar.add(insertIndexAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        tempButton = ppBar.add(insertColumnAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        tempButton = ppBar.add(editSelectedAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        ppBar.addSeparator();
        ppBar.add(createNonIdentifyingRelationshipAction);
        ppBar.add(createIdentifyingRelationshipAction);
        tempButton = ppBar.add(editRelationshipAction);
        tempButton.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
        
        ppBar.setFocusable(false);
        for (Component c : ppBar.getComponents()) {
            c.setFocusable(false);
        }

        Container projectBarPane = getContentPane();
        projectBarPane.setLayout(new BorderLayout());
        projectBarPane.add(projectBar, BorderLayout.NORTH);

        JPanel cp = new JPanel(new BorderLayout());
        cp.add(ppBar, BorderLayout.EAST);
        projectBarPane.add(cp, BorderLayout.CENTER);

        cp.add(splitPane, BorderLayout.CENTER);
        logger.debug("Added splitpane to content pane"); //$NON-NLS-1$
    }
    
    public JMenuBar createNewMenuBar() { 
        ArchitectSwingSessionContext context = session.getContext();
        Action checkForUpdateAction = new CheckForUpdateAction(session);
        Action exportCSVAction = new ExportCSVAction(this, session);
        Action mappingReportAction = new VisualMappingReportAction(this, session);
        Action kettleETL = new KettleJobAction(session);
        Action exportHTMLReportAction = new ExportHTMLReportAction(session);
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(Messages.getString("ArchitectFrame.fileMenu")); //$NON-NLS-1$
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
        fileMenu.add(exportHTMLReportAction);
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

        JMenu editMenu = new JMenu(Messages.getString("ArchitectFrame.editMenu")); //$NON-NLS-1$
        editMenu.setMnemonic('e');
        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.addSeparator();
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.addSeparator();
        editMenu.add(selectAllAction);
        editMenu.addSeparator();
        editMenu.add(searchReplaceAction);
        menuBar.add(editMenu);

        // the connections menu is set up when a new project is created (because it depends on the current DBTree)
        connectionsMenu = new JMenu(Messages.getString("ArchitectFrame.connectionsMenu")); //$NON-NLS-1$
        connectionsMenu.setMnemonic('c');
        menuBar.add(connectionsMenu);
        connectionsMenu.removeAll();
        
        final JMenu dbcsMenu = session.createDataSourcesMenu();
        final JMenuItem propertiesMenu = new JMenuItem(new DataSourcePropertiesAction(session));
        final JMenuItem removeDBCSMenu = new JMenuItem(new RemoveSourceDBAction(dbTree));
        
        connectionsMenu.add(dbcsMenu);
        connectionsMenu.add(propertiesMenu);
        connectionsMenu.add(removeDBCSMenu);
        connectionsMenu.addSeparator();
        connectionsMenu.add(new DatabaseConnectionManagerAction(session));

        connectionsMenu.addMenuListener(new MenuListener(){
            
            private JMenu dbcs = dbcsMenu;
            
            public void menuCanceled(MenuEvent e) {
                // do nothing here
            }
            public void menuDeselected(MenuEvent e) {
                // do nothing here
            }
            
            public void menuSelected(MenuEvent e) {
                // updates for new connections
                connectionsMenu.remove(dbcs);
                dbcs = session.createDataSourcesMenu();
                connectionsMenu.add(dbcs, 0);
                
                // enable/disable dbcs related menu items
                TreePath tp = dbTree.getSelectionPath();
                if (tp != null) {
                    boolean dbcsSelected = !dbTree.isTargetDatabaseNode(tp) && !dbTree.isTargetDatabaseChild(tp);
                    propertiesMenu.setEnabled(dbcsSelected);
                    removeDBCSMenu.setEnabled(dbcsSelected && tp.getLastPathComponent() instanceof SQLDatabase);
                } else {
                    propertiesMenu.setEnabled(false);
                    removeDBCSMenu.setEnabled(false);
                }
            }
        });
        
        JMenu etlMenu = new JMenu(Messages.getString("ArchitectFrame.etlMenu")); //$NON-NLS-1$
        etlMenu.setMnemonic('l');
        etlMenu.add(exportCSVAction);
        etlMenu.add(mappingReportAction);
        etlMenu.add(kettleETL);
        menuBar.add(etlMenu);

        final JMenu olapMenu = new JMenu(Messages.getString("ArchitectFrame.olapMenu")); //$NON-NLS-1$
        olapMenu.setMnemonic('o');
        final JMenu olapEditMenu = buildOLAPEditMenu();
        olapMenu.add(olapEditMenu);
        olapMenu.add(new ImportSchemaAction(session));
        olapMenu.addSeparator();
        olapMenu.add(new OLAPSchemaManagerAction(session));
        olapMenu.addMenuListener(new MenuListener(){
            
            private JMenu editMenu = olapEditMenu;
            
            public void menuCanceled(MenuEvent e) {
                // do nothing here
            }
            public void menuDeselected(MenuEvent e) {
                // do nothing here
            }
            
            public void menuSelected(MenuEvent e) {
                // updates for new OLAP schemas
                olapMenu.remove(editMenu);
                editMenu = buildOLAPEditMenu();
                olapMenu.add(editMenu, 0);
            }
        });
        menuBar.add(olapMenu);

        // Enterprise stuff ...
        enterpriseMenu = new JMenu("Enterprise");
        enterpriseMenu.add(openServerManagerAction);
        enterpriseMenu.add(openProjectManagerAction);
        openRevisionListAction.setEnabled(session.isEnterpriseSession());
        enterpriseMenu.add(openRevisionListAction);
        
        JMenu securityMenu = new JMenu("Security");
        securityMenu.setEnabled(session.isEnterpriseSession());
        securityMenu.add(openSecurityManagerPanelAction);
        securityMenu.add(openProjectSecurityPanelAction);
        
        enterpriseMenu.add(securityMenu);
        enterpriseMenu.add(refreshProjectAction);
        menuBar.add(enterpriseMenu);
        
        JMenu toolsMenu = new JMenu(Messages.getString("ArchitectFrame.toolsMenu")); //$NON-NLS-1$
        toolsMenu.setName("TOOLS_MENU");
        toolsMenu.setMnemonic('t');
        toolsMenu.add(exportDDLAction);
        toolsMenu.add(compareDMAction);
        toolsMenu.add(sqlQueryAction);
        toolsMenu.add(dataMoverAction);

        menuBar.add(toolsMenu);

        JMenu profileMenu = new JMenu(Messages.getString("ArchitectFrame.profileMenu")); //$NON-NLS-1$
        profileMenu.setMnemonic('p');
        profileMenu.add(profileAction);

        menuBar.add(profileMenu);

        JMenu windowMenu = new JMenu(Messages.getString("ArchitectFrame.windowMenu")); //$NON-NLS-1$
        final JCheckBoxMenuItem navigatorMenuItem = new JCheckBoxMenuItem(Messages.getString("ArchitectFrame.navigatorMenu")); //$NON-NLS-1$
        navigatorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (navigatorMenuItem.isSelected()) {
                    Point location = getLocation();
                    location.translate(splitPane.getWidth() - 25, 75);

                    navigatorDialog = new Navigator(session, location);

                    navigatorDialog.addWindowListener(new WindowAdapter(){
                        public void windowClosing(WindowEvent e) {
                            navigatorMenuItem.setSelected(false);
                            navigatorDialog.dispose();
                        }
                    });

                    // Refreshes the overview navigator when viewport changes
                    if (playpenScrollPane != null) {
                        playpenScrollPane.getVerticalScrollBar().addAdjustmentListener(navigatorDialog);
                        playpenScrollPane.getHorizontalScrollBar().addAdjustmentListener(navigatorDialog);
                    }
                } else {
                    navigatorDialog.dispose();
                }
            }
        });
        windowMenu.add(navigatorMenuItem);
        windowMenu.add(new DatabaseConnectionManagerAction(session));
        windowMenu.add(new AbstractAction(Messages.getString("ArchitectFrame.profileManager")) { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e) {
                session.getProfileDialog().setVisible(true);
            }
        });
        
        menuBar.add(windowMenu);

        JMenu helpMenu = new JMenu(Messages.getString("ArchitectFrame.helpMenu")); //$NON-NLS-1$
        helpMenu.setMnemonic('h');
        
        if (!context.isMacOSX()) {
            helpMenu.add(aboutAction);
            
            helpMenu.addSeparator();
        }
        
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_GS_URL, Messages.getString("ArchitectFrame.gettingStartedAction")));
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_DEMO_URL, Messages.getString("ArchitectFrame.tutorialsAction")));
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_FAQ_URL, Messages.getString("ArchitectFrame.faqAction")));
        helpMenu.add(SPSUtils.forumAction);
        helpMenu.addSeparator();
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_UPGRADE_URL, Messages.getString("ArchitectFrame.upgradeAction")));
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_PS_URL, Messages.getString("ArchitectFrame.premiumSupportAction")));
        helpMenu.add(new OpenUrlAction(SPSUtils.ARCHITECT_UG_URL, Messages.getString("ArchitectFrame.userGuideAction")));
        helpMenu.addSeparator();
        helpMenu.add(checkForUpdateAction);
        menuBar.add(helpMenu);
        
        return menuBar;        
    }
   
    private JMenu enterpriseMenu;
    
    public JMenu getEnterpriseMenu() {
        return enterpriseMenu;
    }
    
    private JMenu buildOLAPEditMenu() {
        JMenu menu = new JMenu(Messages.getString("ArchitectFrame.editSchemaMenu")); //$NON-NLS-1$
        menu.add(new JMenuItem(new OLAPEditAction(session, null)));
        menu.addSeparator(); 
        for (OLAPSession olapSession : session.getOLAPRootObject().getChildren()) {
            menu.add(new JMenuItem(new OLAPEditAction(session, olapSession)));
        }
        return menu;
    }
    
    /**
     * Creates a new project in the same session context as this one, 
     * and opens it in a new ArchitectFrame instance.
     * 
     * @return the new session that contains the new project. 
     */
    private ArchitectSwingSession createNewProject() throws SQLObjectException {
        return session.getContext().createSession(session);
    }

    public SwingUIProjectLoader getProject() {
        return session.getProjectLoader();
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
    public void saveSettings() throws SQLObjectException {

        CoreUserSettings us = session.getUserSettings();

        /* These are saved directly in java.util.Preferences.
         * XXX Eventually we should save almost everything there, except
         * the PL.INI contents that must be shared with other non-Java programs.
         */
        Preferences prefs = session.getContext().getPrefs();
        prefs.putInt(ArchitectSwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_X, getLocation().x);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_Y, getLocation().y);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_WIDTH, prefWidth);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_HEIGHT, prefHeight);
        prefs.putBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                us.getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));

        us.write();
        prefs.put(ArchitectSession.PREFS_PL_INI_PATH, session.getContext().getPlDotIniPath());
        
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_NAME, SQLColumn.getDefaultName());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_TYPE, SQLColumn.getDefaultType());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_PREC, SQLColumn.getDefaultPrec());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_SCALE, SQLColumn.getDefaultScale());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_INPK, SQLColumn.isDefaultInPK());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_NULLABLE, SQLColumn.isDefaultNullable());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_AUTOINC, SQLColumn.isDefaultAutoInc());
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_REMARKS, SQLColumn.getDefaultRemarks());
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_DEFAULT_VALUE, SQLColumn.getDefaultForDefaultValue());
        try {
            if (!session.isEnterpriseSession())
                session.getDataSources().write(new File(session.getContext().getPlDotIniPath()));
        } catch (IOException e) {
            logger.error("Couldn't save PL.INI file!", e); //$NON-NLS-1$
        }
    }
    
    /**
     * Disables/Re-enables the option to 'save' and 'save as'
     */
    public void setEnableSaveOption(boolean isEnable) {
            saveProjectAction.setEnabled(isEnable);
            saveProjectAsAction.setEnabled(isEnable);
    }

    /**
     * Creates an ArchitectFrame and sets it visible.  This method is
     * an acceptable way to launch the Architect application.
     */
    @SuppressWarnings("deprecation") //$NON-NLS-1$
    public static void main(final String args[]) throws SQLObjectException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Unable to set native look and feel. Continuing with default.", e);
        }
        
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
                        session.getProjectLoader().setFile(openFile);
                    } else {
                        context.createSession();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //We wish we had a parent component to direct the dialog to
                    //instead of passing a null, but this is being invoked, so 
                    //everything else blew up.
                    ASUtils.showExceptionDialog(null, Messages.getString("ArchitectFrame.architectLaunchFailureMessage"), e); //$NON-NLS-1$
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

    public NotifyingUndoManager getUndoManager() {
        return session.getUndoManager();
    }

    public Action getNewProjectAction() {
        return newProjectAction;
    }

    public void setNewProjectAction(Action newProjectAction) {
        this.newProjectAction = newProjectAction;
    }

    public ZoomToFitAction getZoomToFitAction() {
        return zoomToFitAction;
    }

    public ZoomAction getZoomInAction() {
        return zoomInAction;
    }

    public ZoomAction getZoomOutAction() {
        return zoomOutAction;
    }

    public ZoomResetAction getZoomResetAction() {
        return zoomNormalAction;
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
    
    public EditSelectedAction getEditSelectedAction() {
        return editSelectedAction;
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

    /**
     * Returns the action that edits the index which is currently selected in
     * the DBTree. For PlayPen purposes, see {@link EditSpecificIndexAction}.
     */
    public EditSelectedIndexAction getEditIndexAction() {
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

    public ReverseRelationshipAction getReverseRelationshipAction() {
        return reverseRelationshipAction;
    }

    public ProfileAction getProfileAction() {
        return profileAction;
    }
    
    public AlignTableAction getAlignTableHorizontalAction() {
        return alignTableHorizontalAction;
    }
    
    public AlignTableAction getAlignTableVerticalAction() {
        return alignTableVerticalAction;
    }
    
    public FocusToChildOrParentTableAction getFocusToParentAction() {
        return focusToParentAction;
    }
    
    public FocusToChildOrParentTableAction getFocusToChildAction() {
        return focusToChildAction;
    }

}