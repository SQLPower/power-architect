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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.enterprise.client.RevisionController;
import ca.sqlpower.swingui.SPSUtils;

/**
 * This class is used to display revisions on the server, obtained through a client session, in a table.
 */
public class RevisionsTable extends JTable {
    
    private static final String[] HEADERS = {"Version", "Time Created", "Author", "Description"};
    
    private RevisionController session;
    
    private List<TransactionInformation> transactions = new ArrayList<TransactionInformation>();
    
    private JScrollPane scrollPane;
    
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
            infoArea.append("\nTime created: " + new Date(info.getTimeCreated()));
            String description = "";
            for (String line : info.getSimpleDescription().split(", ")) {
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
    
    private class NoFocusTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }
     }
    
    public RevisionsTable(RevisionController session, long fromRevision, long toRevision) {
        super();
        
        this.session = session;      
        
        this.setCellSelectionEnabled(false);
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(true);
        this.setShowVerticalLines(false);
        this.setShowHorizontalLines(false);        
        this.setAutoscrolls(false);
        this.setIntercellSpacing(new Dimension(0, 0));
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);      
        
        ListSelectionModel selectionModel = this.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);                       
         
        // Make it so there are no focus borders around individual cells.
        this.setDefaultRenderer(Object.class, new NoFocusTableCellRenderer());
        
        this.addMouseListener(new MouseAdapter() {            
            public void mouseClicked(MouseEvent e) {           
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && getSelectedRow() != -1) {  
                    int revisionNo = getSelectedRevisionNumber();
                    infoAction.actionPerformed(new ActionEvent(this, revisionNo, null));
                }
            }
        });
        
        scrollPane = new JScrollPane(this);
        scrollPane.setPreferredSize(new Dimension(2000, 2000));
        refreshRevisionsList(fromRevision, toRevision);
    }
    
    /**
     * Gets the revision list from the server and puts it into a table model.
     */
    public void refreshRevisionsList(long fromRevision, long toRevision) {               
        
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
            transactions = session.getTransactionList(fromRevision, toRevision);
            
            data = new String[transactions.size()][HEADERS.length];                      
            
            for (int i = 0; i < transactions.size(); i++) {                
                TransactionInformation transaction = transactions.get(i);
                data[i][0] = String.valueOf(transaction.getVersionNumber()) + " ";
                data[i][1] = TransactionInformation.DATE_FORMAT.format(transaction.getTimeCreated()) + " ";
                data[i][2] = transaction.getVersionAuthor() + " ";
                data[i][3] = transaction.getSimpleDescription();
                
                // Determine the length of each column by determining the longest piece of data within each.
                for (int j = 0; j < HEADERS.length; j++) {
                    int cellWidth = fm.stringWidth(data[i][j]);
                    if (cellWidth > maxWidth[j]) {
                        maxWidth[j] = cellWidth;
                    }
                }                
            }                   
            
        } catch (Throwable e) {
            throw new RuntimeException("Error getting revision list from server ", e);
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
            
            DefaultTableCellRenderer r = new NoFocusTableCellRenderer();
            r.setHorizontalAlignment(JLabel.RIGHT);
            this.getColumnModel().getColumn(0).setCellRenderer(r);
            
            // Set the scroll bar to the bottom of the list.
            // For some reason, it must be invoked later for it to work.
            final JScrollBar v = scrollPane.getVerticalScrollBar();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    v.setValue(v.getMaximum());      
                }
            });              
        }

        
    }
    
    public TransactionInformation getRevisionInformation(int revisionNo) {        
        
        for (TransactionInformation transaction : transactions) {
            if (transaction.getVersionNumber() == revisionNo) {
                return transaction;
            }
        }
        return null;
    }

    public int getSelectedRevisionNumber() {
        int headersColumn = 0;
        for (int i = 0; i < HEADERS.length; i++) {
            if (HEADERS[i].contains("Version")) {
                headersColumn = i;
            }
        }
        String number = (String) this.getModel().getValueAt(this.getSelectedRow(), headersColumn);
        return Integer.parseInt(number.trim());
    }
    
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

}
