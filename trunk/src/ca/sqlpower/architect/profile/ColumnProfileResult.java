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
package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SPDataSourceType;

public class ColumnProfileResult extends AbstractProfileResult<SQLColumn> {

    private static final Logger logger = Logger.getLogger(ColumnProfileResult.class);

    private int distinctValueCount;
    private Object minValue;
    private Object maxValue;
    private Object avgValue;
    private int minLength;
    private int maxLength;
    private double avgLength;
    private int nullCount;
    private List<ColumnValueCount> topTen = new ArrayList<ColumnValueCount>();
    
    /**
     * Currently, we use the owning ProfileManager as a source of settings
     * for which types of profiling to perform on a column.  We ought to factor
     * these settings out into a ProfileSettings interface, and then change the
     * type of this field to that.
     */
    private ProfileManager manager = null;

    private final TableProfileResult parentResult;
    
    /**
     * A map from data type names used in Architect to the database's actual
     * data type stored in a profile function descriptor.
     */
    protected Map<String, ProfileFunctionDescriptor> profileFunctionMap;
    
    /**
     * This class is used to hold the specific start and end to a LENGTH
     * SQL command based on the database in use.
     */
    private class StringLengthSQLFunction {
        
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
    private class AverageSQLFunction {
        
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
    private class CaseWhenNullSQLFunction {
        
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
     * This creates a column profile result which stores information about a profiled column.
     * <p>
     * Note that if the manager is set to null the SQL functions will not be set up as this
     * profiled column will not connect to the database. The null manager is normally used 
     * for loading through the digester or a dummy ColumnProfileResult used in graphing.
     */
    public ColumnProfileResult(SQLColumn profiledObject, 
            ProfileManager manager, 
            TableProfileResult parentResult) {
        super(profiledObject);
        this.manager = manager;
        this.parentResult = parentResult;
        createProfileFunctions();
    }

    /**
     * This creates a column profile result which stores information about a profiled column.
     * <p>
     * Note that the manager is set to null so the SQL functions will not be set up as this
     * profiled column will not connect to the database. The null manager is normally used 
     * for loading through the digester or a dummy ColumnProfileResult used in graphing.
     */
    public ColumnProfileResult(SQLColumn profiledObject, 
            TableProfileResult parentResult) {
        super(profiledObject);
        this.parentResult = parentResult;
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
    private void createProfileFunctions() {
        if (manager == null) return;
        
        profileFunctionMap = new HashMap<String, ProfileFunctionDescriptor>();
        logger.debug("The property to retrieve is " + ProfileFunctionDescriptor.class.getName() + "_(number)");
        SPDataSourceType dsType = getProfiledObject().getParentTable().getParentDatabase().getDataSource().getParentType();
        
        for (int dataTypeCount = 0;; dataTypeCount += 1) {
            String dataTypeToParse = dsType.getProperty(ProfileFunctionDescriptor.class.getName() + "_" + dataTypeCount);
            if (dataTypeToParse == null) break;
            
            String[] dataTypeParts = dataTypeToParse.split(",");
            int dataTypeValue = Integer.parseInt(dataTypeParts[2].trim());
            
            profileFunctionMap.put(dataTypeParts[0].trim(), new ProfileFunctionDescriptor(dataTypeParts[1].trim(), 
                    dataTypeValue, dataTypeParts[3].trim().startsWith("t"), dataTypeParts[4].trim().startsWith("t"),
                    dataTypeParts[5].trim().startsWith("t"), dataTypeParts[6].trim().startsWith("t"),
                    dataTypeParts[7].trim().startsWith("t"), dataTypeParts[8].trim().startsWith("t"),
                    dataTypeParts[9].trim().startsWith("t"), dataTypeParts[10].trim().startsWith("t")));
        }
        
        logger.debug("The property to retrieve is " + StringLengthSQLFunction.class.getName());
        String function = dsType.getProperty(StringLengthSQLFunction.class.getName());
        String[] functionParts = function.split(":");
        stringLengthSQLFunction = new StringLengthSQLFunction(functionParts[0], functionParts[1]);
        
        function = dsType.getProperty(AverageSQLFunction.class.getName());
        functionParts = function.split(":");
        averageSQLFunction = new AverageSQLFunction(functionParts[0], functionParts[1]);
        
        function = dsType.getProperty(CaseWhenNullSQLFunction.class.getName());
        functionParts = function.split(":");
        caseWhenNullSQLFunction = new CaseWhenNullSQLFunction(functionParts[0], functionParts[1], functionParts[2]);
        
    }

    public double getAvgLength() {
        return avgLength;
    }

    public void setAvgLength(double avgLength) {
        this.avgLength = avgLength;
    }

    /**
     * @return The average value as a Number object, or null if there were
     * 0 values.
     */
    public Object getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Object avgValue) {
        this.avgValue = avgValue;
    }

