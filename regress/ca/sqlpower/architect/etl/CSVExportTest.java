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
package ca.sqlpower.architect.etl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

public class CSVExportTest extends TestCase {

    ExportCSV export;
    private int headerColCount;
    
    protected void setUp() throws Exception {
        super.setUp();
        DataSourceCollection<JDBCDataSource> plIni = new PlDotIni<JDBCDataSource>(JDBCDataSource.class);
        JDBCDataSource ds = new JDBCDataSource(plIni);
        SQLDatabase db1 = new SQLDatabase(ds);
        db1.setPhysicalName("db1");
        SQLCatalog catalog1 = new SQLCatalog(db1,"catalog1");
        SQLSchema schema1 = new SQLSchema(catalog1,"schema1",true);
        SQLTable table1 = new SQLTable(schema1,"table1","","",true);
        SQLColumn column1 = new SQLColumn(table1,"column1",1,1,1);
        JDBCDataSource ppds = new JDBCDataSource(plIni);
        SQLDatabase playpenDB = new SQLDatabase(ppds);
        SQLTable ppTable1 = new SQLTable(playpenDB,true);
        SQLColumn ppColumn1 = new SQLColumn(ppTable1,"ppColumn1",1,1,1);
        ppColumn1.setSourceColumn(column1);
        ArrayList<SQLTable> ppTableList = new ArrayList<SQLTable>();
        ppTableList.add(ppTable1);
        
        export = new ExportCSV(ppTableList);
        
        headerColCount = export.createHeader().split(",").length;
    }
    
    public void testHeader() {
        String header = export.createHeader();
        
        String fields[] = header.split(",");
        
        assertTrue("There needs to be some columns in the header", fields.length >0);
        assertTrue("There should be an even number of fields", fields.length % 2 == 0);
        for (String field: fields) {
            assertTrue("Each field should start with SOURCE_ or TARGET_",field.startsWith("SOURCE_") || field.startsWith("TARGET_"));
        }
    }
    
    public void testBody() throws SQLObjectException{
        List<String> body = export.createBody();
        int rownum = 1;
        for (String line: body) {
            String[] fields = parseCSVRow(line);
            assertEquals("Row "+rownum+" has different field count than header",
                    headerColCount, fields.length);
            rownum++;
        }
        
    }

    enum State { NO_QUOTE, IN_QUOTE }
    
    private String[] parseCSVRow(String row) {
        State state = State.NO_QUOTE;
        StringBuffer buf = new StringBuffer();
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < row.length(); i++) {
            char ch = row.charAt(i);
            if (state == State.NO_QUOTE) {
                if (ch == ',') {
                    items.add(buf.toString());
                    buf = new StringBuffer();
                } else if (ch == '"') {
                    state = State.IN_QUOTE;
                } else {
                    buf.append(ch);
                }
            } else if (state == State.IN_QUOTE) {
                if (ch == '"' && row.charAt(i+1) == '"') {
                    i++;
                    buf.append('"');
                } else if (ch == '"') {
                    state = State.NO_QUOTE;
                } else {
                    buf.append(ch);
                }
            } else {
                throw new IllegalStateException("The computer is on fire");
            }
        }
        items.add(buf.toString());
        return items.toArray(new String[items.size()]);
    }
}
