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
import java.util.Collection;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.SPDataSource;

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
    private StringBuffer dsToCSV(SPDataSource ds) {
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
