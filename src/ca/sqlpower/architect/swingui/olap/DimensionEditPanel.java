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

import mondrian.olap.DimensionType;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DimensionEditPanel implements DataEntryPanel {

    private final Dimension dimension;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox typeBox;
    
    /**
     * Creates a new property editor for the given OLAP dimension. 
     * 
     * @param dimension The dimension to edit
     */
    public DimensionEditPanel(Dimension dimension) {
        this.dimension = dimension;

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(dimension.getName()));
        builder.append("Caption", captionField = new JTextField(dimension.getCaption()));
        builder.append("Type", typeBox = new JComboBox(DimensionType.values()));
        if (dimension.getType() != null) {
            typeBox.setSelectedItem(DimensionType.valueOf(dimension.getType()));
        } else {
            typeBox.setSelectedItem(DimensionType.StandardDimension);
        }
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        dimension.startCompoundEdit("Started modifying dimension properties");
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
        dimension.endCompoundEdit();
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
