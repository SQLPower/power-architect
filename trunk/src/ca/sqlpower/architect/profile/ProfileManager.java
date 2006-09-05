package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.Monitorable;

public class ProfileManager implements Monitorable {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);

    private final Map<SQLObject, ProfileResult> results = new HashMap<SQLObject, ProfileResult>();

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

    private int topNCount = 10;

    public ProfileManager() {

    }

    private void putResult(ProfileResult profileResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("[instance "+hashCode()+"]" +
                    " Adding new profile result for "+profileResult.getProfiledObject().getName()+
                    " existing profile count: "+results.size());
        }
        results.put(profileResult.getProfiledObject(), profileResult);
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
    public synchronized void createProfiles(Collection<SQLTable> tables,
                        JLabel workingOn) throws SQLException, ArchitectException {
        synchronized (monitorableMutex) {
            int objCount = 0;
            for (SQLTable t : tables) {
                objCount += 1;
                objCount += t.getColumns().size();
            }
            jobSize = new Integer(objCount);
            finished = false;
            progress = 0;
            userCancel = false;
            logger.debug("Job Size:"+jobSize+"    progress="+progress);
        }
        try {
            for (SQLTable t : tables) {
                synchronized (monitorableMutex) {
                    currentProfilingTable = t.getName();
                    if ( workingOn != null )
                        workingOn.setText("Profiling: "+currentProfilingTable);
                    if (userCancel) break;
                }
                doTableProfile(t);
                synchronized (monitorableMutex) {
                    progress++;
                    if (userCancel) break;
                    logger.debug("Job Size:"+jobSize+"    progress="+progress);
                }
            }
        } finally {
            synchronized (monitorableMutex) {
                finished = true;
                jobSize = null;
            }
            fireProfileAddedEvent(new ProfileChangeEvent(this, null));
        }
    }

    public synchronized void createProfiles(Collection<SQLTable> tables ) throws SQLException, ArchitectException {
        createProfiles(tables,null);
    }
    private void doTableProfile(SQLTable table) throws SQLException, ArchitectException {
        SQLDatabase db = table.getParentDatabase();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;

        TableProfileResult tableResult = new TableProfileResult(table);
        tableResult.setCreateStartTime(System.currentTimeMillis());

        try {

            conn = db.getConnection();
            String databaseIdentifierQuoteString = null;

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

            // XXX: add where filter later
        } catch (SQLException ex) {
            logger.error("Error in SQL query: "+lastSQL, ex);
            tableResult.setError(true);
            tableResult.setException(ex);
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

            tableResult.setCreateEndTime(System.currentTimeMillis());
            putResult(tableResult);
            doColumnProfile(table.getColumns(), conn);

            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up connection", ex);
            }
        }
    }


    private void doColumnProfile(List<SQLColumn> columns, Connection conn)
                    throws SQLException, ArchitectException {

        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            if ( columns.size() == 0 )
                return;

            SQLColumn col1 = columns.get(0);

            DDLGenerator ddlg = null;

            try {
                ddlg = (DDLGenerator) DDLUtils.createDDLGenerator(
                        col1.getParentTable().getParentDatabase().getDataSource());
            } catch (InstantiationException e1) {
                throw new ArchitectException("problem running Profile Manager", e1);
            } catch ( IllegalAccessException e1 ) {
                throw new ArchitectException("problem running Profile Manager", e1);
            }

            stmt = conn.createStatement();
            stmt.setEscapeProcessing(false);

            for (SQLColumn col : columns ) {

                synchronized (monitorableMutex) {
                    if (userCancel) {
                        remove(col.getParentTable());
                        return;
                    }
                }
                ProfileFunctionDescriptor pfd = ddlg.getProfileFunctionMap().get(col.getSourceDataTypeName());
                ColumnProfileResult colResult = null;
                long profileStartTime = System.currentTimeMillis();

                if ( pfd == null ) {
                    logger.debug(col.getName()+ " Unknown DataType:(" +
                            col.getSourceDataTypeName() + ").");
                    pfd = discoverProfileFunctionDescriptor(col,ddlg,conn);
                }

                try {
                    colResult = execProfileFunction(pfd,col,ddlg,conn);
                } catch ( Exception ex ) {
                    colResult = new ColumnProfileResult(col);
                    colResult.setCreateStartTime(profileStartTime);
                    colResult.setError(true);
                    colResult.setException(ex);
                    colResult.setCreateEndTime(System.currentTimeMillis());
                    logger.error("Error in Column Profiling: "+lastSQL, ex);
                } finally {
                    putResult(colResult);
                }

                synchronized (monitorableMutex) {
                    progress++;
                    if (userCancel) {
                        remove(col.getParentTable());
                        break;
                    }
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

    /**
     * Discovers which profiling functions can be applied to the given column
     * by trial and error.  This could be extremely time-consuming.
     * @param col The column to figureout how to profile
     * @param ddlg The DDL Generator for col's database
     * @param conn A connection to col's database
     * @return A ProfileFunctionDescriptor that is properly configured for the data
     * type of col.
     */
    private ProfileFunctionDescriptor discoverProfileFunctionDescriptor(SQLColumn col, DDLGenerator ddlg, Connection conn) {
        ProfileFunctionDescriptor pfd = new ProfileFunctionDescriptor(col.getSourceDataTypeName(),
                col.getType(),false,false,false,false,false,false,false,false);

        logger.debug("Discovering profile functions for column "+col);

        try {
            pfd.setCountDist(true);
            execProfileFunction(pfd, col, ddlg, conn);
            logger.debug("countDist worked");
        } catch (Exception e) {
            logger.debug("countDist failed", e);
            pfd.setCountDist(false);
        }

        try {
            pfd.setMaxValue(true);
            pfd.setMinValue(true);
            execProfileFunction(pfd, col, ddlg, conn);
            logger.debug("min/max worked");
        } catch (Exception e) {
            logger.debug("min/max failed", e);
            pfd.setMaxValue(false);
            pfd.setMinValue(false);
        }

        try {
            pfd.setAvgValue(true);
            execProfileFunction(pfd, col, ddlg, conn);
            logger.debug("avg worked");
        } catch (Exception e) {
            logger.debug("avg failed", e);
            pfd.setAvgValue(false);
        }

        try {
            pfd.setMaxLength(true);
            pfd.setMinLength(true);
            pfd.setAvgLength(true);
            execProfileFunction(pfd, col, ddlg, conn);
            logger.debug("min/max/avg length worked");
        } catch (Exception e) {
            logger.debug("min/max/avg length failed", e);
            pfd.setMaxLength(false);
            pfd.setMinLength(false);
            pfd.setAvgLength(false);
        }

        try {
            pfd.setSumDecode(true);
            execProfileFunction(pfd, col, ddlg, conn);
            logger.debug("sumDecode worked");
        } catch (Exception e) {
            logger.debug("sumDecode failed", e);
            pfd.setSumDecode(false);
        }


        return pfd;
    }

    public void clear(){
        results.clear();
        fireProfileRemovedEvent(new ProfileChangeEvent(this, null));
    }

    public void remove(SQLObject sqo) throws ArchitectException{
        ProfileResult old = results.remove(sqo);
        
        if ( sqo instanceof SQLTable ) {
            for ( SQLColumn col: ((SQLTable)sqo).getColumns()) {
                results.remove(col);                
            }
        }
        else if ( sqo instanceof SQLColumn ) {
            SQLTable table = ((SQLColumn)sqo).getParentTable();
            boolean allColumnDeleted = true;
            for ( SQLColumn col: table.getColumns()) {
                if ( getResult(col) != null ) {
                    allColumnDeleted = false;
                    break;
                }
            }
            if ( allColumnDeleted ){
                results.remove(table);
            }
        }
        fireProfileRemovedEvent(new ProfileChangeEvent(this, null));
    }

    private ColumnProfileResult execProfileFunction(ProfileFunctionDescriptor pfd,
                                SQLColumn col, DDLGenerator ddlg,
                                Connection conn) throws SQLException {

        long createStartTime = System.currentTimeMillis();
        final int i = 0;
        StringBuffer sql = new StringBuffer();
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        String columnName = null;
        String databaseIdentifierQuoteString = null;
        ColumnProfileResult colResult = new ColumnProfileResult(col);
        colResult.setCreateStartTime(createStartTime);
        SQLTable table = col.getParentTable();
        

        try {
            databaseIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
            sql.append("SELECT 1");
            int tryCount = 0;
            if (findingDistinctCount && pfd.isCountDist() ) {
                sql.append(",\n COUNT(DISTINCT ");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS DISTINCTCOUNT_"+i);
                tryCount++;
            }
            if (findingMin && pfd.isMinValue() ) {
                sql.append(",\n MIN(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MINVALUE_"+i);
                tryCount++;
            }
            if (findingMax && pfd.isMaxValue() ) {
                sql.append(",\n MAX(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MAXVALUE_"+i);
                tryCount++;
            }
            if (findingAvg && pfd.isAvgValue() ) {
                sql.append(",\n ");
                sql.append(ddlg.getAverageSQLFunctionName(databaseIdentifierQuoteString+
                                        col.getName()+
                                        databaseIdentifierQuoteString));
                sql.append(" AS AVGVALUE_"+i);
                tryCount++;
            }
            if (findingMinLength && pfd.isMinLength() ) {
                sql.append(",\n MIN(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                                    col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MINLENGTH_"+i);
                tryCount++;
            }
            if (findingMaxLength && pfd.isMaxLength() ) {
                sql.append(",\n MAX(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MAXLENGTH_"+i);
                tryCount++;
            }
            if (findingAvgLength && pfd.isAvgLength() ) {
                sql.append(",\n AVG(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS AVGLENGTH_"+i);
                tryCount++;
            }

            if ( findingNullCount && pfd.isSumDecode() ) {
                sql.append(",\n SUM(");
                sql.append(ddlg.caseWhenNull(
                        databaseIdentifierQuoteString+
                        col.getName()+
                        databaseIdentifierQuoteString,
                        "1"));
                sql.append(") AS NULLCOUNT_"+i);
                tryCount++;
            }

            if ( tryCount > 0 ) {
                sql.append("\n FROM ");

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
                    if (findingDistinctCount && pfd.isCountDist() ) {
                        columnName = "DISTINCTCOUNT_"+i;
                        colResult.setDistinctValueCount(rs.getInt(columnName));
                    }
                    if (findingMin && pfd.isMinValue() ) {
                        columnName = "MINVALUE_"+i;
                        colResult.setMinValue(rs.getObject(columnName));
                    }
                    if (findingMax && pfd.isMaxValue() ) {
                        columnName = "MAXVALUE_"+i;
                        colResult.setMaxValue(rs.getObject(columnName));
                    }
                    if (findingAvg && pfd.isAvgValue() ) {
                        columnName = "AVGVALUE_"+i;
                        colResult.setAvgValue(rs.getObject(columnName));
                    }
                    if (findingMinLength && pfd.isMinLength() ) {
                        columnName = "MINLENGTH_"+i;
                        colResult.setMinLength(rs.getInt(columnName));
                    }
                    if (findingMaxLength && pfd.isMaxLength() ) {
                        columnName = "MAXLENGTH_"+i;
                        colResult.setMaxLength(rs.getInt(columnName));
                    }
                    if (findingAvgLength && pfd.isAvgLength() ) {
                        columnName = "AVGLENGTH_"+i;
                        colResult.setAvgLength(rs.getDouble(columnName));
                    }

                    if ( findingNullCount && pfd.isSumDecode() ) {
                        columnName = "NULLCOUNT_"+i;
                        colResult.setNullCount(rs.getInt(columnName));
                    }
                }
                else {
                    throw new IllegalStateException("Query executed, but returns no rows:\n" +
                            lastSQL + "\nColumn Name: " + columnName );
                }
                rs.close();
                rs = null;
            }

            if (findingTopTen && pfd.isCountDist() ) {
                sql = new StringBuffer();
                sql.append("SELECT ").append(databaseIdentifierQuoteString);
                sql.append(col.getName()).append(databaseIdentifierQuoteString);
                sql.append(" AS MYVALUE, COUNT(*) AS COUNT1 FROM ");
                sql.append(DDLUtils.toQualifiedName(table.getCatalogName(),
                                                    table.getSchemaName(),
                                                    table.getName(),
                                                    databaseIdentifierQuoteString,
                                                    databaseIdentifierQuoteString));
                sql.append(" GROUP BY ").append(databaseIdentifierQuoteString);
                sql.append(col.getName()).append(databaseIdentifierQuoteString);
                sql.append(" ORDER BY COUNT1 DESC");

                lastSQL = sql.toString();
                rs = stmt.executeQuery(lastSQL);
                for ( int n=0; rs.next() && n < topNCount; n++ ) {
                    colResult.addValueCount(rs.getObject("MYVALUE"), rs.getInt("COUNT1"));
                }
                rs.close();
                rs = null;
            }

            colResult.setCreateEndTime(System.currentTimeMillis());
            return colResult;

        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException ex) {
                logger.error("Couldn't clean up result set", ex);
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
            userCancel = cancelled;
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

    public int getTopNCount() {
        return topNCount;
    }

    public void setTopNCount(int topNCount) {
        this.topNCount = topNCount;
    }

    public Map<SQLObject, ProfileResult> getResults() {
        return results;
    }        
    
    //==================================
    // ProfileManagerListeners
    //==================================
    List<ProfileChangeListener> listeners = new ArrayList<ProfileChangeListener>();
    
    public void addProfileChangeListener(ProfileChangeListener listener){
        listeners.add(listener);
    }
    
    public void removeProfileChangeListener(ProfileChangeListener listener){
        listeners.remove(listener);
    }
    
    private void fireProfileAddedEvent(ProfileChangeEvent event){
        for (ProfileChangeListener listener: listeners){
            listener.profileAdded(event);
        }
    }
    
    private void fireProfileRemovedEvent(ProfileChangeEvent event){
        for (ProfileChangeListener listener: listeners){
            listener.profileRemoved(event);
        }
    }
}
