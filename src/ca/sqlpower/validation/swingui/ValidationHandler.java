package ca.sqlpower.validation.swingui;

import ca.sqlpower.validation.Validator;

public abstract class ValidationHandler {
    Validator validator;
    StatusComponent statusComponent;

    public ValidationHandler(Validator validator, StatusComponent display) {
        super();
        this.validator = validator;
        this.statusComponent = display;
    }
}
