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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class HierarchyEditPanel implements ValidatableDataEntryPanel {
    
    private final Hierarchy hierarchy;
    private final JPanel panel;
    private final JTextField name;
    private final JTextField captionField;
    private final JComboBox tableChooser;
    private final JCheckBox hasAll;
    private final JTextField allLevelName;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    
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
        builder.append(status, 3);
        builder.append("Name", name = new JTextField(hierarchy.getName()));
        builder.append("Caption", captionField = new JTextField(hierarchy.getCaption()));
        builder.append("Has All", hasAll = new JCheckBox("Has All", hierarchy.getHasAll() != null ? hierarchy.getHasAll() : true));
        builder.append("Table", tableChooser = new JComboBox(new Vector<SQLTable>(tables)));
        builder.append("All Level Name", allLevelName = new JTextField("(All)"));
        tableChooser.setSelectedItem(OLAPUtil.tableForHierarchy(hierarchy)); // XXX this isn't quite right.. it would set the default as the local value
        
        handler = new FormValidationHandler(status);
        Validator validator = new OLAPObjectNameValidator(hierarchy.getParent(), hierarchy, true);
        handler.addValidateObject(name, validator);
        
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        hierarchy.startCompoundEdit("Modify hierarchy properties");
        if (!(name.getText().equals(""))) {
            hierarchy.setName(name.getText());
        } else {
            hierarchy.setName(null);
        }
        if (!(captionField.getText().equals(""))) {
            hierarchy.setCaption(captionField.getText());
        } else {
            hierarchy.setCaption(null);
        }
        hierarchy.setHasAll(hasAll.isSelected());
        if (tableChooser.getSelectedItem() != null) {
            SQLTable stable = (SQLTable) tableChooser.getSelectedItem();
            Table table = new Table();
            table.setName(stable.getName());
            table.setSchema(OLAPUtil.getQualifier(stable));
            hierarchy.setRelation(table);
        }
        hierarchy.setAllLevelName(allLevelName.getText());
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
    public ValidationHandler getValidationHandler() {
        return handler;
    }

}
