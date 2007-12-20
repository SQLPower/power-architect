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
 * A SQLObject that represents a sequence in a database. This is not yet a
 * full-fledged SQLObject, because it does not have a place in the overall
 * SQLObject tree, and it is not saved with the project. Its main purpose at
 * this point is to exist temporarily during the DDL generation process. One
 * day, the SQLSequence may be worked into the overall tree; however it is a
 * feature only supported by a small number of platforms (only Oracle and
 * PostgreSQL as far as we know) so it is not a high priority.
 * <p>
 * See {@link SQLColumn#getAutoIncrementSequenceName()} for more details on
 * how a sequence might come to exist.
 */
public class SQLSequence extends SQLObject {

    /**
     * Creates a new SQLSequence with the given name.
     */
    public SQLSequence(String name) {
        setPopulated(true);
        setName(name);
        children = Collections.emptyList();
    }
    
    /**
     * Returns false.
     */
    @Override
    public boolean allowsChildren() {
        return false;
    }

    /**
     * Returns null because this type of object doesn't allow children.
     */
    @Override
    public Class<? extends SQLObject> getChildType() {
        return null;
    }

    /**
     * Returns null because this type of object doesn't have a parent.
     */
    @Override
    public SQLObject getParent() {
        return null;
    }
    
    /**
     * Does nothing because this type of object doesn't have a parent.
     */
    @Override
    protected void setParent(SQLObject parent) {
        // no-op
    }

    /**
     * Returns the name of this SQLSequence.
     */
    @Override
    public String getShortDisplayName() {
        return getName();
    }

    /**
     * Does nothing because this type of object is not reverse-engineered.
     */
    @Override
    protected void populate() throws ArchitectException {
        // no op
    }
    
}
