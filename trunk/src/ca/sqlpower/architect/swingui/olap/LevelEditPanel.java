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
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class LevelEditPanel implements DataEntryPanel {

    private final Level level;
    private final JPanel panel;
    private JTextField name;
    private JTextField captionField;
    private JComboBox columnChooser;

    /**
     * Creates a new property editor for the given level of a hierarchy.
     * 
     * @param cube
     *            The data model of the Level to edit
     * @throws ArchitectException
     *             if digging up the source table results in a database error
     */
    public LevelEditPanel(Level level) throws ArchitectException {
        this.level = level;
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", name = new JTextField(level.getName()));
        builder.append("Caption", captionField = new JTextField(level.getCaption()));
        builder.append("Column", columnChooser = new JComboBox());
        SQLTable dimensionTable = OLAPUtil.tableForHierarchy((Hierarchy) level.getParent());
        if (dimensionTable != null) {
            for (SQLColumn col : dimensionTable.getColumns()) {
                columnChooser.addItem(col);
            }
        } else {
            columnChooser.addItem("Parent dimension has no table");
            columnChooser.setEnabled(false);
        }
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        level.startCompoundEdit("Modify Level Properties");
        level.setName(name.getText());
        if (columnChooser.isEnabled()) {
            SQLColumn col = (SQLColumn) columnChooser.getSelectedItem();
            level.setColumn(col == null ? null : col.getName());
        }
        if (!(captionField.getText().equals(""))) {
            level.setCaption(captionField.getText());
        } else {
            level.setCaption(null);
        }
        level.endCompoundEdit();
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
