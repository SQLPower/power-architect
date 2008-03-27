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
package ca.sqlpower.architect.swingui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitorInputStream;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;

public class OpenProjectAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(OpenProjectAction.class);

    RecentMenu recent;

    public OpenProjectAction(ArchitectSwingSession session) {
        super(session, "Open Project...", "Open", "folder");
        this.recent = session.getRecentMenu();
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(recent.getMostRecentFile());
        chooser.addChoosableFileFilter(SPSUtils.ARCHITECT_FILE_FILTER);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                OpenProjectAction.openAsynchronously(session.getContext().createSession(false), f, session);
            } catch (ArchitectException ex) {
                SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                        "Failed to open project file", ex);
            }
        }
    }

    /**
     * Opens a project file into the given session using a separate worker
     * thread. A dialog box with a progress bar will be displayed during the
     * load process, and any errors that are encountered during the load will be
     * displayed in additional dialogs.
     * <p>
     * Note that this method always returns immediately, so as the caller of
     * this method you have no way of knowing if the load has worked/will work.
     * 
     * @param newSession
     *            The session in which to load the project into.
     * @param f
     *            The project file to load.
     * @param openingSession
     *            The session from which this openAsynchronously call is made.
     *            If the session being opened is the first session being
     *            created, then simply set to null. If the
     *            openingSession.isNew() returns true, (i.e. it's an new, empty,
     *            and unmodified project) then openingSession.close() will be
     *            called once the project is finished loading.
     */
    public static void openAsynchronously(ArchitectSwingSession newSession, File f, ArchitectSwingSession openingSession) {
        LoadFileWorker worker;
        try {
            worker = new LoadFileWorker(f, newSession, openingSession);
            new Thread(worker).start();
        } catch (Exception e1) {
            ASUtils.showExceptionDialogNoReport("Error loading file", e1);
        }

    }

    /**
     * A worker for asynchronously loading a new project file.
     */
    private static class LoadFileWorker extends SPSwingWorker {
        private final ArchitectSwingSessionContext context;

        private final InputStream in;

        private final File file;

        private final RecentMenu recent;

        private final ArchitectSwingSession openingSession;

        /**
         * The session that will get created if loading the file in doStuff() is
         * successful.
         */
        private ArchitectSwingSession session;

        /**
         * Load file worker creates a new worker and opens the given file.
         * 
         * @param file
         *            this file gets opened in the constructor
         * @param newSession
         *            The session in which the project file should be opened
         * @param openingSession
         *            The session from which the open project operation is being
         *            called. Should not be null and should have a frame. If the
         *            openingSession.isNew() returns true, (i.e. it's an new,
         *            empty, and unmodified project) then openingSession.close()
         *            will be called once the project is finished loading.
         * @throws ArchitectException
         *             when the project creation fails.
         * @throws FileNotFoundException
         *             if file doesn't exist
         */
        public LoadFileWorker(File file, ArchitectSwingSession newSession, ArchitectSwingSession openingSession)
                throws ArchitectException, FileNotFoundException {
            // The super constructor registers the LoadFileWorker with the
            // session.
            super(newSession);
            this.context = newSession.getContext();
            this.file = file;
            this.recent = newSession.getRecentMenu();
            this.openingSession = openingSession;

            this.session = newSession;

            // This assumes that the opening session has a frame. 
            // To get rid of this requirement, we need to create an
            // invisible owner frame in the context. we can set its icon
            // to the architect icon, and use it as the owner of all
            // "unowned" dialogs like this one.
            in = new BufferedInputStream(new ProgressMonitorInputStream(openingSession.getArchitectFrame(),
                    "Reading " + file.getName(), new FileInputStream(file)));
        }

        @Override
        public void doStuff() throws Exception {
            session.getProject().load(in, context.getPlDotIni());
            session.getProject().setFile(file);
        }

        @Override
        public void cleanup() throws ArchitectException {
            if (getDoStuffException() != null) {
                Throwable cause = getDoStuffException().getCause();
                // This if clause is to prevent an error from being thrown if
                // the user cancelled the file load,
                // in which ProgressMonitorInputStream throws an
                // InterruptedIOException with message "progress"
                if (!(cause instanceof InterruptedIOException) || !(cause.getMessage().equals("progress"))) {
                    // We have to use the non-session exception dialogue here,
                    // because there is no session available (we just failed to
                    // create one!)
                    ASUtils.showExceptionDialogNoReport("Cannot open project file '" + file.getAbsolutePath() + "'",
                            getDoStuffException());
                    logger.error("Got exception while opening a project", getDoStuffException());
                }
                session.removeSwingWorker(this);
                if (session.getContext().getSessions().size() > 1) {
                    //Prevents the save dialog from being displayed if it contained an error
                    session.getProject().setModified(false); 
                    session.close();
                }
            } else {
                recent.putRecentFileName(file.getAbsolutePath());
                session.initGUI(openingSession);
                ((SQLObject) session.getSourceDatabases().getModel().getRoot()).fireDbStructureChanged();
            }

            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ie) {
                logger.error("got exception while closing project file", ie);
            }
        }
    }
}
