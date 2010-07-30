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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;

/**
 * Holds profile results that pertain to a particular table. Instances of this
 * class are normally created and populated via a ProfileManager, but it is also
 * possible to obtain one by using a TableProfileCreator directly.
 */
public class TableProfileResult extends AbstractProfileResult<SQLTable> {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.<Class<? extends SPObject>>singletonList(ColumnProfileResult.class);

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
    @Constructor
    public TableProfileResult(@ConstructorParameter(propertyName="profiledObject") SQLTable profiledObject, 
            @ConstructorParameter(propertyName="settings") ProfileSettings settings) {
        super(profiledObject);
        setName("New Table Profile");
        setSettings(settings);
    }

    /**
     * This is a deep-copy copy constructor. (ie: all of the descendants will be
     * copied as well). However, the progress monitor will be shared between the
     * results.
     * <p>
     * The table given to this constructor will be the table that will be the
     * profiled object. Since this is a deep copy of the table the column
     * profiles created will be made to match those in the SQLTable given. The
     * profile results will be matched by the UUID of the columns being profiled
     * to the table columns. If there are column profile results that do not
     * match the table's columns they will be removed. The existing column
     * profile results may also be rearranged as they will match the order of
     * the columns in the table.
     */
    public TableProfileResult(TableProfileResult tprToCopy, SQLTable table) throws SQLObjectException {
        super(tprToCopy, table);
        setName("New Table Profile");
        this.rowCount = tprToCopy.rowCount;
        this.progressMonitor = tprToCopy.progressMonitor;
        for (SQLColumn col : table.getColumns()) {
            for (ColumnProfileResult cpr : tprToCopy.getColumnProfileResults()) {
                if (cpr.getProfiledObject().getUUID().equals(col.getUUID())) {
                    addColumnProfileResult(new ColumnProfileResult(cpr, col));
                    break;
                }
            }
        }
    }

    /**
     * Returns the progress monitor that can be polled to track the progress
     * of this profile result being populated.  The progress monitor also
     * provides the means of canceling the population of this profile result.
     */
    @Transient @Accessor
    public Monitorable getProgressMonitor() {
        return progressMonitor;
    }

    /**
     * Returns the number of rows in this table.  This count is not guaranteed to
     * be realistic until this result has been fully profiled.
     */
    @Accessor
    public int getRowCount() {
        return rowCount;
    }

    @Mutator
    public void setRowCount(int rowCount) {
        int oldCount = this.rowCount;
        this.rowCount = rowCount;
        firePropertyChange("rowCount", oldCount, rowCount);
    }

    /**
     * This printf format string is used in our toString() but is also
     * made public for use in UI controls that need an approximation
     * of the format for e.g., sizing a JLabel or other text component
     */
    public static final String TOSTRING_FORMAT = "Rows: %d   %s   Time:  %s   Cols: %d/%d";

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date date = new Date(getCreateStartTime());
        int successfulColCount = 0;
        for (ColumnProfileResult cpr : columnProfileResults) {
            if (cpr.getException() == null) {
                successfulColCount++;
            }
        }
        return String.format(TOSTRING_FORMAT,
                rowCount, df.format(date), formatCreateTime(), successfulColCount, columnProfileResults.size());
    }
    
    /**
     * Format the time it took to create so that it displays
     * the proper time units.
     * @return String representation of the create time
     * with the proper time units.
     */
    private String formatCreateTime() {
        long time = getTimeToCreate();
        
        // when time is less than 1 ms
        if (time == 0) {
            return "0 ms";
        }
        
        // holds the values for each time unit
        int[] timeValues = new int[6];
        int i = 5;
        timeValues[i--] = (int) time % 1000;
        time /= 1000;
        timeValues[i--] = (int) time % 60;
        time /= 60;
        timeValues[i--] = (int) time % 60;
        time /= 60;
        timeValues[i--] = (int) time % 24;
        time /= 24;
        timeValues[i--] = (int) time % 365;
        time /= 365;
        timeValues[i--] = (int) time;

        StringBuilder s = new StringBuilder();
        String[] timeUnits = new String[] {"yr", "day", "hr", "min", "sec", "ms"};
        for (int j = 0; j < 6; j++) {
            if (timeValues[j] > 0) {
                s.append(timeValues[j] + " " + timeUnits[j] + " ");
            }
        }
        return s.toString();
    }

    /**
     * Returns an unmodifiable list of columnProfileResults that
     * belong to this table.
     */
    @NonProperty
    public List<ColumnProfileResult> getColumnProfileResults() {
        return Collections.unmodifiableList(columnProfileResults);
    }

    /**
     * Returns a collection of column profile results associated with this 
     * table. These profile results will probably differ by the
     * date that they were created. If there are no results found for the
     * given table, an empty collection will be returned.
     */
    @NonProperty
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
     * @throws SQLObjectException
     */
    @Transient @Accessor
    public DDLGenerator getDDLGenerator() throws SQLObjectException {
        JDBCDataSource ds = getProfiledObject().getParentDatabase().getDataSource();
        try {
            return DDLUtils.createDDLGenerator(ds);
        } catch (InstantiationException ex) {
            throw new SQLObjectException("Couldn't create DDL Generator for data source "+ds, ex);
        } catch (IllegalAccessException ex) {
            throw new SQLObjectException("Couldn't create DDL Generator for data source "+ds, ex);
        } catch (ClassNotFoundException ex) {
            throw new SQLObjectException("Couldn't create DDL Generator for data source "+ds, ex);
        }
    }

    /**
     * Adds a new column profile result to the end of the result list.
     */
    public void addColumnProfileResult(ColumnProfileResult profileResult) {
        addColumnProfileResult(profileResult, columnProfileResults.size());
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof ColumnProfileResult) {
            addColumnProfileResult((ColumnProfileResult) child, index);
        } else {
            throw new IllegalArgumentException("Cannot handle children of type " + 
                    child.getClass() + " for object " + child);
        }
    }

    private void addColumnProfileResult(ColumnProfileResult child, int index) {
        columnProfileResults.add(child);
        child.setParent(this);
        fireChildAdded(ColumnProfileResult.class, child, index);        
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof ColumnProfileResult) {
            int index = columnProfileResults.indexOf(child);
            if (columnProfileResults.remove(child)) {
                fireChildRemoved(ColumnProfileResult.class, child, index);
                child.setParent(null);
            }
        }
        return false;
    }

    @NonBound
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        List<SPObject> children = new ArrayList<SPObject>();
        children.addAll(columnProfileResults);
        return children;
    }
}
