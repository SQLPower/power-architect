package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.Monitorable;

public class ProfileManager implements Monitorable {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);

    private Map<SQLObject, ProfileResult> results = new HashMap<SQLObject, ProfileResult>();

    private boolean findingMin;

    private boolean findingMax;

    private boolean findingAvg;

    private boolean findingMinLength;

    private boolean findingMaxLength;

    private boolean findingAvgLength;

    private boolean findingDistinctCount;

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
            sql.append("SELECT COUNT(*)");
            for (SQLColumn col : table.getColumns()) {
                if (findingDistinctCount) {
                    sql.append(",\n COUNT(DISTINCT ").append(col.getName()).append(")");
                }
                if (findingMin) {
                    sql.append(",\n MIN(").append(col.getName()).append(")");
                }
                if (findingMax) {
                    sql.append(",\n MAX(").append(col.getName()).append(")");
                }
                if (findingAvg) {
                    sql.append(",\n AVG(").append(col.getName()).append(")");
                }
                if (findingMinLength) {
                    sql.append(",\n MIN(LENGTH(").append(col.getName()).append("))");
                }
                if (findingMaxLength) {
                    sql.append(",\n MAX(LENGTH(").append(col.getName()).append("))");
                }
                if (findingAvgLength) {
                    sql.append(",\n AVG(LENGTH(").append(col.getName()).append("))");
                }
            }
            sql.append("\nFROM ").append(table.getName());
            stmt = conn.createStatement();
            lastSQL = sql.toString();
            
            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery(lastSQL);
            long endTime = System.currentTimeMillis();
            
            if ( rs.next() ) {
                int rscol = 1;
                TableProfileResult tableProfileResult = new TableProfileResult(endTime-startTime,rs.getInt(rscol++));
                putResult(table, tableProfileResult);
                for (SQLColumn col : table.getColumns()) {
                    ColumnProfileResult colResult = new ColumnProfileResult(endTime-startTime);
                    if (findingDistinctCount) {
                        colResult.setDistinctValueCount(rs.getInt(rscol++));
                    }
                    if (findingMin) {
                        colResult.setMinValue(rs.getObject(rscol++));
                    }
                    if (findingMax) {
                        colResult.setMaxValue(rs.getObject(rscol++));
                    }
                    if (findingAvg) {
                        colResult.setAvgValue(rs.getObject(rscol++));
                    }
                    if (findingMinLength) {
                        colResult.setMinLength(rs.getInt(rscol++));
                    }
                    if (findingMaxLength) {
                        colResult.setMaxLength(rs.getInt(rscol++));
                    }
                    if (findingAvgLength) {
                        colResult.setAvgLength(rs.getInt(rscol++));
                    }
                    putResult(col, colResult);
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
}
