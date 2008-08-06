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

import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class VirtualCubeEditPanel implements DataEntryPanel {

    private final VirtualCube vCube;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    
    /**
     * Creates a new property editor for the given OLAP Virtual Cube. 
     * 
     * @param vCube The data model of the Virtual Cube to edit
     */
    public VirtualCubeEditPanel(VirtualCube vCube) {
        this.vCube = vCube;

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(vCube.getName()));
        builder.append("Caption", captionField = new JTextField(vCube.getCaption()));
        builder.append("Default Measure", defMeasure = new JComboBox(vCube.getMeasures().toArray()));
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        vCube.startCompoundEdit("Started modifying virtual cube properties");
        vCube.setName(nameField.getText());
        vCube.setCaption(captionField.getText());
        if (defMeasure.getSelectedItem() != null) {
            vCube.setDefaultMeasure(defMeasure.getSelectedItem().toString());
        }
        vCube.endCompoundEdit();
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
