package ca.sqlpower.architect;

/**
 * The ArchitectRuntimeException is designed to wrap an
 * ArchitectException in cases where a method which is not allowed to
 * throw checked exceptions must propogate an ArchitectException.
 */
public class ArchitectRuntimeException extends RuntimeException {
	protected ArchitectException cause;

	public ArchitectRuntimeException(ArchitectException cause) {
		this.cause = cause;
	}

	/**
	 * Returns the cause of this exception, which will always be an
	 * ArchitectException (or subtype thereof).
	 */
	public Throwable getCause() {
		return cause;
	}

	public String getMessage() {
		if (((ArchitectException) cause).getCause() != null) {
			return ((ArchitectException) cause).getCause().getMessage();
		} else {
			return cause.getMessage();
		}
	}
}
