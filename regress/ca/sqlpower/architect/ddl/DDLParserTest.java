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

import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ddl.DDLParser.DDLParseException;
import ca.sqlpower.architect.ddl.DDLParser.ParsedColumn;
import ca.sqlpower.architect.ddl.DDLParser.ParsedTable;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;

public class DDLParserTest extends TestCase {

    public void testSimpleTable() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "CREATE TABLE person (id INTEGER, name VARCHAR(50));").parse();
        assertEquals(1, tables.size());
        ParsedTable t = tables.get(0);
        assertEquals("person", t.name);
        assertEquals(2, t.columns.size());
        assertEquals("id", t.columns.get(0).name);
        assertEquals("INTEGER", t.columns.get(0).typeName);
        assertEquals("VARCHAR(50)", t.columns.get(1).typeName);
    }

    public void testColumnConstraints() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "CREATE TABLE t (\n" +
                "  id INTEGER NOT NULL PRIMARY KEY,\n" +
                "  active BOOLEAN DEFAULT TRUE,\n" +
                "  note VARCHAR(100) NULL\n" +
                ");").parse();
        ParsedTable t = tables.get(0);
        ParsedColumn id = t.columns.get(0);
        assertFalse(id.nullable);
        assertTrue(id.primaryKey);
        assertTrue(t.primaryKeyColumns.contains("id"));

        ParsedColumn active = t.columns.get(1);
        assertEquals("TRUE", active.defaultValue);

        ParsedColumn note = t.columns.get(2);
        assertTrue(note.nullable);
    }

    public void testTableLevelConstraints() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "CREATE TABLE orders (\n" +
                "  order_id INTEGER,\n" +
                "  customer_id INTEGER,\n" +
                "  PRIMARY KEY (order_id),\n" +
                "  CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer (id)\n" +
                ");").parse();
        ParsedTable t = tables.get(0);
        assertEquals(1, t.primaryKeyColumns.size());
        assertEquals("order_id", t.primaryKeyColumns.get(0));
        assertEquals(1, t.foreignKeys.size());
        assertEquals("customer_id", t.foreignKeys.get(0).columns.get(0));
        assertEquals("customer", t.foreignKeys.get(0).referencedTable);
        assertEquals("id", t.foreignKeys.get(0).referencedColumns.get(0));
    }

    public void testCommentsAreIgnored() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "-- a leading comment\n" +
                "CREATE TABLE t (\n" +
                "  id INTEGER, /* inline comment */\n" +
                "  name VARCHAR(10) -- trailing comment\n" +
                ");").parse();
        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).columns.size());
    }

    public void testQuotedIdentifiers() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "CREATE TABLE \"my table\" (\"my col\" INTEGER, `other` VARCHAR(5), [third] INT);").parse();
        ParsedTable t = tables.get(0);
        assertEquals("my table", t.name);
        assertEquals("my col", t.columns.get(0).name);
        assertEquals("other", t.columns.get(1).name);
        assertEquals("third", t.columns.get(2).name);
    }

    public void testMultipleStatements() throws Exception {
        List<ParsedTable> tables = new DDLParser(
                "CREATE TABLE a (id INT);\n" +
                "CREATE TABLE b (id INT);\n").parse();
        assertEquals(2, tables.size());
        assertEquals("a", tables.get(0).name);
        assertEquals("b", tables.get(1).name);
    }

    public void testParseError() {
        try {
            new DDLParser("CREATE TABLE t (id INTEGER").parse();
            fail("Expected a parse error");
        } catch (DDLParseException e) {
            assertTrue(e.getErrorLine() >= 1);
        }
    }

    public void testBuildTables() throws Exception {
        List<ParsedTable> parsed = new DDLParser(
                "CREATE TABLE person (\n" +
                "  id INTEGER NOT NULL PRIMARY KEY,\n" +
                "  name VARCHAR(50),\n" +
                "  age NUMERIC(3)\n" +
                ");").parse();
        List<SQLTable> tables = new DDLTableBuilder().buildTables(parsed);
        assertEquals(1, tables.size());
        SQLTable table = tables.get(0);
        assertEquals("person", table.getName());
        assertEquals(3, table.getColumns().size());

        SQLColumn id = table.getColumnByName("id");
        assertNotNull(id);
        assertTrue(id.isPrimaryKey());
        SQLColumn age = table.getColumnByName("age");
        assertNotNull(age);
        assertEquals(3, age.getPrecision());
    }
}