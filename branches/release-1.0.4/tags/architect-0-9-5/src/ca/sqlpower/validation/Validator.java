package ca.sqlpower.validation;

/**
 * The general contract of a user-input Validator.
 */
public interface Validator {

    /**
     * Validate the current "contents" against the constructed rules
     * @param contents A String, or JComboBox, or whatever
     * @return
     */
    public ValidateResult validate(Object contents);

}
