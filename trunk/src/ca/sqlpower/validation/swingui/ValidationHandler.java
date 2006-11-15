package ca.sqlpower.validation.swingui;

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
    Validator validator;
    StatusComponent statusComponent;

    public ValidationHandler(Validator validator, StatusComponent display) {
        super();
        this.validator = validator;
        this.statusComponent = display;
    }
}
