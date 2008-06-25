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

import javax.swing.AbstractAction;
import javax.swing.Icon;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A set of basic functionality that all actions in the Architect
 * rely on.
 */
public abstract class AbstractArchitectAction extends AbstractAction {

    protected final ArchitectFrame frame;
    protected final PlayPen playpen;
    protected final ArchitectSwingSession session;
    
    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link ASUtils#createIcon(String, String)} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        
        this(session, actionName, actionDescription,
                iconResourceName == null ?
                        (Icon) null :
                        SPSUtils.createIcon(iconResourceName, actionName, ArchitectSwingSessionContext.ICON_SIZE));
    }

    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param icon The icon to use.  Null means no icon.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription,
            Icon icon) {
        
        super(actionName, icon);
        putValue(SHORT_DESCRIPTION, actionDescription);

        this.session = session;
        if (session == null) throw new NullPointerException("Null session"); //$NON-NLS-1$

        this.frame = session.getArchitectFrame();
        if (frame == null) throw new NullPointerException("Null parentFrame"); //$NON-NLS-1$
        
        this.playpen = frame.getPlayPen();
        if (playpen == null) throw new NullPointerException("Null playpen"); //$NON-NLS-1$
        

    }

    /**
     * Helper constructor that all architect action subclasses that do not
     * use an icon will call. Ensures that the session, its frame, and its 
     * frame's playpen are all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * {@link ASUtils#createIcon(String, String)} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription) {
        this(session, actionName, actionDescription, (Icon) null);
    }
}
