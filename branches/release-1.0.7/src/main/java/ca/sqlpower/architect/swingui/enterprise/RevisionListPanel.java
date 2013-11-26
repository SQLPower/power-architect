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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.enterprise.ArchitectSessionPersister;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RevisionListPanel {
    
    public static final int DEFAULT_REVISION_RANGE = 100;
    
    private class JLongField {
        
        private JFormattedTextField field;
        private long value;
        
        public JLongField(long value) {
            NumberFormatter f = new NumberFormatter(NumberFormat.getInstance());
            f.setValueClass(Long.class);
            f.setMinimum(new Long(1));
            field = new JFormattedTextField(f); 
            setValue(value);
        }
        
        public void setValue(long value) {
            this.value = value;
            field.setValue(new Long(value));
        }
        
        public long getValue() {
            return getValue(true);
        }
        
        public long getValue(boolean update) {
            if (update) {
                this.value = ((Long) field.getValue()).longValue();           
            }
            return this.value;
        }        
        
        public JFormattedTextField getField() {
            return field;
        }
        
        public boolean update() {
            return (this.value != this.getValue(true)); 
        }
        
    }
    
    private static final Logger logger = Logger.getLogger(RevisionListPanel.class);

    private final Component dialogOwner;
    private final ArchitectClientSideSession session;
    private final ArchitectSwingSession swingSession;
    
    private final RevisionsTable revisionsTable;
    
    private final JPanel panel;
    
    private JLongField fromVersion;
    private JLongField toVersion;
    private int currentVersion;
    
    private final JCheckBox autoRefreshBox;
    private final Runnable autoRefresh;
    
    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {

            boolean filterChange = fromVersion.update() || toVersion.update();

            int difference = session.getUpdater().getRevision() - currentVersion;
            if (difference > 0) currentVersion += difference;
            
            if (toVersion.getValue() > currentVersion) {
                toVersion.setValue(currentVersion);
            } else if (fromVersion.getValue() > toVersion.getValue()) {
                long to = toVersion.getValue() - DEFAULT_REVISION_RANGE;
                if (to <= 0) to = 1;
                fromVersion.setValue(to);
            }

            if (difference > 0) {                    
                // If the end of the filter range was the previously most recent revision,
                // update the filter range to the new current revision number.
                if (toVersion.getValue() == currentVersion - difference) {                                               
                    fromVersion.setValue(fromVersion.getValue() + difference);
                    toVersion.setValue(currentVersion);    
                }
            }            
            if (difference > 0 || filterChange) {
                revisionsTable.refreshRevisionsList(fromVersion.getValue(), toVersion.getValue());
                refreshPanel();
            }
        }
    };
    
    private final Action revertAction = new AbstractAction("Revert...") {
        public void actionPerformed(ActionEvent e) {
            final int revisionNo = revisionsTable.getSelectedRevisionNumber();
            if (revisionNo >= currentVersion) {
                JOptionPane.showMessageDialog(dialogOwner, "Cannot revert to the current version number");
                return;
            }
            int response = JOptionPane.showConfirmDialog(dialogOwner, 
                    "Are you sure you would like to revert to version " + revisionNo,
                    "Revert...", JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.OK_OPTION) {
                revertAction.setEnabled(false);                
                swingSession.runInBackground(new Runnable() {
                    public void run() {
                        try {
                            final int currentVersion = session.revertServerWorkspace(revisionNo);
                            swingSession.runInForeground(new Runnable() {
                                public void run() {
                                    if (currentVersion == -1) {
                                        JOptionPane.showMessageDialog(dialogOwner, "The server did not revert" +
                                        " because the target and current revisions are identical");
                                    } else {
                                        // This is doing the refresh action's
                                        // job for it, but this should probably
                                        // auto refresh even when auto-refresh
                                        // is turned off
                                        RevisionListPanel.this.currentVersion = currentVersion;
                                        long range = toVersion.getValue() - fromVersion.getValue();
                                        fromVersion.setValue(currentVersion - range);
                                        toVersion.setValue(currentVersion);
                                        revisionsTable.refreshRevisionsList(fromVersion.getValue(), toVersion.getValue());                                 
                                        int last = revisionsTable.getRowCount() - 1;
                                        revisionsTable.getSelectionModel().setSelectionInterval(last, last);
                                    }
                                }
                            });
                        } catch (Throwable t) {
                            throw new RuntimeException("Error requesting server revert", t);                    
                        } finally {
                            refreshPanel();
                        }   
                    }
                });
            }
        }
    };
    
    private final Action openAction = new AbstractAction("Open...") {
        public void actionPerformed(ActionEvent e) {
            int revisionNo = revisionsTable.getSelectedRevisionNumber();
            ProjectLocation location = session.getProjectLocation();
            ArchitectSwingSession revisionSession = null;            
            try {            
                revisionSession = swingSession.getContext().createSession();
                revisionSession.getUndoManager().setLoading(true);
                revisionSession.setName(location.getName() + " - Revision " + revisionNo);                

                ArchitectSessionPersister sessionPersister = new ArchitectSessionPersister(
                        "inbound-" + location.getUUID(), revisionSession.getWorkspace(),
                        new ArchitectPersisterSuperConverter(
                                revisionSession.getDataSources(), revisionSession.getWorkspace()));

                sessionPersister.setWorkspaceContainer(revisionSession);

                SPJSONMessageDecoder decoder = new SPJSONMessageDecoder(sessionPersister);           

                session.persistRevisionFromServer(revisionNo, decoder);
                revisionSession.getUndoManager().setLoading(false);
                swingSession.getArchitectFrame().addSession(revisionSession);
            } catch (Exception ex) {
                if (revisionSession != null) {
                    revisionSession.close();
                }
                throw new RuntimeException(ex);
            }
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
             
            CompareRevisionsPanel p = new CompareRevisionsPanel(session, closeAction, 
                    fromVersion.getValue(), toVersion.getValue());
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
               
        int currentRevision = session.getUpdater().getRevision();
        long from = currentRevision - DEFAULT_REVISION_RANGE;
        if (from <= 0) from = 1;
        fromVersion = new JLongField(from);        
        toVersion = new JLongField(currentRevision);
        
        revisionsTable = new RevisionsTable(this.session, from, currentRevision);
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
        textFieldBuilder.append(new JLabel("from version "), fromVersion.getField());
        textFieldBuilder.append(new JLabel("to version "), toVersion.getField());
        
        DefaultFormBuilder filterBuilder = new DefaultFormBuilder(new FormLayout("pref"));
        filterBuilder.append(new JLabel("Display revisions "));      
        filterBuilder.append(textFieldBuilder.getPanel());
        
        autoRefreshBox = new JCheckBox("Auto-refresh", false);
        autoRefresh = new Runnable() {
            public void run() {
                while (autoRefreshBox.isSelected()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {                    
                            refreshAction.actionPerformed(null);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        autoRefreshBox.setSelected(false);                        
                    }
                }
            }
        };       
        autoRefreshBox.addItemListener(new ItemListener() { 
            public void itemStateChanged(ItemEvent e) {
                if (autoRefreshBox.isSelected()) {
                    new Thread(autoRefresh).start();
                }
            }
        });
        autoRefreshBox.setSelected(true);
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));
        buttonBarBuilder.append(filterBuilder.getPanel());
        buttonBarBuilder.append(new JLabel("\n"));
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(revertAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(compareAction));
        buttonBarBuilder.append(new JButton(closeAction));
        buttonBarBuilder.append(autoRefreshBox);
        
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
