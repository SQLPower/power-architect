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

import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A DDL warning for when a relationship does not map any columns. There is no
 * quick fix as the user will need to create columns.
 */
public class RelationshipMapsNoColumnsDDLWarning extends AbstractDDLWarning {

    public RelationshipMapsNoColumnsDDLWarning(SQLTable pkTable, SQLTable fkTable) {
        super(Arrays.asList(new SQLObject[] { pkTable, fkTable }), "No columns mapped in relationship between tables " +
                pkTable.getName() + " and " + fkTable.getName(), false, null, null, null);
    }
}
