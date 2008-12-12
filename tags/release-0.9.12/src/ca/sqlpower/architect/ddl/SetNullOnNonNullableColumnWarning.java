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

import java.sql.DatabaseMetaData;
import java.util.Collections;

import ca.sqlpower.architect.SQLColumn;

/**
 * A DDL warning about relationship update or delete rule that tries to set a
 * non-nullable column to null. Some database platforms consider this an error,
 * and others ignore it. In either case, we treat it as a mistake in the data
 * model that the user should rectify.
 */
public class SetNullOnNonNullableColumnWarning extends AbstractDDLWarning {

    /**
     * The column this warning pertains to.
     */
    private final SQLColumn fkcol;

    public SetNullOnNonNullableColumnWarning(SQLColumn fkcol) {
        super(Collections.singletonList(fkcol),
                "SET NULL action in relationship references non-nullable column " + fkcol.getName(),
                true,
                "Make " + fkcol.getName() + " nullable",
                fkcol,
                null);
        this.fkcol = fkcol;
    }
    
    @Override
    public boolean quickFix() {
        fkcol.setNullable(DatabaseMetaData.columnNullable);
        return true; // XXX meaning of returned value not documented in interface
    }

}
