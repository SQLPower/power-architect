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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CubeEditPanel implements ValidatableDataEntryPanel {
    
    private final Cube cube;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    private JComboBox tableChooser;
    
    private JTextArea selectStatements;
    private JTextField viewAlias;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    private JRadioButton tableRadioButton;
    private JRadioButton viewRadioButton;
    
    /**
     * Creates a new property editor for the given OLAP Cube. 
     * 
     * @param cube The data model of the cube to edit
     */
    public CubeEditPanel(Cube cube) throws SQLObjectException {
        this.cube = cube;
        
        List<SQLTable> tables = OLAPUtil.getAvailableTables(cube);
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append(status, 3);
        builder.append("Name", nameField = new JTextField(cube.getName()));
        builder.append("Caption", captionField = new JTextField(cube.getCaption()));
        // default measure is optional so we need to add in a null option
        List<Measure> measures = new ArrayList<Measure>(cube.getMeasures());
        measures.add(0, null);
        builder.append("Default Measure", defMeasure = new JComboBox(measures.toArray()));
        defMeasure.setRenderer(new OLAPObjectListCellRenderer());
        for (Measure ms : cube.getMeasures()) {
            if (ms.getName().equals(cube.getDefaultMeasure())) {
                defMeasure.setSelectedItem(ms);
                break;
            }
        }
        
        builder.appendSeparator("Fact Table");
        tableChooser = new JComboBox(new Vector<SQLTable>(tables));

        Action radioButtonsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                tableChooser.setEnabled(tableRadioButton.isSelected());
                selectStatements.setEnabled(viewRadioButton.isSelected());
                viewAlias.setEnabled(viewRadioButton.isSelected());
            }
        };
        
        tableRadioButton = new JRadioButton();
        tableRadioButton.setAction(radioButtonsAction);
        tableRadioButton.setText("Use Existing Table");
        viewRadioButton = new JRadioButton();
        viewRadioButton.setAction(radioButtonsAction);
        viewRadioButton.setText("Use View");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(tableRadioButton);
        buttonGroup.add(viewRadioButton);
        
        builder.append(tableRadioButton, 3); 
        builder.append(tableChooser, 3);
        builder.append(viewRadioButton, 3); 
        builder.append("Alias", viewAlias = new JTextField());
        builder.append(new JLabel("SELECT Statements"), 3);
        builder.append(new JScrollPane(selectStatements = new JTextArea("Adding View as a source table in a cube" +
        		" has not yet been implemented", 4, 30)), 3);
        selectStatements.setLineWrap(true);
        
        if (tables.isEmpty()) {
            tableChooser.addItem("Database has no tables");
            viewRadioButton.doClick();
            tableRadioButton.setEnabled(false);
            tableChooser.setEnabled(false);
        } else {
            SQLTable t = OLAPUtil.tableForCube(cube);
            //if SQLTable t is not found, then either cube.fact is not defined, or cube.fact is a view
            if (tables.contains(t)) {
                tableChooser.setSelectedItem(t);
                tableRadioButton.doClick();
            } else if (cube.getFact() != null){
                viewRadioButton.doClick();
            } else {
                tableRadioButton.doClick();
            }
        }
        
        panel = builder.getPanel();
        
        handler = new FormValidationHandler(status);
        Validator validator = new OLAPObjectNameValidator(cube.getParent(), cube, false);
        handler.addValidateObject(nameField, validator);
    }

    public boolean applyChanges() {
        try {
            cube.startCompoundEdit("Modify cube properties");
            if (tableRadioButton.isSelected()) {
                if (tableChooser.isEnabled()) {
                    SQLTable table = (SQLTable) tableChooser.getSelectedItem();
                    if (table != null) {
                        Table t = new Table();
                        t.setName(table.getName());
                        t.setSchema(OLAPUtil.getQualifier(table));
                        cube.setFact(t);
                    }
                }
            } else if (viewRadioButton.isSelected()) {
                //TODO construct and set up the view
//                View view = new View();
//                view.setAlias(viewAlias.getText());
//                cube.setFact(view);
            }
            cube.setName(nameField.getText());
            if (!(captionField.getText().equals(""))) {
                cube.setCaption(captionField.getText());
            } else {
                cube.setCaption(null);
            }
            Measure ms = (Measure) defMeasure.getSelectedItem();
            cube.setDefaultMeasure(ms == null ? null : ms.getName());
        } finally {
            cube.endCompoundEdit();
        }
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
