package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.ddl.*;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import ca.sqlpower.sql.DBConnectionSpec;

public class ArchitectFrame extends JFrame {

	private static Logger logger = Logger.getLogger(ArchitectFrame.class);

	/**
	 * The ArchitectFrame is a singleton; this is the main instance.
	 */
	protected static ArchitectFrame mainInstance;

	protected SwingUIProject project = null;
	protected ConfigFile configFile = null;
	protected UserSettings prefs = null;
	protected SwingUserSettings sprefs = null;
	protected JToolBar toolBar = null;
	protected JMenuBar menuBar = null;
	protected JSplitPane splitPane = null;
	protected PlayPen playpen = null;
	protected DBTree dbTree = null;
	
	protected Action newProjectAction;
	protected Action openProjectAction;
	protected Action saveProjectAction;
	protected Action saveProjectAsAction;
	protected DeleteSelectedAction deleteSelectedAction;
	protected EditColumnAction editColumnAction;
	protected InsertColumnAction insertColumnAction;
	protected EditTableAction editTableAction;
	protected CreateTableAction createTableAction;
	protected CreateRelationshipAction createRelationshipAction;
	protected EditRelationshipAction editRelationshipAction;
	protected Action exportDDLAction;

	protected Action exitAction = new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};

	/**
	 * Updates the swing settings and then writes all settings to the
	 * config file whenever actionPerformed is invoked.
	 */
	protected Action saveSettingsAction = new AbstractAction("Save Settings") {
			public void actionPerformed(ActionEvent e) {
				if (configFile == null) configFile = ConfigFile.getDefaultInstance();
				try {
					sprefs.setInt(SwingUserSettings.DIVIDER_LOCATION, splitPane.getDividerLocation());
					sprefs.setInt(SwingUserSettings.MAIN_FRAME_X, getLocation().x);
					sprefs.setInt(SwingUserSettings.MAIN_FRAME_Y, getLocation().y);
					sprefs.setInt(SwingUserSettings.MAIN_FRAME_WIDTH, getWidth());
					sprefs.setInt(SwingUserSettings.MAIN_FRAME_HEIGHT, getHeight());

					configFile.write(prefs);
				} catch (ArchitectException ex) {
					logger.error("Couldn't save settings", ex);
				}
			}
		};

	public ArchitectFrame() throws ArchitectException {
		mainInstance = this;

		try {
			ConfigFile cf = ConfigFile.getDefaultInstance();
			prefs = cf.read();
			sprefs = prefs.getSwingSettings();
		} catch (IOException e) {
			throw new ArchitectException("prefs.read", e);
		}

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		// Create actions
		newProjectAction
			 = new AbstractAction("New Project",
								 ASUtils.createJLFIcon("general/New",
													   "New Project",
													   sprefs.getInt(sprefs.ICON_SIZE, 24))) {
			public void actionPerformed(ActionEvent e) {
				try {
					setProject(new SwingUIProject("New Project"));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(ArchitectFrame.this,
												  "Can't create new project: "+ex.getMessage());
					logger.error("Got exception while creating new project", ex);
				}
			}
		};

		openProjectAction
			= new AbstractAction("Open Project...",
								 ASUtils.createJLFIcon("general/Open",
													   "Open Project",
													   sprefs.getInt(sprefs.ICON_SIZE, 24))) {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();
						chooser.addChoosableFileFilter(ASUtils.architectFileFilter);
						int returnVal = chooser.showOpenDialog(ArchitectFrame.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								SwingUIProject project = new SwingUIProject("Loading...");
								project.setFile(chooser.getSelectedFile());
								project.load();
								setProject(project);
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(ArchitectFrame.this,
															  "Can't open project: "+ex.getMessage());
								logger.error("Got exception while opening project", ex);
							}
						}
					}
				};
		
		saveProjectAction 
			= new AbstractAction("Save Project",
								 ASUtils.createJLFIcon("general/Save",
													   "Save Project",
													   sprefs.getInt(sprefs.ICON_SIZE, 24))) {
					public void actionPerformed(ActionEvent e) {
						saveOrSaveAs(false);
					}
				};
		saveProjectAsAction
			= new AbstractAction("Save Project As...",
								 ASUtils.createJLFIcon("general/SaveAs",
													   "Save Project As...",
													   sprefs.getInt(sprefs.ICON_SIZE, 24))) {
				public void actionPerformed(ActionEvent e) {
					saveOrSaveAs(true);
				}
		};
		exportDDLAction = new ExportDDLAction();
		deleteSelectedAction = new DeleteSelectedAction();
		createRelationshipAction = new CreateRelationshipAction();
		editRelationshipAction = new EditRelationshipAction();
		createTableAction = new CreateTableAction();
		editColumnAction = new EditColumnAction();
		insertColumnAction = new InsertColumnAction();
		editTableAction = new EditTableAction();

		menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(newProjectAction));
		fileMenu.add(new JMenuItem(openProjectAction));
		fileMenu.add(new JMenuItem(saveProjectAction));
		fileMenu.add(new JMenuItem(saveProjectAsAction));
		fileMenu.add(new JMenuItem(exportDDLAction));
		fileMenu.add(new JMenuItem(saveSettingsAction));
		fileMenu.add(new JMenuItem(exitAction));
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		toolBar = new JToolBar();
		toolBar.add(newProjectAction);
		toolBar.add(openProjectAction);
		toolBar.add(saveProjectAction);
		toolBar.addSeparator();
		toolBar.add(deleteSelectedAction);
		toolBar.addSeparator();
		toolBar.add(createTableAction);
		toolBar.addSeparator();
		toolBar.add(insertColumnAction);
		toolBar.add(editColumnAction);
		toolBar.addSeparator();
		toolBar.add(createRelationshipAction);
		toolBar.add(editRelationshipAction);
		cp.add(toolBar, BorderLayout.NORTH);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		logger.debug("Added splitpane to content pane");
		splitPane.setDividerLocation
			(sprefs.getInt(SwingUserSettings.DIVIDER_LOCATION,
						   150)); //dbTree.getPreferredSize().width));

		Rectangle bounds = new Rectangle();
		bounds.x = sprefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = sprefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = sprefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = sprefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setProject(new SwingUIProject("New Project"));
	}

	public void setProject(SwingUIProject p) throws ArchitectException {
		this.project = p;
		logger.debug("Setting project to "+project);
		setTitle(project.getName()+" - Power*Architect");
		playpen = project.getPlayPen();
		dbTree = project.getSourceDatabases();

		setupActions();

		splitPane.setLeftComponent(new JScrollPane(dbTree));
		splitPane.setRightComponent(new JScrollPane(playpen));
	}

	/**
	 * Points all the actions to the correct PlayPen and DBTree
	 * instances.  This method is called by setProject.
	 */
	protected void setupActions() {
		deleteSelectedAction.setPlayPen(playpen);
		editColumnAction.setPlayPen(playpen);
		insertColumnAction.setPlayPen(playpen);
		editTableAction.setPlayPen(playpen);
		createTableAction.setPlayPen(playpen);
		createRelationshipAction.setPlayPen(playpen);
		editRelationshipAction.setPlayPen(playpen);
	}

	public static ArchitectFrame getMainInstance() {
		return mainInstance;
	}
	
	public UserSettings getUserSettings() {
		return prefs;
	}

	/**
	 * Creates an ArchitectFrame and sets is visible.  This method is
	 * an acceptable way to launch the Architect application.
	 */
	public static void main(String args[]) throws Exception {
		new ArchitectFrame();
		
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					mainInstance.setVisible(true);
				}
			});
	}

	public void saveOrSaveAs(boolean showChooser) {
		if (project.getFile() == null || showChooser) {
			JFileChooser chooser = new JFileChooser(project.getFile());
			chooser.addChoosableFileFilter(ASUtils.architectFileFilter);
			int response = chooser.showSaveDialog(ArchitectFrame.this);
			if (response != JFileChooser.APPROVE_OPTION) {
				return;
			} else {
				project.setFile(chooser.getSelectedFile());
				project.setName(project.getFile().getName());
				setTitle(project.getFile().getName());
			}
		}
		final ProgressMonitor pm = new ProgressMonitor
			(ArchitectFrame.this, "Saving Project", "", 0, 100);
		new Thread() {
			public void run() {
				try {
					project.save(pm);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog
						(ArchitectFrame.this,
						 "Can't save project: "+ex.getMessage());
					logger.error("Got exception while saving project", ex);
				}
			}
		}.start();
	}
}
