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

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.SQLObjectComboBoxModel;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.NotNullValidator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class SchemaEditPanel implements ValidatableDataEntryPanel {
    
    private JPanel editorPanel;
    private Schema schema;
    
    private final JComboBox databaseBox;
    private final JTextField nameField;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    
    public SchemaEditPanel(ArchitectSwingSession session, Schema schema) throws SQLObjectException {
        this.schema = schema;
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.append(status, 3);
        builder.append("Database", databaseBox = new JComboBox(new SQLObjectComboBoxModel(session.getRootObject(), SQLObject.class)));
        OLAPSession osession = OLAPUtil.getSession(schema);
        if (osession.getDatabase() != null) {
            databaseBox.setSelectedItem(osession.getDatabase());
        } else {
            databaseBox.setSelectedItem(session.getTargetDatabase());
        }
        builder.append("Name", nameField = new JTextField(schema.getName()));
        
        handler = new FormValidationHandler(status);
        handler.addValidateObject(databaseBox, new NotNullValidator("Schema"));
        
        editorPanel = builder.getPanel();
    }

    public boolean applyChanges() {
        try {
            schema.begin("Modify Schema Properties");
            schema.setName(nameField.getText());
            OLAPSession osession = OLAPUtil.getSession(schema);
            osession.setDatabase((SQLDatabase) databaseBox.getSelectedItem());
        } finally {
            schema.commit();
        }
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return editorPanel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }

    public ValidationHandler getValidationHandler() {
        return handler;
    }

}
