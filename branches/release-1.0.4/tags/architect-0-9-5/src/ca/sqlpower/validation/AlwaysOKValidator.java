package ca.sqlpower.validation;

/**
 * this volidator always return ok status, you can use it on
 * a JComponent that does not require true validation, but to 
 * trigger the whole form validation.
 *
 */
public class AlwaysOKValidator implements Validator {

    public ValidateResult validate(Object contents) {
        return ValidateResult.createValidateResult(Status.OK, "");
    }

}
