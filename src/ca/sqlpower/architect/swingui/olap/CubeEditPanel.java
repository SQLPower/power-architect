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
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CubeEditPanel implements DataEntryPanel {
    
    private final Cube cube;
    private final JPanel panel;
    private final OLAPSession osession;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    private JComboBox tableChooser;
    
    /**
     * Creates a new property editor for the given OLAP Cube. 
     * 
     * @param cube The data model of the cube to edit
     */
    public CubeEditPanel(Cube cube) throws ArchitectException {
        this.cube = cube;
        osession = OLAPUtil.getSession(cube);
        SQLDatabase db = osession.getDatabase();
        List<SQLTable> tables;
        if (db != null) {
            tables = ArchitectUtils.findDescendentsByClass(db, SQLTable.class, new ArrayList<SQLTable>());
        } else {
            tables = Collections.emptyList();
        }
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Name", nameField = new JTextField(cube.getName()));
        builder.append("Table", tableChooser = new JComboBox(new Vector<SQLTable>(tables)));
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
        try {
            cube.startCompoundEdit("Modify cube properties");
            SQLTable table = (SQLTable) tableChooser.getSelectedItem();
            if (table != null) {
                Table t = new Table();
                t.setName(table.getName());
                t.setSchema(OLAPUtil.getQualifier(table));
                cube.setFact(t);
            }
            cube.setName(nameField.getText());
            if (!(captionField.getText().equals(""))) {
                cube.setCaption(captionField.getText());
            } else {
                cube.setCaption(null);
            }
            if (defMeasure.getSelectedItem() != null) {
                cube.setDefaultMeasure(defMeasure.getSelectedItem().toString());
            }
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
}
