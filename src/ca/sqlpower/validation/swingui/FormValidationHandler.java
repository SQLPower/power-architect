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

import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This is the prototype for a variety of ValidationHandlers; it
 * uses a JComboBox and a Validator.
 */
public class FormValidationHandler {

    /** The color to use in the JComponent in the event of error */
    protected final static Color COLOR_ERROR = new Color(255, 170, 170);
    /** The color to use in the JComponent in the event of warnings */
    protected final static Color COLOR_WARNING = Color.YELLOW;

    private StatusComponent display;
    private List<ValidateObject> objects;

    private class ValidateObject {
        /**
         * the Jcomponent, for save and set the backgrond color
         */
        private JComponent component;
        /**
         * the object that we want to validate, could be the text in the JTextField
         * or select item in the JComboBox
         */
        private Object object;
        private Validator validator;
        private Color savedColor;
        private ValidateResult result;

        protected ValidateObject(JComponent component, Validator validator) {
            this.component = component;
            this.validator = validator;
            savedColor = component.getBackground();
        }
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
            if ( v.getResult().getStatus() == ValidateResult.Status.FAIL ) {
                s = v.getResult();
                break;
            } else if ( v.getResult().getStatus() == ValidateResult.Status.WARN &&
                    ( s == null || s.getStatus() != ValidateResult.Status.WARN) ) {
                s = v.getResult();
            }
        }
        display.setResult(s);
        display.setText(message);
    }

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
