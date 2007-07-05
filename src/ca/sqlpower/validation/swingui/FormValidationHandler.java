/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This is the ValidationHandler for JComponents; supporting such
 * classes as JTextComponent, JComboBox, AbstractButton.
 * The FormValidationHandler keeps a List of JComponents and the validators,
 * listens for changes of each JComponent and validates them, update the
 * <b>StatusComponent</b> 'display' to the worst result and changes the
 * background color of the problem JComponent to red or yellow.
 * <br>
 * -for JTextComponent, the validator needs to validate String (text in the component)
 * <br>
 * -for JComboBox, the validator needs to validate Object (Item in the component)
 * <br>
 * -for AbstractButton, the validator needs to validate boolean (status of the component)
 * <br>
 * -for Jtable, the validator needs to validate tableModel
 * <br>
 */
public class FormValidationHandler implements ValidationHandler {

    private static final Logger logger = Logger.getLogger(FormValidationHandler.class);

    /** The color to use in the JComponent in the event of error */
    protected final static Color COLOR_ERROR = new Color(255, 170, 170);
    /** The color to use in the JComponent in the event of warnings */
    protected final static Color COLOR_WARNING = Color.YELLOW;

    /**
     * a JComponent to display result
     */
    private StatusComponent display;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * List of combination of JComponent,validator...
     */
    private List<ValidateObject> objects;
    private ValidateResult worstValidationStatus;

    /**
     * True if this validation handler has handled at least one validation pass
     * since the validation status was last reset.  Mainly used
     * for implementing hasUnsaveChanges() method in EditorPane.
     */
    private boolean havePerformedValidation;

    private class ValidateObject {
        /**
         * the Jcomponent, for save and set the background color
         */
        private JComponent component;
        /**
         * the object we want to validate, could be the text in the JTextField
         * or select item in the JComboBox, etc.
         */
        private Object object;

        /**
         * the validator to do the validation
         */
        private Validator validator;
        private Color savedColor;
        private ValidateResult result;

        protected String getMessage() {
            StringBuffer msg = new StringBuffer();
            if ( getResult() != null ) {
                msg.append("[").append(getResult().getStatus().name()).append("]");
                msg.append(" ").append(getResult().getMessage());
            } else {
                msg.append(" unknown result");
            }
            return msg.toString();
        }

        protected ValidateObject(JComponent component, Validator validator) {
            this.component = component;
            this.validator = validator;
            savedColor = component.getBackground();
        }

        /**
         * calls validator to do the validation; object to be validated
         * may be null, and handled by the validator
         */
        protected void doValidate() {
            result = validator.validate(object);
            havePerformedValidation = true;
            switch(result.getStatus()) {
            case OK:
                component.setBackground(savedColor);
                break;
            case WARN:
                component.setBackground(COLOR_WARNING);
                break;
            case FAIL:
                component.setBackground(COLOR_ERROR);
                break;
            }
        }
        protected Color getSavedColor() {
            return savedColor;
        }
        protected ValidateResult getResult() {
            return result;
        }
        protected void setObject(Object object) {
            this.object = object;
        }

        protected JComponent getComponent() {
            return component;
        }
    }

    public FormValidationHandler(StatusComponent display) {
        this.display = display;
        objects = new ArrayList<ValidateObject>();
    }


