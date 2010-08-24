/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.sqlpower.architect.olap.MondrianModel.CalculatedMember;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Provides a JComponent that acts as the GUI for modifying a Calculated Members
 */
public class CalculatedMemberEditPanel implements DataEntryPanel {

    private CalculatedMember calculatedMember;
    private JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JTextField dimensionField;
    private JTextArea formulaTextArea;
    private JTextField formatField;
    private JCheckBox visibleCheckBox;
    
    public CalculatedMemberEditPanel(CalculatedMember model) {
        this.calculatedMember = model;
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 150dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(model.getName()));
        builder.append("Caption", captionField = new JTextField(model.getCaption()));
        builder.append("Dimension", dimensionField = new JTextField(model.getDimension()));
        builder.append("Visible", visibleCheckBox = new JCheckBox("", model.getVisible() == null ? true : model.getVisible()));
        String formula = null;
        if (model.getFormula() != null) {
            formula = model.getFormula();
        } else if (model.getFormulaElement() != null) {
            formula = model.getFormulaElement().getText();
        }
        builder.append("Formula", formulaTextArea = new JTextArea(formula, 4, 50));
        formulaTextArea.setLineWrap(true);
        builder.append("Format", formatField = new JTextField(model.getFormatString()));
        panel = builder.getPanel();
    }
    
    public boolean applyChanges() {
        calculatedMember.begin("Modifying Calculated Measure Properties");
        calculatedMember.setName(nameField.getText());
        calculatedMember.setCaption(captionField.getText());
        calculatedMember.setDimension(dimensionField.getText());
        calculatedMember.setVisible(visibleCheckBox.isSelected());
        if (calculatedMember.getFormulaElement() == null || calculatedMember.getFormula() != null) {
            // Favour using attribute as Schema Workbench does
            calculatedMember.setFormula(formulaTextArea.getText());
        }
        if (calculatedMember.getFormulaElement() != null) {
            calculatedMember.getFormulaElement().setText(formulaTextArea.getText());
        }
        calculatedMember.setFormatString(formatField.getText());
        calculatedMember.commit();
        return true;
    }

    public void discardChanges() {
        // Do nothing
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }
}
