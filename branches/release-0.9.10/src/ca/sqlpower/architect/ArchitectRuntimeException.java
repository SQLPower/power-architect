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
