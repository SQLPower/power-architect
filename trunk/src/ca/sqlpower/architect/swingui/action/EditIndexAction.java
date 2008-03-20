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

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Abstract base class for the two different index edit actions.  The protected
 * method {@link #makeDialog(SQLIndex)} gets used by subclasses once they've
 * decided which index to edit.
 */
public abstract class EditIndexAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(EditIndexAction.class);

    protected EditIndexAction(ArchitectSwingSession session, String actionName, String actionDescription, String iconResourceName) {
        super(session, actionName, actionDescription, iconResourceName);
    }

    /**
     * Creates and shows the index properties dialog for the given index.
     */
    protected void makeDialog(SQLIndex index) throws ArchitectException {
        final JDialog d;
        final IndexEditPanel editPanel = new IndexEditPanel(index, session);
  
        logger.debug("Showing index edit dialog for " + index);
        
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                editPanel, frame,
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }
}
