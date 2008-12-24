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
 * A class of warning that means some column's data type cannot be
 * accurately represented in the target database.
 */
public class TypeMapDDLWarning extends AbstractDDLWarning {

    /**
     * Creates a new warning about type mapping problems which have already been
     * resolved.
     * <p>
     * XXX: This is inconsistent with the current QuickFix system; we should
     * instead mark this problem as quickfixable and only when quickFix() is called
     * should the column's type be updated.
     * 
     * @param column The column whose type had to be modified.
     * @param message The message to display to the user about this problem.
     * @param oldType The original generic data type that the column had. 
     * @param td The new data type that the column will have in the target database.
     */
    public TypeMapDDLWarning(
            SQLColumn column,
            String message,
            GenericTypeDescriptor oldType,
            GenericTypeDescriptor td) {
        super(Arrays.asList(new SQLObject[] { column }), message, false, null, null, null);
        // TODO do something to hook in the old type and new type with the quickfix system
    }

}
