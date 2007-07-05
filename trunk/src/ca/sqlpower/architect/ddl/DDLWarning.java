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
package ca.sqlpower.architect.ddl;

import java.util.List;

import ca.sqlpower.architect.SQLObject;

/**
 * A DDLWarning object encapsulates the details of a single warning
 * message issued by a DDL generator.
 */
public interface DDLWarning {

	/**
	 * Get the message associated with this warning, e.g., a string
     * like "Primary Key Name is already in use"
	 */
	public String getMessage();

	/**
	 * The subject(s) of this warning.  For instance, if there is a
	 * duplicate table names, the SQLTable objects with the duplicate
	 * names will be the "involved objects".
	 */
	public List<SQLObject> getInvolvedObjects();

	/** Return true if the user has repaired or quickfixed the problem */
	public boolean isFixed();

    public void setFixed(boolean fixed);

    /** Tell whether the user can "quick fix" this problem */
    public boolean isQuickFixable();

    /** If isQuickFixable(), then this gives the message about what
     * will be done.
     */
    public String getQuickFixMessage();

    /** If isQuickFixable(), then this applies the quick fix */
    public boolean quickFix();

}
