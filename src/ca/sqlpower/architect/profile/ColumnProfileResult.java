package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;

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
    private ProfileManager manager;

    private DDLGenerator ddlg;

    public ColumnProfileResult(SQLColumn profiledObject, ProfileManager manager, DDLGenerator ddlg) {
        super(profiledObject);
        this.manager = manager;
        this.ddlg = ddlg;
    }

    public ColumnProfileResult(SQLColumn profiledObject) {
        super(profiledObject);
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
        topTen.add(new ColumnValueCount(value,count));
        return;
    }

    public void addValueCount(ColumnValueCount value) {
        topTen.add(value);
        return;
    }
    public List<ColumnValueCount> getValueCount() {
        return topTen;
    }

    public void doProfile() throws SQLException, ArchitectException {

        if (manager == null || ddlg == null) {
            // Either:
            // This is being created by the Digester, or,
            // this is a "dummy" ColumnProfileResult used in graphing.
            // In neither case do we run against the live database.
            // System.err.println("Creating dummy " + getClass());
            return;
        }
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {

            SQLColumn col = getProfiledObject();
            SQLDatabase db = col.getParentTable().getParentDatabase();
            Connection conn = db.getConnection();
            stmt = conn.createStatement();
            stmt.setEscapeProcessing(false);

            ProfileFunctionDescriptor pfd = ddlg.getProfileFunctionMap().get(col.getSourceDataTypeName());
            long profileStartTime = System.currentTimeMillis();

            if (pfd == null) {
                logger.debug(col.getName()+ " Unknown DataType:(" +
                        col.getSourceDataTypeName() + ").");
                pfd = discoverProfileFunctionDescriptor(col, ddlg, conn);
            }

            try {
                execProfileFunction(pfd, col, ddlg, conn);
            } catch ( Exception ex ) {
                setCreateStartTime(profileStartTime);
                setError(true);
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

    private void execProfileFunction(ProfileFunctionDescriptor pfd,
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
        setCreateStartTime(createStartTime);
        SQLTable table = col.getParentTable();


        try {
            databaseIdentifierQuoteString = conn.getMetaData().getIdentifierQuoteString();
            sql.append("SELECT 1");
            int tryCount = 0;
            if (manager.isFindingDistinctCount() && pfd.isCountDist() ) {
                sql.append(",\n COUNT(DISTINCT ");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS DISTINCTCOUNT_"+i);
                tryCount++;
            }
            if (manager.isFindingMin() && pfd.isMinValue() ) {
                sql.append(",\n MIN(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MINVALUE_"+i);
                tryCount++;
            }
            if (manager.isFindingMax() && pfd.isMaxValue() ) {
                sql.append(",\n MAX(");
                sql.append(databaseIdentifierQuoteString);
                sql.append(col.getName());
                sql.append(databaseIdentifierQuoteString);
                sql.append(") AS MAXVALUE_"+i);
                tryCount++;
            }
            if (manager.isFindingAvg() && pfd.isAvgValue() ) {
                sql.append(",\n ");
                sql.append(ddlg.getAverageSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+
                        databaseIdentifierQuoteString));
                sql.append(" AS AVGVALUE_"+i);
                tryCount++;
            }
            if (manager.isFindingMinLength() && pfd.isMinLength() ) {
                sql.append(",\n MIN(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MINLENGTH_"+i);
                tryCount++;
            }
            if (manager.isFindingMaxLength() && pfd.isMaxLength() ) {
                sql.append(",\n MAX(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS MAXLENGTH_"+i);
                tryCount++;
            }
            if (manager.isFindingAvgLength() && pfd.isAvgLength() ) {
                sql.append(",\n AVG(");
                sql.append(ddlg.getStringLengthSQLFunctionName(databaseIdentifierQuoteString+
                        col.getName()+databaseIdentifierQuoteString));
                sql.append(") AS AVGLENGTH_"+i);
                tryCount++;
            }

            if (manager.isFindingNullCount() && pfd.isSumDecode() ) {
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
                // System.err.println(lastSQL);
                rs = stmt.executeQuery(lastSQL);

                if (rs.next()) {
                    if (manager.isFindingDistinctCount() && pfd.isCountDist() ) {
                        columnName = "DISTINCTCOUNT_"+i;
                        setDistinctValueCount(rs.getInt(columnName));
                    }
                    if (manager.isFindingMin() && pfd.isMinValue() ) {
                        columnName = "MINVALUE_"+i;
                        setMinValue(rs.getObject(columnName));
                    }
                    if (manager.isFindingMax() && pfd.isMaxValue() ) {
                        columnName = "MAXVALUE_"+i;
                        setMaxValue(rs.getObject(columnName));
                    }
                    if (manager.isFindingAvg() && pfd.isAvgValue() ) {
                        columnName = "AVGVALUE_"+i;
                        setAvgValue(rs.getObject(columnName));
                    }
                    if (manager.isFindingMinLength() && pfd.isMinLength() ) {
                        columnName = "MINLENGTH_"+i;
                        setMinLength(rs.getInt(columnName));
                    }
                    if (manager.isFindingMaxLength() && pfd.isMaxLength() ) {
                        columnName = "MAXLENGTH_"+i;
                        setMaxLength(rs.getInt(columnName));
                    }
                    if (manager.isFindingAvgLength() && pfd.isAvgLength() ) {
                        columnName = "AVGLENGTH_"+i;
                        setAvgLength(rs.getDouble(columnName));
                    }

                    if (manager.isFindingNullCount() && pfd.isSumDecode() ) {
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

            if (manager.isfindingTopTen() && pfd.isCountDist() ) {
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
}
