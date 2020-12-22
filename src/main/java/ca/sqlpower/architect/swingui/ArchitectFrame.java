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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.UpdateCheckSettings;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
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
import ca.sqlpower.architect.swingui.action.EditCriticSettingsAction;
import ca.sqlpower.architect.swingui.action.EditLabelAction;
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
import ca.sqlpower.architect.swingui.enterprise.ArchitectServerProjectsManagerPanel;
import ca.sqlpower.architect.swingui.enterprise.ProjectSecurityPanel;
import ca.sqlpower.architect.swingui.enterprise.RevisionListPanel;
import ca.sqlpower.architect.swingui.enterprise.SecurityPanel;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.action.ImportSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPEditAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPSchemaManagerAction;
import ca.sqlpower.enterprise.client.ConnectionTestAction;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable.TransferStyles;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.StackedTabComponent;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory;
import ca.sqlpower.swingui.StackedTabComponent.StackedTab;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory.NonModalSwingUIUserPrompterFactory;
import ca.sqlpower.swingui.action.OpenUrlAction;
import ca.sqlpower.swingui.enterprise.client.SPServerInfoManagerPanel;
import ca.sqlpower.swingui.enterprise.client.ServerProjectsManagerPanel;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.UserPrompterFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class ArchitectFrame extends JFrame {

    private static final String CYCLE_TAB_ACTION = "ca.sqlpower.architect.swingui.CYCLE_TAB_ACTION";

    private static Logger logger = Logger.getLogger(ArchitectFrame.class);
    
    private static final ImageIcon GROUP_ICON = new ImageIcon(ArchitectFrame.class.getResource("enterprise/icons/group.png"));

    public static final double ZOOM_STEP = 0.25;
    
    private ArchitectSwingSessionContext context;
    private ArchitectSwingSession currentSession = null;
    private List<ArchitectSwingSession> sessions = new ArrayList<ArchitectSwingSession>();
    private JToolBar projectBar = null;
    private JToolBar ppBar = null;
    private JMenuBar menuBar = null;
    private JSplitPane splitPane = null;
    private StackedTabComponent stackedTabPane = new StackedTabComponent();
    private BiMap<ArchitectSwingSession, StackedTab> sessionTabs = HashBiMap.create();
    
    private Navigator navigatorDialog;
    
    private int oldWidth;
    private int oldHeight;
    private int prefWidth;
    private int prefHeight;
    
    private final UserPrompterFactory nonModalUserPrompterFactory;
    
    private final DropTargetListener tabDropTargetListener = new TabDropTargetListener();

    private JMenu connectionsMenu;
    private ArchitectLayout autoLayout;
    private UndoAction undoAction;
    private RedoAction redoAction;
    
    private AboutAction aboutAction;
    private Action newProjectAction;
    private Action newWindowAction;
    private OpenProjectAction openProjectAction;
    private Action saveProjectAction;
    private Action saveProjectAsAction;
    private Action saveAllProjectsAction;
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
    
    /**
     * A status bar that can have its content changed to useful messages.
     */
    private final ArchitectStatusBar statusBar = new ArchitectStatusBar();
    
    private RefreshProjectAction refreshProjectAction;
    
    private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
    
    private final EditCriticSettingsAction showCriticsManagerAction = new EditCriticSettingsAction(this);
    
    /**
     * Closes all sessions and terminates the JVM.
     */
    private Action exitAction = new AbstractAction(Messages.getString("ArchitectFrame.exitActionName")) { //$NON-NLS-1$
        public void actionPerformed(ActionEvent e) {
            context.closeAll();
        }
    };
    
    private final Action enterpriseLinkAction = new AbstractAction("Get Enterprise...") {
        public void actionPerformed(ActionEvent e) {
            try {
            BrowserUtil.launch("http://www.sqlpower.ca/page/architect-e");
            } catch (IOException ex) {
                ASUtils.showExceptionDialog(currentSession, "Unable to open link: http://www.sqlpower.ca/page/architect-e", ex);
            }
        }
    };
    
    private final Action openServerManagerAction = new AbstractAction("Configure Server Connections...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Server Connections");
            
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            final SPServerInfoManagerPanel sim = new SPServerInfoManagerPanel(context.getServerManager(), ArchitectFrame.this, closeAction);
            sim.setLoginAction(new AbstractAction("Login") {
                public void actionPerformed(ActionEvent e) {
                    SPServerInfo si = sim.getSelectedServer();
                    if (si != null) {
                        
                        final JDialog dialog = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Projects");
                        Action closeAction = new AbstractAction("Close") {
                            public void actionPerformed(ActionEvent e) {
                                dialog.dispose();
                            }
                        };
                        
                        ServerProjectsManagerPanel spm = new ArchitectServerProjectsManagerPanel(si, currentSession, context, closeAction);
                        if (spm.isConnected()) {
                            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                            dialog.setContentPane(spm.getPanel());
                            
                            SPSUtils.makeJDialogCancellable(dialog, null);
                            dialog.pack();
                            dialog.setLocationRelativeTo(ArchitectFrame.this);
                            dialog.setVisible(true);
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
                        // Clears the cookie store before testing the connection
                        // in case an existing valid connection has already been
                        // established.
                        ArchitectClientSideSession.getCookieStore().clear();
                        
                        List<ProjectLocation> l = 
                            ArchitectClientSideSession.getWorkspaceNames(
                                findPanel((JButton) e.getSource()).getServerInfo(),
                                new SwingUIUserPrompterFactory(ArchitectFrame.this));
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
            
            ServerProjectsManagerPanel spm = new ArchitectServerProjectsManagerPanel(currentSession, context, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openSecurityManagerPanelAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Security Manager");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) currentSession).getDelegateSession()).getSystemSession().getUpdater().setUserPrompterFactory(nonModalUserPrompterFactory);
            SecurityPanel spm = new SecurityPanel(((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) currentSession).getDelegateSession()).getProjectLocation().getServiceInfo(), closeAction, d, currentSession);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
        }
    };
    
    private Action openProjectSecurityPanelAction = new AbstractAction("Project Security Settings...") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(ArchitectFrame.this, "Project Security Settings");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };

            // XXX: Blech!!! This code complete ignores any sort of
            // encapsulation and is highly dependent on the currentSession field
            // being the exactly the ArchitectSwingSessionImpl implementation. It's a
            // reasonable guess, but it creates a dependency on that particular
            // implementation.
            ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) currentSession).getDelegateSession()).getSystemSession().getUpdater().setUserPrompterFactory(nonModalUserPrompterFactory);
            ProjectSecurityPanel spm = new ProjectSecurityPanel(((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) currentSession).getDelegateSession()).getSystemWorkspace(), 
                    currentSession.getWorkspace(), ArchitectSwingProject.class, ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) currentSession).getDelegateSession()).getProjectLocation().getServiceInfo().getUsername(), d, closeAction);
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
            
            final RevisionListPanel p = new RevisionListPanel(currentSession, ArchitectFrame.this, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(p.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(ArchitectFrame.this);
            d.setVisible(true);
            
        }        
    };
    
    private JMenu securityMenu;

    private ChangeListener tabChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            if (currentSession != sessionTabs.inverse().get(stackedTabPane.getSelectedTab())) {
                setCurrentSession(sessionTabs.inverse().get(stackedTabPane.getSelectedTab()));
            }
        }
    };

    private List<CancelableListener> cancelableListeners = new ArrayList<CancelableListener>();

    private ArrayList<FocusListener> focusListeners = new ArrayList<FocusListener>();

    private JButton newProjectButton;

    private JMenuItem newProjectMenu;

    private JMenuItem newWindowMenu;

    private AbstractAction cycleTabAction;

    private JMenuItem enterpriseLinkButton;

    private JButton saveProjectButton;

    private JMenuItem saveProjectMenu;

    private JMenuItem saveProjectAsMenu;

    private JMenuItem saveAllProjectsMenu;

    private EditLabelAction editLabelAction;

    /**
     * Sets up a new ArchitectFrame, which represents a window containing one or
     * more {@link ArchitectSwingSession}s. It will not become visible until
     * {@link #init(ArchitectSwingSession)} is called.
     * 
     * @param context
     *            The ArchitectSwingSessionContext related to this frame.
     * @param bounds
     *            A Rectangle whose x and y properties will be used to determine
     *            the position of newly created ArchitectFrame
     */
    public ArchitectFrame(ArchitectSwingSessionContext context, Rectangle bounds) {

        this.context = context;
        
        setTitle("SQL Power Architect"); //$NON-NLS-1$
        setIconImage(ASUtils.getFrameIconImage());
        
        // close is handled by a window listener
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        stackedTabPane.addChangeListener(tabChangeListener);
        stackedTabPane.setDropTarget(new DropTarget(stackedTabPane, tabDropTargetListener));
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(stackedTabPane);
        
        final Preferences prefs = context.getPrefs();
        //set the default locale
        Locale.setDefault(new Locale(prefs.get(ArchitectSwingUserSettings.DEFAULT_LOCALE,Locale.getDefault().getDisplayLanguage())));
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
        context.getUserSettings().getSwingSettings().setBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                prefs.getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));
        
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
        
        nonModalUserPrompterFactory = new NonModalSwingUIUserPrompterFactory(this);
        
        // Need to clear the cookies in case multiple frames within the same
        // context are working on the same server project under different users.
        addWindowFocusListener(new WindowFocusListener() {
            
            @Override
            public void windowLostFocus(WindowEvent e) {
                // No operation.
            }
            
            @Override
            public void windowGainedFocus(WindowEvent e) {
                ArchitectClientSideSession.getCookieStore().clear();
            }
        });
    }

    /**
     * A separate initialization method for setting up the actions in the
     * ArchitectFrame. To be called after the constructor is finished. This
     * method was created because several of the actions require a reference to
     * this ArchitectFrame instance, and we don't want to be passing a reference
     * to the ArchitectFrame while it's still being constructed. The session
     * passed in will be the first session that this frame contains. It must not
     * be null.
     * 
     * @param session
     *            The first ArchitectSwingSession that this context will
     *            contain. It will be selected by default, and all of the
     *            actions will be initialized to affect it.
     */
    public void init(ArchitectSwingSession session) {
        init(session, true);
    }

    /**
     * This should only be used for testing, when we need the actions
     * initialized, but do not want to show a GUI.
     * 
     * @param session
     * @param showGUI
     */
    public void init(ArchitectSwingSession session, boolean showGUI) {
        if (!context.equals(session.getContext())) {
            throw new IllegalArgumentException("Session must be in the same context as this frame");
        }
        
        currentSession = session;
        
        UserSettings sprefs = context.getUserSettings().getSwingSettings();
        DefaultColumnUserSettings.setColumnDefaults();
        int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        // Create actions

        aboutAction = new AboutAction(this);

        newProjectAction = new AbstractAction(Messages.getString("ArchitectFrame.newProjectActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("new_project",Messages.getString("ArchitectFrame.newProjectActionIconDescription"),sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) { //$NON-NLS-1$ //$NON-NLS-2$
            public void actionPerformed(ActionEvent e) {
                try {
                    ArchitectSwingSession newSession = context.createSession();
                    addSession(newSession);
                    setCurrentSession(newSession);
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(currentSession, Messages.getString("ArchitectFrame.projectCreationFailed"), ex); //$NON-NLS-1$
                    logger.error("Got exception while creating new project", ex); //$NON-NLS-1$
                }
            }
        };
        newProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.newProjectActionDescription")); //$NON-NLS-1$
        newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask));
        
        newWindowAction = new AbstractAction("New Window",
                SPSUtils.createIcon("new_window", "New Window",
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                try {
                    ArchitectSwingSession newSession = context.createSession();
                    Rectangle bounds = new Rectangle(getBounds());
                    bounds.translate(20, 20);
                    ArchitectFrame newFrame = new ArchitectFrame(context, bounds);
                    newFrame.init(newSession);
                    if ((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                        JMenuBar mb = newFrame.menuBar;
                        for (int i = 0; i < mb.getMenuCount(); i++) {
                            if ("TOOLS_MENU".equals(mb.getMenu(i).getName())) {
                                mb.getMenu(i).add(new InvadersAction(newFrame));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(currentSession, Messages.getString("ArchitectFrame.projectCreationFailed"), ex); //$NON-NLS-1$
                    logger.error("Got exception while creating new project", ex); //$NON-NLS-1$
                }
            }
        };
        newWindowAction.putValue(AbstractAction.SHORT_DESCRIPTION, "New Window");
        newWindowAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask+ActionEvent.SHIFT_MASK));
        

        openProjectAction = new OpenProjectAction(this);

        saveProjectAction = new AbstractAction(Messages.getString("ArchitectFrame.saveProjectActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("disk", //$NON-NLS-1$
                        Messages.getString("ArchitectFrame.saveProjectActionIconDescription"), //$NON-NLS-1$
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                currentSession.saveOrSaveAs(false, true);
                stackedTabPane.setTitleAt(stackedTabPane.getSelectedIndex(), currentSession.getName());
                setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", currentSession.getName())); //$NON-NLS-1$
            }
        };
        saveProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.saveProjectActionDescription")); //$NON-NLS-1$
        saveProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));

        saveProjectAsAction = new AbstractAction(Messages.getString("ArchitectFrame.saveProjectAsActionName"), //$NON-NLS-1$
                SPSUtils.createIcon("disk", //$NON-NLS-1$
                        Messages.getString("ArchitectFrame.saveProjectAsActionIconDescription"), //$NON-NLS-1$
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                currentSession.saveOrSaveAs(true, true);
                stackedTabPane.setTitleAt(stackedTabPane.getSelectedIndex(), currentSession.getName());
                setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", currentSession.getName())); //$NON-NLS-1$
            }
        };
        saveProjectAsAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.saveProjectAsActionDescription")); //$NON-NLS-1$
        
        saveAllProjectsAction = new AbstractAction("Save All Projects",
                SPSUtils.createIcon("save_all", //$NON-NLS-1$
                        "Save All Projects",
                        sprefs.getInt(ArchitectSwingUserSettings.ICON_SIZE, ArchitectSwingSessionContext.ICON_SIZE))) {
            public void actionPerformed(ActionEvent e) {
                for (ArchitectSwingSession session : sessions) {
                    session.saveOrSaveAs(false, true);
                    stackedTabPane.setTitleAt(stackedTabPane.indexOfTab(sessionTabs.get(session)), session.getName());
                }
                setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", currentSession.getName())); //$NON-NLS-1$
            }
        };
        saveAllProjectsAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask+ActionEvent.SHIFT_MASK));
        
        closeProjectAction = new CloseProjectAction(this);

        cycleTabAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int index = stackedTabPane.getSelectedIndex();
                int max = stackedTabPane.getTabCount();
                if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
                    index++;
                } else {
                    index--;
                }
                index = (index+max)%max;
                stackedTabPane.setSelectedIndex(index);
            }
        };
        // Before you ask, yes, I did mean to use control and not the
        // accelerator key. This is in use in major browsers.
        cycleTabAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.CTRL_MASK));
        
        prefAction = new PreferencesAction(this);
        projectSettingsAction = new ProjectSettingsAction(this);
        printAction = new PrintAction(this);
        printAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask));

        exportPlaypenToPDFAction = new ExportPlaypenToPDFAction(this);

        zoomInAction = new ZoomAction(this, ZOOM_STEP);
        zoomOutAction = new ZoomAction(this, ZOOM_STEP * -1.0);

        zoomNormalAction = new ZoomResetAction(this);

        zoomToFitAction = new ZoomToFitAction(this);
        zoomToFitAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.zoomToFitActionDescription")); //$NON-NLS-1$

        undoAction = new UndoAction(currentSession, this, currentSession.getUndoManager());
        redoAction = new RedoAction(currentSession, this, currentSession.getUndoManager());
        autoLayoutAction = new AutoLayoutAction(this, Messages.getString("ArchitectFrame.autoLayoutActionName"), Messages.getString("ArchitectFrame.autoLayoutActionDescription"), "auto_layout"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        autoLayout = new FruchtermanReingoldForceLayout();
        autoLayoutAction.setLayout(autoLayout);
        exportDDLAction = new ExportDDLAction(this);
        
        compareDMAction = new CompareDMAction(this);
        
        dataMoverAction = new DataMoverAction(this);
        sqlQueryAction = new SQLQueryAction(this);

        deleteSelectedAction = new DeleteSelectedAction(this);
        createIdentifyingRelationshipAction = new CreateRelationshipAction(this, true);
        createNonIdentifyingRelationshipAction = new CreateRelationshipAction(this, false);
        editRelationshipAction = new EditRelationshipAction(this);
        createTableAction = new CreateTableAction(this);
        editColumnAction = new EditColumnAction(this);
        editSelectedAction = new EditSelectedAction(this);
        insertColumnAction = new InsertColumnAction(this);
        insertIndexAction = new InsertIndexAction(this);
        editLabelAction = new EditLabelAction(this);
        editTableAction = new EditTableAction(this);
        editIndexAction = new EditSelectedIndexAction(this);
        searchReplaceAction = new SearchReplaceAction(this);
        searchReplaceAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, accelMask));
        selectAllAction = new SelectAllAction(this);
        selectAllAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask));

        profileAction = new ProfileAction(this);
        reverseRelationshipAction = new ReverseRelationshipAction(this);
        alignTableHorizontalAction = new AlignTableAction(this, Messages.getString("ArchitectFrame.alignTablesHorizontallyActionName"), Messages.getString("ArchitectFrame.alignTablesHorizontallyActionDescription"), true); //$NON-NLS-1$ //$NON-NLS-2$
        alignTableVerticalAction = new AlignTableAction(this, Messages.getString("ArchitectFrame.alignTablesVerticallyActionName"), Messages.getString("ArchitectFrame.alignTablesVerticallyActionDescription"), false); //$NON-NLS-1$ //$NON-NLS-2$
        focusToParentAction = new FocusToChildOrParentTableAction(this, Messages.getString("ArchitectFrame.setFocusToParentTableActionName"), Messages.getString("ArchitectFrame.setFocusToParentTableActionDescription"), true); //$NON-NLS-1$ //$NON-NLS-2$
        focusToChildAction = new FocusToChildOrParentTableAction(this, Messages.getString("ArchitectFrame.setFocusToChildTableActionName"), Messages.getString("ArchitectFrame.setFocusToChildTableActionDescription"), false); //$NON-NLS-1$ //$NON-NLS-2$
        
        copyAction = new CopySelectedAction(this);
        cutAction = new CutSelectedAction(this);
        pasteAction = new PasteSelectedAction(this);
        
        refreshProjectAction = new RefreshProjectAction(this);
        
        addSession(session);
        
        menuBar = createNewMenuBar();        
        setJMenuBar(menuBar);

        projectBar = new JToolBar(JToolBar.HORIZONTAL);
        ppBar = new JToolBar(JToolBar.VERTICAL);

        newProjectButton = projectBar.add(newProjectAction);
        projectBar.add(openProjectAction);
        saveProjectButton = projectBar.add(saveProjectAction);
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

        splitPane.setRightComponent(session.getProjectPanel());
        cp.add(splitPane, BorderLayout.CENTER);
        logger.debug("Added splitpane to content pane"); //$NON-NLS-1$
        
        projectBarPane.add(statusBar.getStatusBar(), BorderLayout.SOUTH);
        
        stackedTabPane.setSelectedIndex(0);
        
        context.registerFrame(this);
        
        setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", session.getName())); //$NON-NLS-1$
        
        setVisible(showGUI);
    }
    
    public JMenuBar createNewMenuBar() { 
        checkForUpdateAction = new CheckForUpdateAction(this);
        Action exportCSVAction = new ExportCSVAction(this);
        Action mappingReportAction = new VisualMappingReportAction(this);
        Action kettleETL = new KettleJobAction(this);
        Action exportHTMLReportAction = new ExportHTMLReportAction(this);
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(Messages.getString("ArchitectFrame.fileMenu")); //$NON-NLS-1$
        fileMenu.setMnemonic('f');
        newProjectMenu = fileMenu.add(newProjectAction);
        newWindowMenu = fileMenu.add(newWindowAction);
        fileMenu.add(openProjectAction);
        fileMenu.add(new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                File f = new File(fileName);
                try {
                    OpenProjectAction.getFileLoader().open(context.createSession(), f, currentSession, false);
                } catch (SQLObjectException ex) {
                    SPSUtils.showExceptionDialogNoReport(ArchitectFrame.this, Messages.getString("ArchitectSwingSessionImpl.openProjectFileFailed"), ex); //$NON-NLS-1$
                }
            }
        });
        fileMenu.add(closeProjectAction);
        fileMenu.addSeparator();
        saveProjectMenu = fileMenu.add(saveProjectAction);
        saveProjectAsMenu = fileMenu.add(saveProjectAsAction);
        saveAllProjectsMenu = fileMenu.add(saveAllProjectsAction);
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
        
        final JMenu dbcsMenu = currentSession.createDataSourcesMenu();
        final JMenuItem propertiesMenu = new JMenuItem(new DataSourcePropertiesAction(this));
        final JMenuItem removeDBCSMenu = new JMenuItem(new RemoveSourceDBAction(this));
        
        connectionsMenu.add(dbcsMenu);
        connectionsMenu.add(propertiesMenu);
        connectionsMenu.add(removeDBCSMenu);
        connectionsMenu.addSeparator();
        connectionsMenu.add(new DatabaseConnectionManagerAction(this));

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
                dbcs = currentSession.createDataSourcesMenu();
                connectionsMenu.add(dbcs, 0);
                
                // enable/disable dbcs related menu items
                DBTree tree = currentSession.getDBTree();
                TreePath tp = currentSession.getDBTree().getSelectionPath();
                if (tp != null) {
                    boolean dbcsSelected = !tree.isTargetDatabaseNode(tp) && !tree.isTargetDatabaseChild(tp);
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
        olapMenu.add(new ImportSchemaAction(this));
        olapMenu.addSeparator();
        olapMenu.add(new OLAPSchemaManagerAction(this));
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
        enterpriseLinkButton = new JMenuItem(enterpriseLinkAction);
        enterpriseLinkButton.setText("Get Enterprise...");
        enterpriseMenu.add(enterpriseLinkButton);
        enterpriseMenu.add(openServerManagerAction);
        openServerManagerAction.setEnabled(false);
        enterpriseMenu.add(openProjectManagerAction);
        openProjectManagerAction.setEnabled(false);
        openRevisionListAction.setEnabled(currentSession.isEnterpriseSession());
        enterpriseMenu.add(openRevisionListAction);
        
        securityMenu = new JMenu("Security");
        securityMenu.setEnabled(currentSession.isEnterpriseSession());
        
        JMenuItem securityManagerMenuItem = new JMenuItem(openSecurityManagerPanelAction);
        securityManagerMenuItem.setText("Users & Groups...");
        securityManagerMenuItem.setIcon(GROUP_ICON);
        securityMenu.add(securityManagerMenuItem);
        
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

                    navigatorDialog = new Navigator(ArchitectFrame.this, location);

                    navigatorDialog.addWindowListener(new WindowAdapter(){
                        public void windowClosing(WindowEvent e) {
                            navigatorMenuItem.setSelected(false);
                            navigatorDialog.dispose();
                        }
                    });

                } else {
                    navigatorDialog.dispose();
                }
            }
        });
        windowMenu.add(navigatorMenuItem);
        windowMenu.add(new DatabaseConnectionManagerAction(this));
        windowMenu.add(new AbstractAction(Messages.getString("ArchitectFrame.profileManager")) { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e) {
                currentSession.getProfileDialog().setVisible(true);
            }
        });
        windowMenu.add(new JMenuItem(getShowCriticsManagerAction()));
        
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
        
        menuBar.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.CTRL_MASK), CYCLE_TAB_ACTION);
        menuBar.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK), CYCLE_TAB_ACTION);
        menuBar.getActionMap().put(CYCLE_TAB_ACTION, cycleTabAction);
        
        return menuBar;        
    }

    /**
     * Sets the current session to be the active session. Its tab will be
     * selected in the stacked tab pane, and its DBTree shown. Its playpen will
     * be visible in the right-hand pane of this frame, and this frame's actions
     * will change to affect the new session.
     * 
     * @param newSession
     *            The session to become active. It must have been previously
     *            added to this frame.
     * @see #addSession(ArchitectSwingSession)
     */
    public void setCurrentSession(ArchitectSwingSession newSession) {
        if (newSession == currentSession) return;
        
        if (!sessions.contains(newSession)) {
            throw new IllegalArgumentException("Session must already be a part of this frame");
        }
        
        // These actions keep state of the session, so must be dealt with.
        undoAction.setUndoManager(newSession.getUndoManager());
        redoAction.setUndoManager(newSession.getUndoManager());
        
        ArchitectSwingSession oldSession = currentSession;
        currentSession = newSession;
        
        buildOLAPEditMenu();
        toggleEnterpriseMenu();

        for (SelectionListener l : selectionListeners) {
            oldSession.getPlayPen().removeSelectionListener(l);
            for (Selectable s : oldSession.getPlayPen().getSelectedItems()) {
                l.itemDeselected(new SelectionEvent(s, SelectionEvent.DESELECTION_EVENT, SelectionEvent.PLAYPEN_SWITCH_MULTISELECT));
            }
            for (Selectable s : newSession.getPlayPen().getSelectedItems()) {
                l.itemSelected(new SelectionEvent(s, SelectionEvent.SELECTION_EVENT, SelectionEvent.PLAYPEN_SWITCH_MULTISELECT));
            }
            newSession.getPlayPen().addSelectionListener(l);
        }
        
        for (CancelableListener l : cancelableListeners) {
            l.cancel();
            oldSession.getPlayPen().removeCancelableListener(l);
            newSession.getPlayPen().addCancelableListener(l);
        }
        
        for (FocusListener l : focusListeners) {
            oldSession.getPlayPen().removeFocusListener(l);
            newSession.getPlayPen().addFocusListener(l);
        }

        setTitle(Messages.getString("ArchitectSwingSessionImpl.mainFrameTitle", newSession.getName())); //$NON-NLS-1$
        
        stackedTabPane.setSelectedIndex(stackedTabPane.indexOfTab(sessionTabs.get(newSession)));
        stackedTabPane.setTitleAt(stackedTabPane.getSelectedIndex(), newSession.getName());
        
        int div = splitPane.getDividerLocation();
        splitPane.setRightComponent(newSession.getProjectPanel());
        splitPane.setDividerLocation(div);
        
        firePropertyChange("currentSession", oldSession, newSession);
    }
    
    private void toggleEnterpriseMenu() {
        openRevisionListAction.setEnabled(currentSession.isEnterpriseSession());
        securityMenu.setEnabled(currentSession.isEnterpriseSession());
    }
    
    public void addPlayPenFocusListener(FocusListener l) {
        focusListeners.add(l);
        currentSession.getPlayPen().addFocusListener(l);
    }
    
    public void removePlayPenFocusListener(FocusListener l) {
        focusListeners.remove(l);
        currentSession.getPlayPen().removeFocusListener(l);
    }
    
    public void addSelectionListener(SelectionListener l) {
        selectionListeners.add(l);
        currentSession.getPlayPen().addSelectionListener(l);
    }
    
    public void removeSelectionListener(SelectionListener l) {
        selectionListeners.remove(l);
        currentSession.getPlayPen().removeSelectionListener(l);
    }
    
    public void addCancelableListener(CancelableListener l) {
        cancelableListeners.add(l);
        currentSession.getPlayPen().addCancelableListener(l);
    }
    
    public void removeCancelableListener(CancelableListener l) {
        cancelableListeners.remove(l);
        currentSession.getPlayPen().removeCancelableListener(l);
    }
   
    private JMenu enterpriseMenu;

    private CheckForUpdateAction checkForUpdateAction;

    public JMenu getEnterpriseMenu() {
        return enterpriseMenu;
    }
    
    // Must rebuild OLAP Menu whenever session changes
    private JMenu buildOLAPEditMenu() {
        JMenu menu = new JMenu(Messages.getString("ArchitectFrame.editSchemaMenu")); //$NON-NLS-1$
        menu.add(new JMenuItem(new OLAPEditAction(currentSession, null)));
        menu.addSeparator(); 
        for (OLAPSession olapSession : currentSession.getOLAPRootObject().getChildren()) {
            menu.add(new JMenuItem(new OLAPEditAction(currentSession, olapSession)));
        }
        return menu;
    }

    /**
     * Adds a session to the stacked tab tree in this frame. The session will
     * have its GUI components initialized as part of this process.
     * 
     * @param session
     *            The {@link ArchitectSwingSession} to add to this frame. It
     *            must be part of the same context as this frame.
     * @see #setCurrentSession(ArchitectSwingSession)
     */
    public void addSession(ArchitectSwingSession session) {
        if (!context.equals(session.getContext())) {
            throw new IllegalArgumentException("Session must be in the same context as this frame");
        }
        session.initGUI(this);
        session.addSessionLifecycleListener(new SessionLifecycleListener<ArchitectSession>() {
            public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
                removeSession((ArchitectSwingSession) e.getSource());
            }
            public void sessionOpening(SessionLifecycleEvent<ArchitectSession> e) {
            }
        });
        sessions.add(session);
        final StackedTab tab = stackedTabPane.addTab(
                session.getName(), 
                new JScrollPane(SPSUtils.getBrandedTreePanel(session.getDBTree())), 
                true);
        tab.getTabComponent().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (tab.isCloseable() && tab.closeButtonContains(e.getX(), e.getY())) {
                    sessionTabs.inverse().get(tab).close();
                }
            }
        });
        sessionTabs.put(session, tab);
    }
    
    public void removeSession(ArchitectSwingSession session) {
        int i = sessions.indexOf(session);
        sessions.remove(session);
        if (sessions.size() == 0) {
            try {
                saveSettings();
            } catch (SQLObjectException e) {
                logger.error("Exception while saving settings", e);
            }
            dispose();
        } else {
            if (session.equals(currentSession)) {
                setCurrentSession(sessions.get(Math.min(i, sessions.size()-1)));
            }
            stackedTabPane.removeTabAt(stackedTabPane.indexOfTab(sessionTabs.get(session)));
            sessionTabs.remove(session);
        }
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
            List<ArchitectSwingSession> localSessions = new ArrayList<ArchitectSwingSession>(sessions);
            for (ArchitectSession session : localSessions) {
                session.close();
            }
        }
    }

    /**
     * Saves this frame's settings as user prefs.  Settings are frame location,
     * divider location, that kind of stuff.
     */
    public void saveSettings() throws SQLObjectException {

        CoreUserSettings us = context.getUserSettings();

        /* These are saved directly in java.util.Preferences.
         * XXX Eventually we should save almost everything there, except
         * the PL.INI contents that must be shared with other non-Java programs.
         */
        Preferences prefs = context.getPrefs();
        prefs.putInt(ArchitectSwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_X, getLocation().x);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_Y, getLocation().y);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_WIDTH, prefWidth);
        prefs.putInt(ArchitectSwingUserSettings.MAIN_FRAME_HEIGHT, prefHeight);
        prefs.putBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                us.getSwingSettings().getBoolean(ArchitectSwingUserSettings.SHOW_WELCOMESCREEN, true));

        us.write();
        prefs.put(ArchitectSession.PREFS_PL_INI_PATH, context.getPlDotIniPath());
        
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_NAME, SQLColumn.getDefaultName());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_TYPE, SQLColumn.getDefaultType());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_PREC, SQLColumn.getDefaultPrec());
        prefs.putInt(DefaultColumnUserSettings.DEFAULT_COLUMN_SCALE, SQLColumn.getDefaultScale());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_INPK, SQLColumn.isDefaultInPK());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_NULLABLE, SQLColumn.isDefaultNullable());
        prefs.putBoolean(DefaultColumnUserSettings.DEFAULT_COLUMN_AUTOINC, SQLColumn.isDefaultAutoInc());
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_REMARKS, SQLColumn.getDefaultRemarks());
        prefs.put(DefaultColumnUserSettings.DEFAULT_COLUMN_DEFAULT_VALUE, SQLColumn.getDefaultForDefaultValue());
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
                    
                    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(context));
                    
                    boolean headless = false;
                    final List<ArchitectSwingSession> sessions = new ArrayList<ArchitectSwingSession>();
                    if (args.length > 0) {
                        for (int i = 0; i < args.length; i++) {
                            if (args[i].equalsIgnoreCase("-headless")) {
                                headless = true;
                            } else if (new File(args[i]).exists()){
                                File openFile = new File(args[i]);
                                InputStream in = new BufferedInputStream(new FileInputStream(openFile));
                                ArchitectSwingSession session = context.createSession(in);
                                session.getRecentMenu().putRecentFileName(openFile.getAbsolutePath());
                                session.getProjectLoader().setFile(openFile);
                                sessions.add(session);
                            }
                        }
                    } 
                    if (args.length == 0 || sessions.size() == 0){
                        sessions.add(context.createSession());
                    }
                    if (!headless) {
                        final ArchitectFrame frame = new ArchitectFrame(context, null);
                        frame.init(sessions.get(0));
                        for (int i = 1; i < sessions.size(); i++) {
                            frame.addSession(sessions.get(i));
                            frame.setCurrentSession(sessions.get(i));
                        }
                        frame.setCurrentSession(sessions.get(0));
                        
                        if (context.getUserSettings().getUpdateCheckSettings().getBoolean(
                                UpdateCheckSettings.AUTO_UPDATE_CHECK, true)) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    frame.checkForUpdateAction.checkForUpdate(false);
                                }
                            }).start();
                        }
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

    public JToolBar getPlayPenToolBar() {
        return ppBar;
    }

    public Action getNewProjectAction() {
        return newProjectAction;
    }

    public void setNewProjectAction(Action newProjectAction) {
        Object accelKey = this.newProjectAction.getValue(AbstractAction.ACCELERATOR_KEY);
        this.newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY, null);
        newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY, accelKey);
        this.newProjectAction = newProjectAction;
        newProjectButton.setAction(newProjectAction);
        newProjectMenu.setAction(newProjectAction);
    }
    
    public void setNewWindowAction(Action newWindowAction) {
        Object accelKey = this.newWindowAction.getValue(AbstractAction.ACCELERATOR_KEY);
        this.newWindowAction.putValue(AbstractAction.ACCELERATOR_KEY, null);
        newWindowAction.putValue(AbstractAction.ACCELERATOR_KEY, accelKey);
        this.newWindowAction = newWindowAction;
        newWindowMenu.setAction(newWindowAction);
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
    
    public void setEditTableAction(EditTableAction editTableAction) {
        this.editTableAction = editTableAction;
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
    
    public ReverseRelationshipAction getReverseRelationshipAction() {
        return reverseRelationshipAction;
    }

    public ProfileAction getProfileAction() {
        return profileAction;
    }
    
    public JMenuItem getEnterpriseLinkButton() {
        return enterpriseLinkButton;
    }
    
    public OpenProjectAction getOpenProjectAction() {
        return openProjectAction;
    }

    public Action getOpenServerManagerAction() {
        return openServerManagerAction;
    }

    public void setOpenProjectManagerAction(Action openProjectManagerAction) {
        this.openProjectManagerAction = openProjectManagerAction;
    }

    public Action getOpenProjectManagerAction() {
        return openProjectManagerAction;
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

    /**
     * Does a depth-first search of the menus for this frame starting at the
     * left most menu and moves to the right.
     * 
     * @param menuAction
     *            The type of action we are looking for in the menus.
     * @return The first {@link JMenuItem} object found with the given action
     *         type or null if no menu item is found to have that action type.
     */
    public JMenuItem getMenuByAction(Class<? extends Action> menuAction) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu innerMenu = menuBar.getMenu(i);
            for (int j = 0; j < innerMenu.getItemCount(); j++) {
                JMenuItem item = innerMenu.getItem(j);
                
                if (item == null || item.getAction() == null) continue; //separator found or no action
                
                if (menuAction.isAssignableFrom(item.getAction().getClass())) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Returns the main split pane in the frame. The left panel will be the tree
     * and the right panel will be the current model editor.
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }
    
    /**
     * Returns the top tool bar in the frame.
     */
    public JToolBar getProjectBar() {
        return projectBar;
    }

    public ArchitectSwingSession getCurrentSession() {
        return currentSession;
    }
    
    public EditCriticSettingsAction getShowCriticsManagerAction() {
        return showCriticsManagerAction;
    }
    
    public ArchitectStatusBar getStatusBar() {
        return statusBar;
    }

    public EditLabelAction getEditLabelAction() {
        return editLabelAction;
    }

    private class TabDropTargetListener implements DropTargetListener {
        
        public void dragEnter(DropTargetDragEvent dtde) {
            // don't care
        }

        public void dragExit(DropTargetEvent dte) {
            // don't care
        }

        public void dragOver(DropTargetDragEvent dtde) {
            Point mouseLocation = dtde.getLocation();
            int index = stackedTabPane.indexAtLocation(mouseLocation.x, mouseLocation.y);
            if (index != -1) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction());
            } else {
                dtde.rejectDrag();
            }
        }

        public void drop(DropTargetDropEvent dtde) {
            Point mouseLocation = dtde.getLocation();
            int index = stackedTabPane.indexAtLocation(mouseLocation.x, mouseLocation.y);
            StackedTab tab = stackedTabPane.getTabs().get(index);
            PlayPen playPen = sessionTabs.inverse().get(tab).getPlayPen();
            try {
                if (playPen.addTransferable(dtde.getTransferable(), new Point(playPen.getLocation().x + 100, playPen.getLocation().y + 100), TransferStyles.COPY)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    dtde.dropComplete(true);
                }
            } catch (SQLObjectException e) {
                logger.error(e);
                dtde.rejectDrop();
            } catch (UnsupportedFlavorException e) {
                logger.error(e);
                dtde.rejectDrop();
            } catch (IOException e) {
                logger.error(e);
                dtde.rejectDrop();
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub
            
        }
        
    }

}