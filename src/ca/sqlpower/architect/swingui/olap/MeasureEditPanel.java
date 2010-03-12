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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.MeasureExpression;
import ca.sqlpower.architect.olap.MondrianModel.SQL;
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

public class MeasureEditPanel implements ValidatableDataEntryPanel {
    
    private final Measure measure;
    private final JPanel panel;
    private JTextField name;
    private JTextField captionField;
    private JComboBox aggregator;
    private JComboBox columnChooser;
    private JRadioButton columnRadioButton;
    private JTextArea expression;
    private JRadioButton expRadioButton;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    /**
     * Creates a new property editor for the given OLAP Measure. 
     * 
     * @param cube The data model of the measure to edit
     * @throws SQLObjectException
     *             if populating the necessary SQLObjects fails
     */
    public MeasureEditPanel(Measure measure) throws SQLObjectException {
        this.measure = measure;
        
        handler = new FormValidationHandler(status, true);
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append(status, 3);
        builder.append("Name", name = new JTextField(measure.getName()));
        builder.append("Caption", captionField = new JTextField(measure.getCaption()));
        
        String[] rolapAggregates = new String[] {"sum", "count", "min", "max", "avg", "distinct-count"} ;
        builder.append("Aggregator", aggregator = new JComboBox(rolapAggregates));
        if (measure.getAggregator() != null) {
            aggregator.setSelectedItem(measure.getAggregator());
        }
        
        builder.appendSeparator("Value");
        
        Action radioButtonsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                columnChooser.setEnabled(columnRadioButton.isSelected());
                expression.setEnabled(expRadioButton.isSelected());
                handler.performFormValidation();
            }
        };
        
        columnRadioButton = new JRadioButton();
        columnRadioButton.setAction(radioButtonsAction);
        columnRadioButton.setText("Use Column");
        expRadioButton = new JRadioButton();
        expRadioButton.setAction(radioButtonsAction);
        expRadioButton.setText("Use Expression");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(columnRadioButton);
        buttonGroup.add(expRadioButton);
        
        builder.append(columnRadioButton, 3); 
        builder.append(columnChooser = new JComboBox(), 3);
        builder.append(expRadioButton, 3); 
        builder.append(new JScrollPane(expression = new JTextArea(4, 30)), 3);
        expression.setLineWrap(true);
        
        Cube parentCube = (Cube) measure.getParent();
        SQLTable cubeTable = OLAPUtil.tableForCube(parentCube);
        boolean enableColumns = false;
        if (cubeTable == null) {
            columnChooser.addItem("Parent Cube has no table");
        } else if (cubeTable.getColumns().isEmpty()) {
            columnChooser.addItem("Parent Cube table has no columns");
        } else {
            columnChooser.setModel(new SQLObjectComboBoxModel(cubeTable, SQLColumn.class));
            columnRadioButton.doClick();
            for (SQLColumn col : cubeTable.getColumns()) {
                if (col.getName().equalsIgnoreCase(measure.getColumn())) {
                    columnChooser.setSelectedItem(col);
                    break;
                }
            }
            enableColumns = true;
        }
        columnChooser.setEnabled(enableColumns);
        columnRadioButton.setEnabled(enableColumns);
        
        SQL exp = null;
        MeasureExpression mExp = measure.getMeasureExp();
        if (mExp != null) {
            for (SQL sql : measure.getMeasureExp().getExpressions()) {
                // we only support generic expressions right now.
                if (sql.getDialect() != null && sql.getDialect().equalsIgnoreCase("generic")) {
                    exp = sql;
                }
            }
            expRadioButton.doClick();
        }
        expression.setText(exp == null ? "" : exp.getText());
        
        if (!columnRadioButton.isSelected()) {
            expRadioButton.doClick();
        }
        
        Validator validator = new OLAPObjectNameValidator((OLAPObject) measure.getParent(), measure, false);
        handler.addValidateObject(name, validator);
        handler.addValidateObject(columnChooser, new NotNullValidator("Value column"));
        
        panel = builder.getPanel();
    }
    
    public boolean applyChanges() {
        measure.begin("Modify measure properties");
        measure.setName(name.getText());
        if (!(captionField.getText().equals(""))) {
            measure.setCaption(captionField.getText());
        } else {
            measure.setCaption(null);
        }
        measure.setAggregator((String) aggregator.getSelectedItem());

        if (expRadioButton.isSelected()) {
            MeasureExpression mExp = measure.getMeasureExp();
            if (mExp == null) {
                mExp = new MeasureExpression();
                measure.setMeasureExp(mExp);
            }

            SQL exp = null;
            for (SQL sql : mExp.getExpressions()) {
                // we only support generic expressions right now.
                if (sql.getDialect() != null && sql.getDialect().equalsIgnoreCase("generic")) {
                    exp = sql;
                }
            }
            if (exp == null) {
                exp = new SQL();
                exp.setDialect("generic");
                mExp.addExpression(exp);
            }
            exp.setText(expression.getText());
            
            // a measure must have either column or expression but not both.
            measure.setColumn(null);
        } else {
            SQLColumn col = (SQLColumn) columnChooser.getSelectedItem();
            measure.setColumn(col.getName());
            
            // a measure must have either column or expression but not both.
            measure.setMeasureExp(null);
        }
        
        measure.commit();
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
