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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;


/**
 * The CompareDMDialog extends a JDialog and houses a 
 * {@link CompareDMPanel}, it sets up the border and the cancel 
 * button. 
 *
 */
public class CompareDMDialog extends JDialog {
    
    private CompareDMPanel compareDMPanel;
    
    /**
     * Cancels a compare operation if it's running, and closes the window otherwise.
     */
    class CancelCompareAction extends AbstractAction {
        final JDialog dialog;
        CancelCompareAction(JDialog d) {
            super("Cancel");
            dialog = d;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            SPSwingWorker spsw = ((CompareDMPanel.StartCompareAction)compareDMPanel
                    .getStartCompareAction())
                    .getCompareWorker();
            if (spsw == null || spsw.isCancelled() || spsw.isFinished()) {
                dialog.setVisible(false);
                dialog.dispose();
            } else {
                spsw.setCancelled(true);
            }
        }
    }
    
    public CompareDMDialog(ArchitectSwingSession session) {
        // This can not easily be replaced with ArchitectPanelBuilder
        // because the current CompareDMPanel is not an ArchitectPanel
        // (and has no intention of becoming one, without some work).
        
        super(session.getArchitectFrame(),Messages.getString("CompareDMDialog.compareDmDialogTitle")); //$NON-NLS-1$
        
        this.addWindowListener(new WindowListener() { 
            @Override
            public void windowOpened(WindowEvent e) { }
            
            @Override
            public void windowIconified(WindowEvent e) { }
            
            @Override
            public void windowDeiconified(WindowEvent e) { }
            
            @Override
            public void windowDeactivated(WindowEvent e) { }
            
            @Override
            public void windowClosing(WindowEvent e) {
                SPSwingWorker spsw = ((CompareDMPanel.StartCompareAction)compareDMPanel
                        .getStartCompareAction())
                        .getCompareWorker();
                if (spsw != null) {
                    spsw.setCancelled(true);
                }
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
            
            @Override
            public void windowActivated(WindowEvent e) { }
        });
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
        
        
        
        JButton cancelButton = new JButton(new CancelCompareAction(this));   
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
