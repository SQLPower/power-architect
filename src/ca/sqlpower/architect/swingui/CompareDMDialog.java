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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.swingui.CommonCloseAction;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;


/**
 * The CompareDMDialog extends a JDialog and houses a 
 * {@link CompareDMPanel}, it sets up the border and the cancel 
 * button. 
 *
 */
public class CompareDMDialog extends JDialog {
    
    private CompareDMPanel compareDMPanel;
    
    public CompareDMDialog(ArchitectSwingSession session) {
        
        
        // This can not easily be replaced with ArchitectPanelBuilder
        // because the current CompareDMPanel is not an ArchitectPanel
        // (and has no intention of becoming one, without some work).
        
        super(session.getArchitectFrame(),"Compare Data Models");
        JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
                
        compareDMPanel = new CompareDMPanel(session, this);

        cp.add(compareDMPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = compareDMPanel.getButtonPanel();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JButton swapButton = new JButton (compareDMPanel.getSwapSourceTargetAction());
        bottomPanel.add(swapButton, BorderLayout.WEST);

        JDefaultButton okButton = new JDefaultButton(compareDMPanel.getStartCompareAction());
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton(new CommonCloseAction(this));   
        buttonPanel.add(cancelButton);

        bottomPanel.add(buttonPanel,BorderLayout.EAST);
        cp.add(bottomPanel, BorderLayout.SOUTH);
        SPSUtils.makeJDialogCancellable(this, cancelButton.getAction());
        getRootPane().setDefaultButton(okButton);
        setContentPane(cp);
        pack();
        setLocationRelativeTo(session.getArchitectFrame());
    }
    
    /**
     * Calls {@link CompareDMPanel#compareCurrentWithOrig(SQLSchema, SQLCatalog, SQLDatabase)}
     * in its CompareDMPanel
     * 
     * @param schema The schema to set  
     * @param catalog The catalog to set 
     * @param db The database to set
     */
    public void compareCurrentWithOrig(SQLSchema schema, SQLCatalog catalog, SQLDatabase db) {
        compareDMPanel.compareCurrentWithOrig(schema, catalog, db);
    }
}
