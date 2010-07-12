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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.InputStream;

import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.sqlobject.SQLObjectException;

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
     * Link to where you can buy the user guide on the SQL Power website.
     */
    public static final String USER_GUIDE_URL = "http://www.sqlpower.ca/page/architect-userguide";

    /**
     * Link to where you can buy premium support on the SQL Power website.
     */
    public static final String PREMIUM_SUPPORT_URL = "http://www.sqlpower.ca/page/architect_support";

    /**
     * Link to where you can post questions to the community in the SQL Power
     * forums.
     */
    public static final String COMMUNITY_FORUM_URL = "http://www.sqlpower.ca/page/enter_forum";

    /**
     * Creates a new session within this parent context. It will not display a
     * GUI unless added to an {@link ArchitectFrame}.
     * 
     * @throws SQLObjectException
     * @return The newly created Session.
     */
    public abstract ArchitectSwingSession createSession() throws SQLObjectException;

    /**
     * Creates a new session by loading the Architect XML project description
     * from the given input stream. It will not display a GUI unless added to an
     * {@link ArchitectFrame}.
     * 
     * @param in
     *            The input stream to read the XML data from
     * @param showGUI
     *            True if you want this session to have its own (visible)\
     *            ArchitectFrame instance; false for an invisible session.
     * @return The newly created Session.
     */
    public abstract ArchitectSwingSession createSession(InputStream in) throws SQLObjectException, IOException;

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
     * Gets the user settings for this session 
     */
    public abstract CoreUserSettings getUserSettings();
    
    /**
     * This gets either the clipboard internal to the context or the
     * system's clipboard depending on the information contained by each.
     */
    public Transferable getClipboardContents();
    
    /**
     * This sets either the clipboard internal to the context or the
     * system's clipboard depending on the information contained by each.
     */
    public void setClipboardContents(Transferable t);

    /**
     * Registers this frame with the context, and allows enterprise features to
     * modify it.
     * 
     * @param frame
     */
    void registerFrame(ArchitectFrame frame);
}