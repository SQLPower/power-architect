package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ArchitectFrame extends JFrame {

	private static Logger logger = Logger.getLogger(ArchitectFrame.class);

	/**
	 * The ArchitectFrame is a singleton; this is the main instance.
	 */
	protected static ArchitectFrame mainInstance;

	protected ConfigFile configFile = null;
	protected UserSettings prefs = null;
	protected SwingUserSettings sprefs = null;
	protected JToolBar toolBar = null;
	protected JMenuBar menuBar = null;
	protected JSplitPane splitPane = null;
	protected JPanel playpen = null;
	protected JTree dbTree = null;
	
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
		try {
			ConfigFile cf = ConfigFile.getDefaultInstance();
			prefs = cf.read();
			sprefs = prefs.getSwingSettings();
		} catch (IOException e) {
			throw new ArchitectException("prefs.read", e);
		}

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		toolBar = new JToolBar();
		toolBar.add(new JButton(saveSettingsAction));
		cp.add(toolBar, BorderLayout.NORTH);

		menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(saveSettingsAction));
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		playpen = new PlayPen(SQLDatabase.getPlayPenInstance());
		dbTree = new DBTree(prefs.getConnections());
		((SQLObject) dbTree.getModel().getRoot()).addChild(SQLDatabase.getPlayPenInstance());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
								   new JScrollPane(dbTree),
								   new JScrollPane(playpen));
		cp.add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation
			(sprefs.getInt(SwingUserSettings.DIVIDER_LOCATION,
						   dbTree.getPreferredSize().width));
		Rectangle bounds = new Rectangle();
		bounds.x = sprefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = sprefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = sprefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = sprefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		mainInstance = new ArchitectFrame();
		mainInstance.setVisible(true);
	}
	
}
