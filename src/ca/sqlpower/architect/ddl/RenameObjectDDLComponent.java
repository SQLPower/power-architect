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
package ca.sqlpower.architect.ddl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;

public class RenameObjectDDLComponent extends GenericDDLWarningComponent {

    private static final Logger logger = Logger.getLogger(RenameObjectDDLComponent.class);
    
    private final DDLWarning warning;
    private JComponent component;
    
    /**
     * List of text fields that correspond to the name of each
     * SQLObject in the list of involved objects for the warning
     * this component holds.
     */
    final List<JTextField> textFields = new ArrayList<JTextField>();
    
    private Runnable changeApplicator;

    public RenameObjectDDLComponent(DDLWarning warning, Runnable changeApplicator) {
        super(warning);
        
        logger.debug("Creating warning component for " + warning);

        this.warning = warning;
        this.changeApplicator = new Runnable() {

            public void run() {
                //TODO should set component values according to the textbox,
                //however, since the quick fix does not change the textbox,
                //setting component values to textbox value would undo the 
                //quick fix.
            }
            
        };
        component = new JPanel();        
        component.add(getQuickFixButton());                 // XXX anti-pattern
        component.add(new JLabel(warning.getMessage()));
        if (warning.getQuickFixPropertyName() != null) {
            component.add(new JLabel(" Change " + warning.getQuickFixPropertyName() + ": "));
            List<SQLObject> list = warning.getInvolvedObjects();
            for (SQLObject obj : list) {
                JTextField jtf = new JTextField(obj.getName());
                component.add(jtf);
                textFields.add(jtf);
            }
        }
    }

    public void applyChanges() {
        changeApplicator.run();
    }

    public Runnable getChangeApplicator() {
        return changeApplicator;
    }

    public JComponent getComponent() {
        return component;
    }

    public DDLWarning getWarning() {
        return warning;
    }

}
