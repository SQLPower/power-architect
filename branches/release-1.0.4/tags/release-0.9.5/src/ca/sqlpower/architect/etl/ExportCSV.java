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
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

public class ExportCSV {
    private Collection<SQLTable> exportList;
    private boolean passwordVisible;
    private static final String EOL = System.getProperty("line.separator");
    
    public ExportCSV(Collection<SQLTable> exportList) {
        this.exportList = exportList;
    }
    
    public String getCSVMapping() throws ArchitectException {
        StringBuffer buf = new StringBuffer();
        buf.append(createHeader()+EOL);
        for (String row : createBody()) {
            buf.append(row);
            buf.append(EOL);
        }        
        return buf.toString();
    }
    
    public String createHeader() {
        return getColumnToCSVHeaders("SOURCE_")+"," + getColumnToCSVHeaders("TARGET_");
    }
    
    public List<String> createBody() throws ArchitectException {
        List<String> rows = new ArrayList<String>();
        for (Object o :  exportList){
            if (o instanceof SQLTable){
                for ( SQLColumn c : ((SQLTable) o).getColumns()){
                    rows.add(columnToCSV(c.getSourceColumn())+","+columnToCSV(c));
                }
            }             
        }
        return rows;
    }
    
    private String getColumnToCSVHeaders(String prefix){
        StringBuffer buf = new StringBuffer(getDsToCSVHeaders(prefix));
        buf.append(",").append(prefix).append("DATABASE");
        buf.append(",").append(prefix).append("SCHEMA");
        buf.append(",").append(prefix).append("CATALOG");
        buf.append(",").append(prefix).append("TABLE");
        buf.append(",").append(prefix).append("COLUMN");
        return buf.toString();
    }
    
    private StringBuffer columnToCSV(SQLColumn c) throws ArchitectException {
        StringBuffer connection = new StringBuffer();
        StringBuffer database = new StringBuffer();
        StringBuffer schema =new StringBuffer();
        StringBuffer catalog =new StringBuffer();
        StringBuffer table = new StringBuffer();
        if (c != null) {
            SQLObject parent = c.getParent();
            while (parent != null) {
                if (parent instanceof SQLDatabase){
                    SQLDatabase db = (SQLDatabase) parent;
                    connection.append(dsToCSV(db.getDataSource()));
                    database.append(db.getPhysicalName());
                } else if( parent instanceof SQLSchema) {
                    schema.append(parent.getPhysicalName());
                } else if (parent instanceof SQLCatalog) {
                    catalog.append(parent.getPhysicalName());
                } else if (parent instanceof SQLTable){
                    table.append(parent.getPhysicalName());
                } else if (parent instanceof SQLTable.Folder){
                    
                } else{
                    throw new ArchitectException("Invalid object tree, parent should be a database, schema or catalog");
                }
                parent = parent.getParent();
            }
        }
        if (connection.length() == 0) {
            connection.append(dsToCSV(null));
        }
        connection.append(",").append("\""+database.toString().replaceAll("\"","\"\"")+"\"");
        connection.append(",").append("\""+catalog.toString().replaceAll("\"","\"\"")+"\"");
        connection.append(",").append("\""+schema.toString().replaceAll("\"","\"\"")+"\"");
        connection.append(",").append("\""+table.toString().replaceAll("\"","\"\"")+"\"");
        connection.append(",");
        if (c!= null) {
            connection.append("\""+c.getPhysicalName().replaceAll("\"","\"\"")+"\"");
        }
        
        return connection;
    }
    
    private String getDsToCSVHeaders(String prefix){
        StringBuffer header = new StringBuffer();
        header.append(prefix).append("DISPLAY_NAME,"); 
        header.append(prefix).append("DRIVER_CLASS,");
        header.append(prefix).append("JDBC_URL,");
        header.append(prefix).append("USERNAME,");
        if (isPasswordVisible()) {
            header.append(prefix).append("PASSWORD,");
        }
        header.append(prefix).append("ODBC_DSN");
        return header.toString();
    }
    /**
     * Turn an architect datasource into a set of 6 csv columns, the password column is optional
     * With the headers
     * DISPLAY_NAME,DRIVER_CLASS,JDBC_URL,USERNAME,PASSWORD,DATABASE_TYPE,ODBC_DSN
     */
    private StringBuffer dsToCSV(ArchitectDataSource ds) {
        StringBuffer buf = new StringBuffer();
      
        if (ds != null && ds.getDisplayName() != null) { 
            buf.append("\""+ds.getDisplayName().replaceAll("\"","\"\"")+"\"");
        }
        buf.append(",");
        if (ds != null && ds.getDriverClass() != null) {
            buf.append("\""+ds.getDriverClass().replaceAll("\"","\"\"")+"\"");
        }
        buf.append( ",");
        if (ds != null && ds.getUrl() != null ){
            buf.append("\""+ds.getUrl().replaceAll("\"","\"\"")+"\"");
        }
        buf.append( ",");
        if(ds != null && ds.getUser()!= null) {
            buf.append("\""+ds.getUser().replaceAll("\"","\"\"")+"\"");
        }
        if(isPasswordVisible()) {
            buf.append( ",");
            if(ds != null && ds.getPass()!= null ) {
                buf.append("\""+ds.getPass().replaceAll("\"","\"\"")+"\"");
            }
        }
        buf.append( ",");
        if (ds != null && ds.getOdbcDsn()!=null) {
            buf.append("\""+ds.getOdbcDsn().replaceAll("\"","\"\"")+"\"");
        }
       
        return buf;
    }

    public boolean isPasswordVisible() {
        return passwordVisible;
    }

    public void setPasswordVisible(boolean passwordVisible) {
        this.passwordVisible = passwordVisible;
    }

}
