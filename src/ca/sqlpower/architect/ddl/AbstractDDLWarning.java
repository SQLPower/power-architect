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

public abstract class AbstractDDLWarning implements DDLWarning {

    protected List<SQLObject> involvedObjects;
    protected String message;
    boolean fixed;
    protected boolean isQuickFixable;
    protected String quickFixMesssage;
    protected SQLObject whichObjectQuickFixFixes;

    public AbstractDDLWarning(List<SQLObject> involvedObjects,
            String message, boolean isQuickFixable,
            String quickFixMesssage, SQLObject whichObjectQuickFixFixes) {
        super();
        this.involvedObjects = involvedObjects;
        this.message = message;
        this.isQuickFixable = isQuickFixable;
        this.quickFixMesssage = quickFixMesssage;
        this.whichObjectQuickFixFixes = whichObjectQuickFixFixes;
    }


    public List<SQLObject> getInvolvedObjects() {
        return involvedObjects;
    }

    public String getMessage() {
        return message;
    }

    public String getQuickFixMessage() {
        return quickFixMesssage;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isQuickFixable() {
        return isQuickFixable;
    }

    /** Dummy version for subclasses that are not quickfixable */
    public boolean quickFix() {
        throw new IllegalStateException("Called generic version of quickFix");
    }
}
