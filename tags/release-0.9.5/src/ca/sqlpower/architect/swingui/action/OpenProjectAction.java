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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitorInputStream;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import ca.sqlpower.architect.swingui.RecentMenu;

public class OpenProjectAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(OpenProjectAction.class);
    
    RecentMenu recent;
    
    public OpenProjectAction(ArchitectSwingSession session) {
        super(session, "Open Project...", "Open", "folder");
        this.recent = session.getContext().getRecentMenu();
        putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_O,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(ASUtils.ARCHITECT_FILE_FILTER);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            openAsynchronously(session.getContext(), f);
        }
        logger.info("Opening a Project doesn't work yet ;)");
    }

    /**
     * Opens a project file into a new session (created from the given context)
     * using a separate worker thread.  A dialog box with a progress bar will
     * be displayed during the load process, and any errors that are encountered
     * during the load will be displayed in additional dialogs.
     * <p>
     * Note that this method always returns immediately, so as the caller of
     * this method you have no way of knowing if the load has worked/will work.
     * 
     * @param context The context with which to create the session.
     * @param f The project file to load.
     */
    public static void openAsynchronously(ArchitectSwingSessionContext context, File f) {
      LoadFileWorker worker;
        try {
            worker = new LoadFileWorker(f, context);
            new Thread(worker).start();
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(
                    null,
                    "File not found: "+f.getPath());
        } catch (Exception e1) {
            ASUtils.showExceptionDialog("Error loading file", e1);
        }

    }
    
    /**
     * A worker for asynchronously loading a new project file.
     */
    private static class LoadFileWorker extends ArchitectSwingWorker {
        private final ArchitectSwingSessionContext context;
        private final InputStream in;
        private final File file;
        private final RecentMenu recent;
        
        /**
         * The session that will get created if loading the file
         * in doStuff() is successful.
         */
        private ArchitectSwingSession newSession;
        
        /**
         * Load file worker creates a new worker and opens the given file.
         *
         * @param file  this file gets opened in the constructor
         * @param recent optional recent menu in which to add the file
         * @throws ArchitectException when the project creation fails.
         * @throws FileNotFoundException if file doesn't exist
         */
        public LoadFileWorker(File file, ArchitectSwingSessionContext context) throws ArchitectException, FileNotFoundException {
                this.context = context;
                this.file = file;
                this.recent = context.getRecentMenu();
                
                // XXX this progress dialog has the coffee cup icon instead
                // of the architect icon. To fix this, we need to create an
                // invisible owner frame in the context. we can set its icon
                // to the architect icon, and use it as the owner of all
                // "unowned" dialogs like this one.
                in = new BufferedInputStream(
                    new ProgressMonitorInputStream(
                         null,
                         "Reading " + file.getName(),
                         new FileInputStream(file)));
        }

        @Override
        public void doStuff() throws IOException {
            try {
                newSession = context.createSession(in, false);
            } catch (ArchitectException e) {
                setDoStuffException(e);
            }
            if (recent != null) {
                recent.putRecentFileName(file.getAbsolutePath());
            }
        }

        @Override
        public void cleanup() throws ArchitectException {
            if (getDoStuffException() != null) {
                Throwable cause = getDoStuffException().getCause();
                // This if clause is to prevent an error from being thrown if the user cancelled the file load, 
                // in which ProgressMonitorInputStream throws an InterruptedIOException with message "progress"
                if (!(cause instanceof InterruptedIOException) || !(cause.getMessage().equals("progress"))) {
                    // We have to use the non-session exception dialogue here,
                    // because there is no session available (we just failed to create one!)
                    ASUtils.showExceptionDialog(
                            "Cannot open project file '" + file.getAbsolutePath() + "'",
                            getDoStuffException());
                    logger.error("Got exception while opening a project", getDoStuffException());
                }
            } else {
                newSession.initGUI();
                ((SQLObject) newSession.getSourceDatabases().getModel().getRoot()).fireDbStructureChanged();
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

