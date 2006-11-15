package ca.sqlpower.validation.swingui;

import java.awt.Color;

import ca.sqlpower.validation.Validator;

/**
 * A ValidationHandler is a non-visual component that ties a given
 * Validator (which in turn probably depends on a given
 * JComponent, e.g., a JTextComponent) into the StatusComponent;
 * upon events from the JComponent (such as KeyEvents
 * for the TextComponentValidationHandler) the ValidationHandler
 * invokes the Validator's validate() method and updates the
 * JComponent's appearance accordingly.
 * @see ca.sqlpower.validation.TextComponentValidationHandler
 */
public abstract class ValidationHandler {
    /** The Validator to use */
    protected Validator validator;
    /** Where to display results */
    protected StatusComponent statusComponent;
    /** The color to use in the JComponent in the event of error */
    protected final static Color COLOR_ERROR = new Color(255, 170, 170);
    /** The color to use in the JComponent in the event of warnings */
    protected final static Color COLOR_WARNING = Color.YELLOW;

    public ValidationHandler(Validator validator, StatusComponent display) {
        super();
        this.validator = validator;
        this.statusComponent = display;
    }
}
