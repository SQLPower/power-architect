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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RevisionListPanel {
    
    private static final String[] headers = {"Version", "Time Created", "Author", "Description"};

    private final Component dialogOwner;
    private final ArchitectClientSideSession session;
    private final ArchitectSwingSession swingSession;
    private final UserPrompter revertPrompt;    
    private final Action closeAction;
    
    private List<TransactionInformation> transactions;
    
    private final JTable revisionsTable;
    private final JPanel panel;
    
    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {
            refreshRevisionsList();
        }
    };
    
    private final Action revertAction = new AbstractAction("Revert...") {
        public void actionPerformed(ActionEvent e) {
            int revisionNo = Integer.parseInt((String) revisionsTable.getValueAt(revisionsTable.getSelectedRow(), 0));
            int response = JOptionPane.showConfirmDialog(dialogOwner, 
                    "Are you sure you would like to revert to version " + revisionNo,
                    "Revert...", JOptionPane.OK_CANCEL_OPTION);            
            if (response == JOptionPane.OK_OPTION) {
                try {
                    session.revertServerWorkspace(revisionNo);
                    refreshRevisionsList();
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
            JOptionPane.showMessageDialog(dialogOwner, "This feature is not yet supported. Try again soon!");
        }
    };  
    
    private void refreshRevisionsList() {               
        
        try {
            transactions = session.getTransactionList();
        } catch (Throwable e) {
            throw new RuntimeException("Error getting revision list from server: " + e);
        }
                
        String[][] data = new String[transactions.size()][4];
        
        for (int i = 0; i < transactions.size(); i++) {
            TransactionInformation transaction = transactions.get(i);
            data[i][0] = String.valueOf(transaction.getVersionNumber());
            data[i][1] = transaction.getTimeCreated().toString();
            data[i][2] = transaction.getVersionAuthor();
            data[i][3] = transaction.getVersionDescription();
        }        
        
        revisionsTable.setModel(new DefaultTableModel(data, headers) {
            public boolean isCellEditable(int x, int y) {
                return false;
            }
        });
        
    }
    
    public RevisionListPanel(ArchitectSwingSession swingSession, ArchitectFrame architectFrame, Action closeAction) {
        
        this.dialogOwner = architectFrame;
        this.swingSession = swingSession;
        this.session = swingSession.getEnterpriseSession();
        this.closeAction = closeAction;
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref:grow, 5dlu, pref",
                "pref, pref, pref"));              
        
        revisionsTable = new JTable();
        revisionsTable.setColumnSelectionAllowed(false);
        revisionsTable.setShowVerticalLines(false);
        revisionsTable.setShowHorizontalLines(false);
                
        refreshRevisionsList();
                
        ListSelectionModel selectionModel = revisionsTable.getSelectionModel();
        selectionModel.setSelectionInterval(transactions.size(), transactions.size());
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        revisionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {                    
                    revertAction.actionPerformed(null);
                }
            }
        });

        JScrollPane revisionsPane = new JScrollPane(revisionsTable);        
        
        CellConstraints cc = new CellConstraints();
        builder.add(new JLabel("Revisions:"), cc.xyw(3, 1, 2));
        builder.nextLine();
        builder.add(revisionsPane, cc.xywh(3, 2, 1, 2));
        
        revertPrompt = swingSession.createUserPrompter("Are you sure you would like the server to revert to version {0}?", 
                UserPromptType.BOOLEAN, 
                UserPromptOptions.OK_CANCEL, 
                UserPromptResponse.OK, 
                UserPromptResponse.OK, 
                "OK", "Cancel");
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(revertAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(compareAction));
        buttonBarBuilder.append(new JButton(closeAction));
        builder.add(buttonBarBuilder.getPanel(), cc.xy(5, 2));
        builder.setDefaultDialogBorder();
        panel = builder.getPanel();
        panel.setPreferredSize(new Dimension(700, 250));
        
    }
    
    public JPanel getPanel() {
        return panel;
    }

}
