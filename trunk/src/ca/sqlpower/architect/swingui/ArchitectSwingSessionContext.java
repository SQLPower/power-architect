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

import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSessionContext;

/**
 * The ArchitectSwingSessionContext interface specifies a set of
 * properties and methods for creating new Architect Swing Sessions.
 * Additionally, the session context is the gateway to information
 * that is specific to the current user's environment (as opposed
 * to information that is attached to specific projects, which is
 * stored in the session).
 */
public interface ArchitectSwingSessionContext extends ArchitectSessionContext {

    /**
     * The size, in pixels, of the icons in the toolbar.  This used to be managed as a
     * user preference, but we no longer maintain two sets of icons at different pixel
     * sizes.
     */
    public static final int ICON_SIZE = 16;

    /**
     * Creates a new session within this parent context.  This will cause an
     * Architect Frame to appear on the user's desktop with a new empty project
     * in it.
     * <p>
     * Important note: This method must be called on the Swing Event Dispatch
     * Thread.  See SwingUtilities.invokeLater() for a way of ensuring this method
     * is called on the proper thread.
     * @throws ArchitectException 
     */
    public abstract ArchitectSwingSession createSession() throws ArchitectException;
    
    /**
     * Creates a new session within this parent context.  This will cause an
     * Architect Frame to appear on the user's desktop with a new empty project
     * in it.
     * <p>
     * <b>Important note:</b> If showGUI is true, this method must be called on
     * the Swing Event Dispatch Thread.  See SwingUtilities.invokeLater() for a
     * way of ensuring this method is called on the proper thread.
     * 
     * @param showGUI True if you want this session to have its own (visible)\
     * ArchitectFrame instance; false for an invisible session.
     * @return The new session
     */
    public abstract ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException;
    
    /**
     * Creates a new session by loading the Architect XML project description
     * from the given input stream.
     * 
     * @param in The input stream to read the XML data from
     * @param showGUI True if you want this session to have its own (visible)\
     * ArchitectFrame instance; false for an invisible session.
     * @return The new session
     */
    public abstract ArchitectSwingSession createSession(InputStream in, boolean showGUI) throws ArchitectException, IOException;

    /**
     * Returns true iff this context is running on a Mac OS X machine.  Some
     * UI features are different under that platform to increase the illusion
     * that the Architect is a native application.
     */
    public abstract boolean isMacOSX();
    
    /**
     * Closes all sessions and terminates the VM.  This is the typical "exit"
     * action for a project.
     */
    public void closeAll();

    /**
     * Optional setting which will have the context call System.exit() after there
     * are no open sessions remaining.  This option is useful for a full single-user
     * Architect application, but not a good idea for embedded use of the Architect,
     * since it will quit your app when you're finished with the Architect API.
     * <p>
     * The default behaviour for all context implementations must be <b>not</b> to
     * terminate the VM.
     * 
     * @param allowExit True will allow the context to terminate the VM; false
     * means the context will never call System.exit().
     */
    public abstract void setExitAfterAllSessionsClosed(boolean allowExit);
    
    /**
     * Returns the current session for exiting after all sessions are closed.
     * See {@link #setExitAfterAllSessionsClosed(boolean)} for details.
     */
    public boolean getExitAfterAllSessionsClosed();

    /**
     * Shows the connection manager dialog for this context's data source collection. 
     * 
     * @param owner The owner of the dialog.
     */
    public abstract void showConnectionManager(Window owner);

    /**
     * Shows the user preferences dialog for this application context.
     * 
     * @param owner The owner of the dialog
     */
    public void showPreferenceDialog(Window owner);
}