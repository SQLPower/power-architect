package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.qfa.ExceptionHandler;

import com.jgoodies.forms.factories.Borders;

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
     *  FIXME: Fix LoadFileWorker class
     */
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
        public LoadFileWorker(File file, RecentMenu recent) throws ArchitectException, FileNotFoundException {
                /*project = new SwingUIProject("Loading...");
                project.setFile(file);
                this.file = file;
                this.recent = recent;
                in = new BufferedInputStream(
                    new ProgressMonitorInputStream(
                         null,
                         "Reading " + file.getName(),
                         new FileInputStream(file)));*/
        }

        @Override
        public void doStuff() throws IOException, ArchitectException {
            /*project.load(in, userSettings.getPlDotIni());
            if (recent != null) {
                recent.putRecentFileName(file.getAbsolutePath());
            }*/
        }

        @Override
        public void cleanup() throws ArchitectException {
            /*if (getDoStuffException() != null) {
                JOptionPane.showMessageDialog(null,
                        "Cannot open project file '" + project.getName() + ".architect': \n" + getDoStuffException().getMessage());
                logger.error("Got exception while opening a project", getDoStuffException());
            } else {
                createSession(project);
                ((SQLObject) project.getSourceDatabases().getModel().getRoot()).fireDbStructureChanged();
            }
            
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ie) {
                logger.error("got exception while closing project file", ie);
            }*/
        }
    }

    /**
     * The preferences node that user-specific preferences are stored in.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(ArchitectSwingSessionContextImpl.class);

    /**
     * A more structured interface to the prefs node.  Might be going away soon.
     */
    CoreUserSettings userSettings;
    
    /**
     * The menu of recently-opened project files on this system.
     */
    private final RecentMenu recent;

    /**
     * All live sessions that exist in (and were created by) this conext.  Sessions
     * will be removed from this list when they fire their sessionClosing lifecycle
     * event.
     */
    private final Collection<ArchitectSwingSession> sessions;

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
        sessions = new HashSet<ArchitectSwingSession>();
        
        ArchitectUtils.startup();

        System.setProperty("apple.laf.useScreenMenuBar", "true");

        ArchitectUtils.configureLog4j();

        // this doesn't appear to have any effect on the motion threshold
        // in the Playpen, but it does seem to work on the DBTree...
        logger.debug("current motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));
        System.setProperty("awt.dnd.drag.threshold","10");
        logger.debug("new motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

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
                            null,
                            "File not found: "+f.getPath());
                } catch (Exception e1) {
                    ASUtils.showExceptionDialog(
                            "Error loading file", e1);
                }
            }
        };
        
        userSettings = new CoreUserSettings(getPrefs());

        while (!userSettings.isPlDotIniPathValid()) {
            String message;
            String[] options = new String[] {"Browse", "Create"};
            if (userSettings.getPlDotIniPath() == null) {
                message = "location is not set";
            } else if (new File(userSettings.getPlDotIniPath()).isFile()) {
                message = "file \n\n\""+userSettings.getPlDotIniPath()+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+userSettings.getPlDotIniPath()+"\"\n\n does not exist";
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
                userSettings.setPlDotIniPath(newPlIniFile.getPath());
            } catch (IOException e1) {
                logger.error("Caught IO exception while creating empty PL.INI at \""
                        +newPlIniFile.getPath()+"\"", e1);
                JOptionPane.showMessageDialog(null, "Failed to create file \""+newPlIniFile.getPath()+"\":\n"+e1.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Creates a new session with an empty project.
     */
    public ArchitectSwingSession createSession() throws ArchitectException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called on the Swing Event Dispatch Thread.");
        }
        ArchitectSwingSession session = createSessionImpl("New Project");
        return session;
    }
    
    /**
     * Launches a worker thread to do the actual loading, which ultimately
     * calls {@link #createSessionImpl(String)} to create the session.
     * @throws IOException 
     */
    public void createSession(File projectFile, boolean showGUI) throws ArchitectException, IOException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called on the Swing Event Dispatch Thread.");
        }
        
        ArchitectSwingSession session = createSession();
        
        SwingUIProject project = new SwingUIProject(session); 
        project.load(new BufferedInputStream(new FileInputStream(projectFile)), session.getUserSettings().getPlDotIni());    
        
//        LoadFileWorker worker = new LoadFileWorker(projectFile, recent);
//        new Thread(worker).start();
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#createSession(ca.sqlpower.architect.swingui.SwingUIProject)
     */
    public ArchitectSwingSession createSession(String projectName) throws ArchitectException {
        return createSessionImpl(projectName);
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
     */
    private ArchitectSwingSession createSessionImpl(String projectName) throws ArchitectException {
        logger.debug("About to create a new session for project \"" + projectName + "\"");
        ArchitectSwingSessionImpl session = new ArchitectSwingSessionImpl(this, projectName);
        sessions.add(session);
        session.addSessionLifecycleListener(this);
        
        logger.debug("Creating the Architect frame...");
        session.init();
        
        if (sessions.size() == 1) {
            showWelcomeScreen(session.getArchitectFrame());
        }
        
        return session;
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#isMacOSX()
     */
    public boolean isMacOSX() {
        return MAC_OS_X;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#getRecentMenu()
     */
    public RecentMenu getRecentMenu() {
        return recent;
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#getPrefs()
     */
    public Preferences getPrefs() {
        return prefs;
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.swingui.ArchitectSwingSessionContext#getUserSettings()
     */
    public CoreUserSettings getUserSettings() {
        return userSettings;
    }

    public Collection<ArchitectSwingSession> getSessions() {
        return Collections.unmodifiableCollection(sessions);
    }
    
    private void showWelcomeScreen(Component dialogOwner) {
        // should almost certainly move this into the swing context
        final JCheckBox showPrefsAgain;
        if (getUserSettings().getSwingSettings().getBoolean(SwingUserSettings.SHOW_WELCOMESCREEN, true)) {
            JComponent welcomePanel = WelcomeScreen.getPanel();
            final JDialog d = ASUtils.makeOwnedDialog(dialogOwner, "Welcome to the Power*Architect");
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
            d.setLocationRelativeTo(dialogOwner);
            d.setVisible(true);
        }
    }
}
