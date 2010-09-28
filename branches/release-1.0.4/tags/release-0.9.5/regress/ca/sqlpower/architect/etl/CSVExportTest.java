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
package ca.sqlpower.architect.etl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class CSVExportTest extends TestCase {

    ExportCSV export;
    private int headerColCount;
    
    protected void setUp() throws Exception {
        super.setUp();
        ArchitectDataSource ds = new ArchitectDataSource();
        SQLDatabase db1 = new SQLDatabase(ds);
        db1.setPhysicalName("db1");
        SQLCatalog catalog1 = new SQLCatalog(db1,"catalog1");
        SQLSchema schema1 = new SQLSchema(catalog1,"schema1",true);
        SQLTable table1 = new SQLTable(schema1,"table1","","",true);
        SQLColumn column1 = new SQLColumn(table1,"column1",1,1,1);
        ArchitectDataSource ppds = new ArchitectDataSource();
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
    
    public void testBody() throws ArchitectException{
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
