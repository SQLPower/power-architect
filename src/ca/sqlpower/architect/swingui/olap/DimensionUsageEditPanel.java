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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DimensionUsageEditPanel implements DataEntryPanel{

    private final DimensionUsage dimensionUsage;

    private final JPanel panel;

    private JTextField captionField;

    private JComboBox foreignKeyChooser;

    private final Dimension dimension;

    /**
     * Creates a new property editor for the given dimension usage.
     * 
     * @param dimensionUsage
     *            usage The data model of the dimension usage to edit
     * @param dimension
     *            The dimension this dimension usage is referring to
     *            
     * @throws ArchitectException
     *             if digging up the source table results in a database error
     */
    public DimensionUsageEditPanel(DimensionUsage dimensionUsage, Dimension dimension) throws ArchitectException {
        this.dimensionUsage = dimensionUsage;
        this.dimension = dimension;

        FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Caption", captionField = new JTextField(dimensionUsage.getCaption()));
        builder.append("Foreign Key", foreignKeyChooser = new JComboBox());

        Cube cube = (Cube) dimensionUsage.getParent();
        SQLTable factTable = OLAPUtil.tableForCube(cube);
        if (factTable == null) {
            foreignKeyChooser.addItem("Parent Cube has no fact table");
            foreignKeyChooser.setEnabled(false);
        } else if (factTable.getColumns().isEmpty()) {
            foreignKeyChooser.addItem("Parent Cube Fact table has no columns");
            foreignKeyChooser.setEnabled(false);
        } else {
            for (SQLColumn col : factTable.getColumns()) {
                foreignKeyChooser.addItem(col);
                if (col.getName().equals(dimensionUsage.getForeignKey())) {
                    foreignKeyChooser.setSelectedItem(col);
                }
            }
        }
        panel = builder.getPanel();
    }

    public boolean applyChanges() {
        dimensionUsage.startCompoundEdit("Modify Dimension Usage Properties");
        if (!(captionField.getText().equals(""))) {
            dimensionUsage.setCaption(captionField.getText());
        } else {
            dimensionUsage.setCaption(null);
        }

        if (foreignKeyChooser.isEnabled()) {
            SQLColumn selectedCol = (SQLColumn) foreignKeyChooser.getSelectedItem();
            String pk = selectedCol.getName();
            dimensionUsage.setForeignKey(pk);
            dimension.setForeignKey(pk);
        }
        dimensionUsage.endCompoundEdit();
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
