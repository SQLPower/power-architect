package ca.sqlpower.architect;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A general exception class for the Architect application.
 */
public class ArchitectException extends Exception implements java.io.Serializable {
	protected Throwable cause;

	public ArchitectException(String message) {
		this(message, null);
	}

	public ArchitectException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}

	public String toString() {
		if (cause != null) {
			return super.toString()+" (cause: "+cause.toString()+")";
		} else {
			return super.toString();
		}
	}

	public void printStackTrace() {
		printStackTrace(System.out);
	}

	public void printStackTrace(PrintWriter out) {
		super.printStackTrace(out);
		if (cause != null) {
			out.println("Root Cause:");
			cause.printStackTrace(out);
		}
	}

	public void printStackTrace(PrintStream out) {
		super.printStackTrace(out);
		if (cause != null) {
			out.println("Root Cause:");
			cause.printStackTrace(out);
		}
	}
}
