package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Let the user choose a column for profiling and display the Profile results
 */
public class ProfilePanel extends JPanel {
    private static Logger logger = Logger.getLogger(ProfilePanel.class);
    
    private JComboBox tableSelector;
    private JList columnSelector;
    private int rowCount;
    private JLabel title;
    private JLabel rowCountDisplay;
    private JPanel controlsArea, displayArea;
    private JLabel nullableLabel;
    private JLabel minValue;
    private JLabel maxValue;
    private JLabel avgValue;
    private JLabel nullCountLabel;
    private JLabel nullPercentLabel;
    private JLabel minLengthLabel;
    private JLabel maxLengthLabel;

    private ProfileManager pm = new ProfileManager();
    
    public ProfilePanel() {
        FormLayout controlsLayout = new FormLayout(
                "4dlu,fill:min(150dlu;default):grow, 4dlu", // columns
                "default, 4dlu, default, 4dlu,  fill:min(200dlu;default):grow"); // rows
 
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
                SQLTable t = (SQLTable) tableSelector.getSelectedItem();
                if (t == null) {
                    return;
                }
                try {   
                    columnSelector.setModel(new SQLTableListModel(t));
                    
                    // Do the work - build the profiles for this table
                    pm.createProfiles(Collections.nCopies(1, t));
                    
                    TableProfileResult r = (TableProfileResult) pm.getResult(t);
                    setRowCount(r.getRowCount());
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(null, 
                            "Error in profile", "Error", JOptionPane.ERROR_MESSAGE);
                    evt.printStackTrace();
                }
            }
            
        });
        columnSelector = new JList();
        // XXX maybe someday we can allow the user to compare profiles by
        // removing this...
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
                displayProfile((SQLTable) tableSelector.getSelectedItem(), col);              
            }           
        });
        rowCountDisplay = new JLabel("Row Count: --");
        
        PanelBuilder pb = new PanelBuilder(controlsLayout,controlsArea);
        pb.setDefaultDialogBorder();    
        
        pb.add(tableSelector, cc.xy(2, 1));
        pb.add(rowCountDisplay, cc.xy(2, 3));
        pb.add(new JScrollPane(columnSelector), cc.xy(2, 5));
        
        this.add(controlsArea, BorderLayout.WEST);
        createDisplayPanel();
        this.add(displayArea, BorderLayout.CENTER);

    }
    
    protected void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        rowCountDisplay.setText("Row count: " + rowCount);
    }

    public void setTables(List<SQLTable> tables) {       
        tableSelector.setModel(new DefaultComboBoxModel(tables.toArray()));
    }
    
    public void setTitle(String title) {
        this.title.setText(title);
    }
    
    private void createDisplayPanel() {
        FormLayout displayLayout = new FormLayout(
                "4dlu, default, 4dlu, default, 4dlu, fill:min(300dlu;default):grow, 4dlu", // columns
                "4dlu, default, 6dlu"); // rows
        CellConstraints cc = new CellConstraints();
        
        displayArea = logger.isDebugEnabled()  ? new FormDebugPanel(displayLayout) : new JPanel(displayLayout);
        displayArea.setBorder(BorderFactory.createEtchedBorder());
        
        Font bodyFont = getFont();
        Font titleFont = bodyFont.deriveFont(Font.BOLD, bodyFont.getSize() * 1.25F);
        
        title = new JLabel("Column Name");
        title.setFont(titleFont);
        
        PanelBuilder pb = new PanelBuilder(displayLayout, displayArea);
        pb.add(title, cc.xyw(2, 2, 5));
        
        int row = 4;
        nullableLabel = makeInfoRow(pb, "Nullable", row); row += 2;
        nullCountLabel = makeInfoRow(pb, "Null Count", row); row += 2;
        nullPercentLabel = makeInfoRow(pb, "% Null Records", row); row += 2;
        minLengthLabel = makeInfoRow(pb, "Minimum Length", row); row += 2;
        maxLengthLabel = makeInfoRow(pb, "Maximum Length", row); row += 2;
        minValue = makeInfoRow(pb, "Minimum Value", row); row += 2;
        maxValue = makeInfoRow(pb, "Maximum Value", row); row += 2;
        avgValue = makeInfoRow(pb, "Average Value", row); row += 2;
        
        // Now add something to represent the chart
        Canvas c = new Canvas();
        c.setSize(250, 250);
        c.setBackground(Color.PINK);
        pb.appendRow("1dlu:grow");
        pb.add(c, cc.xywh(6, 4, 1, 17));
    }

    private void displayProfile(SQLTable t, SQLColumn c) {       
        ColumnProfileResult cr = (ColumnProfileResult) pm.getResult(c);
        if (cr == null) {
            logger.error("displayProfile called but unable to get ColumnProfileResult");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(c);
        if (c.isPrimaryKey()) {
            sb.append(' ').append("[PK]");
        }
        setTitle(sb.toString());
        nullableLabel.setText(Boolean.toString(c.isDefinitelyNullable()));
        nullCountLabel.setText(Integer.toString(cr.getNullCount()));
        int nullsInRecords = cr.getNullCount();
        double ratio = rowCount > 0 ? nullsInRecords / rowCount : 0;
        nullPercentLabel.setText(format(ratio));
        
        minLengthLabel.setText(Integer.toString(cr.getMinLength()));
        maxLengthLabel.setText(Integer.toString(cr.getMaxLength()));
        
        minValue.setText(cr.getMinValue() == null ? "" : cr.getMinValue().toString());
        maxValue.setText(cr.getMaxValue() == null ? "" : cr.getMaxValue().toString());
        Object o = cr.getAvgValue();
        if (o == null) {
            avgValue.setText("");
        } else if (o instanceof BigDecimal) {
            double d = ((BigDecimal)o).doubleValue();
            avgValue.setText(format(d));
        } else {
            logger.debug("Got avgValue of type: " + o.getClass().getName());
            avgValue.setText(cr.getAvgValue().toString());  
        }
    }
    
    private String format(double d) {
        return String.format("%6.2f", d);
    }
    
    private JLabel makeInfoRow(PanelBuilder pb, String title, int row) {
        CellConstraints cc = new CellConstraints();
        pb.appendRow("default");
        pb.appendRow("2dlu");
        pb.add(new JLabel(title), cc.xy(2, row));
        JLabel label = new JLabel("--");
        pb.add(label, cc.xy(4, row));
        return label;
    }
}
