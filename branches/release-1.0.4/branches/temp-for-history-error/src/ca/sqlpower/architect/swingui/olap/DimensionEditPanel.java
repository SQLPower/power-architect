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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.swingui.SQLObjectComboBoxModel;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.NotNullValidator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DimensionEditPanel implements ValidatableDataEntryPanel {

    /**
     * An enumeration of the dimension types that you can create in the OLAP
     * editor.
     */
    private enum DimensionType {
        StandardDimension,
        TimeDimension;
    }
    
    private final Dimension dimension;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox typeBox;
    private JComboBox foreignKeyChooser;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    /**
     * Creates a new property editor for the given OLAP dimension. 
     * 
     * @param dimension The dimension to edit
     * @throws SQLObjectException
     *             if digging up the source table results in a database error
     */
    public DimensionEditPanel(Dimension dimension) throws SQLObjectException {
        this.dimension = dimension;

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append(status, 3);
        builder.append("Name", nameField = new JTextField(dimension.getName()));
        builder.append("Caption", captionField = new JTextField(dimension.getCaption()));
        builder.append("Type", typeBox = new JComboBox(DimensionType.values()));
        
        if (dimension.getType() != null) {
            typeBox.setSelectedItem(DimensionType.valueOf(dimension.getType()));
        } else {
            typeBox.setSelectedItem(DimensionType.StandardDimension);
        }
        
        handler = new FormValidationHandler(status, true);
        Validator validator = new OLAPObjectNameValidator((OLAPObject) dimension.getParent(), dimension, false);
        handler.addValidateObject(nameField, validator);
        
        // private dimensions only.
        if (dimension.getParent() instanceof Cube) {
            builder.append("Foreign Key", foreignKeyChooser = new JComboBox());
            handler.addValidateObject(foreignKeyChooser, new NotNullValidator("Foreign key"));

            Cube cube = (Cube) dimension.getParent();
            SQLTable factTable = OLAPUtil.tableForCube(cube);
            if (factTable == null) {
                foreignKeyChooser.addItem("Parent Cube has no fact table");
                foreignKeyChooser.setEnabled(false);
            } else if (factTable.getColumns().isEmpty()) {
                foreignKeyChooser.addItem("Parent Cube Fact table has no columns");
                foreignKeyChooser.setEnabled(false);
            } else {
                foreignKeyChooser.setModel(new SQLObjectComboBoxModel(factTable, SQLColumn.class));
                for (SQLColumn col : factTable.getColumns()) {
                    if (col.getName().equals(dimension.getForeignKey())) {
                        foreignKeyChooser.setSelectedItem(col);
                    }
                }
            }
        }
        
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        dimension.begin("Started modifying dimension properties");
        dimension.setName(nameField.getText());
        if (!(captionField.getText().equals(""))) {
            dimension.setCaption(captionField.getText());
        } else {
            dimension.setCaption(null);
        }
        DimensionType type = (DimensionType) typeBox.getSelectedItem();
        if (type != null) {
            dimension.setType(type.toString());
        } else {
            dimension.setType(DimensionType.StandardDimension.toString());
        }
        
        if (foreignKeyChooser != null && foreignKeyChooser.isEnabled()) {
            SQLColumn selectedCol = (SQLColumn) foreignKeyChooser.getSelectedItem();
            String pk = selectedCol.getName();
            dimension.setForeignKey(pk);
        }
        
        dimension.commit();
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
