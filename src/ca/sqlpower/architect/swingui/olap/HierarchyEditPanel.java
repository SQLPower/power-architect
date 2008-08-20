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

package ca.sqlpower.architect.swingui.olap;

import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class HierarchyEditPanel implements DataEntryPanel {
    
    private final Hierarchy hierarchy;
    private final JPanel panel;
    private final JTextField name;
    private final JTextField captionField;
    private final JComboBox tableChooser;
    
    /**
     * Creates a new property editor for the given OLAP Hierarchy. 
     * 
     * @param cube The data model of the hierarchy to edit
     */
    public HierarchyEditPanel(Hierarchy hierarchy) throws ArchitectException {
        this.hierarchy = hierarchy;
        
        List<SQLTable> tables = OLAPUtil.getAvailableTables(hierarchy);

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", name = new JTextField(hierarchy.getName()));
        builder.append("Caption", captionField = new JTextField(hierarchy.getCaption()));
        builder.append("Table", tableChooser = new JComboBox(new Vector<SQLTable>(tables)));
        tableChooser.setSelectedItem(OLAPUtil.tableForHierarchy(hierarchy)); // XXX this isn't quite right.. it would set the default as the local value
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        hierarchy.startCompoundEdit("Modify hierarchy properties");
        hierarchy.setName(name.getText());
        if (!(captionField.getText().equals(""))) {
            hierarchy.setCaption(captionField.getText());
        } else {
            hierarchy.setCaption(null);
        }
        if (tableChooser.getSelectedItem() != null) {
            SQLTable stable = (SQLTable) tableChooser.getSelectedItem();
            Table table = new Table();
            table.setName(stable.getName());
            table.setSchema(OLAPUtil.getQualifier(stable));
            hierarchy.setRelation(table);
        }
        hierarchy.endCompoundEdit();
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }

}
