package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.BrowserUtil;
import ca.sqlpower.architect.ConfigFile;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.etl.ExportCSV;
import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.qfa.ExceptionHandler;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.action.CompareDMAction;
import ca.sqlpower.architect.swingui.action.CreateRelationshipAction;
import ca.sqlpower.architect.swingui.action.CreateTableAction;
import ca.sqlpower.architect.swingui.action.DataMoverAction;
import ca.sqlpower.architect.swingui.action.DeleteSelectedAction;
import ca.sqlpower.architect.swingui.action.EditColumnAction;
import ca.sqlpower.architect.swingui.action.EditRelationshipAction;
import ca.sqlpower.architect.swingui.action.EditTableAction;
import ca.sqlpower.architect.swingui.action.ExportDDLAction;
import ca.sqlpower.architect.swingui.action.ExportPLJobXMLAction;
import ca.sqlpower.architect.swingui.action.ExportPLTransAction;
import ca.sqlpower.architect.swingui.action.HelpAction;
import ca.sqlpower.architect.swingui.action.InsertColumnAction;
import ca.sqlpower.architect.swingui.action.PreferencesAction;
import ca.sqlpower.architect.swingui.action.PrintAction;
import ca.sqlpower.architect.swingui.action.ProfilePanelAction;
import ca.sqlpower.architect.swingui.action.ProjectSettingsAction;
import ca.sqlpower.architect.swingui.action.QuickStartAction;
import ca.sqlpower.architect.swingui.action.RedoAction;
import ca.sqlpower.architect.swingui.action.SQLRunnerAction;
import ca.sqlpower.architect.swingui.action.SearchReplaceAction;
import ca.sqlpower.architect.swingui.action.SelectAllAction;
import ca.sqlpower.architect.swingui.action.UndoAction;
import ca.sqlpower.architect.swingui.action.ViewProfileAction;
import ca.sqlpower.architect.swingui.action.ZoomAction;
import ca.sqlpower.architect.undo.UndoManager;

