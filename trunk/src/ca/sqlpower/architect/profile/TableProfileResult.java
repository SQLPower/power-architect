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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;

/**
 * Holds profile results that pertain to a particular table. Instances of this
 * class are normally created and populated via a ProfileManager, but it is also
 * possible to obtain one by using a TableProfileCreator directly.
 */
public class TableProfileResult extends AbstractProfileResult<SQLTable> {

    private static final Logger logger = Logger.getLogger(TableProfileResult.class);

    private int rowCount;
    
    /**
     * The "children" of this profile result: the profile results for the columns
     * of this table.
     */
    private List<ColumnProfileResult> columnProfileResults = new ArrayList<ColumnProfileResult>();

    private Monitorable progressMonitor = new MonitorableImpl();
    
    /**
     * Creates a profile result which is not yet populated.  Normally, profile results
     * are created by the ProfileManager's createProfile() or asynchCreateProfiles()
     * method, so users will not use this constructor directly.
     * <p>
     * Note that the profile result will be empty until it has been populated by a ProfileCreator
     * (also taken care of by the ProfileManager that creates this result).
     * 
     * @param profiledObject
     * @param manager
     * @param settings
     */
    public TableProfileResult(SQLTable profiledObject, ProfileSettings settings) {
        super(profiledObject);
        setSettings(settings);
    }

    /**
     * Returns the progress monitor that can be polled to track the progress
     * of this profile result being populated.  The progress monitor also
     * provides the means of canceling the population of this profile result.
     */
    public Monitorable getProgressMonitor() {
        return progressMonitor;
    }

    /**
     * Returns the number of rows in this table.  This count is not guaranteed to
     * be realistic until this result has been fully profiled.
     */
    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * This printf format string is used in our toString() but is also
     * made public for use in UI controls that need an approximation
     * of the format for e.g., sizing a JLabel or other text component
     */
    public static final String TOSTRING_FORMAT = "Rows: %d   %s   Time:  %d ms";

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date date = new Date(getCreateStartTime());
        return String.format(TOSTRING_FORMAT, rowCount, df.format(date), getTimeToCreate());
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
     * Adds a new column profile result to the end of the result list.
     */
    public void addColumnProfileResult(ColumnProfileResult profileResult) {
        columnProfileResults.add(profileResult);
    }
}
