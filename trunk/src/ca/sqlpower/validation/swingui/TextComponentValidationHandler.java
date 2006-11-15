package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import ca.sqlpower.validation.Validator;

/**
 * This is the prototype for a variety of ValidationHandlers; it
 * uses a JTextField or JTextArea and a Validator.
 * <p>
 * As you should expect, the validation must be done with
 * SwingUtilities.invokeLater, since our KeyListener is no
 * different from (and maybe ahead of or after) the JTextArea's
 * KeyListener; if you do the validation directly in the
 * KeyListener, you may get the text from the JTextField before
 * the JTextField's key controller has had a chance to insert the text!
 */
public class TextComponentValidationHandler extends ValidationHandler {

    private JTextComponent source;
    private final Color errorColor = new Color(255, 170, 170);

    public TextComponentValidationHandler(
            Validator val, StatusComponent display,
            JTextComponent textcomp) {

        super(val, display);
        this.source = textcomp;

        textcomp.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        String text = source.getText();
                        boolean oK = validator.validate(text);
                        System.out.printf("Validating %s (%b)%n", text, oK);
                        statusComponent.setError(!oK);
                        if (!oK) {
                            statusComponent.setText(validator.getMessage());
                        }
                        source.setBackground(oK ?
                                source.getParent().getBackground() :
                                errorColor);
                    }
                });
            }

        });
    }
}
