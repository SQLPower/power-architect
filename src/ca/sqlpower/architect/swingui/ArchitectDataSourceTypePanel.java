package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectDataSourceTypePanel implements ArchitectPanel {

    private static final Logger logger = Logger.getLogger(ArchitectDataSourceTypePanel.class);
    
    private ArchitectDataSourceType dsType;
    private JPanel panel;
    private JTabbedPane tabbedPane;
    final private JTextField name = new JTextField();
    final private JTextField connectionStringTemplate = new JTextField();
    final private JTextField driverClass = new JTextField();
    final private PlatformSpecificConnectionOptionPanel template =
        new PlatformSpecificConnectionOptionPanel(new JTextField());
    final private JComboBox kettleConnectionType = new JComboBox();
    
    public ArchitectDataSourceTypePanel() {
        buildPanel();
        editDsType(null);
    }
    
    private void buildPanel() {
        
        connectionStringTemplate.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                dsType.setJdbcUrl(connectionStringTemplate.getText());
                template.setTemplate(dsType);
            }

            public void insertUpdate(DocumentEvent e) {
                dsType.setJdbcUrl(connectionStringTemplate.getText());
                template.setTemplate(dsType);                
            }

            public void removeUpdate(DocumentEvent e) {
                dsType.setJdbcUrl(connectionStringTemplate.getText());
                template.setTemplate(dsType);                
            }
            
        });
        
        tabbedPane = new JTabbedPane();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout(
                "4dlu,pref,4dlu,pref:grow,4dlu",
                "4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
        
        CellConstraints cc = new CellConstraints();
        CellConstraints cl = new CellConstraints();
        int row = 2;
        pb.addLabel("Name",cl.xy(2, row), name, cc.xy(4, row));
        row += 2;
        pb.addLabel("Driver Class",cl.xy(2, row), driverClass, cc.xy(4, row));
        row += 2;
        pb.addLabel("Connection String Template",cl.xy(2, row), connectionStringTemplate, cc.xy(4, row));
        row += 2;
        connectionStringTemplate.setToolTipText("Variables should be of the form <variable name:default value>");
        pb.addTitle("Options Editor Preview (based on URL template)",cl.xyw(2, row,3));
        row += 2;
        pb.addLabel("Sample Options",cl.xy(2, row), template.getPanel(), cc.xy(4, row));
        
        tabbedPane.addTab("General", pb.getPanel());
        
        pb = new PanelBuilder(new FormLayout(
                "4dlu,pref,4dlu,pref:grow,4dlu",
                "4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
        
        cc = new CellConstraints();
        cl = new CellConstraints();
        row = 2;
        pb.addLabel("Kettle Connection Type", cl.xy(2, row), kettleConnectionType, cc.xy(4, row));
        List<String> dbConnectionNames = KettleUtils.retrieveKettleConnectionTypes();
        for (String dbConnectionName: dbConnectionNames) {
            kettleConnectionType.addItem(dbConnectionName);
        }
        kettleConnectionType.setSelectedIndex(-1);
        
        tabbedPane.addTab("Kettle", pb.getPanel());
        
        panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
    }
    
    public void editDsType(ArchitectDataSourceType dst) {
        dsType = dst;
        if (dst == null) {
            name.setText("");
            name.setEnabled(false);
            
            driverClass.setText("");
            driverClass.setEnabled(false);
            
            connectionStringTemplate.setText("");
            kettleConnectionType.setSelectedItem("");

            // template will get updated by document listener
        } else {
            name.setText(dst.getName());
            name.setEnabled(true);
            
            driverClass.setText(dst.getJdbcDriver());
            driverClass.setEnabled(true);
            
            connectionStringTemplate.setText(dst.getJdbcUrl());
            connectionStringTemplate.setEnabled(true);
            
            kettleConnectionType.setSelectedItem
                (dst.getProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY));
            
            // template will get updated by document listener
        }
    }

    public boolean applyChanges() {
        logger.debug("Applying changes to data source type "+dsType);
        if (dsType != null) {
            dsType.setName(name.getText());
            dsType.setJdbcDriver(driverClass.getText());
            dsType.setJdbcUrl(connectionStringTemplate.getText());
            dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, 
                               (String)kettleConnectionType.getSelectedItem());
        }
        return true;
    }

    public void discardChanges() {
        // no action needed
    }

    public JPanel getPanel() {
        return panel;
    }

}
