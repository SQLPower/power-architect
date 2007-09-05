package ca.sqlpower.architect.swingui;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.db.DataSourceTypeEditorTabPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An option panel for setting the Kettle connection type for an SPDataSourceType
 * It has a combobox populated with Kettle connection types retrieved from
 * {@link ca.sqlpower.architect.etl.kettle.KettleUtils}.
 */
public class KettleDataSourceTypeOptionPanel implements DataSourceTypeEditorTabPanel {

    private JPanel panel;
    private JComboBox kettleConnectionType = new JComboBox();
    private SPDataSourceType dsType;
    
    public KettleDataSourceTypeOptionPanel() {
        PanelBuilder pb = new PanelBuilder(new FormLayout(
                "4dlu,pref,4dlu,pref:grow,4dlu",
                "4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
        
        CellConstraints cc = new CellConstraints();
        CellConstraints cl = new CellConstraints();
        int row = 2;
        pb.addLabel("Kettle Connection Type", cl.xy(2, row), kettleConnectionType, cc.xy(4, row));
        List<String> dbConnectionNames = KettleUtils.retrieveKettleConnectionTypes();
        for (String dbConnectionName: dbConnectionNames) {
            kettleConnectionType.addItem(dbConnectionName);
        }
        kettleConnectionType.setSelectedIndex(-1);  
        
        panel = pb.getPanel();
    }
    
    public boolean applyChanges() {
        if (dsType != null) {
            dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, 
                          (String)kettleConnectionType.getSelectedItem());
        }
        return true;
    }

    public void discardChanges() {
        // no action needed
    }

    public JComponent getPanel() {
        return panel;
    }

    public void editDsType(SPDataSourceType dsType) {
        this.dsType = dsType;
        if (dsType == null) {
            kettleConnectionType.setSelectedItem("");
        } else {
            kettleConnectionType.setSelectedItem
            (dsType.getProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY));
        }
    }

}
