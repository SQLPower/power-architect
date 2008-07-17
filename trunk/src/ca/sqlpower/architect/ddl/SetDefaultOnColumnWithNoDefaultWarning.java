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

import java.util.Collections;

import ca.sqlpower.architect.SQLColumn;

/**
 * A DDL warning about relationship update or delete rule that tries to set default
 * on a column with no default value. Some database platforms consider this an error,
 * and others ignore it. In either case, we treat it as a mistake in the data model
 * that the user should rectify.
 * <p>
 * There is no quick fix for this one, because we can't guess what a suitable default
 * value would be.
 */
public class SetDefaultOnColumnWithNoDefaultWarning extends AbstractDDLWarning {
    
    public SetDefaultOnColumnWithNoDefaultWarning(SQLColumn fkcol) {
        super(Collections.singletonList(fkcol),
                "SET DEFAULT action in relationship references column " + fkcol.getName() + " with no default value",
                false,
                null,
                fkcol,
                "defaultValue");
    }
    
}