    /**
     * Add one Jcomponent and its validator to the List
     */
    public void addValidateObject(final JComponent component, final Validator validator) {
        final ValidateObject validateObject = new ValidateObject(component,validator);
        objects.add(validateObject);

        if (component instanceof JTextComponent) {
            validateObject.setObject(((JTextComponent)component).getText());
            ((JTextComponent)component).getDocument()
                .addDocumentListener(new DocumentListener(){
                    public void insertUpdate(DocumentEvent e) {
                        doStuff();
                    }
                    public void removeUpdate(DocumentEvent e) {
                        doStuff();
                    }
                    public void changedUpdate(DocumentEvent e) {
                        doStuff();
                    }
                    private void doStuff() {
                        validateObject.setObject(((JTextComponent)component).getText());
                        performFormValidation();
                    }
                });
        } else if ( component instanceof JComboBox ) {
            validateObject.setObject(((JComboBox)component).getSelectedItem());
            ((JComboBox)component).addItemListener(new ItemListener(){
                public void itemStateChanged(ItemEvent e) {
                    validateObject.setObject(((JComboBox)component).getSelectedItem());
                    performFormValidation();
                }});
        } else if ( component instanceof AbstractButton ) {
            validateObject.setObject(((AbstractButton)component).isSelected());
            ((AbstractButton)component).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    validateObject.setObject(((AbstractButton)component).isSelected());
                    performFormValidation();
                }});
        } else if ( component instanceof JTable ) {
            JTable table = (JTable)component;

            final TableModel tableModel = table.getModel();
            validateObject.setObject(tableModel);
            final TableModelListener tableModelListener = new TableModelListener(){
                public void tableChanged(TableModelEvent arg0) {
                    validateObject.setObject(tableModel);
                    performFormValidation();
                }};
            tableModel.addTableModelListener(tableModelListener);
            table.addPropertyChangeListener("model", new PropertyChangeListener(){

                public void propertyChange(PropertyChangeEvent evt) {
                    TableModel old = (TableModel) evt.getOldValue();
                    old.removeTableModelListener(tableModelListener);
                    TableModel newModel = (TableModel) evt.getNewValue();
                    newModel.addTableModelListener(tableModelListener);
                    performFormValidation();
                }

            });

        } else {
            throw new IllegalArgumentException("Unsupported JComponent type:"+
                    component.getClass());
        }
    }

    private void performFormValidation() {

        ValidateResult worst = null;

        for (ValidateObject o : objects) {
            o.doValidate();
            if ( o.getResult() == null ) {
            } else if ( o.getResult().getStatus() == Status.FAIL &&
                    (worst == null || worst.getStatus() != Status.FAIL) ) {
                worst = o.getResult();
            } else if ( o.getResult().getStatus() == Status.WARN &&
                    ( worst == null || worst.getStatus() == Status.OK) ) {
                worst = o.getResult();
            } else if ( worst == null ) {
                worst = o.getResult();
            }
        }

        setWorstValidationStatus(worst);
        display.setResult(worst);
    }

    public ValidateResult getWorstValidationStatus() {
        return worstValidationStatus;
    }

    private void setWorstValidationStatus(ValidateResult result) {
        ValidateResult oldResult = this.worstValidationStatus;
        this.worstValidationStatus = result;
        pcs.firePropertyChange("worstValidationStatus", oldResult, result);
    }

    public List<String> getFailResults() {
        return getResults(Status.FAIL);
    }
    public List<String> getWarnResults() {
        return getResults(Status.WARN);
    }
    private List<String> getResults(Status status) {
        List <String> msg = new ArrayList<String>();
        for ( ValidateObject o : objects ) {
            if ( o.getResult() == null )
                continue;
            if ( o.getResult().getStatus() == status ) {
                msg.add(o.getMessage());
            }
        }
        return msg;
    }


    /**
     * get the validate result of the given object, it should be one of the Jcomponent
     * that added to the handler earlier.
     * @param object -- one of the Jcomponent that added to the handler earlier.
     * @return Validate Result
     * @throws IllegalArgumentException if the object is not on the list
     */
    public ValidateResult getResultOf(Object object) {
        for ( ValidateObject o : objects ) {
            if (object == o.getComponent() ) {
                return o.getResult();
            }
        }
        throw new IllegalArgumentException("Object:" +
                (object==null?"null":object) + " not found!");
    }

    // listener stuff
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeSupport getPCS(){
        return pcs;
    }

    /** 
     * See {@link #havePerformedValidation}.
     */
    public boolean hasPerformedValidation() {
        return havePerformedValidation;
    }

    /**
     * Sets the havePerformedValidation property back to false.
     */
    public void resetHasValidated() {
        this.havePerformedValidation = false;
    }
}
