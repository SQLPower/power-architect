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
import ca.sqlpower.sql.SPDataSource;

public class TableProfileResult extends AbstractProfileResult<SQLTable> {

    private static final Logger logger = Logger.getLogger(TableProfileResult.class);

    private int rowCount;
    private List<ColumnProfileResult> columnProfileResults = new ArrayList<ColumnProfileResult>();

    /**
     * The profile manager that "owns" this profile result.
     */
    private ProfileManager manager;

    /**
     * The progress so far. Row count is the first chunk of progress, then each column
     * counts as one more chunk.
     */
    private int progress;
    
    /**
     * Creates a profile result which is not yet populated.  Normally, profile results
     * are created by the ProfileManager's createProfile() or asynchCreateProfiles()
     * method, so users will not use this constructor directly.
     * <p>
     * Note that the profile result will be empty until its populate() method is called
     * (also taken care of by the ProfileManager that creates this result).
     * <p>
     * The only reason this method is public is because the SwingUIProject class needs
     * to create profiles directly when reading in a project file.  It would be nice
     * to come up with a better API and make this constructor protected.
     * 
     * @param profiledObject
     * @param manager
     * @param settings
     */
    public TableProfileResult(SQLTable profiledObject, ProfileManager manager, ProfileSettings settings) {
        super(profiledObject);
        this.manager = manager;
        setSettings(settings);
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

    protected void doProfile() throws SQLException, ArchitectException {
        progress = 0;
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

            progress += 1;
            
            rs = stmt.executeQuery(lastSQL);

            if ( rs.next() ) {
                rowCount = rs.getInt("ROW__COUNT");
            }
            
            List<SQLColumn> columns = table.getColumns();
            if ( columns.size() == 0 ) {
                return;
            }
            DDLGenerator ddlg = getDDLGenerator();
            for (SQLColumn col : columns ) {
                ColumnProfileResult columnResult = new ColumnProfileResult(col, manager, ddlg, this);
                columnResult.populate();
                columnProfileResults.add(columnResult);
                progress += 1;
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
     * Returns the number of columns plus 1. This allows the progress bar to
     * move before each column gets profiled, which makes it appear to be doing
     * something right away.
     */
    @Override
    public synchronized Integer getJobSize() {
        SQLTable temp = getProfiledObject();
        Integer ret = null;
        try {
            ret = new Integer(temp.getColumns().size() + 1);
        } catch (ArchitectException e) {
            throw new IllegalStateException("Failed to populate necessary columns.");
        }
        return ret;
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
    
    /**
     * Returns the DDL generator associated with this table profile.
     * 
     * @param col1
     * @return
     * @throws ArchitectException
     */
    public DDLGenerator getDDLGenerator() throws ArchitectException {
        SPDataSource ds = getProfiledObject().getParentDatabase().getDataSource();
        try {
            return DDLUtils.createDDLGenerator(ds);
        } catch (InstantiationException ex) {
            throw new ArchitectException("Couldn't create DDL Generator for data source "+ds, ex);
        } catch (IllegalAccessException ex) {
            throw new ArchitectException("Couldn't create DDL Generator for data source "+ds, ex);
        } catch (ClassNotFoundException ex) {
            throw new ArchitectException("Couldn't create DDL Generator for data source "+ds, ex);
        }
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
