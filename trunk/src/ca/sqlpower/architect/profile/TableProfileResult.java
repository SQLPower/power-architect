package ca.sqlpower.architect.profile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;

public class TableProfileResult extends AbstractProfileResult<SQLTable> {

    private static final Logger logger = Logger.getLogger(TableProfileResult.class);

    private int rowCount;

    public TableProfileResult(SQLTable profiledObject) {
        super(profiledObject);
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public String toString() {
        return "RowCount:" + rowCount +
                "   Run Date:[" + new Date(getCreateStartTime()) + "]" +
                "   Time To Create:" + getTimeToCreate() + "ms";
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
            setRowCount(rs.getInt("ROW__COUNT"));
        }

        rs.close();
        rs = null;

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
}
