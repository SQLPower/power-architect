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

import ca.sqlpower.architect.olap.MondrianDef.Dimension;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DimensionEditPanel implements DataEntryPanel {

    private final Dimension dimension;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox typeBox;
    
    public DimensionEditPanel(Dimension dimension) {
        this.dimension = dimension;

        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(dimension.getInstanceName()));
        builder.append("Caption", captionField = new JTextField(dimension.getCaption()));
        builder.append("Type", typeBox = new JComboBox(DimensionType.values()));
        typeBox.setSelectedItem(dimension.getDimensionType());
        
        panel = builder.getPanel();
    }
    public boolean applyChanges() {
        // TODO make this a compound edit
        dimension.setInstanceName(nameField.getText());
        dimension.setCaption(captionField.getText());
        // TODO ability to set type, or remove type field from form (whichever makes sense)
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
