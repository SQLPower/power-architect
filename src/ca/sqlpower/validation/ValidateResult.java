package ca.sqlpower.validation;

/** The result of a validation
 */
public class ValidateResult {

    private ValidateResult(Status status, String message) {
        if (status == null) {
            throw new IllegalArgumentException("Status may not be null");
        }
        this.status = status;
        this.message = message;
    }

    /**
     * Factory method, since ValidateResults objects are immutable.
     * @param status
     * @param message
     * @return
     */
    public static ValidateResult createValidateResult(Status status, String message) {
        if (status == Status.OK && (message == null || message.length() == 0))
            return sharedOKResult;
        return new ValidateResult(status, message);
    }

    private static final ValidateResult sharedOKResult =
        new ValidateResult(Status.OK, "");

    /**
     * the status of the validation
     */
    private Status status;
    /**
     * the error or warning or ok message
     */
    private String message;

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
