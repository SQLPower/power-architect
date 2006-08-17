package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManager;

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


    private JComboBox tableSelector;
    private JList columnSelector;
    private ChartTypes chartType = ChartTypes.PIE;
    
    private JPanel controlsArea;
    private ProfileGraphPanel displayPanel;
    
    private final JProgressBar progressBar = new JProgressBar();
    private final ProfileManager pm;
    
    public ProfilePanel(ProfileManager pm) {
        this.pm = pm;
        displayPanel = new ProfileGraphPanel(this, 0,pm);
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
             * Called when the user selects a table; create its profile (slow)
             */
            public void actionPerformed(ActionEvent e) {
                final SQLTable t = (SQLTable) tableSelector.getSelectedItem();
                if (t == null) {
                    return;
                }
                try {
                    columnSelector.setModel(new SQLTableListModel(t));
                    new ProgressWatcher(progressBar,pm);
                    // Do the work - build the profiles for this table
                    new Thread(new Runnable() {

                        public void run() {
                            try {
                                progressBar.setVisible(true);
                                if (pm.getResult(t) == null) {
                                    pm.setCancelled(false);
                                    pm.createProfiles(Collections.nCopies(1, t));
                                }                               
                                progressBar.setVisible(false);
                            } catch (SQLException e) {
                                logger.error("Error in Profile Action ", e);
                                ASUtils.showExceptionDialogNoReport(ProfilePanel.this, "Error during profile run", e);
                            } catch (ArchitectException e) {
                                e.printStackTrace();
                            }
                        }

                    }).start();

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
                displayPanel.displayProfile((SQLTable) tableSelector.getSelectedItem(), col);              
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

      public void setTables(List<SQLTable> tables) {
        tableSelector.setModel(new DefaultComboBoxModel(tables.toArray()));
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
}
