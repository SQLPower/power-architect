/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
