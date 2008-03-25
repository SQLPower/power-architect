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
        if (dsType != null && dsType.getKettleNames().size() > 0) {
            kettleConnectionType.setSelectedItem
            (dsType.getKettleNames().get(0));
        } else {
            kettleConnectionType.setSelectedItem("");
        }
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

}
