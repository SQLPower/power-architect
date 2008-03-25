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
    