package ca.sqlpower.validation;

/** The result of a validation
 */
public class ValidateResult {

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


    /**
     * the error or warning or ok message
     */
    private String message;
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * the status of the validation, should be one of the OK,WARN,FAIL
     */
    private Status status = Status.OK;
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

}
