package ca.sqlpower.architect.profile;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
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
            jobSize = new Integer(tables.size());
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
        try {
            conn = db.getConnection();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT COUNT(*) \"ROWCOUNT\"");

            int i = 0;
            for (SQLColumn col : table.getColumns()) {

                if (findingDistinctCount &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n COUNT(DISTINCT ").append(col.getName()).append(") \"DISTINCTCOUNT_"+i+"\"");
                }
                if (findingMin &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.VARBINARY &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n MIN(").append(col.getName()).append(") \"MINVALUE_"+i+"\"");
                }
                if (findingMax &&
                    col.getType() != Types.LONGVARCHAR &&                        
                    col.getType() != Types.VARBINARY &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n MAX(").append(col.getName()).append(") \"MAXVALUE_"+i+"\"");
                }
                if (findingAvg &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.LONGVARBINARY &&
                    col.getType() != Types.VARBINARY &&
                    col.getType() != Types.TIMESTAMP &&
                    col.getType() != Types.CHAR &&
                    col.getType() != Types.VARCHAR ) {
                    sql.append(",\n AVG(").append(col.getName()).append(") \"AVGVALUE_"+i+"\"");
                }
                if (findingMinLength &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n MIN(LENGTH(").append(col.getName()).append(")) \"MINLENGTH_"+i+"\"");
                }
                if (findingMaxLength &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n MAX(LENGTH(").append(col.getName()).append(")) \"MAXLENGTH_"+i+"\"");
                }
                if (findingAvgLength &&
                    col.getType() != Types.LONGVARCHAR &&
                    col.getType() != Types.LONGVARBINARY ) {
                    sql.append(",\n AVG(LENGTH(").append(col.getName()).append(")) \"AVGLENGTH_"+i+"\"");
                }
                
                if ( findingNullCount &&
                    col.getType() != Types.LONGVARBINARY &&
                    col.getType() != Types.LONGVARCHAR ) {
                    
                    if ( db.getDataSource().getDriverClass().equals("oracle.jdbc.driver.OracleDriver") ) {
                        sql.append(",\n SUM(DECODE(").append(col.getName()).append(",NULL,1)) \"NULLCOUNT_"+i+"\"");
                    }
                    else if ( db.getDataSource().getDriverClass().equals("com.microsoft.jdbc.sqlserver.SQLServerDriver") ) {
                        sql.append(",\n SUM(CASE WHEN ").append(col.getName()).append(" IS NULL THEN 1 ELSE 0 END) \"NULLCOUNT_"+i+"\"");
                    }
                    else if ( db.getDataSource().getDriverClass().equals("org.postgresql.Driver") ) {
                        sql.append(",\n SUM(CASE WHEN ").append(col.getName()).append(" IS NULL THEN 1 ELSE 0 END) \"NULLCOUNT_"+i+"\"");
                    }
                    else if ( db.getDataSource().getDriverClass().equals("ibm.sql.DB2Driver") ) {
                        sql.append(",\n SUM(CASE WHEN ").append(col.getName()).append(" IS NULL THEN 1 ELSE 0 END) \"NULLCOUNT_"+i+"\"");
                    }
                }
                

                
                i++;
            }
            sql.append("\nFROM ").append(DDLUtils.toQualifiedName(table.getCatalogName(),table.getSchemaName(),table.getName()));
            stmt = conn.createStatement();
            lastSQL = sql.toString();
    
            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery(lastSQL);
            long endTime = System.currentTimeMillis();
            
            if ( rs.next() ) {

                TableProfileResult tableProfileResult = new TableProfileResult(endTime-startTime,rs.getInt("ROWCOUNT"));
                putResult(table, tableProfileResult);
                i = 0;
                for (SQLColumn col : table.getColumns()) {
                    ColumnProfileResult colResult = new ColumnProfileResult(endTime-startTime);

                    if (findingDistinctCount &&
                        col.getType() != Types.LONGVARCHAR &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "DISTINCTCOUNT_"+i;
                        colResult.setDistinctValueCount(rs.getInt(lastSQL));
                    }
                    if (findingMin &&
                        col.getType() != Types.LONGVARCHAR &&
                        col.getType() != Types.VARBINARY &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "MINVALUE_"+i;
                        colResult.setMinValue(rs.getObject(lastSQL));
                    }
                    if (findingMax &&
                        col.getType() != Types.LONGVARCHAR &&                        
                        col.getType() != Types.VARBINARY &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "MAXVALUE_"+i;
                        colResult.setMaxValue(rs.getObject(lastSQL));
                    }
                    if (findingAvg &&
                            col.getType() != Types.LONGVARCHAR &&
                            col.getType() != Types.LONGVARBINARY &&
                            col.getType() != Types.VARBINARY &&
                            col.getType() != Types.TIMESTAMP &&
                            col.getType() != Types.CHAR &&
                            col.getType() != Types.VARCHAR ) {
                        lastSQL = "AVGVALUE_"+i;
                        colResult.setAvgValue(rs.getObject(lastSQL));
                    }
                    if (findingMinLength &&
                        col.getType() != Types.LONGVARCHAR &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "MINLENGTH_"+i;
                        colResult.setMinLength(rs.getInt(lastSQL));
                    }
                    if (findingMaxLength &&
                        col.getType() != Types.LONGVARCHAR &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "MAXLENGTH_"+i;
                        colResult.setMaxLength(rs.getInt(lastSQL));
                    }
                    if (findingAvgLength &&
                        col.getType() != Types.LONGVARCHAR &&
                        col.getType() != Types.LONGVARBINARY ) {
                        lastSQL = "AVGLENGTH_"+i;
                        colResult.setAvgLength(rs.getInt(lastSQL));
                    }
                    
                    if ( findingNullCount &&
                         col.getType() != Types.LONGVARBINARY &&
                         col.getType() != Types.LONGVARCHAR ) {
                        try {
                            lastSQL = "NULLCOUNT_"+i;
                            colResult.setNullCount(rs.getInt(lastSQL));
                        } catch ( SQLException ex1 ) {
                            
                        }
                    }
                    
                    putResult(col, colResult);
                    i++;
                }
            }
            
            rs.close();
            rs = null;
            
            // XXX: add where filter later
        } catch (SQLException ex) {
            logger.error("Error in SQL query: "+lastSQL, ex);
            throw ex;
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
