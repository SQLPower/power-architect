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
