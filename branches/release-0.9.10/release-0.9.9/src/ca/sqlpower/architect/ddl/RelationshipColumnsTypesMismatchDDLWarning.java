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

import java.util.Arrays;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;

/**
 * A DDLWarning for when the types of two columns in a relationship do not match.
 * There is no quick fix as simply changing one column type could recursively 
 * cause other errors, thus the relationship is just not created and the relationship
 * sql statement is commented out.
 */
public class RelationshipColumnsTypesMismatchDDLWarning extends AbstractDDLWarning {

	/**
	 * Creates a DDLWarning with a the given pk and fk columns as involved objects
	 * and a message identifying the error and the involved columns' names.
	 * @param pkColumn The pk column that is involved in the types mismatch.
	 * @param fkColumn The fk column that is involved in the types mismatch.
	 */
    public RelationshipColumnsTypesMismatchDDLWarning(SQLColumn pkColumn,
            SQLColumn fkColumn) {
        super(Arrays.asList(new SQLObject[] {pkColumn, fkColumn}),
                "Column types mismatch in mapping for " +
                pkColumn.getName() + " to " + fkColumn.getName(),
                false, null, null, null);
    }
}
    