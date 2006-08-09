package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.Monitorable;

public class ProfileManager implements Monitorable {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);

    private Map<SQLObject, ProfileResult> results = new HashMap<SQLObject, ProfileResult>();

    private boolean findingMin = true;

    private boolean findingMax = true;

    private boolean findingAvg = true;

    private boolean findingMinLength = true;

    private boolean findingMaxLength = true;

    private boolean findingAvgLength = true;

    private boolean findingDistinctCount = true;

    private boolean findingNullCount = true;
    
    private boolean findingTopTen = true;
    
    private Integer jobSize;

    /**
     * This object is the mutex for controlling access to the fields
     * that implement the Monitorable interface.
     */
    private Object monitorableMutex = new Object();

    private int progress;

    private boolean finished;

    private String currentProfilingTable;

    private boolean userCancel;

    public void putResult(SQLObject sqlObject, ProfileResult profileResult) {
        results.put(sqlObject, profileResult);
    }

    public ProfileResult getResult(SQLObject sqlObject) {
        return results.get(sqlObject);
    }
    
    /**
     * Creates a new profile object for the given SQL Object.
     * 
     * @param obj The database object you want to profile.
     * @throws ArchitectException 
     * @throws SQLException 
     */
    public synchronized void createProfiles(Collection<SQLTable> tables) throws SQLException, ArchitectException {
        synchronized (monitorableMutex) {
            int objCount = 0;
            for (SQLTable t : tables) {
                objCount += 1;
                objCount += 2*t.getColumns().size();
            }
            jobSize = new Integer(objCount);
            finished = false;
            progress = 0;
            userCancel = false;
        }
        try {
            for (SQLTable t : tables) {
                synchronized (monitorableMutex) {
                    currentProfilingTable = t.getName();
                    if (userCancel) break;
                }
                doTableProfile(t);
                synchronized (monitorableMutex) {
                    progress++;
                    if (userCancel) break;
                }
            }
        } finally {
            synchronized (monitorableMutex) {
                finished = true;
                jobSize = null;
            }
        }
    }

    private void doTableProfile(SQLTable table) throws SQLException, ArchitectException {
        SQLDatabase db = table.getParentDatabase();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        
        TableProfileResult tableResult = new TableProfileResult(System.currentTimeMillis());
        
        try {

            conn = db.getConnection();
            String databaseIdentifierQuoteString = null;
            
        
/*            DatabaseMetaData dbmd = conn.getMetaData();
            rs = dbmd.getTypeInfo();
            while (rs.next()) {
                System.out.println("name="+rs.getString(1)+"  type:"+rs.getInt(2));
            }
            rs.close();*/

            databaseIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
                
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT COUNT(*) AS ROW__COUNT");
            sql.append("\nFROM ");
            sql.append(DDLUtils.toQualifiedName(table.getCatalogName(),
                                                table.getSchemaName(),
                                                table.getName(),
                                                databaseIdentifierQuoteString,
                                                databaseIdentifierQuoteString));
            stmt = conn.createStatement();
            stmt.setEscapeProcessing(false);
            lastSQL = sql.toString();

            rs = stmt.executeQuery(lastSQL);
            
            if ( rs.next() ) {
                tableResult.setRowCount(rs.getInt("ROW__COUNT"));
            }
                       
            rs.close();
            rs = null;
            
            doColumnProfile(table.getColumns(), conn);
            
            // XXX: add where filter later
        } catch (SQLException ex) {
            logger.error("Error in SQL query: "+lastSQL, ex);
            tableResult.setError(true);
            tableResult.setEx(ex);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up result set", ex);
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up statement", ex);
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up connection", ex);
            }
            tableResult.setCreateEndTime(System.currentTimeMillis());
            putResult(table, tableResult);
        }
    }


    private void doColumnProfile(List<SQLColumn> columns, Connection conn) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        String databaseIdentifierQuoteString = null;
        
        try {
            if ( columns.size() == 0 )
                return;
            
            SQLColumn col1 = columns.get(0);

            DDLGenerator ddlg = null;

            try {
                ddlg = (DDLGenerator) DDLUtils.createDDLGenerator(
                        col1.getParentTable().getParentDatabase().getDataSource());
                databaseIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
            } catch (InstantiationException e1) {
                logger.error("problem running Profile Manager", e1);
            } catch ( IllegalAccessException e1 ) {
                logger.error("problem running Profile Manager", e1);
            }

            stmt = conn.createStatement();
            stmt.setEscapeProcessing(false);
            
            int i = 0;
            for (SQLColumn col : columns ) {
                
                synchronized (monitorableMutex) {
                    if (userCancel) return;
                }
                ProfileFunctionDescriptor pfd = ddlg.getProfileFunctionMap().get(col.getSourceDataTypeName());
                ColumnProfileResult colResult = new ColumnProfileResult(System.currentTimeMillis());

                if ( pfd == null ) {
                    System.out.println(col.getName()+
                            " Unknown DataType:(" +
                            col.getSourceDataTypeName() + 
                            "). please setup the profile function mapping");
                    continue;
                }
                
                StringBuffer sql = new StringBuffer();
                
                sql.append("SELECT 1");

                if (findingDistinctCount && pfd.isCountDist() ) {
                    sql.append(",\n COUNT(DISTINCT \"");
                    sql.append(col.getName());
                    sql.append("\") AS DISTINCTCOUNT_"+i);
                }
                if (findingMin && pfd.isMinValue() ) {
                    sql.append(",\n MIN(\"");
                    sql.append(col.getName());
                    sql.append("\") AS MINVALUE_"+i);
                }
                if (findingMax && pfd.isMaxValus() ) {
                    sql.append(",\n MAX(\"");
                    sql.append(col.getName());
                    sql.append("\") AS MAXVALUE_"+i);
                }
                if (findingAvg && pfd.isAvgValue() ) {
                    sql.append(",\n ");
                    sql.append(ddlg.getAverageSQLFunctionName("\""+col.getName()+"\""));
                    sql.append(" AS AVGVALUE_"+i);
                }
                if (findingMinLength && pfd.isMinLength() ) {
                    sql.append(",\n MIN(");
                    sql.append(ddlg.getStringLengthSQLFunctionName("\""+col.getName()+"\""));
                    sql.append(") AS MINLENGTH_"+i);
                }
                if (findingMaxLength && pfd.isMaxLength() ) {
                    sql.append(",\n MAX(");
                    sql.append(ddlg.getStringLengthSQLFunctionName("\""+col.getName()+"\""));
                    sql.append(") AS MAXLENGTH_"+i);
                }
                if (findingAvgLength && pfd.isAvgLength() ) {
                    sql.append(",\n AVG(");
                    sql.append(ddlg.getStringLengthSQLFunctionName("\""+col.getName()+"\""));
                    sql.append(") AS AVGLENGTH_"+i);
                }
                
                if ( findingNullCount && pfd.isSumDecode() ) {
                    sql.append(",\n SUM(");
                    sql.append(ddlg.caseWhen("\""+col.getName()+"\"", "NULL", "1"));
                    sql.append(") AS NULLCOUNT_"+i);
                }
                SQLTable table = col.getParentTable();
                sql.append("\n FROM ");
                sql.append(DDLUtils.toQualifiedName(table.getCatalogName(),
                                                    table.getSchemaName(),
                                                    table.getName(),
                                                    databaseIdentifierQuoteString,
                                                    databaseIdentifierQuoteString));

                try {
                    
                    lastSQL = sql.toString();
                    rs = stmt.executeQuery(lastSQL);

                    if ( rs.next() ) {
                        if (findingDistinctCount && pfd.isCountDist() ) {
                            lastSQL = "DISTINCTCOUNT_"+i;
                            colResult.setDistinctValueCount(rs.getInt(lastSQL));
                        }
                        if (findingMin && pfd.isMinValue() ) {
                            lastSQL = "MINVALUE_"+i;
                            colResult.setMinValue(rs.getObject(lastSQL));
                        }
                        if (findingMax && pfd.isMaxValus() ) {
                            lastSQL = "MAXVALUE_"+i;
                            colResult.setMaxValue(rs.getObject(lastSQL));
                        }
                        if (findingAvg && pfd.isAvgValue() ) {
                            lastSQL = "AVGVALUE_"+i;
                            colResult.setAvgValue(rs.getObject(lastSQL));
                        }
                        if (findingMinLength && pfd.isMinLength() ) {
                            lastSQL = "MINLENGTH_"+i;
                            colResult.setMinLength(rs.getInt(lastSQL));
                        }
                        if (findingMaxLength && pfd.isMaxLength() ) {
                            lastSQL = "MAXLENGTH_"+i;
                            colResult.setMaxLength(rs.getInt(lastSQL));
                        }
                        if (findingAvgLength && pfd.isAvgLength() ) {
                            lastSQL = "AVGLENGTH_"+i;
                            colResult.setAvgLength(rs.getInt(lastSQL));
                        }
                        
                        if ( findingNullCount && pfd.isSumDecode() ) {
                            lastSQL = "NULLCOUNT_"+i;
                            colResult.setNullCount(rs.getInt(lastSQL));
                        }
                    }
                        
                } catch ( SQLException ex ) {
                    colResult.setError(true);
                    colResult.setEx(ex);
                    logger.error("Error in Column Profiling: "+lastSQL, ex);
                } finally {
                    colResult.setCreateEndTime(System.currentTimeMillis());
                    putResult(col, colResult);
                    
                    try {
                        if (rs != null) rs.close();
                    } catch (SQLException ex) {
                        logger.error("Couldn't clean up result set", ex);
                    }
                    rs = null;
                }
                i++;
                
                synchronized (monitorableMutex) {
                    progress++;
                    if (userCancel) break;
                }

                
                if (findingTopTen && pfd.isCountDist() ) {
                    sql = new StringBuffer();
                    sql.append("SELECT \"");
                    sql.append(col.getName());
                    sql.append("\" AS MYVALUE, COUNT(*) AS COUNT1 FROM ");
                    sql.append(DDLUtils.toQualifiedName(table.getCatalogName(),
                                                        table.getSchemaName(),
                                                        table.getName(),
                                                        databaseIdentifierQuoteString,
                                                        databaseIdentifierQuoteString));
                    sql.append(" GROUP BY \"");
                    sql.append(col.getName());
                    sql.append("\" ORDER BY COUNT1 DESC");

                    colResult = (ColumnProfileResult) getResult(col);
                    try {
                        lastSQL = sql.toString();
                        rs = stmt.executeQuery(lastSQL);
                        for ( int n=0; rs.next() && n < 10; n++ ) {
                            colResult.addValueCount(rs.getObject("MYVALUE"), rs.getInt("COUNT1"));
                        }
                        
                    } catch ( SQLException ex ) {
                        colResult.setError(true);
                        colResult.setEx(ex);
                        logger.error("Error in Column Profiling: "+lastSQL, ex);
                    } finally {
                        colResult.setCreateEndTime(System.currentTimeMillis());
                        putResult(col, colResult);
                        
                        try {
                            if (rs != null) rs.close();
                        } catch (SQLException ex) {
                            logger.error("Couldn't clean up result set", ex);
                        }
                        rs = null;
                    }
                }

                synchronized (monitorableMutex) {
                    progress++;
                    if (userCancel) break;
                }

                
            }      
            // XXX: add where filter later
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up result set", ex);
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up statement", ex);
            }
        }
    }            
    
    // =========== Monitorable Interface =============
    public int getProgress() throws ArchitectException {
        synchronized (monitorableMutex ) {
            return progress;
        }
    }

    public Integer getJobSize() throws ArchitectException {
        synchronized (monitorableMutex) {
            return jobSize;
        }
    }

    public boolean hasStarted() throws ArchitectException {
        synchronized (monitorableMutex) {
            return jobSize != null;
        }
    }

    public boolean isFinished() throws ArchitectException {
        synchronized (monitorableMutex) {
            return finished;
        }
    }

    public String getMessage() {
        synchronized (monitorableMutex) {
            return currentProfilingTable;
        }
    }

    public void setCancelled(boolean cancelled) {
        synchronized (monitorableMutex) {
            userCancel = true;
        }
    }

    public boolean isFindingAvg() {
        return findingAvg;
    }

    public void setFindingAvg(boolean findingAvg) {
        this.findingAvg = findingAvg;
    }

    public boolean isFindingAvgLength() {
        return findingAvgLength;
    }

    public void setFindingAvgLength(boolean findingAvgLength) {
        this.findingAvgLength = findingAvgLength;
    }

    public boolean isFindingDistinctCount() {
        return findingDistinctCount;
    }

    public void setFindingDistinctCount(boolean findingDistinctCount) {
        this.findingDistinctCount = findingDistinctCount;
    }

    public boolean isFindingMax() {
        return findingMax;
    }

    public void setFindingMax(boolean findingMax) {
        this.findingMax = findingMax;
    }

    public boolean isFindingMaxLength() {
        return findingMaxLength;
    }

    public void setFindingMaxLength(boolean findingMaxLength) {
        this.findingMaxLength = findingMaxLength;
    }

    public boolean isFindingMin() {
        return findingMin;
    }

    public void setFindingMin(boolean findingMin) {
        this.findingMin = findingMin;
    }

    public boolean isFindingMinLength() {
        return findingMinLength;
    }

    public void setFindingMinLength(boolean findingMinLength) {
        this.findingMinLength = findingMinLength;
    }

    public boolean isFindingNullCount() {
        return findingNullCount;
    }

    public void setFindingNullCount(boolean findingNullCount) {
        this.findingNullCount = findingNullCount;
    }
}
