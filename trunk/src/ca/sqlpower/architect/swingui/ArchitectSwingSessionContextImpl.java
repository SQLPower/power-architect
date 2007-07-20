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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.qfa.ExceptionHandler;
import ca.sqlpower.architect.swingui.action.OpenProjectAction;

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
                OpenProjectAction.openAsynchronously(ArchitectSwingSessionContextImpl.this, f);
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
     * Loads the XML project description from the input stream,
     * optionally creating the GUI for you.
     * <p>
     * <b>Important Note:</b> If you set showGUI to true, this method
     * must be called on the Swing Event Dispatch Thread.  If this is
     * not possible or practical, call this method with showGUI false,
     * then call {@link ArchitectSwingSession#initGUI()} on the returned
     * session using the event dispatch thread some time later on.
     * @throws IOException If the file is not found or can't be read.
     * @throws ArchitectException if there is some problem with the file
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    public ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws ArchitectException, IOException {

        ArchitectSwingSession session = createSessionImpl("Loading...", false);
        
        session.getProject().load(in, session.getUserSettings().getPlDotIni());
        
        if (showGUI) {
            session.initGUI();
        }
        
        return session;
    }
    
    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession() throws ArchitectException {
        return createSession(true);
    }

    /* javadoc inherited from interface */
    public ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException {
        return createSessionImpl("New Project", showGUI);
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
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    private ArchitectSwingSession createSessionImpl(String projectName, boolean showGUI) throws ArchitectException {
        logger.debug("About to create a new session for project \"" + projectName + "\"");
        ArchitectSwingSessionImpl session = new ArchitectSwingSessionImpl(this, projectName);
        sessions.add(session);
        session.addSessionLifecycleListener(new SessionLifecycleListenerImpl(session));
        
        if (showGUI) {
            logger.debug("Creating the Architect frame...");
            session.initGUI();
        
            if (sessions.size() == 1) {
                showWelcomeScreen(session.getArchitectFrame());
            }
        }
        
        return session;
    }
    
    private class SessionLifecycleListenerImpl implements SessionLifecycleListener {
        
        ArchitectSwingSession session;
        
        public SessionLifecycleListenerImpl(ArchitectSwingSession session) {
            this.session = session;
        }

        public void sessionClosing() {
            sessions.remove(session);
        }
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
