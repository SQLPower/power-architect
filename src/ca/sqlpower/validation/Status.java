package ca.sqlpower.validation;

/** The status of a validation
 */
public enum Status {
    /** The validation is "just grrreat"
     */
    OK,
    /** The validation has some warning, but can proceed with
     * the given use input value (simple validations may be unable
     * to notice this state and might return only OK or FAIL).
     */
    WARN,
    /** The validation is not acceptable and we cannot proceed
     * with the given user input
     */
    FAIL
}
