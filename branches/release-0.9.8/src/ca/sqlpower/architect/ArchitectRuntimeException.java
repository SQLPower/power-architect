/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
