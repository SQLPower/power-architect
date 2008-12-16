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

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class GenericDDLGeneratorTest extends TestCase {

    /**
     * Regression testing for bug 1354. If a relationship is forward engineered
     * but has no columns then it should be skipped.
     */
    public void testSkipRelationWithNoColumns() throws Exception {
        GenericDDLGenerator ddl = new GenericDDLGenerator();
        SQLRelationship r = new SQLRelationship();
        r.setName("Test Relation");
        SQLTable pkTable = new SQLTable();
        pkTable.initFolders(true);
        SQLTable fkTable = new SQLTable();
        fkTable.initFolders(true);
        r.attachRelationship(pkTable, fkTable, true);
        ddl.addRelationship(r);
        
        assertEquals(1, ddl.getWarnings().size());
    }
}