import com.darwinsys.util.PrefsUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class ArchitectFrame extends JFrame {

	private static Logger logger = Logger.getLogger(ArchitectFrame.class);

	/**
	 * The ArchitectFrame is a singleton; this is the main instance.
	 */
	protected static ArchitectFrame mainInstance;

    static final String FORUM_URL = "http://www.sqlpower.ca/forum/";
    static final String DRIVERS_URL = "http://www.sqlpower.ca/forum/posts/list/401.page";

    public static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

	public static final double ZOOM_STEP = 0.25;

	protected final Preferences prefs;

    /**
	 * Tracks whether or not the most recent "save project" operation was
	 * successful.
	 */
	private boolean lastSaveOpSuccessful;

	protected ArchitectSession architectSession = null;
	protected SwingUIProject project = null;
	protected ConfigFile configFile = null;
	protected UserSettings sprefs = null;
	protected JToolBar projectBar = null;
	protected JToolBar ppBar = null;
	protected JMenuBar menuBar = null;
	protected JSplitPane splitPane = null;
	protected PlayPen playpen = null;
	protected DBTree dbTree = null;

	private UndoAction undoAction;
	private RedoAction redoAction;

    private JMenu connectionsMenu;

    private RecentMenu recent;
	protected AboutAction aboutAction;
    protected Action newProjectAction;
	protected Action openProjectAction;
	protected Action saveProjectAction;
	protected Action saveProjectAsAction;
	protected PreferencesAction prefAction;
	protected ProjectSettingsAction projectSettingsAction;
	protected PrintAction printAction;
    protected ProfilePanelAction profileAction;
    protected ViewProfileAction viewProfileAction; //not being used for second architect release
 	protected ZoomAction zoomInAction;
 	protected ZoomAction zoomOutAction;
 	protected Action zoomNormalAction;
 	protected Action zoomAllAction;
 	protected  JComponent contentPane;
	private AutoLayoutAction autoLayoutAction;

	private ArchitectLayout autoLayout;
	// playpen edit actions
	protected EditColumnAction editColumnAction;
	protected InsertColumnAction insertColumnAction;
	protected EditTableAction editTableAction;
	protected DeleteSelectedAction deleteSelectedAction;
	protected CreateTableAction createTableAction;
	protected CreateRelationshipAction createIdentifyingRelationshipAction;
	protected CreateRelationshipAction createNonIdentifyingRelationshipAction;
	protected EditRelationshipAction editRelationshipAction;
	protected SearchReplaceAction searchReplaceAction;
	protected SelectAllAction selectAllAction;

	protected Action exportDDLAction;
	protected Action compareDMAction;
    protected Action dataMoverAction;
	protected ExportPLTransAction exportPLTransAction;
    protected ExportPLJobXMLAction exportPLJobXMLAction;

	protected QuickStartAction quickStartAction;
	protected ArchitectFrameWindowListener afWindowListener;

	protected Action exitAction = new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
	        exit();
	    }
	};

    protected static Action forumAction = new AbstractAction("Support on the Web",
            // Alas this is now static so the size can't be gotten from sprefs...
            ASUtils.createJLFIcon("development/WebComponent","New Project", 16)) {
        public void actionPerformed(ActionEvent evt) {
            try {
                BrowserUtil.launch(FORUM_URL);
            } catch (IOException e) {
                ASUtils.showExceptionDialog(
                        "Could not launch browser for Forum View", e);
            }
        }
    };


	/**
	 * Updates the swing settings and then writes all settings to the
	 * config file whenever actionPerformed is invoked.
	 */
	protected Action saveSettingsAction = new AbstractAction("Save User Preferences") {
	    public void actionPerformed(ActionEvent e) {
	        try {
	            saveSettings();
	        } catch (ArchitectException ex) {
	            logger.error("Couldn't save settings", ex);
	        }
	    }
	};


	/**
	 * You can't create an architect frame using this constructor.  You have to
	 * call {@link #getMainInstance()}.
	 *
	 * @throws ArchitectException
	 */
	private ArchitectFrame() throws ArchitectException {
		synchronized (ArchitectFrame.class) {
			mainInstance = this;
		}
        setIconImage(ASUtils.getFrameIconImage());
	    // close handled by window listener
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    architectSession = ArchitectSessionImpl.getInstance();
	    prefs = PrefsUtils.getUserPrefsNode(architectSession);
	    init();
	}

	/**
	 * Checks if the project is modified, and if so presents the user with the option to save
	 * the existing project.  This is useful to use in actions that are about to get rid of
	 * the currently open project.
	 *
	 * @return True if the project can be closed; false if the project should remain open.
	 */
    protected boolean promptForUnsavedModifications() {
        if (project.isModified()) {
            int response = JOptionPane.showOptionDialog(ArchitectFrame.this,
                    "Your project has unsaved changes", "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] {"Don't Save", "Cancel", "Save"}, "Save");
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

	private void init() throws ArchitectException {
		int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	    CoreUserSettings us;
	    // must be done right away, because a static
	    // initializer in this class effects BeanUtils
	    // behaviour which the XML Digester relies
	    // upon heavily
	    //TypeMap.getInstance();
	    contentPane = (JComponent)getContentPane();

	    ConfigFile cf = ConfigFile.getDefaultInstance();
	    us = cf.read(getArchitectSession());
	    architectSession.setUserSettings(us);
	    sprefs = architectSession.getUserSettings().getSwingSettings();

		while (!us.isPlDotIniPathValid()) {
		    String message;
		    String[] options = new String[] {"Browse", "Create"};
		    if (us.getPlDotIniPath() == null) {
		        message = "location is not set";
		    } else if (new File(us.getPlDotIniPath()).isFile()) {
		        message = "file \n\n\""+us.getPlDotIniPath()+"\"\n\n could not be read";
		    } else {
		        message = "file \n\n\""+us.getPlDotIniPath()+"\"\n\n does not exist";
		    }
		    int choice = JOptionPane.showOptionDialog(null,   // blocking wait
		            "The Architect keeps its list of database connections" +
		            "\nin a file called PL.INI.  Your PL.INI "+message+"." +
		            "\n\nYou can browse for an existing PL.INI file on your system" +
		            "\nor allow the Architect to create a new one in your home directory." +
		            "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
		            "\nan existing PL.INI in your Power*Loader installation directory.",
		            "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);
		    File newPlIniFile;
            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new ArchitectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
		        JFileChooser fc = new JFileChooser();
		        fc.setFileFilter(ASUtils.INI_FILE_FILTER);
		        fc.setDialogTitle("Locate your PL.INI file");
		        int fcChoice = fc.showOpenDialog(null);       // blocking wait
		        if (fcChoice == JFileChooser.APPROVE_OPTION) {
		            newPlIniFile = fc.getSelectedFile();
		        } else {
		            newPlIniFile = null;
		        }
		    } else if (choice == 1) {
		        newPlIniFile = new File(System.getProperty("user.home"), "pl.ini");
		    } else
                throw new ArchitectException("Unexpected return from JOptionPane.showOptionDialog to get pl.ini");

		    if (newPlIniFile != null) try {
		        newPlIniFile.createNewFile();
		        us.setPlDotIniPath(newPlIniFile.getPath());
		    } catch (IOException e1) {
		        logger.error("Caught IO exception while creating empty PL.INI at \""
		                +newPlIniFile.getPath()+"\"", e1);
		        JOptionPane.showMessageDialog(null, "Failed to create file \""+newPlIniFile.getPath()+"\":\n"+e1.getMessage(),
		                "Error", JOptionPane.ERROR_MESSAGE);
		    }
		}

		// Create actions
		aboutAction = new AboutAction();

        Action helpAction = new HelpAction();

		newProjectAction
			 = new AbstractAction("New Project",
					      ASUtils.createJLFIcon("general/New","New Project",sprefs.getInt(SwingUserSettings.ICON_SIZE, 24))) {
			public void actionPerformed(ActionEvent e) {
			    if (promptForUnsavedModifications()) {
			        try {
			        	prefs.putInt(SwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
			        	closeProject(getProject());
			            setProject(new SwingUIProject("New Project"), false);
			            logger.debug("Glass pane is "+getGlassPane());
			        } catch (Exception ex) {
			            JOptionPane.showMessageDialog(ArchitectFrame.this,
			                    "Can't create new project: "+ex.getMessage());
			            logger.error("Got exception while creating new project", ex);
			        }
			    }
			}
		};
		newProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, "New");
		newProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask));

		recent = new RecentMenu(this) {
			@Override
			public void loadFile(String fileName) throws IOException {
				File f = new File(fileName);

				LoadFileWorker worker;
				try {
					worker = new LoadFileWorker(f,null);
					new Thread(worker).start();
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(
							ArchitectFrame.this,
							"File not found: "+f.getPath());
				} catch (Exception e1) {
					ASUtils.showExceptionDialog(
							"Error loading file", e1);
				}
			}
		};

		openProjectAction = new OpenProjectAction(recent);

		saveProjectAction
			= new AbstractAction("Save Project",
								 ASUtils.createJLFIcon("general/Save",
													   "Save Project",
													   sprefs.getInt(SwingUserSettings.ICON_SIZE, 24))) {
					public void actionPerformed(ActionEvent e) {
						saveOrSaveAs(false, true);
					}
				};
		saveProjectAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Save");
		saveProjectAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));

		saveProjectAsAction
			= new AbstractAction("Save Project As...",
								 ASUtils.createJLFIcon("general/SaveAs",
													   "Save Project As...",
													   sprefs.getInt(SwingUserSettings.ICON_SIZE, 24))) {
					public void actionPerformed(ActionEvent e) {
						saveOrSaveAs(true, true);
					}
				};
		saveProjectAsAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Save As");

		prefAction = new PreferencesAction();
		projectSettingsAction = new ProjectSettingsAction();
		projectSettingsAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, accelMask));
		printAction = new PrintAction();
		printAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMask));

		zoomInAction = new ZoomAction(ZOOM_STEP);
		zoomOutAction = new ZoomAction(ZOOM_STEP * -1.0);

		zoomNormalAction
			= new AbstractAction("Reset Zoom",
								 ASUtils.createJLFIcon("general/Zoom",
													   "Reset Zoom",
													   sprefs.getInt(SwingUserSettings.ICON_SIZE, 24))) {
					public void actionPerformed(ActionEvent e) {
						playpen.setZoom(1.0);
					}
				};
		zoomNormalAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Reset Zoom");


		zoomAllAction = new AbstractAction("Zoom to fit",
							 ASUtils.createJLFIcon("general/Zoom",
												   "Reset Zoom",
												   sprefs.getInt(SwingUserSettings.ICON_SIZE, 24))) {
				public void actionPerformed(ActionEvent e) {
					Rectangle rect = null;
					if ( playpen != null ) {
						for (int i = 0; i < playpen.getContentPane().getComponentCount(); i++) {
							PlayPenComponent ppc = playpen.getContentPane().getComponent(i);
							if ( rect == null ) {
								rect = new Rectangle(ppc.getLocation(),ppc.getSize());
							}
							else {
								rect.add(ppc.getBounds());
							}
						}
					}

					if ( rect == null )
						return;

					double zoom = Math.min(playpen.getViewportSize().getHeight()/rect.height,
									playpen.getViewportSize().getWidth()/rect.width);
					zoom *= 0.90;

					playpen.setZoom(zoom);
					playpen.scrollRectToVisible(playpen.zoomRect(rect));
				}
			};
		zoomAllAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom to fit");

		undoAction = new UndoAction();
		redoAction = new RedoAction();
		autoLayoutAction = new AutoLayoutAction();
		autoLayout = new FruchtermanReingoldForceLayout();
		autoLayoutAction.setLayout(autoLayout);
		exportDDLAction = new ExportDDLAction();
        compareDMAction = new CompareDMAction();
        dataMoverAction = new DataMoverAction(this, getArchitectSession());

		exportPLTransAction = new ExportPLTransAction(this) {
		    @Override
		    public void actionPerformed(ActionEvent e) {
                try {
                    setExportingTables(getProject().getTargetDatabase().getTables());
                    super.actionPerformed(e);
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog(
                            "Error Creating List of Tables to Export",
                            ex);
                }
		    }
        };
        exportPLJobXMLAction = new ExportPLJobXMLAction();
		quickStartAction = new QuickStartAction();
        Action exportCSVAction = new AbstractAction("Export CSV File") {

            public void actionPerformed(ActionEvent e) {
                try {
                    ExportCSV export = new ExportCSV(getProject().getPlayPen().getDatabase().getTables());

                    File file = null;

                    JFileChooser fileDialog = new JFileChooser();
                    fileDialog.setSelectedFile(new File("map.csv"));

                    if (fileDialog.showSaveDialog(ArchitectFrame.getMainInstance()) == JFileChooser.APPROVE_OPTION){
                        file = fileDialog.getSelectedFile();
                    } else {
                        return;
                    }

                    FileWriter output = null;
                    output = new FileWriter(file);
                    output.write(export.getCSVMapping());
                    output.flush();
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                } catch (ArchitectException e1) {
                    throw new ArchitectRuntimeException(e1);
                }
            }
        };
        Action mappingReportAction = new AbstractAction("Visual Mapping Report") {

            // TODO convert this to an architect pane
            public void actionPerformed(ActionEvent e) {
                try {
                    final MappingReport mr ;
                    final List<SQLTable> selectedTables;
                    if (playpen.getSelectedTables().size() == 0) {
                        selectedTables = new ArrayList(playpen.getTables());
                    } else {
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ArchitectFrame.getMainInstance(),"View only the "+playpen.getSelectedTables().size()+" selected tables","Show Mapping",JOptionPane.YES_NO_OPTION)) {
                            selectedTables = new ArrayList<SQLTable>();
                            for(TablePane tp: playpen.getSelectedTables()) {
                                selectedTables.add(tp.getModel());
                            }
                        } else {
                            selectedTables = new ArrayList(playpen.getTables());
                        }
                    }
                    mr = new MappingReport(selectedTables);

                    final JFrame f = new JFrame("Mapping Report");
                    f.setIconImage(ASUtils.getFrameIconImage());

                    // You call this a radar?? -- No sir, we call it Mr. Panel.
                    JPanel mrPanel = new JPanel() {
                        protected void paintComponent(java.awt.Graphics g) {

                            super.paintComponent(g);
                            try {
                                mr.drawHighLevelReport((Graphics2D) g,null);
                            } catch (ArchitectException e1) {
                                logger.error("ArchitectException while generating mapping diagram", e1);
                                ASUtils.showExceptionDialog(
                                        "Couldn't generate mapping diagram", e1);
                            }
                        }
                    };
                    mrPanel.setDoubleBuffered(true);
                    mrPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    mrPanel.setPreferredSize(mr.getRequiredSize());
                    mrPanel.setOpaque(true);
                    mrPanel.setBackground(Color.WHITE);
                    ButtonBarBuilder buttonBar = new ButtonBarBuilder();
                    JButton csv = new JButton(new AbstractAction(){

                        public void actionPerformed(ActionEvent e) {
                            try {
                                ExportCSV export = new ExportCSV(selectedTables);

                                File file = null;

                                JFileChooser fileDialog = new JFileChooser();
                                fileDialog.setSelectedFile(new File("map.csv"));

                                if (fileDialog.showSaveDialog(f) == JFileChooser.APPROVE_OPTION){
                                    file = fileDialog.getSelectedFile();
                                } else {
                                    return;
                                }

                                FileWriter output = null;
                                output = new FileWriter(file);
                                output.write(export.getCSVMapping());
                                output.flush();
                            } catch (IOException e1) {
                                throw new RuntimeException(e1);
                            } catch (ArchitectException e1) {
                                throw new ArchitectRuntimeException(e1);
                            }
                        }

                    });
                    csv.setText("Export CSV");
                    buttonBar.addGriddedGrowing(csv);
                    ExportPLTransAction plTransaction = new ExportPLTransAction(f);
                    JButton pl = new JButton(plTransaction);
                    plTransaction.setExportingTables(selectedTables);
                    pl.setText("Export PL Transaction");
                    buttonBar.addRelatedGap();
                    buttonBar.addGriddedGrowing(pl);
                    JButton close = new JButton(new AbstractAction(){

                        public void actionPerformed(ActionEvent e) {
                            f.dispose();
                        }

                    });
                    close.setText("Close");
                    buttonBar.addRelatedGap();
                    buttonBar.addGriddedGrowing(close);
                    JPanel basePane = new JPanel(new BorderLayout(5,5));
                    basePane.add(new JScrollPane(mrPanel),BorderLayout.CENTER);
                    basePane.add(buttonBar.getPanel(),BorderLayout.SOUTH);
                    f.setContentPane(basePane);
                    f.pack();
                    f.setLocationRelativeTo(ArchitectFrame.getMainInstance());
                    f.setVisible(true);
                } catch (ArchitectException e1) {
                    throw new ArchitectRuntimeException(e1);
                }
            }
        };
		deleteSelectedAction = new DeleteSelectedAction();
		createIdentifyingRelationshipAction = new CreateRelationshipAction(true);
		createNonIdentifyingRelationshipAction = new CreateRelationshipAction(false);
		editRelationshipAction = new EditRelationshipAction();
		createTableAction = new CreateTableAction();
		editColumnAction = new EditColumnAction();
		insertColumnAction = new InsertColumnAction();
		editTableAction = new EditTableAction();
		searchReplaceAction = new SearchReplaceAction();
		searchReplaceAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, accelMask));
		selectAllAction = new SelectAllAction();
		selectAllAction.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask));

        /* profiling stuff */
        profileAction = new ProfilePanelAction();
        //viewProfileAction = new ViewProfileAction(); not being used for second architect release

		menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(newProjectAction);
		fileMenu.add(openProjectAction);
		fileMenu.add(recent);
		fileMenu.addSeparator();
		fileMenu.add(saveProjectAction);
		fileMenu.add(saveProjectAsAction);
		fileMenu.add(printAction);
		fileMenu.addSeparator();
		if (!MAC_OS_X) {
		    fileMenu.add(prefAction);
		}
		fileMenu.add(saveSettingsAction);
		fileMenu.add(projectSettingsAction);
		if (!MAC_OS_X) {
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

		JMenu etlMenu = new JMenu("ETL");
		etlMenu.setMnemonic('l');
		JMenu etlSubmenuOne = new JMenu("Power*Loader");
		etlSubmenuOne.add(exportPLTransAction);

		// Todo add in ability to run the engine from the architect
        /*
            Action runPL = new RunPLAction();
            runPL.putValue(Action.NAME,"Run Power*Loader");
		    etlSubmenuOne.add(runPL);
        */

		etlSubmenuOne.add(exportPLJobXMLAction);

		etlSubmenuOne.add(quickStartAction);
		etlMenu.add(etlSubmenuOne);
        etlMenu.add(exportCSVAction);
        etlMenu.add(mappingReportAction);
		menuBar.add(etlMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(exportDDLAction);
		toolsMenu.add(compareDMAction);
        toolsMenu.add(new SQLRunnerAction(ArchitectFrame.getMainInstance()));

        // disabled for 0.9.0 release (still has too many bugs to work out)
        toolsMenu.add(dataMoverAction);

		menuBar.add(toolsMenu);

        JMenu profileMenu = new JMenu("Profile");
        profileMenu.setMnemonic('p');
        profileMenu.add(profileAction);
        //profileMenu.add(viewProfileAction);not being used for second architect release
        menuBar.add(profileMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
        helpMenu.add(forumAction);
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

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		cp.add(splitPane, BorderLayout.CENTER);
		logger.debug("Added splitpane to content pane");

		splitPane.setDividerLocation(prefs.getInt(SwingUserSettings.DIVIDER_LOCATION,150));

		Rectangle bounds = new Rectangle();
		bounds.x = prefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = prefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = prefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = prefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
		addWindowListener(afWindowListener = new ArchitectFrameWindowListener());
        getUserSettings().getSwingSettings().setBoolean(SwingUserSettings.SHOW_WELCOMESCREEN,
            prefs.getBoolean(SwingUserSettings.SHOW_WELCOMESCREEN, true));

		setProject(new SwingUIProject("New Project"), true);
	}

	public void setProject(SwingUIProject p, boolean showWelcome) throws ArchitectException {
		this.project = p;
		logger.debug("Setting project to "+project);
		setTitle(project.getName()+" - Power*Architect");
		playpen = project.getPlayPen();
		dbTree = project.getSourceDatabases();

		setupActions();

		setupConnectionsMenu();

		splitPane.setLeftComponent(new JScrollPane(dbTree));
		splitPane.setRightComponent(new JScrollPane(playpen));

        final JCheckBox showPrefsAgain;
        if (showWelcome &&
                getUserSettings().getSwingSettings().getBoolean(SwingUserSettings.SHOW_WELCOMESCREEN, true)) {
            JComponent welcomePanel = WelcomeScreen.getPanel();
            final JDialog d = new JDialog(this, "Welcome to the Power*Architect");
            d.setLayout(new BorderLayout(12, 12));
            ((JComponent) d.getContentPane()).setBorder(Borders.DIALOG_BORDER);

            showPrefsAgain = new JCheckBox("Show this Welcome Screen in future");
            showPrefsAgain.setSelected(true);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getUserSettings().getSwingSettings().setBoolean(SwingUserSettings.SHOW_WELCOMESCREEN,
                        showPrefsAgain.isSelected());
                    d.dispose();
                }
            });

            d.add(welcomePanel, BorderLayout.CENTER);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(showPrefsAgain, BorderLayout.WEST);
            bottomPanel.add(closeButton, BorderLayout.EAST);
            d.add(bottomPanel, BorderLayout.SOUTH);
            d.getRootPane().setDefaultButton(closeButton);
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }

		splitPane.setDividerLocation(prefs.getInt(SwingUserSettings.DIVIDER_LOCATION,150));
	}

	public SwingUIProject getProject() {
		return this.project;
	}

	/**
	 * Sets up the items in the Connections menu to operate on the current dbtree.
	 */
	protected void setupConnectionsMenu() {
	    connectionsMenu.removeAll();
	    dbTree.refreshMenu(null);
	    connectionsMenu.add(dbTree.connectionsMenu);
	    connectionsMenu.add(new JMenuItem(dbTree.dbcsPropertiesAction));
	    connectionsMenu.add(new JMenuItem(dbTree.removeDBCSAction));
	}

	/**
	 * Points all the actions to the correct PlayPen and DBTree
	 * instances.  This method is called by setProject.
	 * @throws ArchitectException if the undo manager can't get the playpen's children on the listener list
	 */
	protected void setupActions() throws ArchitectException {
		// playpen actions
		aboutAction.setPlayPen(playpen);
		printAction.setPlayPen(playpen);
		deleteSelectedAction.setPlayPen(playpen);
		editColumnAction.setPlayPen(playpen);
		insertColumnAction.setPlayPen(playpen);
		editTableAction.setPlayPen(playpen);
		searchReplaceAction.setPlayPen(playpen);
		selectAllAction.setPlayPen(playpen);
		createTableAction.setPlayPen(playpen);
		createIdentifyingRelationshipAction.setPlayPen(playpen);
		createNonIdentifyingRelationshipAction.setPlayPen(playpen);
		editRelationshipAction.setPlayPen(playpen);
		exportPLTransAction.setExportingTables(playpen.getTables());
        exportPLJobXMLAction.setPlayPen(playpen);
		zoomInAction.setPlayPen(playpen);
		zoomOutAction.setPlayPen(playpen);
		autoLayoutAction.setPlayPen(playpen);
        //viewProfileAction.setPlayPen(playpen);not being used for second architect release

		undoAction.setManager(project.getUndoManager());
		redoAction.setManager(project.getUndoManager());

		// dbtree actions
		editColumnAction.setDBTree(dbTree);
		insertColumnAction.setDBTree(dbTree);
		editRelationshipAction.setDBTree(dbTree);
		deleteSelectedAction.setDBTree(dbTree);
		editTableAction.setDBTree(dbTree);
		searchReplaceAction.setDBTree(dbTree);
		profileAction.setDBTree(dbTree);
		profileAction.setProfileManager(project.getProfileManager());
		profileAction.setDialog(project.getProfileDialog());
        //viewProfileAction.setProfileManager(project.getProfileManager());not being used for second architect release

		prefAction.setArchitectFrame(this);
		projectSettingsAction.setArchitectFrame(this);
	}

	public static synchronized ArchitectFrame getMainInstance() {
		if (mainInstance == null) {
			try {
				new ArchitectFrame();
			} catch (ArchitectException e) {
				throw new RuntimeException("Couldn't create ArchitectFrame instance!");
			}
		}
		return mainInstance;
	}

	/**
	 * Convenience method for getArchitectSession().getUserSettings().
	 */
	public CoreUserSettings getUserSettings() {
		return architectSession.getUserSettings();
	}

	public ArchitectSession getArchitectSession() {
		return architectSession;
	}

	/**
	 * Determine if either create relationship action is currently active.
	 */
	public boolean createRelationshipIsActive () {
		if (createIdentifyingRelationshipAction.isActive()) return true;
		if (createNonIdentifyingRelationshipAction.isActive()) return true;
		return false;
	}

	private class LoadFileWorker extends ArchitectSwingWorker {
		InputStream in;
		SwingUIProject project;
        File file;
        RecentMenu recent;
		/**
         * Load file worker creates a new worker and opens the given file.
         *
         * @param file  this file gets opened in the constructor
         * @param recent optional recent menu in which to add the file
         * @throws ArchitectException when the project creation fails.
         * @throws FileNotFoundException if file doesn't exist
		 */
		public LoadFileWorker(File file,RecentMenu recent) throws ArchitectException, FileNotFoundException {
				closeProject(getProject());
				project = new SwingUIProject("Loading...");
				project.setFile(file);
                this.file = file;
                this.recent = recent;
				in = new BufferedInputStream
				(new ProgressMonitorInputStream
						(ArchitectFrame.this,
								"Reading " + file.getName(),
								new FileInputStream(file)));
		}

		@Override
		public void doStuff() throws IOException, ArchitectException {
			project.load(in);
            if (recent != null) {
                recent.putRecentFileName(file.getAbsolutePath());
            }
		}

		@Override
		public void cleanup() throws ArchitectException {
            setProject(project, false);
            ((SQLObject) project.getSourceDatabases().getModel().getRoot()).fireDbStructureChanged();
        	try {
        		if (in != null) {
        			in.close();
        		}
        	} catch (IOException ie) {
        		logger.error("got exception while closing project file", ie);
        	}

		}
	}

	private class OpenProjectAction extends AbstractAction {
		RecentMenu recent;
		private OpenProjectAction(RecentMenu recent) {
			super("Open Project...", ASUtils.createJLFIcon("general/Open",
					   "Open Project",
					   sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
			this.recent = recent;
			putValue(AbstractAction.SHORT_DESCRIPTION, "Open");
			putValue(AbstractAction.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_O,
							Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e) {
		    if (promptForUnsavedModifications()) {
		    	prefs.putInt(SwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
		        JFileChooser chooser = new JFileChooser();
		        chooser.addChoosableFileFilter(ASUtils.ARCHITECT_FILE_FILTER);
		        int returnVal = chooser.showOpenDialog(ArchitectFrame.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	File f = chooser.getSelectedFile();
		        	LoadFileWorker worker;
					try {
						worker = new LoadFileWorker(f,recent);
						new Thread(worker).start();
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(
								ArchitectFrame.this,
								"File not found: "+f.getPath());
					} catch (Exception e1) {
						ASUtils.showExceptionDialog(
								"Error loading file", e1);
					}
		        }
		    }
		}


	}

	class ArchitectFrameWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			exit();
		}
	}

	public void saveSettings() throws ArchitectException {
		if (configFile == null) configFile = ConfigFile.getDefaultInstance();

        CoreUserSettings us = getUserSettings();

        /** These are saved directly in java.util.Preferences.
         * XXX Eventually we should save almost everything there, except
         * the PL.INI contents that must be shared with other non-Java programs.
         */
		prefs.putInt(SwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
		prefs.putInt(SwingUserSettings.MAIN_FRAME_X, getLocation().x);
		prefs.putInt(SwingUserSettings.MAIN_FRAME_Y, getLocation().y);
		prefs.putInt(SwingUserSettings.MAIN_FRAME_WIDTH, getWidth());
		prefs.putInt(SwingUserSettings.MAIN_FRAME_HEIGHT, getHeight());
        prefs.putBoolean(SwingUserSettings.SHOW_WELCOMESCREEN,
                us.getSwingSettings().getBoolean(SwingUserSettings.SHOW_WELCOMESCREEN, true));

		configFile.write(getArchitectSession());

		try {
            us.getPlDotIni().write(new File(us.getPlDotIniPath()));
        } catch (IOException e) {
            logger.error("Couldn't save PL.INI file!", e);
        }
	}

	/**
	 * Calling this method quits the application and terminates the
	 * JVM.
	 */
	public void exit() {
		if (getProject().isSaveInProgress()) {
			// project save is in progress, don't allow exit
	        JOptionPane.showMessageDialog(this, "Project is saving, cannot exit the Power Architect.  Please wait for the save to finish, and then try again.",
	                "Warning", JOptionPane.WARNING_MESSAGE);
	        return;
		}

	    if (promptForUnsavedModifications()) {
	        try {
	        	closeProject(getProject());
	            saveSettings();
	        } catch (ArchitectException e) {
	            logger.error("Couldn't save settings: "+e);
	        }
	        System.exit(0);
	    }
	}

	/**
	 * Creates an ArchitectFrame and sets it visible.  This method is
	 * an acceptable way to launch the Architect application.
	 */
	public static void main(String args[]) throws ArchitectException {

        ArchitectUtils.startup();

        System.setProperty("apple.laf.useScreenMenuBar", "true");

		ArchitectUtils.configureLog4j();


       String architectFileArg = null;
       final File openFile;
        if (args.length > 0) {
            architectFileArg = args[0];
            openFile = new File(architectFileArg);
        } else {
            openFile = null;
        }
		getMainInstance();


		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {

		        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		        // this doesn't appear to have any effect on the motion threshold
		        // in the Playpen, but it does seem to work on the DBTree...
		        logger.debug("current motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));
		        System.setProperty("awt.dnd.drag.threshold","10");
		        logger.debug("new motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));

		        getMainInstance().macOSXRegistration();

		        getMainInstance().setVisible(true);
		        LoadFileWorker worker;
		        if (openFile != null) {
		            try {
		                worker = getMainInstance().new LoadFileWorker(openFile,getMainInstance().recent);
		                new Thread(worker).start();
		            } catch (FileNotFoundException e1) {
		                JOptionPane.showMessageDialog(
		                        getMainInstance(),
		                        "File not found: "+openFile.getPath());
		            } catch (Exception e1) {
		                ASUtils.showExceptionDialog(
		                        "Error loading file", e1);
		            }
		        }
		    }
		});
	}

    /**
     * Registers this application in Mac OS X if we're running on that platform.
     *
     * <p>This code came from Apple's "OS X Java Adapter" example.
     */
    private void macOSXRegistration() {
        if (MAC_OS_X) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter");

                // The main registration method.  Takes quitAction, prefsAction, aboutAction.
                Class[] defArgs = { Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                if (registerMethod != null) {
                    Object[] args = { exitAction, prefAction, aboutAction };
                    registerMethod.invoke(osxAdapter, args);
                }

                // The enable prefs method.  Takes a boolean.
                defArgs = new Class[] { boolean.class };
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                if (prefsEnableMethod != null) {
                    Object args[] = {Boolean.TRUE};
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:");
                e.printStackTrace();
            }
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
		if (project.getFile() == null || showChooser) {
			JFileChooser chooser = new JFileChooser(project.getFile());
			chooser.addChoosableFileFilter(ASUtils.ARCHITECT_FILE_FILTER);
			int response = chooser.showSaveDialog(ArchitectFrame.this);
			if (response != JFileChooser.APPROVE_OPTION) {
				return false;
			} else {
				File file = chooser.getSelectedFile();
				if (!file.getPath().endsWith(".architect")) {
					file = new File(file.getPath()+".architect");
				}
				if (file.exists()) {
				    response = JOptionPane.showConfirmDialog(
				            ArchitectFrame.this,
				            "The file\n\n"+file.getPath()+"\n\nalready exists. Do you want to overwrite it?",
				            "File Exists", JOptionPane.YES_NO_OPTION);
				    if (response == JOptionPane.NO_OPTION) {
				        return saveOrSaveAs(true, separateThread);
				    }
				}
				project.setFile(file);
				String projName = file.getName().substring(0, file.getName().length()-".architect".length());
				project.setName(projName);
				setTitle(projName);
			}
		}
		final boolean finalSeparateThread = separateThread;
		final ProgressMonitor pm = new ProgressMonitor
			(ArchitectFrame.this, "Saving Project", "", 0, 100);

		Runnable saveTask = new Runnable() {
			public void run() {
				try {
					lastSaveOpSuccessful = false;
					project.setSaveInProgress(true);
					project.save(finalSeparateThread ? pm : null);
					lastSaveOpSuccessful = true;
					JOptionPane.showMessageDialog(ArchitectFrame.this, "Save successful");
				} catch (Exception ex) {
					lastSaveOpSuccessful = false;
					JOptionPane.showMessageDialog
						(ArchitectFrame.this,
						 "Can't save project: "+ex.getMessage());
					logger.error("Got exception while saving project", ex);
				} finally {
					project.setSaveInProgress(false);
				}
			}
		};
		if (separateThread) {
		    new Thread(saveTask).start();
		    return true; // this is an optimistic lie
		} else {
		    saveTask.run();
		    return lastSaveOpSuccessful;
		}
	}
	/*
	 * Close database connections and files.
	 */
	protected void closeProject(SwingUIProject project) {
		// close connections
		Iterator it = project.getSourceDatabases().getDatabaseList().iterator();
		while (it.hasNext()) {
			SQLDatabase db = (SQLDatabase) it.next();
			logger.debug ("closing connection: " + db.getName());
			db.disconnect();
		}
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
		// XXX: Matt may have had other ideas; not sure what they were
		return getProject().getUndoManager();
	}

	public UserSettings getSwingUserSettings() {
		return sprefs;
	}

	public UserSettings getSprefs() {
		return sprefs;
	}

	public void setSprefs(UserSettings sprefs) {
		this.sprefs = sprefs;
	}

	public Action getNewProjectAction() {
		return newProjectAction;
	}

	public void setNewProjectAction(Action newProjectAction) {
		this.newProjectAction = newProjectAction;
	}

	public Preferences getPrefs() {
		return prefs;
	}

	public ZoomAction getZoomInAction() {
		return zoomInAction;
	}

	public ZoomAction getZoomOutAction() {
		return zoomOutAction;
	}
}
