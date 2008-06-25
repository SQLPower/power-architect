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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ShowColumnsPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Shows and hides selected columns in playpen
 */
public class ShowColumnsAction extends AbstractArchitectAction{
    
    private static final Logger logger = Logger.getLogger(ShowColumnsAction.class);
    
    protected JDialog editDialog;           
    protected ShowColumnsPanel showColumnsPanel;
    protected ArchitectSwingSession session;
    
    public ShowColumnsAction(ArchitectSwingSession session) {
        super(session, Messages.getString("ShowColumnsAction.name"), Messages.getString("ShowColumnsAction.description")); //$NON-NLS-1$ //$NON-NLS-2$
        this.session = session;
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(12,12));
        panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        
        showColumnsPanel = new ShowColumnsPanel(session);
        panel.add(showColumnsPanel, BorderLayout.CENTER);

        editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                showColumnsPanel,
                frame,
                Messages.getString("ShowColumnsAction.dialogTitle"), //$NON-NLS-1$
                Messages.getString("ShowColumnsAction.OkOption"), //$NON-NLS-1$
                new Callable<Boolean>(){
                    public Boolean call() {
                        return showColumnsPanel.applyChanges();
                    }
                }, 
                new Callable<Boolean>(){
                    public Boolean call() {
                        showColumnsPanel.discardChanges();
                        return new Boolean(true);
                    }
                });
        panel.setOpaque(true);
        editDialog.pack();
        editDialog.setLocationRelativeTo(frame);
        editDialog.setVisible(true);
    }

}
