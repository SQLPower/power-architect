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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;
import ca.sqlpower.architect.profile.output.ProfileColumn;
import ca.sqlpower.architect.swingui.ProfilePanel.ChartTypes;
import ca.sqlpower.architect.swingui.action.SaveProfileAction;
import ca.sqlpower.architect.swingui.table.ProfileJTable;
import ca.sqlpower.architect.swingui.table.ProfileTableModel;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.swingui.table.TableUtils;

/**
 * A class that manages a viewer component for a set of Profile Results.
 */
public class ProfileResultsViewer {

    private static final Logger logger = Logger.getLogger(ProfileResultsViewer.class);
    
    /**
     * The profile manager that owns the results this component views.
     */
    private final ProfileManager profileManager;

    /**
     * The results this viewer is viewing.
     */
    private final Collection<TableProfileResult> results;

    /**
     * The frame all the viewer stuff lives in.  Gets disposed automatically
     * when (and if) all the profiles this component is displaying are removed
     * from the profile manager.
     */
    private final JFrame frame;
    
    private final ProfileTableModel tm;
    
    /**
     * The listener responsible for watching over the Profile List.
     * It does:
     * - adding results to the TableModel (doesn't happen yet XXX)
     * - disposing this dialog if it becomes empty
     * due to profile results being removed from the manager.
     */
    private final ProfileChangeListener profileChangeListener = new ProfileChangeListener() {
        public void profilesAdded(ProfileChangeEvent e) {
            List<ProfileResult> profileResult = e.getProfileResults();
            logger.debug("ProfileResultsViewer.inner.profileAdded()"  + profileResult); //$NON-NLS-1$
            // XXX this doesn't get invoked!?
        }

        public void profileListChanged(ProfileChangeEvent event) {
            disposeIfEmpty();
        }

        public void profilesRemoved(ProfileChangeEvent e) {
            disposeIfEmpty();
        }
        
        /**
         * Disposes the dialog if it has become empty due to all the profiles
         * we're viewing being deleted from the profile manager.
         */
        private void disposeIfEmpty() {
            for (TableProfileResult tpr : results) {
                if (profileManager.getResults().contains(tpr)) {
                    // there's still a use for this viewer!
                    return;
                }
            }
            
            // we made it this far, so none of our profiles are in the manager anymore
            frame.dispose();
        }
    };

    /**
     * Double-click handler that switches to the column view when the user double-clicks
     * one in the table view.
     */
    private class ProfilePanelMouseListener extends MouseAdapter 
    {
        private ProfilePanel profilePanel;
        private JTabbedPane tabPane;

        public ProfilePanel getProfilePanel() {
            return profilePanel;
        }

        public void setProfilePanel(ProfilePanel profilePanel) {
            this.profilePanel = profilePanel;
        }

        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getClickCount() == 2) {
                if ( obj instanceof JTable ) {
                    JTable t = (JTable)obj;
                    SQLColumn col = (SQLColumn)t.getValueAt(t.getSelectedRow(),
                            t.convertColumnIndexToView(
                                    ProfileColumn.valueOf("COLUMN").ordinal())); //$NON-NLS-1$
                    profilePanel.getTableSelector().setSelectedItem(col.getParentTable());
                    profilePanel.getColumnSelector().setSelectedValue(col,true);
                    tabPane.setSelectedIndex(1);
                }
            }
        }

        public JTabbedPane getTabPane() {
            return tabPane;
        }

        public void setTabPane(JTabbedPane tabPane) {
            this.tabPane = tabPane;
        }
    }


    /**
     * Creates but does not show a new profile result viewer dialog.
     */
    public ProfileResultsViewer(ProfileManager profileManager) {
        this.profileManager = profileManager;
        this.results = new ArrayList<TableProfileResult>();
        this.frame = new JFrame(Messages.getString("ProfileResultsViewer.frameTitle")); //$NON-NLS-1$
        frame.setIconImage(ASUtils.getFrameIconImage());
        
        JTabbedPane tabPane = new JTabbedPane();

        tm = new ProfileTableModel(profileManager);
        profileManager.addProfileChangeListener(profileChangeListener);

        TableModelSearchDecorator searchDecorator =
            new TableModelSearchDecorator(tm);
        TableModelSortDecorator tableModelSortDecorator =
            new TableModelSortDecorator(searchDecorator);
        final ProfileJTable viewTable =
            new ProfileJTable(tableModelSortDecorator);
        searchDecorator.setTableTextConverter(viewTable);
        TableModelColumnAutofit columnAutoFit =
            new TableModelColumnAutofit(tableModelSortDecorator, viewTable);

        JTableHeader tableHeader = viewTable.getTableHeader();
        tableModelSortDecorator.setTableHeader(tableHeader);
        columnAutoFit.setTableHeader(tableHeader);

        viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ProfilePanelMouseListener profilePanelMouseListener =
            new ProfilePanelMouseListener();
        profilePanelMouseListener.setTabPane(tabPane);
        viewTable.addMouseListener( profilePanelMouseListener);
        viewTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                TableUtils.fitColumnWidths(viewTable, 2);
            }
        });
        JScrollPane editorScrollPane = new JScrollPane(viewTable);
        editorScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(800, 600));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));

        ProfilePanel p = new ProfilePanel(tm);
        p.setViewTable(viewTable);
        p.setTabPane(tabPane);
        p.setTableModel(tm);
        tabPane.addTab(Messages.getString("ProfileResultsViewer.graphViewTab"),p); //$NON-NLS-1$
        
        JPanel tableViewPane = new JPanel(new BorderLayout());

        tableViewPane.add(editorScrollPane,BorderLayout.CENTER);

        JLabel searchLabel = new JLabel(Messages.getString("ProfileResultsViewer.search")); //$NON-NLS-1$
        JTextField searchField = new JTextField(searchDecorator.getDoc(),"",25); //$NON-NLS-1$
        searchField.setEditable(true);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        tableViewPane.add(searchPanel,BorderLayout.NORTH);
        tabPane.addTab(Messages.getString("ProfileResultsViewer.tableViewTab"), tableViewPane ); //$NON-NLS-1$
        

        profilePanelMouseListener.setProfilePanel(p);
        p.setChartType(ChartTypes.PIE);

        if ( viewTable.getRowCount() > 0 ) {
            SQLColumn col = (SQLColumn)viewTable.getValueAt(0,
                    viewTable.convertColumnIndexToView(
                            ProfileColumn.valueOf("COLUMN").ordinal())); //$NON-NLS-1$
            p.getTableSelector().setSelectedItem(col.getParentTable());
            p.getColumnSelector().setSelectedValue(col,true);
        }
 
        frame.add(tabPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton(
                new SaveProfileAction(frame, viewTable));
        bottomPanel.add(save);
        JButton closeButton = new JButton(Messages.getString("ProfileResultsViewer.closeButton")); //$NON-NLS-1$
        bottomPanel.add(closeButton);
        frame.add(bottomPanel,BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); 
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        SPSUtils.makeJDialogCancellable(frame, null);
    }
    
    public void addTableProfileResult(TableProfileResult result) {
        results.add(result);
        tm.refresh();
    }
    
    public void addTableProfileResultToScan(TableProfileResult result) {
        tm.addTableResultToScan(result);
    }

    public void removeTableProfileResultToScan(TableProfileResult result) {
        tm.removeTableResultToScan(result);
    }
    
    public void clearScanList() {
        tm.clearScanList();
    }

    public JFrame getDialog() {
        return frame;
    }

}
