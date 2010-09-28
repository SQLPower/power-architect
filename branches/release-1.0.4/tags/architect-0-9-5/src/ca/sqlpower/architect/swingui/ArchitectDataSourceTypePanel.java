package ca.sqlpower.architect.swingui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSourceType;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectDataSourceTypePanel implements ArchitectPanel {

    private static final Logger logger = Logger.getLogger(ArchitectDataSourceTypePanel.class);
    
    private ArchitectDataSourceType dsType;
    private JPanel panel;
    final private JTextField name = new JTextField();
    final private JTextField connectionStringTemplate = new JTextField();
    final private JTextField driverClass = new JTextField();
    final private PlatformSpecificConnectionOptionPanel template =
        new PlatformSpecificConnectionOptionPanel(new JTextField());
    
    public ArchitectDataSourceTypePanel() {
        buildPanel();
        editDsType(null);
    }
    
    private void buildPanel() {
        
        connectionStringTemplate.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                template.setTemplate(connectionStringTemplate.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                template.setTemplate(connectionStringTemplate.getText());                
            }

            public void removeUpdate(DocumentEvent e) {
                template.setTemplate(connectionStringTemplate.getText());                
            }
            
        });
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
        panel = pb.getPanel();
    }
    
    public void editDsType(ArchitectDataSourceType dst) {
        dsType = dst;
        if (dst == null) {
            name.setText("");
            name.setEnabled(false);
            
            driverClass.setText("");
            driverClass.setEnabled(false);
            
            connectionStringTemplate.setText("");

            // template will get updated by document listener
        } else {
            name.setText(dst.getName());
            name.setEnabled(true);
            
            driverClass.setText(dst.getJdbcDriver());
            driverClass.setEnabled(true);
            
            connectionStringTemplate.setText(dst.getJdbcUrl());
            connectionStringTemplate.setEnabled(true);
            
            // template will get updated by document listener
        }
    }

    public boolean applyChanges() {
        logger.debug("Applying changes to data source type "+dsType);
        if (dsType != null) {
            dsType.setName(name.getText());
            dsType.setJdbcDriver(driverClass.getText());
            dsType.setJdbcUrl(connectionStringTemplate.getText());
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
