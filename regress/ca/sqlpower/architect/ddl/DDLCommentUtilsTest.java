/*
 * Copyright (c) 2026, SQL Power Group Inc.
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

import java.sql.Types;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;

public class DDLCommentUtilsTest extends TestCase {

    public void testValidateAcceptsNormalNotes() {
        assertNull(DDLCommentUtils.validate("Just a note"));
        assertNull(DDLCommentUtils.validate("Line one\nLine two\twith tab"));
        assertNull(DDLCommentUtils.validate(""));
        assertNull(DDLCommentUtils.validate(null));
    }

    public void testValidateRejectsControlCharacters() {
        assertNotNull(DDLCommentUtils.validate("bad\u0000note"));
        assertNotNull(DDLCommentUtils.validate("bell\u0007"));
    }

    public void testToCommentBlockPrefixesEveryLine() {
        String block = DDLCommentUtils.toCommentBlock("first\nsecond");
        assertEquals("-- first\n-- second\n", block);
    }

    public void testNotesAppendedToGeneratedDDL() throws Exception {
        GenericDDLGenerator ddl = new GenericDDLGenerator(false);
        SQLTable table = new SQLTable();
        table.initFolders(true);
        table.setPhysicalName("note_table");
        SQLColumn id = new SQLColumn(table, "id", Types.INTEGER, 0, 0);
        table.addColumn(id);
        table.setDdlNotes("Hand-written note\nwith two lines");

        ddl.addTable(table);

        boolean found = false;
        for (DDLStatement stmt : ddl.getDdlStatements()) {
            if (stmt.getSQLText().contains("-- Hand-written note")) {
                assertTrue(stmt.getSQLText().contains("-- with two lines"));
                found = true;
            }
        }
        assertTrue("Expected the DDL notes to be emitted as comments", found);
    }

    public void testNoNotesMeansNoExtraStatement() throws Exception {
        GenericDDLGenerator ddl = new GenericDDLGenerator(false);
        SQLTable table = new SQLTable();
        table.initFolders(true);
        table.setPhysicalName("plain_table");
        table.addColumn(new SQLColumn(table, "id", Types.INTEGER, 0, 0));
        ddl.addTable(table);

        List<DDLStatement> statements = ddl.getDdlStatements();
        for (DDLStatement stmt : statements) {
            assertFalse(stmt.getSQLText().contains("-- "));
        }
    }
}