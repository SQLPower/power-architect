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

package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;

/**
 * A profile creator implementation that makes the RDBMS do all the hard work.
 * This profiler is best used when the database is separated from the local JVM
 * by a slow network, since only the results of the profile are transmitted over
 * the network. This profile creator is a poor choice when the database table
 * being profiled is larger than the remote database's available work memory,
 * since the SQL queries it issues require several full passes over the table,
 * as well as several partial sorts of the table.
 * <p>
 * Since the profiling is done by asking the remote database to aggregate the
 * data, some aggregates will not always be available on all data types. For
 * example, if the remote database is PostgreSQL, the average date of a date
 * column will be calculated; if the remote database is Oracle, the average date
 * will not be calculated. This class looks for the configuration of which
 * aggregate functions work with which data types in the SPDataSourceType of the
 * given SQLTable's data source.  See {@link #createProfileFunctions(JDBCDataSourceType)}
 * for details.
 * <p>
 * For a profiler better suited for use on large tables, see
 * {@link LocalReservoirProfileCreator}.
 */
public class RemoteDatabaseProfileCreator extends AbstractTableProfileCreator {

    private static final Logger logger = Logger.getLogger(RemoteDatabaseProfileCreator.class);
    
    /**
     * A map from data type names used in Architect to the database's actual
     * data type stored in a profile function descriptor.
     */
    private Map<String, ProfileFunctionDescriptor> profileFunctionMap;
    
    /**
     * This class is used to hold the specific start and end to a LENGTH
     * SQL command based on the database in use.
     */
    public class StringLengthSQLFunction {
        
        /**
         * The part of the LENGTH SQL command that comes before the argument
         */
        private String startOfLength;
        
        /**
         * The part of the LENGTH SQL command that comes after the argument
         */
        private String endOfLength;
        
        public StringLengthSQLFunction(String startOfLength, String endOfLength) {
            this.startOfLength = startOfLength;
            this.endOfLength = endOfLength;
        }
        
        /**
         * Returns a string of the expression wrapped in the database specific 
         * LENGTH function
         */
        public String getStringLengthSQLFunction(String expression) {
            return startOfLength + expression + endOfLength;
        }
    }
    
    /**
     * This class is used to hold the specific start and end to a AVERAGE
     * SQL command based on the database in use.
     */
    public class AverageSQLFunction {
        
        /**
         * The part of the AVERAGE SQL command that comes before the argument
         */
        private String startOfAverage;
        
        /**
         * The part of the AVERAGE SQL command that comes after the argument
         */
        private String endOfAverage;
        
        public AverageSQLFunction(String startOfAverage, String endOfAverage) {
            this.startOfAverage = startOfAverage;
            this.endOfAverage = endOfAverage;
        }
        
        /**
         * Returns a string of the expression wrapped in the database specific 
         * AVERAGE function
         */
        public String getAverageSQLFunction(String expression) {
            return startOfAverage + expression + endOfAverage;
        }
    }
    
    /**
     * A class that can create a SQL CASE statement that will evaluates the
     * given expression is null and return a particular value based on the value
     * of that expression.
     * <p>
     * In ANSI SQL, this method will return something like:
     * <code>CASE WHEN <i>expression IS NULL</i> THEN <i>then</i> END</code>.
     * Some platforms don't support ANSI case statements, so the DDL Generators
     * for those platforms (i.e. Oracle) will be different, but should have the
     * same meaning.
     */
    public class CaseWhenNullSQLFunction {
        
        /**
         * The part of the CASE statement for evaluating a null expression in SQL that
         * comes before the <i>expression</i>.
         */
        private String startOfNullCase;
        
        /**
         * The part of the CASE statement for evaluating a null expression in SQL that
         * comes after the <i>expression</i> but before the <i>then</i> part.
         */
        private String middleOfNullCase;
        
        /**
         * The part of the CASE statement for evaluating a null expression in SQL that
         * comes after the <i>then</i> part.
         */
        private String endOfNullCase;
        
        public CaseWhenNullSQLFunction(String startOfNullCase, String middleOfNullCase, String endOfNullCase) {
            this.startOfNullCase = startOfNullCase;
            this.middleOfNullCase = middleOfNullCase;
            this.endOfNullCase = endOfNullCase;
        }
        
