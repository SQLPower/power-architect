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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.swingui.SPSUtils;

/**
 * This class is used to display revisions on the server, obtained through a client session, in a table.
 */
public class RevisionsTable extends JTable {
    
    private static final String[] HEADERS = {"Version", "Time Created", "Author", "Description"};
    
    private ArchitectClientSideSession session;
    
    private List<TransactionInformation> transactions = new ArrayList<TransactionInformation>();
    
    /**
     * Displays information about a revision when it is double clicked
     */
    private final Action infoAction = new AbstractAction("Information") {
        public void actionPerformed(ActionEvent e) {
            int revisionNo = e.getID();
            
            final JDialog d = SPSUtils.makeOwnedDialog(RevisionsTable.this.getParent(), "Revision " + revisionNo);                      
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(new RevisionInformationPanel(revisionNo));
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.setPreferredSize(new Dimension(600, 300));
            d.pack();
            d.setLocationRelativeTo(RevisionsTable.this.getParent());
            d.setVisible(true);       
        }
    };
    
    /**
     * A simple component that displays the information of a revision in a scrolling text area.
     */
    private class RevisionInformationPanel extends JPanel {
        
        public RevisionInformationPanel(int revisionNo) {
            
            super(new BorderLayout());
            TransactionInformation info = RevisionsTable.this.getRevisionInformation(revisionNo);
            JTextArea infoArea = new JTextArea();
            infoArea.setMargin(new Insets(6, 10, 4, 6));
            infoArea.setEditable(false);                  
            infoArea.append("Revision " + revisionNo);
            infoArea.append("\n\nAuthor: " + info.getVersionAuthor());
            infoArea.append("\nTime created: " + info.getTimeCreated().toString());
            String description = "";
            for (String line : info.getVersionDescription().split(", ")) {
                description += "\n" + line;
            }
            infoArea.append("\n\nDescription:" + description);
            final JScrollPane sp = new JScrollPane(infoArea);
            this.add(sp);
            
            // Reset scroll bars
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sp.getHorizontalScrollBar().setValue(sp.getHorizontalScrollBar().getMinimum());
                    sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMinimum());                    
                }
            });
            
        }
        
    }
    
    /**
     * Creates a RevisionsTable with a custom mouse listener, or none.
     * @param session
     * @param listener
     */
    public RevisionsTable(ArchitectClientSideSession session) {
        super();
        
        this.session = session;
        
        this.setColumnSelectionAllowed(false);
        this.setShowVerticalLines(false);
        this.setShowHorizontalLines(false);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);       
        
        refreshRevisionsList();
        
        ListSelectionModel selectionModel = this.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.addMouseListener(new MouseAdapter() {            
            public void mouseClicked(MouseEvent e) {           
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && getSelectedRow() != -1) {  
                    int revisionNo = getSelectedRevisionNumber();
                    infoAction.actionPerformed(new ActionEvent(this, revisionNo, null));
                }
            }
        });
    }
    
    /**
     * Gets the revision list from the server and puts it into a table model.
     */
    public void refreshRevisionsList() {               
        
        String[][] data = {{""}};
        int selected = this.getSelectedRow();
        
        // Determine the length of each of the headers by using the DefaultTableCellRenderer's FontMetrics
        Font f = new DefaultTableCellRenderer().getFont();
        FontMetrics fm = new DefaultTableCellRenderer().getFontMetrics(f); 
        int[] maxWidth = new int[HEADERS.length];
        for (int i = 0; i < HEADERS.length; i++) {
            maxWidth[i] = fm.stringWidth(HEADERS[i]);
        }
      
        try {
            transactions = session.getTransactionList();
            
            data = new String[transactions.size()][HEADERS.length];                      
            
            for (int i = 0; i < transactions.size(); i++) {                
                TransactionInformation transaction = transactions.get(i);
                data[i][0] = String.valueOf(transaction.getVersionNumber());
                data[i][1] = transaction.getTimeString() + " ";
                data[i][2] = transaction.getVersionAuthor();
                data[i][3] = transaction.getVersionDescription();
                
                // Determine the length of each column by determining the longest piece of data within each.
                for (int j = 0; j < HEADERS.length; j++) {
                    int cellWidth = fm.stringWidth(data[i][j]);
                    if (cellWidth > maxWidth[j]) {
                        maxWidth[j] = cellWidth;
                    }
                }                
            }                   
            
        } catch (Throwable e) {
            throw new RuntimeException("Error getting revision list from server: " + e);
        } finally {
            this.setModel(new DefaultTableModel(data, HEADERS) {
                public boolean isCellEditable(int x, int y) {
                    return false;
                }
            });
            
            for (int i = 0; i < HEADERS.length; i++) {
                this.getColumnModel().getColumn(i).setPreferredWidth(maxWidth[i]);
            }
            
            if (selected < data.length && selected != -1) {
                this.setRowSelectionInterval(selected, selected);
            }
        }

        
    }
    
    public TransactionInformation getRevisionInformation(int revisionNo) {        
        
        return transactions.get(revisionNo);
        
    }

    public int getSelectedRevisionNumber() {
        int headersColumn = 0;
        for (int i = 0; i < HEADERS.length; i++) {
            if (HEADERS[i].contains("Version")) {
                headersColumn = i;
            }
        }
        return Integer.parseInt((String) this.getModel().getValueAt(this.getSelectedRow(), headersColumn));
    }

}
