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

import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.CoreUserSettings;

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
     * Creates a new session that will have its GUI components positioned relative to the GUI components
     * of the given ArchitectSwingSession. Typically, the given ArchitectSwingSession is the session from
     * where the call to create a new session was made. (ex. the given session's 'Open Project' button was
     * pressed). If the given session is null, then it will default to using the most recently saved GUI
     * component positions in the user preferences in {@link ArchitectSwingUserSettings}. 
     * 
     * @param openingSession
     * @return
     * @throws ArchitectException
     */
    public abstract ArchitectSwingSession createSession(ArchitectSwingSession openingSession) throws ArchitectException;
    
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
    
    /**
     * Gets the user settings for this session 
     */
    public abstract CoreUserSettings getUserSettings();
}