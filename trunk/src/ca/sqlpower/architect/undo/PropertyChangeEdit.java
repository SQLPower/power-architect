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

package ca.sqlpower.architect.undo;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * This is the generic edit class that dynamically modifies bean properties
 * according to the PropertyChangeEvent source and the property name.
 * 
 * @author kaiyi
 *
 */
public class PropertyChangeEdit extends AbstractUndoableEdit {
    private static final Logger logger = Logger.getLogger(PropertyChangeEdit.class);

    private ArrayList<PropertyChangeEvent> list;

    public PropertyChangeEdit(PropertyChangeEvent e) {
        list = new ArrayList<PropertyChangeEvent>();
        list.add(e);
    }

    public PropertyChangeEdit(Collection<PropertyChangeEvent> list) {
        this.list = new ArrayList<PropertyChangeEvent>();
        this.list.addAll(list);
    }

    /**
     * Sets the value of the property to be the old value
     */
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for (int i = list.size() - 1; i >= 0; i--) {
            try {
                Method setter = PropertyUtils.getWriteMethod(PropertyUtils.getPropertyDescriptor(list.get(i).getSource(), list.get(i).getPropertyName()));
                setter.invoke(list.get(i).getSource(), list.get(i).getOldValue());
                
            } catch (IllegalAccessException ex) {
                logger.error("Exception while trying to undo the changes for" + list.get(i).getPropertyName());
                throw new CannotUndoException();
            } catch (NoSuchMethodException ex) {
                logger.error("Mutator method not found while trying to undo the changes for" + list.get(i).getPropertyName());
                throw new CannotUndoException();
            } catch (InvocationTargetException ex) {
                logger.error("Exception while trying to undo the changes for" + list.get(i).getPropertyName());
                throw new CannotUndoException();
            }
        }
    }

    /**
     * Sets the value of the property to be the new value
     */
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        for (PropertyChangeEvent e : list) {
            try {
                Method setter = PropertyUtils.getWriteMethod(PropertyUtils.getPropertyDescriptor(e.getSource(), e.getPropertyName()));
                setter.invoke(e.getSource(), e.getNewValue());
                
            } catch (IllegalAccessException ex) {
                logger.error("Exception while trying to undo the changes for" + e.getPropertyName());
                throw new CannotUndoException();
            } catch (NoSuchMethodException ex) {
                logger.error("Mutator method not found while trying to undo the changes for" + e.getPropertyName());
                throw new CannotUndoException();
            } catch (InvocationTargetException ex) {
                logger.error("Exception while trying to undo the changes for" + e.getPropertyName());
                throw new CannotUndoException();
            }
        }
    }

    @Override
    public String getPresentationName() {
        return "property change edit";
    }

    @Override
    public String toString() {
        return "Changing property: \"" + list.get(0).getPropertyName() + "\" by "+list;
    }
}
