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
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RevisionListPanel {
    
    private static final Logger logger = Logger.getLogger(RevisionListPanel.class);

    private final Component dialogOwner;
    private final ArchitectClientSideSession session;
    private final ArchitectSwingSession swingSession;
    
    private final RevisionsTable revisionsTable;
    
    private final JPanel panel;
    
    private JFormattedTextField fromVersion;
    private JFormattedTextField toVersion;
    
    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {
            int currentVersion = session.getCurrentRevisionNo();
            String message = "ok";
            boolean doRefresh = false;
            
            long fromVersion = currentVersion - 100;
            long toVersion = currentVersion;
           
            try {
                fromVersion = ((Long) RevisionListPanel.this.fromVersion.getValue()).longValue();
                toVersion = ((Long) RevisionListPanel.this.toVersion.getValue()).longValue();
                
                if (toVersion > currentVersion) {
                    message = "Revisions up to " + toVersion + " cannot be shown " +
                        "because the current revision is only " + currentVersion;
                } else if (fromVersion > toVersion) {
                    message = "Cannot show revisions from a higher version to a lower version";
                } else {
                    doRefresh = true;
                }
            } catch (ClassCastException ex) {
                // This shoudn't happen because of the way the text field is formatted
                message = "Input formatted invalidly";
                RevisionListPanel.this.fromVersion.setValue(new Long(fromVersion));
                RevisionListPanel.this.toVersion.setValue(new Long(toVersion));
                doRefresh = true;
            }
            if (doRefresh) {
                revisionsTable.refreshRevisionsList(fromVersion, toVersion);
                refreshPanel();
            }
            if (!message.equals("ok")) {
                JOptionPane.showMessageDialog(dialogOwner, message);
            }
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
                } catch (Throwable t) {
                    throw new RuntimeException("Error requesting server revert", t);
                }  
                // TODO make this wait for the client to update before refreshing
                // Can't do it now because client is currently broken
                int currentVersion = session.getCurrentRevisionNo(); 
                fromVersion.setValue((long) currentVersion - 100);
                toVersion.setValue((long) currentVersion);
                refreshAction.actionPerformed(null);          
                int last = revisionsTable.getRowCount() - 1;
                revisionsTable.getSelectionModel().setSelectionInterval(last, last);
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
            
            long fromVersion;
            long toVersion;
            try {
                fromVersion = ((Long) RevisionListPanel.this.fromVersion.getValue()).longValue();
            } catch (ClassCastException ex) {
                fromVersion = ((Integer) RevisionListPanel.this.fromVersion.getValue()).longValue();
            }
            try {
                toVersion = ((Long) RevisionListPanel.this.toVersion.getValue()).longValue();
            } catch (ClassCastException ex) {
                toVersion = ((Integer) RevisionListPanel.this.toVersion.getValue()).longValue();
            } 
            CompareRevisionsPanel p = new CompareRevisionsPanel(session, closeAction, fromVersion, toVersion);
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
                "default:grow, 5dlu, pref",
                "pref, 2dlu, default:grow"));                                 
        
               
        int currentRevision = session.getCurrentRevisionNo();
        NumberFormatter f = new NumberFormatter(NumberFormat.getInstance());
        f.setValueClass(Long.class);
        f.setMinimum(new Long(0));
        fromVersion = new JFormattedTextField(f);        
        toVersion = new JFormattedTextField(f);        
        fromVersion.setValue(new Long(currentRevision - 100));        
        toVersion.setValue(new Long(currentRevision));
        
        revisionsTable = new RevisionsTable(this.session, currentRevision - 100, currentRevision);
        revisionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshPanel();
            }
        });
        
        CellConstraints cc = new CellConstraints();
        builder.add(new JLabel("Revisions:"), cc.xy(1, 1));        
        builder.add(revisionsTable.getScrollPane(), cc.xy(1, 3));              
        
        DefaultFormBuilder textFieldBuilder = new DefaultFormBuilder(new FormLayout("pref, pref"));
        textFieldBuilder.append(new JLabel("from version "), fromVersion);
        textFieldBuilder.append(new JLabel("to version "), toVersion);
        
        DefaultFormBuilder filterBuilder = new DefaultFormBuilder(new FormLayout("pref"));
        filterBuilder.append(new JLabel("Display revisions "));
        filterBuilder.append(textFieldBuilder.getPanel());
               
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(revertAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(compareAction));
        buttonBarBuilder.append(new JButton(closeAction)); 
        buttonBarBuilder.append(filterBuilder.getPanel());
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
