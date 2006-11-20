package ca.sqlpower.validation;

/** The status of a validation
 */
public enum Status {
    /** The validation was successful
     */
    OK,
    /** The validation has some warning, but can proceed with
     * the given user input value (simple validations may be unable
     * to notice this state and might return only OK or FAIL).
     * In MatchMaker, this means that the user can save but can
     * not run the match engine against this setup.
     */
    WARN,
    /** The validation is not acceptable and we cannot proceed
     * with the given user input.
     * In MatchMarker this means that the user cannot save
     * the current form.
     */
    FAIL
}