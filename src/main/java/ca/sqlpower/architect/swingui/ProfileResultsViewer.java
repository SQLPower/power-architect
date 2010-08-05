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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;
import ca.sqlpower.architect.profile.output.ProfileColumn;
import ca.sqlpower.architect.swingui.action.SaveProfileAction;
import ca.sqlpower.architect.swingui.table.MultiFreqValueCountTableModel;
import ca.sqlpower.architect.swingui.table.ProfileJTable;
import ca.sqlpower.architect.swingui.table.ProfileTableModel;
import ca.sqlpower.architect.swingui.table.TableFilterDecorator;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.TimedDocumentListener;
import ca.sqlpower.swingui.table.DateTableCellRenderer;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.PercentTableCellRenderer;
import ca.sqlpower.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.swingui.table.TableUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
     * The frame all the viewer stuff lives in. Gets disposed automatically when
     * (and if) all the profiles this component is displaying are removed from
     * the profile manager.
     * <p>
     * A listener attached to this frame ensures resources get cleaned up when
     * the frame is closed.
     */
    private final JFrame frame;

    /**
     * The listener responsible for listening to changes to the table notes text
     * area, and updating the object model accordingly.
     */
    private TimedDocumentListener tableNotesFieldListener;
    
    private TableProfileResult currentTable;

    private final ProfileTableModel tm;

    /**
     * The listener responsible for watching over the Profile List. It does: -
     * adding results to the TableModel (doesn't happen yet XXX) - disposing
     * this dialog if it becomes empty due to profile results being removed from
     * the manager.
     */
    private final ProfileChangeListener profileChangeListener = new ProfileChangeListener() {
        public void profilesAdded(ProfileChangeEvent e) {
            List<ProfileResult> profileResult = e.getProfileResults();
            logger.debug("ProfileResultsViewer.inner.profileAdded()" + profileResult); //$NON-NLS-1$
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

            // we made it this far, so none of our profiles are in the manager
            // anymore
            frame.dispose();
        }
    };

    /**
     * Double-click handler that switches to the column view when the user
     * double-clicks one in the table view.
     */
    private class ProfilePanelMouseListener extends MouseAdapter {
        private ProfilePanel profilePanel;

        private JTabbedPane tabPane;

        public void setProfilePanel(ProfilePanel profilePanel) {
            this.profilePanel = profilePanel;
        }

        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getClickCount() == 2 && obj instanceof JTable) {
                JTable t = (JTable) obj;
                SQLColumn col = (SQLColumn) t.getValueAt(t.getSelectedRow(), t.convertColumnIndexToView(ProfileColumn
                        .valueOf("COLUMN").ordinal())); //$NON-NLS-1$
                profilePanel.getTableSelector().setSelectedItem(col.getParent());
                profilePanel.getColumnSelector().setSelectedValue(col, true);
                tabPane.setSelectedIndex(0);
            }
        }

        public void setTabPane(JTabbedPane tabPane) {
            this.tabPane = tabPane;
        }
    }

    private class ComboBoxSynchronizationListener implements ActionListener {

        private JComboBox target;

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JComboBox) {
                if (target != null) {
                    if (target.getSelectedItem() == null ? ((JComboBox) e.getSource()).getSelectedItem() != null
                            : !target.getSelectedItem().equals(((JComboBox) e.getSource()).getSelectedItem())) {
                        target.setSelectedItem(((JComboBox) e.getSource()).getSelectedItem());
                    }
                }
            }
        }

        public void setTarget(JComboBox target) {
            this.target = target;
        }
    }

    /**
     * The standard close action for this viewer.
     */
    private final Action closeAction = new AbstractAction(Messages.getString("ProfileResultsViewer.closeButton")) {
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
        }
    };

    private JComboBox tableSelector;

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

        // The UI for this window is shown in three tabs. Each tab is defined in
        // its own block, both for readability, and to prevent too much
        // interdependency between them.
        JTabbedPane tabPane = new JTabbedPane();

        tm = new ProfileTableModel(profileManager);
        profileManager.addProfileChangeListener(profileChangeListener);

        // This listener monitors for double clicks in the table view tab, and
        // responds by showing the appropriate column in the column view tab.
        ProfilePanelMouseListener profilePanelMouseListener = new ProfilePanelMouseListener();

        // These listeners keep the two table selectors (one on the graph tab,
        // and one on the table view tab) in sync
        ComboBoxSynchronizationListener graphPanelListener = new ComboBoxSynchronizationListener();
        ComboBoxSynchronizationListener tableViewListener = new ComboBoxSynchronizationListener();

        // The first tab is the column specific tab. It contains a ProfilePanel,
        // which contains a ProfileGraphPanel, which displays the column
        // information and pie chart.
        {
            JPanel profilePanel = new JPanel(new BorderLayout());
            final ProfilePanel p = new ProfilePanel(tm, profileManager);

            p.setTabPane(tabPane);
            p.setTableModel(tm);
            profilePanel.add(p, BorderLayout.CENTER);
            ButtonBarBuilder2 profileButtons = new ButtonBarBuilder2();
            profileButtons.addGlue();
            profileButtons.addFixed(new JButton(closeAction));
            profilePanel.add(profileButtons.getPanel(), BorderLayout.SOUTH);
            tabPane.addTab(Messages.getString("ProfileResultsViewer.graphViewTab"), profilePanel); //$NON-NLS-1$

            profilePanelMouseListener.setProfilePanel(p);
            p.getTableSelector().addActionListener(graphPanelListener);
            tableViewListener.setTarget(p.getTableSelector());

            frame.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                }
                @Override
                public void windowIconified(WindowEvent e) {
                }
                @Override
                public void windowDeiconified(WindowEvent e) {
                }
                @Override
                public void windowDeactivated(WindowEvent e) {
                }
                @Override
                public void windowClosing(WindowEvent e) {
                }
                @Override
                public void windowClosed(WindowEvent e) {
                    p.close();
                    if (tableNotesFieldListener != null) {
                        tableNotesFieldListener.cancel();
                    }
                }
                @Override
                public void windowActivated(WindowEvent e) {
                }
            });
        }

        // This tab displays a table that shows data for all columns from the
        // selected table, or all tables.
        {
            TableModelSearchDecorator searchDecorator = new TableModelSearchDecorator(tm);
            final TableFilterDecorator filterTableModel = new TableFilterDecorator(searchDecorator);
            TableModelSortDecorator tableModelSortDecorator = new TableModelSortDecorator(filterTableModel);
            final ProfileJTable viewTable = new ProfileJTable(tableModelSortDecorator);
            searchDecorator.setTableTextConverter(viewTable);
            TableModelColumnAutofit columnAutoFit = new TableModelColumnAutofit(tableModelSortDecorator, viewTable);

            JTableHeader tableHeader = viewTable.getTableHeader();
            tableModelSortDecorator.setTableHeader(tableHeader);
            columnAutoFit.setTableHeader(tableHeader);

            viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            profilePanelMouseListener.setTabPane(tabPane);
            viewTable.addMouseListener(profilePanelMouseListener);
            viewTable.getModel().addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    TableUtils.fitColumnWidths(viewTable, 2);
                }
            });
            JScrollPane editorScrollPane = new JScrollPane(viewTable);
            editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(800, 600));
            editorScrollPane.setMinimumSize(new Dimension(10, 10));

            JPanel tableViewPane = new JPanel(new BorderLayout());

            final JTextArea notesField;
            if (profileManager.getWorkspaceContainer() instanceof ArchitectSession &&
                    ((ArchitectSession) profileManager.getWorkspaceContainer()).isEnterpriseSession()) {

                notesField = new JTextArea();
                notesField.setEnabled(false); // Kind of a hack, but the default selection is all tables, so this shoudn't be enabled
                notesField.setText("Select a Table");
                JScrollPane notesScroll = new JScrollPane(notesField);
                notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                notesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                notesScroll.setMinimumSize(new Dimension(0, 50));
                JPanel notesPanel = new JPanel(new BorderLayout());
                notesPanel.add(new JLabel("Table Profile Notes:"), BorderLayout.NORTH);
                notesPanel.add(notesScroll, BorderLayout.CENTER);

                JSplitPane tableViewSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                tableViewSplit.setTopComponent(editorScrollPane);
                tableViewSplit.setBottomComponent(notesPanel);

                tableViewPane.add(tableViewSplit, BorderLayout.CENTER);
            } else {
                tableViewPane.add(editorScrollPane);
                notesField = null;
            }

            JPanel searchPanel = new JPanel();
            {
                FormLayout layout = new FormLayout("4dlu, pref, 4dlu, pref, 4dlu, pref:grow, 4dlu, pref, 4dlu, pref, 4dlu", "pref");
                DefaultFormBuilder builder = new DefaultFormBuilder(layout, searchPanel);
                CellConstraints cc = new CellConstraints();
                
                JLabel tableLabel = new JLabel("Table:");
                tableSelector = new JComboBox();
                tableSelector.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        TableProfileResult tpr = (TableProfileResult) value;
                        StringBuffer buf = new StringBuffer();
                        if (tpr != null) {
                            buf.append(tpr.getProfiledObject().getName());
                            buf.append(" (");
                            DateFormat df = DateFormat.getDateTimeInstance();
                            buf.append(df.format(new Date(tpr.getCreateStartTime())));
                            buf.append(")");
                        } else {
                            buf.append("All");
                        }
                        return super.getListCellRendererComponent(list, buf.toString(), index, isSelected,
                                cellHasFocus);
                    }
                });
                tableSelector.addActionListener(new ActionListener() {
                    private AbstractSPListener tableNotesListener;

                    public void actionPerformed(ActionEvent e) {
                        final TableProfileResult tpr = (TableProfileResult) tableSelector.getSelectedItem();
                        filterTableModel.setFilter(tpr);
                        
                        if (notesField != null) {
                            if (tableNotesFieldListener != null) {
                                notesField.getDocument().removeDocumentListener(tableNotesFieldListener);
                                tableNotesFieldListener.cancel();
                            }
                            if (currentTable != null) {
                                currentTable.removeSPListener(tableNotesListener);
                            }
                            currentTable = tpr;
                            if (tpr != null) {
                                notesField.setText(tpr.getNotes());
                                tableNotesListener = new AbstractSPListener() {
                                    @Override
                                    public void propertyChanged(PropertyChangeEvent evt) {
                                        if ("notes".equals(evt.getPropertyName())) {
                                            if (!evt.getNewValue().equals(notesField.getText())) {
                                                notesField.setText((String) evt.getNewValue());
                                            }
                                        }
                                    }
                                };
                                tpr.addSPListener(tableNotesListener);
                                tableNotesFieldListener = new TimedDocumentListener(2500) {
                                    @Override
                                    public void textChanged() {
                                        profileManager.getRunnableDispatcher().runInForeground(new Runnable() {
                                            public void run() {
                                                if (!tpr.getNotes().equals(notesField.getText())) {
                                                    tpr.setNotes(notesField.getText());
                                                }
                                            }
                                        });
                                    }
                                };
                                notesField.setEnabled(true);
                            } else {
                                notesField.setEnabled(false);
                                notesField.setText("Select a Table");
                            }
                            notesField.getDocument().addDocumentListener(tableNotesFieldListener);
                        }
                    }
                });
                tableSelector.addActionListener(tableViewListener);
                graphPanelListener.setTarget(tableSelector);
                
                JLabel searchLabel = new JLabel(Messages.getString("ProfileResultsViewer.search")); //$NON-NLS-1$
                JTextField searchField = new JTextField(searchDecorator.getDoc(), "", 25); //$NON-NLS-1$
                searchField.setEditable(true);
                
                builder.add(tableLabel, cc.xy(2, 1));
                builder.add(tableSelector, cc.xy(4, 1));
                builder.add(searchLabel, cc.xy(8, 1));
                builder.add(searchField, cc.xy(10, 1));
            }
            
            tableViewPane.add(searchPanel, BorderLayout.NORTH);
            ButtonBarBuilder2 tableButtons = new ButtonBarBuilder2();
            tableButtons.addGlue();
            tableButtons.addFixed(new JButton(new SaveProfileAction(frame, Messages
                    .getString("ProfileResultsViewer.PDFExport"), viewTable, SaveProfileAction.SaveableFileType.PDF)));
            tableButtons.addFixed(new JButton(new SaveProfileAction(frame, Messages
                    .getString("ProfileResultsViewer.CSVExport"), viewTable, SaveProfileAction.SaveableFileType.CSV)));
            tableButtons
                    .addFixed(new JButton(new SaveProfileAction(frame, Messages
                            .getString("ProfileResultsViewer.HTMLExport"), viewTable,
                            SaveProfileAction.SaveableFileType.HTML)));
            tableButtons.addFixed(new JButton(closeAction));
            tableViewPane.add(tableButtons.getPanel(), BorderLayout.SOUTH);
            tabPane.addTab(Messages.getString("ProfileResultsViewer.tableViewTab"), tableViewPane); //$NON-NLS-1$
        }

        // This tab displays a table with top value information from all columns.
        {
            final MultiFreqValueCountTableModel columnTableModel = new MultiFreqValueCountTableModel(tm);

            JTextField columnSearchField = new JTextField("", 25); //$NON-NLS-1$
            final FancyExportableJTable columnTable = new FancyExportableJTable(columnTableModel, columnSearchField
                    .getDocument());
            columnTableModel.addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    TableUtils.fitColumnWidths(columnTable, 15);
                }
            });
            columnTable.setColumnFormatter(4, new PercentTableCellRenderer(false).getFormat());
            columnTable.setColumnFormatter(5, new DateTableCellRenderer().getFormat());

            for (int i = 0; i < columnTableModel.getColumnCount(); i++) {
                columnTable.getColumnModel().getColumn(i).setCellRenderer(columnTableModel.getCellRenderer(i));
            }
            JPanel columnViewerPanel = new JPanel(new BorderLayout());
            columnViewerPanel.add(new JScrollPane(columnTable), BorderLayout.CENTER);

            JPanel columnSearchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            columnSearchPanel.add(new JLabel(Messages.getString("ProfileResultsViewer.search"))); //$NON-NLS-1$
            columnSearchPanel.add(columnSearchField);
            columnViewerPanel.add(columnSearchPanel, BorderLayout.NORTH);

            ButtonBarBuilder2 columnButtonBar = new ButtonBarBuilder2();
            columnButtonBar.addGlue();
            final JButton csvExportButton = new JButton(columnTable.getExportCSVAction());
            csvExportButton.setText(Messages.getString("ProfileResultsViewer.CSVExport"));
            columnButtonBar.addFixed(csvExportButton);
            final JButton htmlExportButton = new JButton(columnTable.getExportHTMLAction());
            htmlExportButton.setText(Messages.getString("ProfileResultsViewer.HTMLExport"));
            columnButtonBar.addFixed(htmlExportButton);
            columnButtonBar.addFixed(new JButton(closeAction));
            columnViewerPanel.add(columnButtonBar.getPanel(), BorderLayout.SOUTH);
            tabPane.addTab(Messages.getString("ProfileResultsViewer.columnViewTab"), columnViewerPanel);
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
        List<TableProfileResult> profileResults = new ArrayList<TableProfileResult>(tm.getTableResultsToScan());
        profileResults.add(0, null);
        tableSelector.setModel(new DefaultComboBoxModel(profileResults.toArray()));
    }

    public void removeTableProfileResultToScan(TableProfileResult result) {
        tm.removeTableResultToScan(result);
        tableSelector.setModel(new DefaultComboBoxModel(tm.getTableResultsToScan().toArray()));
    }

    public void clearScanList() {
        tm.clearScanList();
    }

    public JFrame getDialog() {
        return frame;
    }

}
