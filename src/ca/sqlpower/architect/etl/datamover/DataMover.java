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
package ca.sqlpower.architect.etl.datamover;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;


public class DataMover {

    private static final Logger logger = Logger.getLogger(DataMover.class);
    
    /**
     * The most recent SQL string any method has tried to execute.
     * Useful for debugging when things go wrong.
     */
    private String lastSqlString;

    /**
     * If set to true, this DataMover will delete all rows from target tables
     * before loading data into them.
     */
    private boolean truncatingDestinationTable;

    /**
     * In the rare case there is a blob in the source table that maps to a string
     * in the target table, this is the text encoding to assume the bytes in the
     * blob are using.  Default is ISO Latin 1. 
     */
    private String blobTextEncoding = "iso8859-1";
    
    /**
     * Copies all the data in the source table (in the source
     * database) to the table the the given name in the destination
     * database.
     * @throws ArchitectException 
     * @throws UnsupportedEncodingException if there is a blob-to-text
     *  mapping and the current setting of {@link #blobTextEncoding} is
     *  not recognised by the java library.
     */
    public int copyTable(SQLTable destTable, SQLTable sourceTable) throws SQLException, ArchitectException, UnsupportedEncodingException {
        String destTableName = DDLUtils.toQualifiedName(destTable);
        String sourceTableName = DDLUtils.toQualifiedName(sourceTable);
        
        Connection srcCon = null;
        Connection dstCon = null;
        Statement srcStmt = null;
        PreparedStatement dstStmt = null;
        ResultSet srcRS = null;
        ResultSetMetaData srcRSMD = null;
        long startTime = System.currentTimeMillis();
        int numRows = 0;
        try {
            srcCon = sourceTable.getParentDatabase().getConnection();
            dstCon = destTable.getParentDatabase().getConnection();
            
            srcStmt = srcCon.createStatement();
            lastSqlString = "select * from "+sourceTableName;
            srcRS = srcStmt.executeQuery(lastSqlString);
            srcRSMD = srcRS.getMetaData();

            if (logger.isInfoEnabled()) {
                logger.info(summarizeResultSetMetaData(srcRSMD));
            }

            if (truncatingDestinationTable) {
                Statement delStmt = null;
                try {
                    delStmt = dstCon.createStatement();
                    lastSqlString = "DELETE FROM "+destTableName;
                    int count = delStmt.executeUpdate(lastSqlString);
                    logger.info("Deleted "+count+" rows from destination table");
                } finally {
                    if (delStmt != null) delStmt.close();
                }
            }

            // crazy special case.. not for general use!
            if (destTable.getColumnByName("rowid") != null) {
                SQLColumn rowidCol = destTable.getColumnByName("rowid");
                destTable.removeColumn(rowidCol);
            }
            
            lastSqlString = generateInsertStatement(destTable);
            dstStmt = dstCon.prepareStatement(lastSqlString);

            int numberOfColumns = destTable.getColumns().size();
            while (srcRS.next()) {
                logger.debug("Row "+numRows+" of "+destTableName);
                for (int col = 0; col < numberOfColumns; col++) {
                    
                    SQLColumn destCol = destTable.getColumn(col);
                    SQLColumn sourceCol = sourceTable.getColumnByName(destCol.getName());
                    int sourceColIndex = sourceCol == null ? -1 : srcRS.findColumn(sourceCol.getName());
                    
                    if (logger.isDebugEnabled()) {
                        if (sourceCol == null) {
                            logger.debug("   Source column "+destCol.getName()+" not found!");
                        } else {
                            logger.debug("   "+srcRS.getObject(sourceColIndex)+ " (type="+srcRSMD.getColumnType(sourceColIndex)+")");
                        }
                    }
                    
                    if (sourceCol == null) {
                        
                        // the source database doesn't have this column -- use null
                        dstStmt.setObject(col+1, null, destCol.getType());
                        
                    } else if (srcRSMD.getColumnType(sourceColIndex) == Types.BLOB && destCol.getType() == Types.VARCHAR) {
                        
                        // note, this was a special case for copying a jforum schema from oracle
                        // to postgresql (source was a blob; target was text) but who knows.. it
                        // might be useful in the future!
                        
                        Blob blob = srcRS.getBlob(sourceColIndex);
                        String text = new String(blob.getBytes(1, (int) blob.length()), blobTextEncoding);
                        dstStmt.setString(col+1, text);
                        
                        // TODO we should handle the more usual case of BLOB-BLOB mapping
                        
                    } else {
                        
                        // generic case.. hope the target database will do the right thing
                        dstStmt.setObject(col+1, srcRS.getObject(sourceColIndex), srcRSMD.getColumnType(sourceColIndex));
                    }
                }
                dstStmt.executeUpdate();
                numRows++;
            }
            
            dstCon.commit();
            logger.info("Committed transaction");
            
        } catch (SQLException e) {
            dstCon.rollback();
            throw e;
        } finally {
            if (srcRS != null) srcRS.close();
            if (srcStmt != null) srcStmt.close();
            if (dstStmt != null) dstStmt.close();
            if (srcCon != null) srcCon.close();
            if (dstCon != null) dstCon.close();
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime-startTime;
        logger.info(numRows+" rows copied in "+elapsedTime+" ms. ("+((double) numRows)/((double) elapsedTime)*1000.0+" rows/sec)");
        return numRows;
    }
    
    /**
     * Just for debugging.
     */
    private String summarizeResultSetMetaData(ResultSetMetaData rsmd) throws SQLException {
        StringBuffer summary = new StringBuffer(200);
        int numberOfColumns = rsmd.getColumnCount();
        summary.append("Table ").append(rsmd.getTableName(1)).append("\n");
        for (int col = 1; col <= numberOfColumns; col++) {
            summary.append("Column ").append(col).append(": ");
            summary.append(rsmd.getColumnName(col));
            summary.append(" JDBC datatype ").append(rsmd.getColumnType(col));
            summary.append(" (").append(rsmd.getColumnClassName(col)).append(")");
            summary.append(rsmd.isNullable(col)==ResultSetMetaData.columnNullable?"":" NOT NULL");
            summary.append("\n");
        }
        return summary.toString();
    }

    /**
     * Generates a string that you can pass to
     * Connection.prepareStatement() for inserting data into every
     * column of destTable in the order that its SQLColumn children
     * exist.
     */
    private String generateInsertStatement(SQLTable destTable)
        throws SQLException, ArchitectException {
        StringBuffer sql = new StringBuffer(200);
        sql.append("INSERT INTO ").append(DDLUtils.toQualifiedName(destTable)).append(" (");
        boolean first = true;
        for (SQLColumn col : destTable.getColumns()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(col.getName());
            first = false;
        }
        sql.append(") VALUES (");
        for (int i = 0; i < destTable.getColumns().size(); i++) {
            if (i != 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }

    /**
     * Copies all the tables in the sourceTables collection which also exist in the destTableContainer.
     * This is done in a safe order based on the foreign key constraints that exist in the destination
     * database.
     * 
     * @param destTableContainer The SQLDatabase, SQLSchema, or SQLCatalog that directly contains
     * the destination tables.
     * @param sourceTables The list of tables to copy (the order is unimportant)
     * @throws SQLException 
     * @throws UnsupportedEncodingException 
     */
    public void copyTables(SQLObject destTableContainer, Collection<SQLTable> sourceTables) throws ArchitectException, SQLException, UnsupportedEncodingException {
        
        // list of tables in target db that we will load
        Set<SQLTable> destTables = new HashSet<SQLTable>();
        
        // mapping of destination table name to source table instance
        Map<String, SQLTable> sourceTableMap = new HashMap<String, SQLTable>();
        
        for (SQLTable sourceTable : sourceTables) {
            SQLTable destTable = (SQLTable) destTableContainer.getChildByNameIgnoreCase(sourceTable.getName());
            if (destTable == null) {
                logger.warn("Skipping source table "+sourceTable+" because there is no corresponding target table");
            } else {
                destTables.add(destTable);
                sourceTableMap.put(destTable.getName(), sourceTable);
            }
        }
        
        List<SQLTable> allDestTables = destTableContainer.getChildren();
        DepthFirstSearch dfs = new DepthFirstSearch(allDestTables);
        allDestTables = dfs.getFinishOrder();
        logger.debug("Safe load order is: "+allDestTables);
        
        if (truncatingDestinationTable) {
            Connection destCon = ArchitectUtils.getAncestor(destTableContainer, SQLDatabase.class).getConnection();
            Statement destStmt = destCon.createStatement();
            List<SQLTable> backwardDestList = new ArrayList<SQLTable>(allDestTables);
            backwardDestList.retainAll(destTables);
            Collections.reverse(backwardDestList);
            for (SQLTable t : backwardDestList) {
                final String destTableName = DDLUtils.toQualifiedName(t);
                logger.debug("About to delete from "+destTableName);
                destStmt.executeUpdate(lastSqlString = "DELETE FROM "+destTableName);
            }
            destCon.close();
        }
        
        for (SQLTable destTable : allDestTables) {
            if (destTables.contains(destTable)) {
                SQLTable sourceTable = (SQLTable) sourceTableMap.get(destTable.getName());
                copyTable(destTable, sourceTable);
            }
        }
    }
    
    
    
    
	public String getBlobTextEncoding() {
        return blobTextEncoding;
    }

    public void setBlobTextEncoding(String blobTextEncoding) {
        this.blobTextEncoding = blobTextEncoding;
    }

    public boolean isTruncatingDestinationTable() {
        return truncatingDestinationTable;
    }

    public void setTruncatingDestinationTable(boolean truncatingDestinationTable) {
        this.truncatingDestinationTable = truncatingDestinationTable;
    }

    public String getLastSqlString() {
        return lastSqlString;
    }

    /**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        
	    DataMover dm = new DataMover();
	    DataSourceCollection dataSources = null;
        String sourceConnectionName = null;
        String destConnectionName = null;
        String sourceCat = null;
        String destCat = null;
        String sourceSchema = null;
        String destSchema = null;
        boolean updatingSequences = false;
        
        // process arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("--")) {
                Pattern p = Pattern.compile("--(.*)=(.*)");
                Matcher m = p.matcher(arg);
                if (!m.matches()) {
                    System.out.println("Malformed parameter: "+arg);
                    usage();
                    return;
                }
                String argName = m.group(1);
                String argVal = m.group(2);
                
                if (argName.equals("pl-ini")) {
                    dataSources = new PlDotIni();
                    dataSources.read(new File(argVal));
                } else if (argName.equals("blob-encoding")) {
                    dm.setBlobTextEncoding(argVal);
                } else if (argName.equals("truncate-dest")) {
                    dm.setTruncatingDestinationTable(argVal.equalsIgnoreCase("true"));
                } else if (argName.equals("update-seq")) {
                    updatingSequences = argVal.equalsIgnoreCase("true");
                } else if (argName.equals("source-db")) {
                    sourceConnectionName = argVal;
                } else if (argName.equals("target-db")) {
                    destConnectionName = argVal;
                } else if (argName.equals("source-cat")) {
                    sourceCat = argVal;
                } else if (argName.equals("target-cat")) {
                    destCat = argVal;
                } else if (argName.equals("source-schema")) {
                    sourceSchema = argVal;
                } else if (argName.equals("target-schema")) {
                    destSchema = argVal;
                } else {
                    System.out.println("Unknown argument "+argName);
                    usage();
                    return;
                }
            }
        }
        
        if (dataSources == null) {
            System.out.println("The pl-ini argument is required.");
            return;
        }

        if (sourceConnectionName == null) {
            System.out.println("The source-db argument is required.");
            return;
        }
        final ArchitectDataSource sourceConnectionSpec = dataSources.getDataSource(sourceConnectionName);
        if (sourceConnectionSpec == null) {
            System.out.println("Couldn't find connection \""+sourceConnectionName+"\" in the pl.ini. Available connections:");
            System.out.println(dataSources.getConnections());
            return;
        }
        SQLDatabase source = new SQLDatabase(sourceConnectionSpec);

        if (destConnectionName == null) {
            System.out.println("The target-db argument is required.");
            return;
        }
        final ArchitectDataSource destConnectionSpec = dataSources.getDataSource(destConnectionName);
        if (destConnectionSpec == null) {
            System.out.println("Couldn't find connection \""+destConnectionName+"\" in the pl.ini. Available connections:");
            System.out.println(dataSources.getConnections());
            return;
        }
        SQLDatabase dest = new SQLDatabase(destConnectionSpec);

        SQLObject sourceTableContainer = ArchitectUtils.getTableContainer(source, sourceCat, sourceSchema);
        if (sourceTableContainer == null) {
            System.out.println("Couldn't find database location. db="+source.getName()+" catalog="+sourceCat+" schema="+sourceSchema);
            return;
        }
        List<SQLTable> sourceTables = sourceTableContainer.getChildren();
        System.out.println("Found source tables: "+sourceTables);
        
        SQLObject destTableContainer = ArchitectUtils.getTableContainer(dest, destCat, destSchema);
        
        dm.copyTables(destTableContainer, sourceTables);
        
        if (updatingSequences) {
            final Connection srcCon = source.getConnection();
            final Connection dstCon = dest.getConnection();
            updateSequences(srcCon, dstCon);
            srcCon.close();
            dstCon.close();
        }
	}
    
    private static void usage() {
        System.out.println("DataMover: A utility for synchronizing the data between databases");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  datamover [args] --source-db=dbName --source-cat=catalog --source-schema=schema --target-db=dbName --target-cat=catalog --target-schema=schema");
        System.out.println("");
        System.out.println("  sourceDbName and targetDbName are pl.ini logical connection names");
        System.out.println("");
        System.out.println("Available Arguments:");
        System.out.println("  --pl-ini=path       Path (relative or absolute) to the PL.INI file to use");
        System.out.println("  --blob-encoding=enc Character encoding to assume for BLOBs that contain text");
        System.out.println("  --truncate-dest=t/f truncate destination table before loading? (true/false)");
        System.out.println("  --update-seq=t/f    update current value of sequences in target database (true/false)");
    }
	
    /**
     * Reads the current value of all the sequences owned by the current user in the source database
     * (must be Oracle) and sets the current values of the correspondingly-named
     * sequences in the destination database (must be postgresql).
     * <p>
     * Obviously this method is useless in most cases, but it's a good starting
     * point for a generic method that does the same thing.
     */
	private static void updateSequences(Connection srcCon, Connection dstCon) throws SQLException {
		Statement srcStmt = srcCon.createStatement();
		Statement dstStmt = dstCon.createStatement();
		
		ResultSet rs = srcStmt.executeQuery(
				"SELECT sequence_name, last_number " +
				"FROM user_sequences");
		
		while(rs.next()) {
			String sequenceName = rs.getString(1);
			int lastNumber = rs.getInt(2);
			
            String sql = "ALTER SEQUENCE "+sequenceName+" RESTART WITH "+lastNumber;
            logger.debug(sql);
            try {
                dstStmt.executeUpdate(sql);
            } catch (SQLException ex) {
                logger.warn("Couldn't execute sequence update: "+sql, ex);
            }
		}
		
		dstStmt.close();
		rs.close();
		srcStmt.close();
	}
}
