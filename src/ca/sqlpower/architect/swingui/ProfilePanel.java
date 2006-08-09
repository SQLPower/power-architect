package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

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
    
    public ProfilePanel() {
        FormLayout controlsLayout = new FormLayout(
                "4dlu,fill:min(150dlu;default):grow, 4dlu", // columns
                "default, 4dlu, default, 4dlu,  fill:min(200dlu;default):grow"); // rows
 
        CellConstraints cc = new CellConstraints();
        
        setLayout(new BorderLayout());

        controlsArea = logger.isDebugEnabled()  ? new FormDebugPanel(controlsLayout) : new JPanel(controlsLayout);

        //controlsArea.setBorder(BorderFactory.createTitledBorder("Column To Profile"));
        controlsArea.setLayout(new BoxLayout(controlsArea, BoxLayout.Y_AXIS));

        tableSelector = new JComboBox();
        tableSelector.addItem("Table 1");
        columnSelector = new JList();
        columnSelector.setListData(new String[] { "Col 1", "Col 2" });
        rowCountDisplay = new JLabel("Row Count: 42");
        
        PanelBuilder pb = new PanelBuilder(controlsLayout,controlsArea);
        pb.setDefaultDialogBorder();    
        
        pb.add(tableSelector, cc.xy(2, 1));
        pb.add(rowCountDisplay, cc.xy(2, 3));
        pb.add(columnSelector, cc.xy(2, 5));

        
        this.add(controlsArea, BorderLayout.WEST);
        createDisplayPanel();
        this.add(displayArea, BorderLayout.CENTER);

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
        
        JLabel title = new JLabel("Column 1: Integer(10,4) PK");
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

    private JLabel makeInfoRow(PanelBuilder pb, String title, int row) {
        CellConstraints cc = new CellConstraints();
        pb.appendRow("default");
        pb.appendRow("2dlu");
        pb.add(new JLabel(title), cc.xy(2, row));
        JLabel label = new JLabel("SomeValue");
        pb.add(label, cc.xy(4, row));
        return label;
    }
}
