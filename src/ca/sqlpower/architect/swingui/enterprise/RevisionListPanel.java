/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.enterprise;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RevisionListPanel {

    private final Component dialogOwner;
    private final ArchitectClientSideSession session;
    private final ArchitectSwingSession swingSession;
    
    private final RevisionsTable revisionsTable;
    
    private final JPanel panel;
    
    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {            
            revisionsTable.refreshRevisionsList();
            refreshPanel();
        }
    };
    
    private final Action revertAction = new AbstractAction("Revert...") {
        public void actionPerformed(ActionEvent e) {
            int revisionNo = revisionsTable.getSelectedRevisionNumber();
            int response = JOptionPane.showConfirmDialog(dialogOwner, 
                    "Are you sure you would like to revert to version " + revisionNo,
                    "Revert...", JOptionPane.OK_CANCEL_OPTION);            
            if (response == JOptionPane.OK_OPTION) {
                try {
                    session.revertServerWorkspace(revisionNo);
                    revisionsTable.refreshRevisionsList();
                } catch (Throwable t) {
                    throw new RuntimeException("Error requesting server revert", t);
                }
            }
        }
    };
    
    private final Action openAction = new AbstractAction("Open...") {
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(dialogOwner, "This feature is not yet supported. Try again soon!");
        }
    };   

    private final Action compareAction = new AbstractAction("Compare...") {
        public void actionPerformed(ActionEvent e) {
            final JDialog d = SPSUtils.makeOwnedDialog(swingSession.getArchitectFrame(), "Compare Revisions");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };
            
            CompareRevisionsPanel p = new CompareRevisionsPanel(session, closeAction);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(p.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(RevisionListPanel.this.getPanel());
            d.setVisible(true);            
        }
    };
    
    public RevisionListPanel(ArchitectSwingSession swingSession, ArchitectFrame architectFrame, Action closeAction) {
        
        this.dialogOwner = architectFrame;
        this.swingSession = swingSession;
        this.session = swingSession.getEnterpriseSession();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref",
                "pref, 2dlu, default:grow"));                                 
        
        revisionsTable = new RevisionsTable(this.session);
        revisionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshPanel();
            }
        });
        
        JScrollPane revisionsPane = new JScrollPane(revisionsTable);
        
        CellConstraints cc = new CellConstraints();
        builder.add(new JLabel("Revisions:"), cc.xy(1, 1));        
        builder.add(revisionsPane, cc.xy(1, 3));
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(revertAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(compareAction));
        buttonBarBuilder.append(new JButton(closeAction));        
        builder.add(buttonBarBuilder.getPanel(), cc.xy(3, 3));
        builder.setDefaultDialogBorder();

        panel = builder.getPanel();              
        panel.setPreferredSize(new Dimension(700, 500));
        
        refreshPanel();
        
    }
    
    private void refreshPanel() {
        if (revisionsTable.getSelectedRow() == -1) {
            openAction.setEnabled(false);
            revertAction.setEnabled(false);
        } else {
            openAction.setEnabled(true);
            revertAction.setEnabled(true);
        }
    }
    
    public JPanel getPanel() {
        return panel;
    }

}
