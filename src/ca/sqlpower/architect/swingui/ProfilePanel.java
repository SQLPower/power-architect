/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.output.ProfileColumn;
import ca.sqlpower.architect.swingui.table.ProfileTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Let the user choose a column for profiling and display the Profile results
 */
public class ProfilePanel extends JPanel {
    static Logger logger = Logger.getLogger(ProfilePanel.class);
    public enum ChartTypes { BAR, PIE }

    
    private final ProfileTableModel profileTableModel;
    private JComboBox tableSelector;
    private JList columnSelector;
    private ChartTypes chartType = ChartTypes.PIE;
    
    private JPanel controlsArea;
    private ProfileGraphPanel displayPanel;
    
    private final JProgressBar progressBar = new JProgressBar();
    
    private JTable viewTable;
    private JTabbedPane tabPane;
    private ProfileTableModel tableModel;
    private TableModelListener listener = new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
            resetTableSelectorModel();
        }
    };
    
    public ProfilePanel(ProfileTableModel profileTableModel) {
        this.profileTableModel = profileTableModel;
        displayPanel = new ProfileGraphPanel(this, 0);
        setup();
    }
    
    private void setup() {
        progressBar.setVisible(false);
        FormLayout controlsLayout = new FormLayout(
                "4dlu,fill:min(150dlu;default):grow, 4dlu", // columns
                "default, 4dlu, fill:min(200dlu;default):grow,4dlu,default"); // rows

        CellConstraints cc = new CellConstraints();
        setLayout(new BorderLayout());
        controlsArea = logger.isDebugEnabled()  ? new FormDebugPanel(controlsLayout) : new JPanel(controlsLayout);
        controlsArea.setLayout(new BoxLayout(controlsArea, BoxLayout.Y_AXIS));
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
                }
                return super.getListCellRendererComponent(list, buf.toString(), index, isSelected,
                		cellHasFocus);
            }
        });
        tableSelector.addActionListener(new ActionListener() {

            /*
             * Called when the user selects a table; displays its profile (fast)
             */
            public void actionPerformed(ActionEvent e) {
                final TableProfileResult tpr = (TableProfileResult) tableSelector.getSelectedItem();
                if (tpr == null) {
                    return;
                }
                try {
                    List<SQLColumn> columns = new ArrayList<SQLColumn>();
                    for (ColumnProfileResult cpr : tpr.getColumnProfileResults() ) {
                            SQLColumn column = cpr.getProfiledObject();
                            columns.add(column);
                    }
                    
                    SQLColumn selectedColumn = null;
                    if ( columnSelector.getSelectedIndex() >= 0 ) {
                        selectedColumn = (SQLColumn) columnSelector.getSelectedValues()[0];
                    }

                    columnSelector.setModel(new DefaultComboBoxModel(columns.toArray()));
                    
                    if ( columns.size() > 0 ) {
                        if ( selectedColumn != null && columns.contains(selectedColumn)) {
                            columnSelector.setSelectedValue(selectedColumn,true);
                        } else {
                            columnSelector.setSelectedIndex(0);
                        }
                    }
                } catch (Exception ex) {
                    ASUtils.showExceptionDialogNoReport(ProfilePanel.this, "Error in profile", ex);
                }
            }

        });
        columnSelector = new JList();
        
        // TODO maybe someday we can allow the user to compare profiles by removing this...
        columnSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        columnSelector.addListSelectionListener(new ListSelectionListener() {
            /*
             * Called when the user selects a column; gets its profile (fast)
             */
            public void valueChanged(ListSelectionEvent e) {
                SQLColumn col = (SQLColumn) columnSelector.getSelectedValue();
                if (col == null) {
                    logger.debug("Null selection in columnSelector.ListSelectionListener");
                    return;
                }
                for (ProfileResult pr : tableModel.getResultList() ) {
                    if (pr instanceof ColumnProfileResult) {
                        SQLColumn column = (SQLColumn)pr.getProfiledObject();
                        if (col == column) {
                            displayPanel.displayProfile((ColumnProfileResult) pr);
                            break; 
                        }
                    }
                }
            }           
        });
        columnSelector.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    SQLColumn col = (SQLColumn)columnSelector.getSelectedValue();
                    for ( int i=0; i<viewTable.getRowCount(); i++ ) {
                        if ( col == viewTable.getValueAt(i,
                                viewTable.convertColumnIndexToView( ProfileColumn.valueOf("COLUMN").ordinal()))) {
                            viewTable.setRowSelectionInterval(i,i);
                            break;
                        }
                    }
                    tabPane.setSelectedIndex(0);
                }                
            }

            public void mousePressed(MouseEvent e) {
                // don't care
            }

            public void mouseReleased(MouseEvent e) {
                // don't care
            }

            public void mouseEntered(MouseEvent e) {
                // don't care
            }

            public void mouseExited(MouseEvent e) {
                // don't care
            }
        });
        

        PanelBuilder pb = new PanelBuilder(controlsLayout,controlsArea);
        pb.setDefaultDialogBorder();

        pb.add(tableSelector, cc.xy(2, 1));
        
        pb.add(new JScrollPane(columnSelector), cc.xy(2, 3));
        pb.add(progressBar,cc.xy(2,5));
        this.add(controlsArea, BorderLayout.WEST);
        displayPanel.setChartType(chartType);
        this.add(displayPanel.getDisplayArea(), BorderLayout.CENTER);

    }

 
    public ChartTypes getChartType() {
        return chartType;
    }
    public void setChartType(ChartTypes chartType) {
        this.chartType = chartType;
    }
    public JList getColumnSelector() {
        return columnSelector;
    }
    public void setColumnSelector(JList columnSelector) {
        this.columnSelector = columnSelector;
    }
    public JComboBox getTableSelector() {
        return tableSelector;
    }
    public void setTableSelector(JComboBox tableSelector) {
        this.tableSelector = tableSelector;
    }
    public JTable getViewTable() {
        return viewTable;
    }
    public void setViewTable(JTable viewTable) {
        this.viewTable = viewTable;
    }
    public JTabbedPane getTabPane() {
        return tabPane;
    }
    public void setTabPane(JTabbedPane tabPane) {
        this.tabPane = tabPane;
    }

    public ProfileTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(ProfileTableModel tableModel) {        
        if ( this.tableModel != null ) {
            this.tableModel.removeTableModelListener(listener);
        }
        this.tableModel = tableModel;
        if ( this.tableModel != null ) {
            this.tableModel.addTableModelListener(listener);
        }
        resetTableSelectorModel();
    }
    
    public void resetTableSelectorModel() {
        List<TableProfileResult> tableResults = new ArrayList<TableProfileResult>();
        TableProfileResult selectedResult = null;
        
        if ( tableSelector.getSelectedIndex() >= 0 ) {
            selectedResult = (TableProfileResult) tableSelector.getSelectedObjects()[0];
        }
        
        for (TableProfileResult pr : profileTableModel.getTableResultsToScan()) {
            tableResults.add(pr);
        }
        tableSelector.setModel(new DefaultComboBoxModel(tableResults.toArray()));

        
        if ( tableSelector.getModel().getSize() > 0 ) {
            if ( selectedResult != null && tableResults.contains(selectedResult)) {
                tableSelector.setSelectedItem(selectedResult);
            } else {
                tableSelector.setSelectedIndex(0);
            }
        }
    }
}

