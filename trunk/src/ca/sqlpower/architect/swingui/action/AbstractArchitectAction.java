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

import javax.swing.AbstractAction;

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
        
        super(actionName,
                iconResourceName == null ?
                        null :
                        SPSUtils.createIcon(iconResourceName, actionName, ArchitectSwingSessionContext.ICON_SIZE));
        putValue(SHORT_DESCRIPTION, actionDescription);

        this.session = session;
        if (session == null) throw new NullPointerException("Null session");

        this.frame = session.getArchitectFrame();
        if (frame == null) throw new NullPointerException("Null parentFrame");
        
        this.playpen = frame.getPlayPen();
        if (playpen == null) throw new NullPointerException("Null playpen");
        

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
        this(session, actionName, actionDescription, null);
    }
}
