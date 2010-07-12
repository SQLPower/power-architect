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
    private final PlayPen playpen;
    private final ArchitectSwingSession session;
    
    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param playpen The play pen to use. For actions that deal with the relational
     * play pen, this should be the ArchitectSwingSession's play pen. For OLAP actions,
     * this should be the appropriate OLAP play pen.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param icon The icon to use.  Null means no icon.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            ArchitectFrame frame,
            PlayPen playpen,
            String actionName,
            String actionDescription,
            Icon icon) {
        
        super(actionName, icon);
        putValue(SHORT_DESCRIPTION, actionDescription);

        this.session = session;
        if (session == null) throw new NullPointerException("Null session"); //$NON-NLS-1$

        this.frame = frame;
        if (frame == null) throw new NullPointerException("Null parentFrame"); //$NON-NLS-1$
        
        this.playpen = playpen;
        if (playpen == null) throw new NullPointerException("Null playpen"); //$NON-NLS-1$
    }
    
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            PlayPen playpen,
            String actionName,
            String actionDescription,
            Icon icon) {
        this(session, session.getArchitectFrame(), playpen, actionName, actionDescription, icon);
    }
    
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            ArchitectFrame frame,
            String actionName,
            String actionDescription,
            Icon icon) {
        super(actionName, icon);
        putValue(SHORT_DESCRIPTION, actionDescription);
        
        this.frame = frame;
        this.session = session;
        playpen = null;
    }

    /**
     * Creates an action that has an icon, and affects the current active
     * session in the given frame.
     * 
     * @param frame
     * @param actionName
     * @param actionDescription
     * @param icon
     */
    public AbstractArchitectAction(
            ArchitectFrame frame,
            String actionName,
            String actionDescription,
            Icon icon) {
        super (actionName, icon);
        putValue(SHORT_DESCRIPTION, actionDescription);
        
        this.frame = frame;
        session = null;
        playpen = null;
    }

    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param frame The ArchitectFrame that owns this action.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link SPSUtils#createIcon(String, String, int))} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            ArchitectFrame frame,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        
        this(session, frame, actionName, actionDescription,
                iconResourceName == null ?
                        (Icon) null :
                            SPSUtils.createIcon(iconResourceName, actionName, ArchitectSwingSessionContext.ICON_SIZE));
    }

    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param frame The ArchitectFrame that owns this action.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link SPSUtils#createIcon(String, String, int))} for details.
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
     * Creates an action that has an icon, and affects the current active
     * session in the given frame.
     * 
     * @param frame
     * @param actionName
     * @param actionDescription
     * @param iconResourceName
     */
    public AbstractArchitectAction(
            ArchitectFrame frame,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        this (frame, actionName, actionDescription,
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
     * @param pp The play pen to use. For actions that deal with the relational
     * play pen, this should be the ArchitectSwingSession's play pen. For OLAP actions,
     * this should be the appropriate OLAP play pen.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link SPSUtils#createIcon(String, String, int))} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            PlayPen pp,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        
        this(session, pp, actionName, actionDescription,
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
        
        this(session, session.getPlayPen(), actionName, actionDescription, icon);
    }

    /**
     * Helper constructor that all architect action subclasses that do not
     * use an icon will call. Ensures that the session, its frame, and its 
     * frame's playpen are all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * {@link SPSUtils#createIcon(String, String)} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription) {
        this(session, actionName, actionDescription, (Icon) null);
    }
    
    public AbstractArchitectAction(
            ArchitectFrame frame,
            String actionName,
            String actionDescription) {
        this(frame, actionName, actionDescription, (Icon) null);
    }

    protected ArchitectSwingSession getSession() {
        return (session == null ? frame.getCurrentSession() : session);
    }
    
    protected PlayPen getPlaypen() {
        return (playpen == null ? getSession().getPlayPen() : playpen);
    }
}
