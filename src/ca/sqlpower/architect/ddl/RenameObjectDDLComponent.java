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
package ca.sqlpower.architect.ddl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

public class RenameObjectDDLComponent extends GenericDDLWarningComponent {

    private static final Logger logger = Logger.getLogger(RenameObjectDDLComponent.class);
    
    private final DDLWarning warning;
    private JComponent component;
    
    /**
     * List of text fields that correspond to the name of each
     * SQLObject in the list of involved objects for the warning
     * this component holds.
     */
    final Map<JTextField, SQLObject> textFields = new HashMap<JTextField, SQLObject>();
    
    private Runnable changeApplicator;

    public RenameObjectDDLComponent(DDLWarning warning, Runnable changeApplicator) {
        super(warning);
        
        logger.debug("Creating warning component for " + warning);

        this.warning = warning;
        this.changeApplicator = new Runnable() {

            public void run() {
                //TODO should set component values according to the textbox,
                //however, since the quick fix does not change the textbox,
                //setting component values to textbox value would undo the 
                //quick fix.
                for (Map.Entry<JTextField, SQLObject> entry : textFields.entrySet()) {
                    entry.getValue().setName(entry.getKey().getText());
                }
            }
            
        };
        component = new JPanel();        
        component.add(getQuickFixButton());                 // XXX anti-pattern
        component.add(new JLabel(warning.getMessage()));
        if (warning.getQuickFixPropertyName() != null) {
            component.add(new JLabel(" Change " + warning.getQuickFixPropertyName() + ": "));
            List<? extends SQLObject> list = warning.getInvolvedObjects();
            for (SQLObject obj : list) {
                final JTextField jtf = new JTextField(obj.getName());
                obj.addSQLObjectListener(new SQLObjectListener() {
                    public void dbStructureChanged(SQLObjectEvent e) {
                    }
                    public void dbObjectChanged(SQLObjectEvent e) {
                        jtf.setText(e.getSQLSource().getName());
                    }
                    public void dbChildrenRemoved(SQLObjectEvent e) {
                    }
                    public void dbChildrenInserted(SQLObjectEvent e) {
                    }
                });
                component.add(jtf);
                textFields.put(jtf, obj);
            }
        }
    }

    public void applyChanges() {
        changeApplicator.run();
    }

    public Runnable getChangeApplicator() {
        return changeApplicator;
    }

    public JComponent getComponent() {
        return component;
    }

    public DDLWarning getWarning() {
        return warning;
    }

}
