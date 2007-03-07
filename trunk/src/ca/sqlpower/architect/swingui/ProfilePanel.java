package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
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

    private final TableProfileManager profileManager;
    
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
    
    public ProfilePanel(TableProfileManager pm) {
        this.profileManager = pm;
        displayPanel = new ProfileGraphPanel(this, 0, pm);
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
        tableSelector.addActionListener(new ActionListener() {

            /*
             * Called when the user selects a table; displays its profile (fast)
             */
            public void actionPerformed(ActionEvent e) {
                final SQLTable t = (SQLTable) tableSelector.getSelectedItem();
                if (t == null) {
                    return;
                }
                try {
                    
                    List<SQLColumn> columns = new ArrayList<SQLColumn>();
                    for (ProfileResult pr : tableModel.getResultList() ) {
                        if (pr instanceof ColumnProfileResult) {
                            SQLColumn column = (SQLColumn)pr.getProfiledObject();
                            SQLTable t2 = column.getParentTable();
                            if ( t == t2 && (!columns.contains(column)) ) {
                                columns.add(column);
                            }
                        }
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
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(null,
                            "Error in profile", "Error", JOptionPane.ERROR_MESSAGE);
                    evt.printStackTrace();
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
        List<SQLTable> tables = new ArrayList<SQLTable>();
        SQLTable selectedTable = null;
        
        if ( tableSelector.getSelectedIndex() >= 0 ) {
            selectedTable = (SQLTable) tableSelector.getSelectedObjects()[0];
        }
        
        for (ProfileResult pr : tableModel.getResultList() ) {
            SQLTable t = ((SQLColumn)pr.getProfiledObject()).getParentTable();
            if ( !tables.contains(t)) {
                tables.add(t);
            }
        }
        tableSelector.setModel(new DefaultComboBoxModel(tables.toArray()));

        
        if ( tableSelector.getModel().getSize() > 0 ) {
            if ( selectedTable != null && tables.contains(selectedTable)) {
                tableSelector.setSelectedItem(selectedTable);
            } else {
                tableSelector.setSelectedIndex(0);
            }
        }
    }
}

