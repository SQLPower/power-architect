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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;
import ca.sqlpower.architect.profile.output.ProfileColumn;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.action.SaveProfileAction;
import ca.sqlpower.architect.swingui.table.MultiFreqValueCountTableModel;
import ca.sqlpower.architect.swingui.table.ProfileJTable;
import ca.sqlpower.architect.swingui.table.ProfileTableModel;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.swingui.table.TableUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;

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
     * <p>
     * A listener attached to this frame ensures resources get cleaned up when the
     * frame is closed.
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
            if (evt.getClickCount() == 2 && obj instanceof JTable) {
                JTable t = (JTable)obj;
                SQLColumn col = (SQLColumn)t.getValueAt(t.getSelectedRow(),
                        t.convertColumnIndexToView(
                                ProfileColumn.valueOf("COLUMN").ordinal())); //$NON-NLS-1$
                profilePanel.getTableSelector().setSelectedItem(col.getParent());
                profilePanel.getColumnSelector().setSelectedValue(col,true);
                tabPane.setSelectedIndex(1);
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
     * The standard close action for this viewer.
     */
    private final Action closeAction = new AbstractAction(
            Messages.getString("ProfileResultsViewer.closeButton")) {
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
        }
    };

    /**
     * Creates but does not show a new profile result viewer dialog.
     */
    public ProfileResultsViewer(ProfileManager pm) {
        this.profileManager = pm;
        this.results = new ArrayList<TableProfileResult>();
        this.frame = new JFrame(Messages.getString("ProfileResultsViewer.frameTitle")); //$NON-NLS-1$
        frame.setIconImage(ASUtils.getFrameIconImage());
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                tm.cleanup();
                profileManager.removeProfileChangeListener(profileChangeListener);
            }
        });
        
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

        JPanel profilePanel = new JPanel(new BorderLayout());
        ProfilePanel p = new ProfilePanel(tm);
        p.setViewTable(viewTable);
        p.setTabPane(tabPane);
        p.setTableModel(tm);
        profilePanel.add(p, BorderLayout.CENTER);
        ButtonBarBuilder profileButtons = new ButtonBarBuilder();
        profileButtons.addGlue();
        profileButtons.addFixed(new JButton(closeAction));
        profilePanel.add(profileButtons.getPanel(), BorderLayout.SOUTH);
        tabPane.addTab(Messages.getString("ProfileResultsViewer.graphViewTab"), profilePanel); //$NON-NLS-1$
        
        JPanel tableViewPane = new JPanel(new BorderLayout());

        tableViewPane.add(editorScrollPane,BorderLayout.CENTER);

        JLabel searchLabel = new JLabel(Messages.getString("ProfileResultsViewer.search")); //$NON-NLS-1$
        JTextField searchField = new JTextField(searchDecorator.getDoc(),"",25); //$NON-NLS-1$
        searchField.setEditable(true);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        tableViewPane.add(searchPanel,BorderLayout.NORTH);
        ButtonBarBuilder tableButtons = new ButtonBarBuilder();
        tableButtons.addGlue();
        tableButtons.addFixed(new JButton(new SaveProfileAction(frame, 
                Messages.getString("ProfileResultsViewer.PDFExport"), viewTable, 
                SaveProfileAction.SaveableFileType.PDF)));
        tableButtons.addFixed(new JButton(new SaveProfileAction(frame, 
                Messages.getString("ProfileResultsViewer.CSVExport"), viewTable, 
                SaveProfileAction.SaveableFileType.CSV)));
        tableButtons.addFixed(new JButton(new SaveProfileAction(frame, 
                Messages.getString("ProfileResultsViewer.HTMLExport"), viewTable, 
                SaveProfileAction.SaveableFileType.HTML)));
        tableButtons.addFixed(new JButton(closeAction));
        tableViewPane.add(tableButtons.getPanel(), BorderLayout.SOUTH);
        tabPane.addTab(Messages.getString("ProfileResultsViewer.tableViewTab"), tableViewPane ); //$NON-NLS-1$
        
        final MultiFreqValueCountTableModel columnTableModel = new MultiFreqValueCountTableModel(tm);

        JTextField columnSearchField = new JTextField("",25); //$NON-NLS-1$
        final FancyExportableJTable columnTable = new FancyExportableJTable(columnTableModel, columnSearchField.getDocument());
        columnTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                TableUtils.fitColumnWidths(columnTable, 15);
            }
        });
        
        for (int i = 0; i < columnTableModel.getColumnCount(); i++) {
            columnTable.getColumnModel().getColumn(i).setCellRenderer(columnTableModel.getCellRenderer(i));
        }
        JPanel columnViewerPanel = new JPanel(new BorderLayout());
        columnViewerPanel.add(new JScrollPane(columnTable), BorderLayout.CENTER);
        
        JPanel columnSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        columnSearchPanel.add(new JLabel(Messages.getString("ProfileResultsViewer.search"))); //$NON-NLS-1$
        columnSearchPanel.add(columnSearchField);
        columnViewerPanel.add(columnSearchPanel, BorderLayout.NORTH);
        
        ButtonBarBuilder columnButtonBar = new ButtonBarBuilder();
        columnButtonBar.addGlue();
        final JButton csvExportButton = new JButton(columnTable.getExportCSVAction());
        csvExportButton.setText(Messages.getString("ProfileResultsViewer.CSVExport"));
        columnButtonBar.addFixed(csvExportButton);
        final JButton htmlExportButton = new JButton(columnTable.getExportHTMLAction());
        htmlExportButton.setText(Messages.getString("ProfileResultsViewer.HTMLExport"));
        columnButtonBar.addFixed(htmlExportButton);
        columnButtonBar.addFixed(new JButton(closeAction));
        columnViewerPanel.add(columnButtonBar.getPanel(), BorderLayout.SOUTH);
        tabPane.addTab(Messages.getString("ProfileResultsViewer.columnViewTab"), 
                columnViewerPanel);

        profilePanelMouseListener.setProfilePanel(p);

        if ( viewTable.getRowCount() > 0 ) {
            SQLColumn col = (SQLColumn)viewTable.getValueAt(0,
                    viewTable.convertColumnIndexToView(
                            ProfileColumn.valueOf("COLUMN").ordinal())); //$NON-NLS-1$
            p.getTableSelector().setSelectedItem(col.getParent());
            p.getColumnSelector().setSelectedValue(col,true);
        }
 
        frame.add(tabPane, BorderLayout.CENTER);
        
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
