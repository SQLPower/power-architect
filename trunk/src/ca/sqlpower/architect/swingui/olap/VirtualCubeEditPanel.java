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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class VirtualCubeEditPanel implements ValidatableDataEntryPanel {

    private final VirtualCube vCube;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    
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
        builder.append(status, 3);
        builder.append("Name", nameField = new JTextField(vCube.getName()));
        builder.append("Caption", captionField = new JTextField(vCube.getCaption()));
        
        // default measure is optional so we need to add in a null option
        List<VirtualCubeMeasure> measures = new ArrayList<VirtualCubeMeasure>(vCube.getMeasures());
        measures.add(0, null);
        builder.append("Default Measure", defMeasure = new JComboBox(measures.toArray()));
        defMeasure.setRenderer(new OLAPObjectListCellRenderer());
        for (VirtualCubeMeasure vMs : vCube.getMeasures()) {
            if (vMs.getName().equals(vCube.getDefaultMeasure())) {
                defMeasure.setSelectedItem(vMs);
                break;
            }
        }
        
        handler = new FormValidationHandler(status);
        Validator validator = new OLAPObjectNameValidator((OLAPObject) vCube.getParent(), vCube, false);
        handler.addValidateObject(nameField, validator);
        
        panel = builder.getPanel();
    }
    
    public boolean applyChanges() {
        vCube.begin("Started modifying virtual cube properties");
        vCube.setName(nameField.getText());
        if (!(captionField.getText().equals(""))) {
            vCube.setCaption(captionField.getText());
        } else {
            vCube.setCaption(null);
        }
        VirtualCubeMeasure ms = (VirtualCubeMeasure) defMeasure.getSelectedItem();
        vCube.setDefaultMeasure(ms == null ? null : ms.getName());
        vCube.commit();
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
