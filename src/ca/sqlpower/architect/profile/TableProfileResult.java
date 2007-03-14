package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;

public class TableProfileResult extends AbstractProfileResult<SQLTable> {

    private static final Logger logger = Logger.getLogger(TableProfileResult.class);

    private int rowCount;
    private List<ColumnProfileResult> columnProfileResults = new ArrayList<ColumnProfileResult>();

    /**
     * The profile manager that "owns" this profile result.
     */
    private ProfileManager manager;

    private int progress;
    
    public TableProfileResult(SQLTable profiledObject, ProfileManager manager) {
        super(profiledObject);
        this.manager = manager;
    }

    public int getRowCount() {
        return rowCount;
    }

    final static Date date = new Date();
    final static DateFormat df = DateFormat.getDateTimeInstance();

    /**
     * This printf format string is used in our toString() but is also
     * made public for use in UI controls that need an approximation
     * of the format for e.g., sizing a JLabel or other text component
     */
    public static final String TOSTRING_FORMAT = "Rows: %d   %s   Time:  %d ms";

    @Override
    public String toString() {
        date.setTime(getCreateStartTime());
        return String.format(TOSTRING_FORMAT, rowCount, df.format(date), getTimeToCreate());
    }

    public void doProfile() throws SQLException, ArchitectException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            SQLTable table = getProfiledObject();
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

            rs = stmt.executeQuery(lastSQL);

            if ( rs.next() ) {
                rowCount = rs.getInt("ROW__COUNT");
            }
            
            rs.close();
            conn.close();
            
            List<SQLColumn> columns = table.getColumns();
            if ( columns.size() == 0 ) {
                return;
            }
            DDLGenerator ddlg = getDDLGenerator(columns.get(0));
            for (SQLColumn col : columns ) {
                ColumnProfileResult columnResult = new ColumnProfileResult(col, manager, ddlg, this);
                columnResult.populate();
                columnProfileResults.add(columnResult);
                ++progress;
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

    /* Return null instead of the expected real jobsize so that the
     * progress bar will go into indeterminate mode which seems to make
     * it start sooner & move more often than per-column increment of progress.
     * @see ca.sqlpower.architect.profile.AbstractProfileResult#getJobSize()
     */
    @Override
    public synchronized Integer getJobSize() {
        return null;
    }
    @Override
    public synchronized int getProgress() {
        return progress;
    }
    /**
     * Returns an unmodifiable list of columnProfileResults that
     * belong to this table.
     */
    public List<ColumnProfileResult> getColumnProfileResults() {
        return Collections.unmodifiableList(columnProfileResults);
    }

    /**
     * Returns a collection of column profile results associated with this 
     * table. These profile results will probably differ by the
     * date that they were created. If there are no results found for the
     * given table, an empty collection will be returned.
     */
    public Collection<ColumnProfileResult> getColumnProfileResult(SQLColumn c) {
        Collection<ColumnProfileResult> retCollection = new ArrayList<ColumnProfileResult>();
        for (ColumnProfileResult result : columnProfileResults) {
            if (c == result.getProfiledObject()) {
                retCollection.add(result);
            }
        }
        
        return retCollection;
    }
    
    private DDLGenerator getDDLGenerator(SQLColumn col1) throws ArchitectException {
        DDLGenerator ddlg = null;

        try {
            ddlg = (DDLGenerator) DDLUtils.createDDLGenerator(
                    col1.getParentTable().getParentDatabase().getDataSource());
        } catch (InstantiationException e1) {
            throw new ArchitectException("problem running Profile Manager", e1);
        } catch ( IllegalAccessException e1 ) {
            throw new ArchitectException("problem running Profile Manager", e1);
        }
        return ddlg;
    }

    /**
     * Add a new column profile result to the end of the result list
     */
    public void addColumnProfileResult(ColumnProfileResult profileResult) {
        columnProfileResults.add(profileResult);
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    
}
