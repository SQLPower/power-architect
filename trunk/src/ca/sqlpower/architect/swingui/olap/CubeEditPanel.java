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

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CubeEditPanel implements DataEntryPanel {
    
    private final Cube cube;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    
    /**
     * Creates a new property editor for the given OLAP Cube. 
     * 
     * @param cube The data model of the cube to edit
     */
    public CubeEditPanel(Cube cube) {
        this.cube = cube;

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(cube.getName()));
        builder.append("Caption", captionField = new JTextField(cube.getCaption()));
        builder.append("Default Measure", defMeasure = new JComboBox(cube.getMeasures().toArray()));
        for (Measure ms : cube.getMeasures()) {
            if (ms.getName().equals(cube.getDefaultMeasure())) {
                defMeasure.setSelectedItem(ms);
                break;
            }
        }
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        cube.startCompoundEdit("Started modifying cube properties");
        cube.setName(nameField.getText());
        if (!(captionField.getText().equals(""))) {
            cube.setCaption(captionField.getText());
        } else {
            cube.setCaption(null);
        }
        if (defMeasure.getSelectedItem() != null) {
            cube.setDefaultMeasure(defMeasure.getSelectedItem().toString());
        }
        cube.endCompoundEdit();
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
