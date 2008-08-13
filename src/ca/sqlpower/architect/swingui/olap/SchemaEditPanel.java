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

import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class SchemaEditPanel implements DataEntryPanel {
    
    private JPanel editorPanel;
    private Schema schema;
    
    private final JComboBox databaseBox;
    private final JTextField nameField;
    
    public SchemaEditPanel(Schema schema, ArchitectSwingSession session) {
        this.schema = schema;
        
        List<SQLDatabase> databases;
        try {
            databases = session.getRootObject().getChildren();
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append("Database", databaseBox = new JComboBox(new Vector<SQLDatabase>(databases)));
        if (schema.getParent() != null) {
            databaseBox.setSelectedItem(((OLAPSession)schema.getParent()).getDatabase());
        }
        builder.append("Name", nameField = new JTextField(schema.getName()));
        editorPanel = builder.getPanel();
    }

    public boolean applyChanges() {
        try {
            schema.startCompoundEdit("Modify Schema Properties");
            schema.setName(nameField.getText());
            OLAPSession osession = OLAPUtil.getSession(schema);
            osession.setDatabase((SQLDatabase) databaseBox.getSelectedItem());
        } finally {
            schema.endCompoundEdit();
        }
        return true;
    }

    public void discardChanges() {
        // TODO Auto-generated method stub
        
    }

    public JComponent getPanel() {
        return editorPanel;
    }

    public boolean hasUnsavedChanges() {
        // TODO Auto-generated method stub
        return false;
    }

}
