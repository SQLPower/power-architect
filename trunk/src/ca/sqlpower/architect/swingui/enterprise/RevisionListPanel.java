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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    
    private class JLongField {
        
        private JFormattedTextField field;        
        
        public JLongField() {
            NumberFormatter f = new NumberFormatter(NumberFormat.getInstance());
            f.setValueClass(Long.class);
            f.setMinimum(new Long(0));
            field = new JFormattedTextField(f);
        }
        
        public JLongField(long value) {
            this();
            setValue(value);
        }
        
        public void setValue(long value) {
            field.setValue(new Long(value));
        }
        
        public long getValue() {
            return ((Long) field.getValue()).longValue();
        }
        
        public JFormattedTextField getField() {
            return field;
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
    
    private final Runnable autoRefresh;
    
    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {
            
            String message = "ok";
            
            // revertAction updates the currentVersion
            int difference = session.getLocalRevisionNo() - currentVersion;
            if (e.getSource() != revertAction) {                            
                if (difference > 0) {
                    currentVersion += difference;
                    // If the end of the filter range was the previously most recent revision,
                    // update the filter range to the new current revision number.
                    if (toVersion.getValue() == currentVersion - difference) {                                               
                        fromVersion.setValue(fromVersion.getValue() + difference);
                        toVersion.setValue(currentVersion);     
                    }
                } else if (e.getSource() == autoRefresh) {                    
                    return;
                }
            }
                        
            if (!e.getActionCommand().equals("ignoreWarnings")) {            
                if (toVersion.getValue() > currentVersion) {
                    message = "Revisions up to " + toVersion.getValue() + " cannot be shown " +
                    "because the current revision is only " + currentVersion;
                } else if (fromVersion.getValue() > toVersion.getValue()) {
                    message = "Cannot show revisions from a higher version to a lower version";
                }        
            }
            
            if (message.equals("ok")) {
                revisionsTable.refreshRevisionsList(fromVersion.getValue(), toVersion.getValue());
                refreshPanel();
            } else {
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
                    int currentVersion = session.revertServerWorkspace(revisionNo);
                    if (currentVersion == -1) {
                        JOptionPane.showMessageDialog(dialogOwner, "The server did not revert" +
                        " because the target and current revisions are identical");
                    } else {                        
                        RevisionListPanel.this.currentVersion = currentVersion;
                        long range = toVersion.getValue() - fromVersion.getValue();
                        fromVersion.setValue(currentVersion - range);
                        toVersion.setValue(currentVersion);
                        refreshAction.actionPerformed(new ActionEvent(this, 0, "ignoreWarnings"));          
                        int last = revisionsTable.getRowCount() - 1;
                        revisionsTable.getSelectionModel().setSelectionInterval(last, last);
                    }
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
        
               
        int currentRevision = session.getLocalRevisionNo();
        long from = currentRevision - 100;
        if (from < 0) from = 0;
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
        
        final JCheckBox autoRefreshBox = new JCheckBox("Auto-refresh", false);
        autoRefresh = new Runnable() {
            public void run() {
                while (autoRefreshBox.isSelected()) {
                    refreshAction.actionPerformed(new ActionEvent(this, 0, "ignoreWarnings"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        };        
        autoRefreshBox.addChangeListener(new ChangeListener() {            
            public void stateChanged(ChangeEvent e) {
                new Thread(autoRefresh).start();
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