        /**
         * Creates a SQL CASE statement that will evaluates the given expression is null
         *  and return a particular value based on the value of that expression.
         * <p>
         * In ANSI SQL, this method will return something like:
         * <code>CASE WHEN <i>expression IS NULL</i> THEN <i>then</i> END</code>.  Some
         * platforms don't support ANSI case statements, so the DDL Generators for those platforms
         * (i.e. Oracle) will be different, but should have the same meaning.
         */
        public String getCaseWhenNullSQLFunction(String expression, String then) {
            return startOfNullCase + expression + middleOfNullCase + then + endOfNullCase;
        }
    }
    
    /**
     * An object to store the string length function for the database.
     */
    private StringLengthSQLFunction stringLengthSQLFunction;
    
    /**
     * An object to store the average function for the database.
     */
    private AverageSQLFunction averageSQLFunction;
    
    /**
     * An object to store the case when null function for the database.
     */
    private CaseWhenNullSQLFunction caseWhenNullSQLFunction;

    /**
     * The settings for this profile creator.
     */
    private final ProfileSettings settings;
    
    public RemoteDatabaseProfileCreator(ProfileSettings settings) {
        this.settings = settings;
    }
    
    public boolean doProfileImpl(TableProfileResult tpr) {
        MonitorableImpl pm = (MonitorableImpl) tpr.getProgressMonitor();
        try {
            
            doTableProfile(tpr);
            
            SQLTable table = tpr.getProfiledObject();
            JDBCDataSourceType dsType = table.getParentDatabase().getDataSource().getParentType();
            createProfileFunctions(dsType);
            for (SQLColumn col : table.getColumns()) {
                ColumnProfileResult columnResult = new ColumnProfileResult(col);
                tpr.addColumnProfileResult(columnResult);
                doColumnProfile(columnResult, pm);
                pm.setProgress(pm.getProgress() + 1);
            }

            return !pm.isCancelled();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Performs just the overall table part of the profiling.  No column profiling
     * is performed.
     * 
     * @param tpr The table profile result to populate.
     */
    private void doTableProfile(TableProfileResult tpr) throws SQLException, SQLObjectException {
        logger.debug("Doing profile for table " + tpr.getProfiledObject());
        MonitorableImpl pm = (MonitorableImpl) tpr.getProgressMonitor();
        pm.setProgress(0);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            SQLTable table = tpr.getProfiledObject();
            pm.setJobSize(table.getColumns().size() + 1);
            SQLDatabase db = table.getParentDatabase();
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
            String lastSQL = sql.toString();

            pm.setProgress(pm.getProgress() + 1);
            
            rs = stmt.executeQuery(lastSQL);

            if (rs.next()) {
                tpr.setRowCount(rs.getInt("ROW__COUNT"));
            } else {
                throw new AssertionError("No rows came back from COUNT(*) query!");
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
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Executes all of the profile functions defined in the given descriptor.  These
     * functions are executed against the given column, and the results are stored
     * in the given {@link ColumnProfileResult} object.
     * 
     * @param cpr The object to store the profile results in.
     * @param pfd The descriptor that says which profiling functions can be calculated
     * for col in the RDBMS it's stored in (the data types supported by the various
     * aggregate functions differ by platform).
     * @param col The column to perform the profiling on.
     * @param con The connection to use to the database <tt>col</tt> is in.
     * @param pm The progress monitor for this operation.  It will be polled to see if the current
     * profiling operation has been cancelled.  It will not be manipulated in any other way.
     * @throws SQLException If profiling fails.  This is most likely due to an incorrect
     * function descriptor for col's data type, or database connectivity issues.
     */
    private void execProfileFunctions(
            ColumnProfileResult cpr,
            ProfileFunctionDescriptor pfd,
            SQLColumn col,
            Connection con,
            Monitorable pm) throws SQLException {

        logger.debug("Starting execProfileFunctions for " + col);
        long createStartTime = System.currentTimeMillis();
        final int i = 0;
        StringBuffer sql = new StringBuffer();
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        String columnName = null;
        String databaseIdentifierQuoteString = null;
        cpr.setCreateStartTime(createStartTime);
        SQLTable table = col.getParent();

        try {
            databaseIdentifierQuoteString = con.getMetaData().getIdentifierQuoteString();
            sql.append("SELECT 1");
            int tryCount = 0;
            if (settings.isFindingDistinctCount() && pfd.isCountDist() ) {
                sql.append(",\n COUNT(DISTINCT ");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS DISTINCTCOUNT_"+i);
                tryCount++;
            }
            if (settings.isFindingMin() && pfd.isMinValue() ) {
                sql.append(",\n MIN(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MINVALUE_"+i);
                tryCount++;
            }
            if (settings.isFindingMax() && pfd.isMaxValue() ) {
                sql.append(",\n MAX(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MAXVALUE_"+i);
                tryCount++;
            }
            if (settings.isFindingAvg() && pfd.isAvgValue() ) {
                sql.append(",\n ");
                sql.append(averageSQLFunction.getAverageSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+
                        databaseIdentifierQuoteString));
                sql.append(" AS AVGVALUE_"+i);
                tryCount++;
            }
            if (settings.isFindingMinLength() && pfd.isMinLength() ) {
                sql.append(",\n MIN(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MINLENGTH_"+i);
                tryCount++;
            }
            if (settings.isFindingMaxLength() && pfd.isMaxLength() ) {
                sql.append(",\n MAX(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MAXLENGTH_"+i);
                tryCount++;
            }
            if (settings.isFindingAvgLength() && pfd.isAvgLength() ) {
                sql.append(",\n AVG(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS AVGLENGTH_"+i);
                tryCount++;
            }

            if (settings.isFindingNullCount() && pfd.isSumDecode() ) {
                sql.append(",\n SUM(");
                sql.append(caseWhenNullSQLFunction.getCaseWhenNullSQLFunction(
                        databaseIdentifierQuoteString+
                        col.getName()+
                        databaseIdentifierQuoteString,
                "1"));
                sql.append(") AS NULLCOUNT_"+i);
                tryCount++;
            }

            if ( tryCount > 0 && !pm.isCancelled() ) {
                sql.append("\n FROM ");

                sql.append(DDLUtils.toQualifiedName(table.getCatalogName(),
                        table.getSchemaName(),
                        table.getName(),
                        databaseIdentifierQuoteString,
                        databaseIdentifierQuoteString));

                stmt = con.createStatement();
                stmt.setEscapeProcessing(false);

                lastSQL = sql.toString();
                
                if (pm.isCancelled()) return;
                rs = stmt.executeQuery(lastSQL);
                if (pm.isCancelled()) return;

                if (rs.next()) {
                    if (settings.isFindingDistinctCount() && pfd.isCountDist() ) {
                        columnName = "DISTINCTCOUNT_"+i;
                        cpr.setDistinctValueCount(rs.getInt(columnName));
                    }
                    if (settings.isFindingMin() && pfd.isMinValue() ) {
                        columnName = "MINVALUE_"+i;
                        cpr.setMinValue(rs.getObject(columnName));
                    }
                    if (settings.isFindingMax() && pfd.isMaxValue() ) {
                        columnName = "MAXVALUE_"+i;
                        cpr.setMaxValue(rs.getObject(columnName));
                    }
                    if (settings.isFindingAvg() && pfd.isAvgValue() ) {
                        columnName = "AVGVALUE_"+i;
                        cpr.setAvgValue(rs.getObject(columnName));
                    }
                    if (settings.isFindingMinLength() && pfd.isMinLength() ) {
                        columnName = "MINLENGTH_"+i;
                        cpr.setMinLength(rs.getInt(columnName));
                    }
                    if (settings.isFindingMaxLength() && pfd.isMaxLength() ) {
                        columnName = "MAXLENGTH_"+i;
                        cpr.setMaxLength(rs.getInt(columnName));
                    }
                    if (settings.isFindingAvgLength() && pfd.isAvgLength() ) {
                        columnName = "AVGLENGTH_"+i;
                        cpr.setAvgLength(rs.getDouble(columnName));
                    }

                    if (settings.isFindingNullCount() && pfd.isSumDecode() ) {
                        columnName = "NULLCOUNT_"+i;
                        cpr.setNullCount(rs.getInt(columnName));
                    }
                }
                else {
                    throw new IllegalStateException("Query executed, but returns no rows:\n" +
                            lastSQL + "\nColumn Name: " + columnName );
                }
                rs.close();
                rs = null;
            }

            if (settings.isFindingTopTen() && pfd.isCountDist() && !pm.isCancelled() ) {
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
                int topNCount = settings.getTopNCount();
                int topNSum = 0;
                for (int n = 0; rs.next() && n < topNCount; n++) {
                    cpr.addValueCount(rs.getObject("MYVALUE"), rs.getInt("COUNT1"));
                    topNSum += rs.getInt("COUNT1");
                }
                int remainingCount = cpr.getParent().getRowCount() - topNSum;
                if (remainingCount > 0) {
                    cpr.addValueCount(ColumnValueCount.OTHER_VALUE_OBJECT, remainingCount);
                }
                rs.close();
                rs = null;
            }

            cpr.setCreateEndTime(System.currentTimeMillis());

        } catch (SQLException ex) {
            logger.error("Profiling query failed.  Will throw exception.  Query was:");
            logger.error(lastSQL);
            throw ex;
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
    
    /**
     * Performs profiling at the column level by issuing a SELECT statement against
     * the column referenced by <tt>cpr</tt>.
     * 
     * @param cpr The profile result to populate
     * @param pm The progress monitor.  This progress monitor is only used for checking
     * if the operation is canceled; it is not updated with progress information.
     */
    protected void doColumnProfile(ColumnProfileResult cpr, MonitorableImpl pm) throws SQLException, SQLObjectException {
        logger.debug("Doing profile for column " + cpr.getProfiledObject().getName());
        if (pm.isCancelled()) {
            return;
        }
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            SQLColumn col = cpr.getProfiledObject();
            SQLDatabase db = col.getParent().getParentDatabase();
            con = db.getConnection();
            stmt = con.createStatement();
            stmt.setEscapeProcessing(false);
            
            ProfileFunctionDescriptor pfd = profileFunctionMap.get(col.getSourceDataTypeName());
            long profileStartTime = System.currentTimeMillis();

            if (pfd == null) {
                logger.debug(col.getName()+ " Unknown DataType:(" +
                        col.getSourceDataTypeName() + ").");
                logger.debug("Known data types are: " + profileFunctionMap.keySet());
                pfd = discoverProfileFunctionDescriptor(col, con, pm);
                profileFunctionMap.put(col.getSourceDataTypeName(), pfd);
            }

            try {
                execProfileFunctions(cpr, pfd, col, con, pm);
            } catch (Exception ex) {
                cpr.setCreateStartTime(profileStartTime);
                cpr.setException(ex);
                cpr.setCreateEndTime(System.currentTimeMillis());
                logger.error("Error in Column Profiling: "+lastSQL, ex);
            }

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
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * This creates and sets up the map from data type names used in Architect
     * to the database's actual data type stored in a profile function
     * descriptor. The data mapping and SQL functions are only set up if the
     * manager is not null. A null manager means that we will not be connecting
     * to the database. The data for the mapping comes from the pl.ini file so
     * it can be specified by the user. This is the method that parses the
     * strings from the pl.ini file. The java.sql.Types class defines what the
     * integer values represent for the data types.
     * <p>
     * This method also sets up the string length SQL function, average SQL
     * function, and case when null SQL function as they are also database
     * specific.
     */
    private void createProfileFunctions(JDBCDataSourceType dsType) {
        
        profileFunctionMap = new HashMap<String, ProfileFunctionDescriptor>();
        logger.debug("The property to retrieve is " + ProfileFunctionDescriptor.class.getName() + "_(number)");
        
        for (int dataTypeCount = 0;; dataTypeCount += 1) {
            String dataTypeToParse = dsType.getProperty(ProfileFunctionDescriptor.class.getName() + "_" + dataTypeCount);
            if (dataTypeToParse == null) break;
            
            ProfileFunctionDescriptor pfd = ProfileFunctionDescriptor.parseDescriptorString(dataTypeToParse);
            profileFunctionMap.put(pfd.getArchitectSpecificName(), pfd);
        }
        
        logger.debug("The property to retrieve is " + propName(StringLengthSQLFunction.class));
        String function = dsType.getProperty(propName(StringLengthSQLFunction.class));
        String[] functionParts = function.split(":");
        if (functionParts.length != 2) {
            throw new RuntimeException(
                "Configuration error in SQL String Length Function Descriptor for " + dsType.getName() + ":\n" +
                "Function descriptor must have exactly one : character in it (eg. 'LENGTH(:)')\n" +
                "Current setting for your database is '" + function + "'"
                );
        }
        stringLengthSQLFunction = new StringLengthSQLFunction(functionParts[0], functionParts[1]);
        
        function = dsType.getProperty(propName(AverageSQLFunction.class));
        functionParts = function.split(":");
        if (functionParts.length != 2) {
            throw new RuntimeException(
                "Configuration error in SQL Average Function Descriptor for " + dsType.getName() + ":\n" +
                "Function descriptor must have exactly one : character in it (eg. 'AVG(:)')\n" +
                "Current setting for your database is '" + function + "'"
                );
        }
        averageSQLFunction = new AverageSQLFunction(functionParts[0], functionParts[1]);
        
        function = dsType.getProperty(propName(CaseWhenNullSQLFunction.class));
        functionParts = function.split(":");
        if (functionParts.length != 3) {
            throw new RuntimeException(
                "Configuration error in SQL WHEN NULL Function Descriptor for " + dsType.getName() + ":\n" +
                "Function descriptor must have exactly two : characters in it (eg. 'CASE WHEN : IS NULL THEN : END')\n" +
                "Current setting for your database is '" + function + "'"
                );
        }
        caseWhenNullSQLFunction = new CaseWhenNullSQLFunction(functionParts[0], functionParts[1], functionParts[2]);
        
    }

    /**
     * Discovers which profiling functions can be applied to the given column
     * by trial and error.  This could be extremely time-consuming.
     * 
     * @param col The column to figureout how to profile
     * @param conn A connection to col's database
     * @return A ProfileFunctionDescriptor that is properly configured for the data
     * type of col.
     */
    private ProfileFunctionDescriptor discoverProfileFunctionDescriptor(SQLColumn col, Connection conn, Monitorable pm) {
        ProfileFunctionDescriptor pfd = new ProfileFunctionDescriptor(col.getSourceDataTypeName(),
                col.getType(),false,false,false,false,false,false,false,false);

        TableProfileResult dummyParent = new TableProfileResult(col.getParent(), new ProfileSettings());
        dummyParent.setRowCount(1);
        ColumnProfileResult dummy = new ColumnProfileResult(col);
        dummyParent.addColumnProfileResult(dummy);
        
        logger.debug("Discovering profile functions for column "+col);

        if (pm.isCancelled()) return null;
        
        try {
            pfd.setCountDist(true);
            execProfileFunctions(dummy, pfd, col, conn, pm);
            logger.debug("countDist worked");
        } catch (Exception e) {
            logger.debug("countDist failed", e);
            pfd.setCountDist(false);
        }

        if (pm.isCancelled()) return null;

        try {
            pfd.setMaxValue(true);
            pfd.setMinValue(true);
            execProfileFunctions(dummy, pfd, col, conn, pm);
            logger.debug("min/max worked");
        } catch (Exception e) {
            logger.debug("min/max failed", e);
            pfd.setMaxValue(false);
            pfd.setMinValue(false);
        }

        if (pm.isCancelled()) return null;

        try {
            pfd.setAvgValue(true);
            execProfileFunctions(dummy, pfd, col, conn, pm);
            logger.debug("avg worked");
        } catch (Exception e) {
            logger.debug("avg failed", e);
            pfd.setAvgValue(false);
        }

        if (pm.isCancelled()) return null;

        try {
            pfd.setMaxLength(true);
            pfd.setMinLength(true);
            pfd.setAvgLength(true);
            execProfileFunctions(dummy, pfd, col, conn, pm);
            logger.debug("min/max/avg length worked");
        } catch (Exception e) {
            logger.debug("min/max/avg length failed", e);
            pfd.setMaxLength(false);
            pfd.setMinLength(false);
            pfd.setAvgLength(false);
        }

        if (pm.isCancelled()) return null;

        try {
            pfd.setSumDecode(true);
            execProfileFunctions(dummy, pfd, col, conn, pm);
            logger.debug("sumDecode worked");
        } catch (Exception e) {
            logger.debug("sumDecode failed", e);
            pfd.setSumDecode(false);
        }

        return pfd;
    }

    /**
     * Returns the appropriate profile function descriptor property name
     * for the given profiling function class.  The reason this function
     * exists is because the property names are the fully-qualified class
     * names of the function class, but the functions got moved.  To preserve
     * backward compatibility, we have to keep using the old property names.
     * 
     * @param forClass
     * @return
     */
    public static String propName(Class<?> forClass) {
        if (forClass == AverageSQLFunction.class) {
            return "ca.sqlpower.architect.profile.ColumnProfileResult$AverageSQLFunction";
        } else if (forClass == CaseWhenNullSQLFunction.class) {
            return "ca.sqlpower.architect.profile.ColumnProfileResult$CaseWhenNullSQLFunction";
        } else if (forClass == StringLengthSQLFunction.class) {
            return "ca.sqlpower.architect.profile.ColumnProfileResult$StringLengthSQLFunction";
        } else {
            // none of the other classes were renamed or moved
            return forClass.getName();
        }
    }
    
    @Override
    public String toString() {
        return "Remote Database";
    }
}
