package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This is the ValidationHandlers for JComponents, for now it supports
 * JTextComponent, JComboBox, AbstractButton.
 * the FormValidationHandler keeps a List of Jcomponents and the validators,
 * listens the changes of each JComponent and validate them, update the
 * <b>StatusComponent</b> 'display' to the worst result and change the
 * backgroound color of the problem JComponent to red or yellow.
 *<br>
 *<br>
 * -for JTextComponent, the validator needs to validate String (text in the component)
 * <br>
 * -for JComboBox, the validator needs to validate Object (Item in the component)
 * <br>
 * -for AbstractButton, the validator needs to validate boolean (status of the component)
 * <br>
 */
public class FormValidationHandler implements ValidationHandler {

    /** The color to use in the JComponent in the event of error */
    protected final static Color COLOR_ERROR = new Color(255, 170, 170);
    /** The color to use in the JComponent in the event of warnings */
    protected final static Color COLOR_WARNING = Color.YELLOW;

    /**
     * a JComponent to display result
     */
    private StatusComponent display;

    /**
     * List of combination of JComponent,validator...
     */
    private List<ValidateObject> objects;

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
    }

    public FormValidationHandler(StatusComponent display) {
        this.display = display;
        objects = new ArrayList<ValidateObject>();
    }

    private void showValidate() {
        ValidateResult s = null;
        String message = null;

        for ( ValidateObject v : objects ) {
            if ( v.getResult() == null ) continue;
            message = v.getResult().getMessage();
            if ( v.getResult().getStatus() == Status.FAIL ) {
                s = v.getResult();
                break;
            } else if ( v.getResult().getStatus() == Status.WARN &&
                    ( s == null || s.getStatus() != Status.WARN) ) {
                s = v.getResult();
            }
        }
        display.setResult(s);
        display.setText(message);
    }

    /**
     * add your Jcomponent and validator to the List
     */
    public void addValidateObject(final JComponent component, final Validator validator) {
        final ValidateObject validateObject = new ValidateObject(component,validator);
        objects.add(validateObject);

        if ( component instanceof JTextComponent ) {
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
                        validateObject.doValidate();
                        showValidate();
                    }
                });
        } else if ( component instanceof JComboBox ) {
            ((JComboBox)component).addItemListener(new ItemListener(){
                public void itemStateChanged(ItemEvent e) {
                    validateObject.setObject(((JComboBox)component).getSelectedItem());
                    validateObject.doValidate();
                    showValidate();
                }});
        } else if ( component instanceof AbstractButton ) {
            ((AbstractButton)component).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    validateObject.setObject(((AbstractButton)component).isSelected());
                    validateObject.doValidate();
                    showValidate();
                }});
        } else {
            throw new IllegalArgumentException("Unsupported JComponent type:"+
                    component.getClass());
        }
    }
}