    public int getDistinctValueCount() {
        return distinctValueCount;
    }

    public void setDistinctValueCount(int distinctValueCount) {
        this.distinctValueCount = distinctValueCount;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    @Override
    public String toString() {
        return "[ColumnProfileResult:" +
        "; distinctValues: "+distinctValueCount+
        "; minLength: "+minLength+
        "; maxLength: "+maxLength+
        "; avgLength: "+avgLength+
        "; minValue: "+getMinValue()+
        "; maxValue: "+getMaxValue()+
        "; avgValue: "+avgValue+
        "; nullCount: "+getNullCount()+ "]";
    }

    public int getNullCount() {
        return nullCount;
    }

    public void setNullCount(int nullCount) {
        this.nullCount = nullCount;
    }

    public void addValueCount(Object value, int count) {
        ColumnValueCount columnValueCount = new ColumnValueCount(value,count);
        if (!topTen.contains(columnValueCount)) {
            topTen.add(columnValueCount);
            logger.debug("Added Value Count: Value: " + value + " Count: " + count);
        }
    }

    public void addValueCount(ColumnValueCount value) {
        topTen.add(value);
    }
    public List<ColumnValueCount> getValueCount() {
        return topTen;
    }

    protected void doProfile() throws SQLException, ArchitectException {
        logger.debug("Starting to profile " + getProfiledObject().getName());
        if (parentResult.isCancelled()) {
            return;
        }
        
        if (manager == null) {
            // Either:
            // This is being created by the Digester, or,
            // this is a "dummy" ColumnProfileResult used in graphing.
            // In neither case do we run against the live database.
            // System.err.println("Creating dummy " + getClass());
            return;
        }
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            SQLColumn col = getProfiledObject();
            SQLDatabase db = col.getParentTable().getParentDatabase();
            conn = db.getConnection();
            stmt = conn.createStatement();
            stmt.setEscapeProcessing(false);
            
            ProfileFunctionDescriptor pfd = profileFunctionMap.get(col.getSourceDataTypeName());
            long profileStartTime = System.currentTimeMillis();

            if (pfd == null) {
                logger.debug(col.getName()+ " Unknown DataType:(" +
                        col.getSourceDataTypeName() + ").");
                pfd = discoverProfileFunctionDescriptor(col, conn);
            }

            try {
                execProfileFunction(pfd, col, conn);
            } catch ( Exception ex ) {
                setCreateStartTime(profileStartTime);
                setException(ex);
                setCreateEndTime(System.currentTimeMillis());
                logger.error("Error in Column Profiling: "+lastSQL, ex);
            }

            // XXX: add "where" filter later
            // XXX I wonder what he meant by that?
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
     * Discovers which profiling functions can be applied to the given column
     * by trial and error.  This could be extremely time-consuming.
     * @param col The column to figureout how to profile
     * @param ddlg The DDL Generator for col's database
     * @param conn A connection to col's database
     * @return A ProfileFunctionDescriptor that is properly configured for the data
     * type of col.
     */
    private ProfileFunctionDescriptor discoverProfileFunctionDescriptor(SQLColumn col, Connection conn) {
        ProfileFunctionDescriptor pfd = new ProfileFunctionDescriptor(col.getSourceDataTypeName(),
                col.getType(),false,false,false,false,false,false,false,false);

        logger.debug("Discovering profile functions for column "+col);

        try {
            pfd.setCountDist(true);
            execProfileFunction(pfd, col, conn);
            logger.debug("countDist worked");
        } catch (Exception e) {
            logger.debug("countDist failed", e);
            pfd.setCountDist(false);
        }

        try {
            pfd.setMaxValue(true);
            pfd.setMinValue(true);
            execProfileFunction(pfd, col, conn);
            logger.debug("min/max worked");
        } catch (Exception e) {
            logger.debug("min/max failed", e);
            pfd.setMaxValue(false);
            pfd.setMinValue(false);
        }

        try {
            pfd.setAvgValue(true);
            execProfileFunction(pfd, col, conn);
            logger.debug("avg worked");
        } catch (Exception e) {
            logger.debug("avg failed", e);
            pfd.setAvgValue(false);
        }

        try {
            pfd.setMaxLength(true);
            pfd.setMinLength(true);
            pfd.setAvgLength(true);
            execProfileFunction(pfd, col, conn);
            logger.debug("min/max/avg length worked");
        } catch (Exception e) {
            logger.debug("min/max/avg length failed", e);
            pfd.setMaxLength(false);
            pfd.setMinLength(false);
            pfd.setAvgLength(false);
        }

        try {
            pfd.setSumDecode(true);
            execProfileFunction(pfd, col, conn);
            logger.debug("sumDecode worked");
        } catch (Exception e) {
            logger.debug("sumDecode failed", e);
            pfd.setSumDecode(false);
        }


        return pfd;
    }

    private void execProfileFunction(ProfileFunctionDescriptor pfd,
            SQLColumn col, Connection conn) throws SQLException {

        logger.debug("Starting execProfileFunction, if only it was documented.");
        long createStartTime = System.currentTimeMillis();
        final int i = 0;
        StringBuffer sql = new StringBuffer();
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        String columnName = null;
        String databaseIdentifierQuoteString = null;
        setCreateStartTime(createStartTime);
        SQLTable table = col.getParentTable();


        try {
            databaseIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
            sql.append("SELECT 1");
            int tryCount = 0;
            if (manager.getDefaultProfileSettings().isFindingDistinctCount() && pfd.isCountDist() ) {
                sql.append(",\n COUNT(DISTINCT ");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS DISTINCTCOUNT_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingMin() && pfd.isMinValue() ) {
                sql.append(",\n MIN(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MINVALUE_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingMax() && pfd.isMaxValue() ) {
                sql.append(",\n MAX(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MAXVALUE_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingAvg() && pfd.isAvgValue() ) {
                sql.append(",\n ");
                sql.append(averageSQLFunction.getAverageSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+
                        databaseIdentifierQuoteString));
                sql.append(" AS AVGVALUE_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingMinLength() && pfd.isMinLength() ) {
                sql.append(",\n MIN(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MINLENGTH_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingMaxLength() && pfd.isMaxLength() ) {
                sql.append(",\n MAX(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MAXLENGTH_"+i);
                tryCount++;
            }
            if (manager.getDefaultProfileSettings().isFindingAvgLength() && pfd.isAvgLength() ) {
                sql.append(",\n AVG(");
                sql.append(stringLengthSQLFunction.getStringLengthSQLFunction(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS AVGLENGTH_"+i);
                tryCount++;
            }

            if (manager.getDefaultProfileSettings().isFindingNullCount() && pfd.isSumDecode() ) {
                sql.append(",\n SUM(");
                sql.append(caseWhenNullSQLFunction.getCaseWhenNullSQLFunction(
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
                // System.err.println(lastSQL);
                rs = stmt.executeQuery(lastSQL);

                if (rs.next()) {
                    if (manager.getDefaultProfileSettings().isFindingDistinctCount() && pfd.isCountDist() ) {
                        columnName = "DISTINCTCOUNT_"+i;
                        setDistinctValueCount(rs.getInt(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingMin() && pfd.isMinValue() ) {
                        columnName = "MINVALUE_"+i;
                        setMinValue(rs.getObject(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingMax() && pfd.isMaxValue() ) {
                        columnName = "MAXVALUE_"+i;
                        setMaxValue(rs.getObject(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingAvg() && pfd.isAvgValue() ) {
                        columnName = "AVGVALUE_"+i;
                        setAvgValue(rs.getObject(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingMinLength() && pfd.isMinLength() ) {
                        columnName = "MINLENGTH_"+i;
                        setMinLength(rs.getInt(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingMaxLength() && pfd.isMaxLength() ) {
                        columnName = "MAXLENGTH_"+i;
                        setMaxLength(rs.getInt(columnName));
                    }
                    if (manager.getDefaultProfileSettings().isFindingAvgLength() && pfd.isAvgLength() ) {
                        columnName = "AVGLENGTH_"+i;
                        setAvgLength(rs.getDouble(columnName));
                    }

                    if (manager.getDefaultProfileSettings().isFindingNullCount() && pfd.isSumDecode() ) {
                        columnName = "NULLCOUNT_"+i;
                        setNullCount(rs.getInt(columnName));
                    }
                }
                else {
                    throw new IllegalStateException("Query executed, but returns no rows:\n" +
                            lastSQL + "\nColumn Name: " + columnName );
                }
                rs.close();
                rs = null;
            }

            if (manager.getDefaultProfileSettings().isFindingTopTen() && pfd.isCountDist() ) {
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
                int topNCount = 10; // XXX
                for (int n=0; rs.next() && n < topNCount; n++ ) {
                    addValueCount(rs.getObject("MYVALUE"), rs.getInt("COUNT1"));
                }
                rs.close();
                rs = null;
            }

            setCreateEndTime(System.currentTimeMillis());

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

    public TableProfileResult getParentResult() {
        return parentResult;
    }
}
