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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.SQL;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.olap.MondrianModel.View;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CubeEditPanel implements ValidatableDataEntryPanel {
    
    private static final Logger logger = Logger.getLogger(CubeEditPanel.class);
    
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
     * TODO remove the dbMapping if it is no longer needed as the session will
     * be used from the playpen or remove the session from the view and join
     * entry panel constructors and pass a dbMapping as required instead.
     * 
     * @param cube
     *            The data model of the cube to edit
     */
    public CubeEditPanel(Cube cube, final PlayPen playPen, final SQLDatabaseMapping dbMapping) throws SQLObjectException {
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
        
        final JButton viewEditButton = new JButton(new AbstractAction("Edit...") {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(new ViewEntryPanel(
                        playPen.getSession(), getDatabase(), CubeEditPanel.this), playPen, "View Builder", DataEntryPanelBuilder.OK_BUTTON_LABEL);
                dialog.pack();
                dialog.setVisible(true);        
            }
        });
        
        builder.appendSeparator("Fact Table");
        tableChooser = new JComboBox(new Vector<SQLTable>(tables));

        final JScrollPane selectStatementsPane = new JScrollPane(selectStatements = new JTextArea("", 4, 30));
        
        Action radioButtonsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                tableChooser.setEnabled(tableRadioButton.isSelected());
                selectStatementsPane.setEnabled(viewRadioButton.isSelected());
                viewAlias.setEnabled(viewRadioButton.isSelected());
                viewEditButton.setEnabled(viewRadioButton.isSelected());
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
        
        DefaultFormBuilder factBuilder = new DefaultFormBuilder(new FormLayout("9dlu, 3dlu, pref, 3dlu, pref:grow"));
        builder.append(factBuilder.getPanel(), 3);
        
        factBuilder.append(tableRadioButton, 5);
        factBuilder.append("", tableChooser, 3);
        factBuilder.append(viewRadioButton, 5); 
        factBuilder.append("", new JLabel("Alias"), viewAlias = new JTextField());
        factBuilder.append("", new JLabel("SELECT Statements"), 3);
        factBuilder.append("", selectStatementsPane, 3);
        
        factBuilder.append("", viewEditButton);
        factBuilder.nextLine();
        selectStatements.setLineWrap(true);
        selectStatements.setEditable(false);
        
        if (cube.getFact() instanceof View) {
            viewRadioButton.doClick();
            tableRadioButton.setEnabled(false);
            tableChooser.setEnabled(false);
            
            //XXX There could be multiple SQL objects in a view but we can only edit one at a time right now.
            final List<SQL> selects = ((View) cube.getFact()).getSelects();
            for (SQL sql : selects) {
                if (sql.getDialect() == null || sql.getDialect().equals("generic")) {
                    selectStatements.append(sql.getText());
                    break;
                }
            }
            if (selectStatements.getText().trim().length() == 0 && !selects.isEmpty()) {
                selectStatements.append(selects.get(0).getText());
            }
        } else if (tables.isEmpty()) {
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
        Validator validator = new OLAPObjectNameValidator((OLAPObject) cube.getParent(), cube, false);
        handler.addValidateObject(nameField, validator);
    }

    public boolean applyChanges() {
        try {
            cube.begin("Modify cube properties");
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
                View view = new View();
                view.setAlias(viewAlias.getText());
                SQL sql = new SQL();
                sql.setText(selectStatements.getText());
                view.addSelect(sql);
                cube.setFact(view);
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
            cube.commit();
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
    
    /**
     * This is the database that is being used in the session. The
     * cube can be made from tables in this database.
     */
    private SQLDatabase getDatabase() {
        return SQLPowerUtils.getAncestor(CubeEditPanel.this.cube, OLAPSession.class).getDatabase();
    }

    public String getSelectText() {
        return selectStatements.getText();
    }

    public void setSelectText(String text) {
        selectStatements.setText(text);
    }

    public void setViewSelected() {
        viewRadioButton.doClick();
    }
}
