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

import java.util.Collections;

/**
 * A SQLExceptionNode exists for reporting failures in the SQLObject
 * hierarchy.  For example, when the DBTree tries to expand a node and
 * that results in failure, it adds one of these at the failure point.
 */
public class SQLExceptionNode extends SQLObject {

	protected Throwable exception;
	protected String message;
	protected SQLObject parent;

	public SQLExceptionNode(Throwable exception, String message) {
		this.exception = exception;
		this.message = message;
        this.children = Collections.EMPTY_LIST;
	}
	
	/**
	 * If you wanna show the exception to the user later on, get it
	 * here.  But don't kid yourself: users don't read error messages.
	 */
	public Throwable getException() {
		return exception;
	}
	

	// ------------- SQLObject Methods -------------
	
	/**
	 * Returns the message.
	 */
	public String getName() {
		return message;
	}
	
	public SQLObject getParent() {
		return parent;
	}
	
	/**
	 * Because these nodes get added just as they are needed, it is
	 * sometimes necessary for users of the class (DBTreeModel) to set
	 * the parent directly.
	 */
	public void setParent(SQLObject parent) {
        SQLObject oldParent = this.parent;
		this.parent = parent;
        fireDbObjectChanged("parent", oldParent, parent);
	}

	public void populate() {
        // nothing to populate
	}

	protected void addChildImpl(int index, SQLObject child) {
		throw new UnsupportedOperationException("SQLExceptionNodes can't have children");
	}

	public boolean isPopulated() {
		return true;
	}
	
	public String getShortDisplayName() {
		return "Error: "+message;
	}
	
	public boolean allowsChildren() {
		return false;
	}
	
	public String toString() {
		return getShortDisplayName();
	}

	public String getMessage() {
	    return message;
	}
	
	public void setMessage(String v) {
        String oldMessage = message;
	    message = v;
        fireDbObjectChanged("message", oldMessage, message);
	}

	@Override
	public Class<? extends SQLObject> getChildType() {
		return null;
	}
}
