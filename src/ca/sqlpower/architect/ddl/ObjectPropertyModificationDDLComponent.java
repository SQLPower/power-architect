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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.Messages;

/**
 * An instance of this class displays the warning message associated with an
 * object's property, and allows user to actively modify the value of the
 * property to avoid possible SQL failures.
 * <p>
 * By default, if not specified by the warning. An instance would modify the
 * name of the object.
 */
public class ObjectPropertyModificationDDLComponent extends GenericDDLWarningComponent {

    private static final Logger logger = Logger.getLogger(ObjectPropertyModificationDDLComponent.class);
    
    /**
     * This DDL warning specifies the property to be modified by this
     * component.
     */
    private final DDLWarning warning;
    
    private String propertyName;
    
    private JComponent component;
    
    /**
     * List of text fields that correspond to the property of each
     * SQLObject in the list of involved objects for the warning
     * this component holds. The property is determined by the warning
     * type an instance of this class carries.
     */
    final Map<JTextField, SQLObject> textFields = new Hashtable<JTextField, SQLObject>();
    
    private Runnable changeApplicator;

    public ObjectPropertyModificationDDLComponent(DDLWarning warning) {
        super(warning);
        
        logger.debug("Creating warning component for " + warning); //$NON-NLS-1$

        this.warning = warning;
        propertyName = warning.getQuickFixPropertyName();
        
        this.changeApplicator = new Runnable() {
            public void run() {
                logger.debug("Now attempt to modify object property"); //$NON-NLS-1$
                for (Map.Entry<JTextField, SQLObject> entry : textFields.entrySet()) {
                    try {
                        Method setter = PropertyUtils.getWriteMethod(PropertyUtils.getPropertyDescriptor(entry.getValue(), propertyName));
                        setter.invoke(entry.getValue(), entry.getKey().getText());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to update property:" + propertyName + " on " + entry.getValue(), e); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        };
        component = new JPanel(); 
        if (propertyName != null) {
            JButton updateProperty = new JButton(Messages.getString("ObjectPropertyModificationDDLComponent.UpdateProperty")); //$NON-NLS-1$
            updateProperty.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyChanges();
                }
            });
            component.add(updateProperty);
        } else {
            component.add(getQuickFixButton());           
        }
        
        component.add(new JLabel(warning.getMessage()));
        
        if (propertyName != null) {
            component.add(new JLabel(Messages.getString("ObjectPropertyModificationDDLComponent.Change", warning.getQuickFixPropertyName()) + ": "));  //$NON-NLS-1$ //$NON-NLS-2$
            List<? extends SQLObject> list = warning.getInvolvedObjects();
            for (SQLObject obj : list) {
                JTextField jtf = new JTextField();
                jtf.setColumns(5);
                try {
                    Method getter = PropertyUtils.getReadMethod(PropertyUtils.getPropertyDescriptor(obj, propertyName));
                    jtf.setText((String)(getter.invoke(obj)));
                    logger.debug("Successfully modified object's property."); //$NON-NLS-1$
                } catch (Exception e) {
                    throw new RuntimeException("Failed to update property:" + propertyName + " on " + obj, e); //$NON-NLS-1$ //$NON-NLS-2$
                }
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
