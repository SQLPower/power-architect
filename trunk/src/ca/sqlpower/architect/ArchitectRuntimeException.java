package ca.sqlpower.architect;

/**
 * The ArchitectRuntimeException is designed to wrap an
 * ArchitectException in cases where a method which is not allowed to
 * throw checked exceptions must propogate an ArchitectException.
 *
 * <p>This exception takes on the message and cause of the
 * ArchitectException that it wraps, so it will rarely be necessary to
 * "unwrap" an ArchitectException from an ArchitectRuntimeException.
 * If you do need that (for instance, when re-throwing as a checked
 * exception), use the asArchitectException method.
 */
public class ArchitectRuntimeException extends RuntimeException {
	protected ArchitectException wrapped;

	/**
	 * Creates an unchecked exception wrapper for the given
	 * ArchitectException.
	 */
	public ArchitectRuntimeException(ArchitectException wrapme) {
		this.wrapped = wrapme;
	}

	/**
	 * Returns the cause of the wrapped ArchitectException.  The
	 * return value will be null if the wrapped exception has no
	 * cause.
	 */
	public Throwable getCause() {
		return wrapped.getCause();
	}

	/**
	 * Returns the message of the wrapped ArchitectException.
	 */
	public String getMessage() {
		return wrapped.getMessage();
	}
	
	/**
	 * Returns the actual ArchitectException that this exception
	 * wraps.  It shouldn't normally be nexessary to use this method.
	 */
	public ArchitectException asArchitectException() {
		return wrapped;
	}
}
