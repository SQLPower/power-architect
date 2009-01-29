/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLIndex;

/**
 * An action that, when invoked, pops up the IndexEditPanel for a certain
 * pre-determined index.
 */
public class EditSpecificIndexAction extends EditIndexAction {

    /**
     * This is the index associated with this edit action
     */
    private final SQLIndex index;

    /**
     * Creates a new instance of this action which will pop up an IndexEditPanel
     * for the given index, regardless of the current selection. For a
     * selection-sensitive index edit action, see
     * {@link ArchitectFrame#getEditIndexAction()}.
     * 
     * @param session
     *            The session to which this action belongs.
     * @param index
     *            The index this action instance will edit. Cannot be modified
     *            once this action is constructed.
     */
    public EditSpecificIndexAction(ArchitectSwingSession session, SQLIndex index){
        super(session, index.getName() + "...", index.getName(), null); //$NON-NLS-1$
        if (index == null) {
            throw new NullPointerException("Null index not allowed"); //$NON-NLS-1$
        }
        this.index = index;
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            makeDialog(index);
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        } 
    }
}
