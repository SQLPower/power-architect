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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.util.reservoir.BasicReservoir;
import ca.sqlpower.util.reservoir.JDBCReserviorDataSource;
import ca.sqlpower.util.reservoir.Reservoir;
import ca.sqlpower.util.reservoir.ReservoirDataException;

/**
 * A profile creator that takes a statistical sample of the rows in the table
 * and then performs the profiling operation on that sample. The profiling is
 * done inside the local JVM, which means that a known set of aggregates will be
 * computed for each data type, regardless of the remote database platform's
 * support for aggregating different data types. For example, this profiler will
 * always calculate the average date of a date column.
 * <p>
 * Unless the network connection to the remote database is very slow, expect
 * this profile creator to be much faster than the
 * {@link RemoteDatabaseProfileCreator}, especially on large tables.
 */
public class LocalReservoirProfileCreator extends AbstractTableProfileCreator {

    private static final Logger logger = Logger.getLogger(LocalReservoirProfileCreator.class);
    
    /**
     * The settings for this profile creator.
     */
    private final ProfileSettings settings;

    /**
     * The number of rows that the reservoir should keep for us to profile.
     */
    private int sampleSize = 50000;

    public LocalReservoirProfileCreator(ProfileSettings settings) {
        if (settings == null) {
            throw new NullPointerException("Null settings");
        }
        this.settings = settings;
    }
    
    @Override
    protected boolean doProfileImpl(TableProfileResult tpr) 
    throws ReservoirDataException, SQLException, SQLObjectException {
        Connection con = null;
        Object[][] sample = null;
        SQLTable table = tpr.getProfiledObject();

        MonitorableImpl pm = (MonitorableImpl) tpr.getProgressMonitor();
        pm.setJobSize(table.getColumns().size() + 1);
        pm.setProgress(1);

        try {
            con = table.getParentDatabase().getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            boolean first = true;
            for (SQLColumn col : table.getColumns()) {
                if (!first) sql.append(", ");
                sql.append(col.getName());
                first = false;
            }
            sql.append(" FROM ").append(DDLUtils.toQualifiedName(table));
            
            logger.debug("About to execute profiling query: " + sql);
            try {
                JDBCReserviorDataSource ds = new JDBCReserviorDataSource(con, sql.toString());
                Reservoir<Object[]> r = new BasicReservoir<Object[]>();
                sample = r.getSample(ds, sampleSize);
                tpr.setRowCount(ds.getRowCount());
            } catch (Throwable ex) {
                logger.error("something bad happened", ex);
                throw new RuntimeException(ex);
            }
            
            logger.debug("Finished sampling result set. Row count=" + tpr.getRowCount() + "; sample size = " + sample.length);
            
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                logger.error("Failed to close connection. Squishing this exception: ", ex);
            }
        }
        
        // now the columns (notice we have already released the connection because it's no longer required)
        for (SQLColumn col : table.getColumns()) {
            tpr.addColumnProfileResult(new ColumnProfileResult(col));
        }
        profileColumnsFromSample(tpr, sample, pm);
        return true;
    }

    private void profileColumnsFromSample(TableProfileResult tpr, Object[][] sample, MonitorableImpl pm) {
        // Profile each column in a separate pass so we only have to track one set
        // of "top n values" at a time.
        if (sample.length > 0) {
            for (int col = 0; col < sample[0].length; col++) {
                pm.setProgress(col + 1);
                ColumnProfileResult cpr = tpr.getColumnProfileResults().get(col);
                cpr.setCreateStartTime(System.currentTimeMillis());
                
                // this map is for counting distinct values as well as finding the "top n" values
                Map<Object, Integer> valueCounts = new HashMap<Object, Integer>();
                
                // for calculating the average value (nulls count as 0)
                double sum = 0.0;
    
                // for calculating the average length, based on string length (nulls count as 0)
                double lengthSum = 0.0;
                
                int minLength = 0;
                int maxLength = 0;
                
                Comparable minValue = null;
                Comparable maxValue = null;
                
                int nullCount = 0;
                
                for (int row = 0; row < sample.length; row++) {
                    Object val = sample[row][col];
                    String sval = (val == null ? null : String.valueOf(val));
                    
                    Integer oldCount = valueCounts.get(val);
                    if (oldCount == null) oldCount = new Integer(0);
                    valueCounts.put(val, oldCount + 1);
                    
                    if (val == null) {
                        nullCount++;
                    } else if (val instanceof Number) {
                        sum += ((Number) val).doubleValue();
                    }
                    
                    if (sval != null) {
                        lengthSum += sval.length();
                    }
                    
                    if (val instanceof Comparable) {
                        Comparable cval = (Comparable) val;
                        if (minValue == null || (cval.compareTo(minValue) < 0)) {
                            minValue = cval;
                        }
                        if (maxValue == null || (cval.compareTo(maxValue) > 0)) {
                            maxValue = cval;
                        }
                    }
                }
                
                // TODO: scale results by the ratio of the sample size to the total row count
                // (the actual row count is in tpr.getRowCount())
                
                cpr.setAvgLength(lengthSum / ((double) sample.length));
                cpr.setAvgValue(sum / ((double) sample.length));
                cpr.setDistinctValueCount(valueCounts.size());
                cpr.setMaxLength(maxLength);
                cpr.setMaxValue(maxValue);
                cpr.setMinLength(minLength);
                cpr.setMinValue(minValue);
                cpr.setNullCount(nullCount);
                
                List<Map.Entry<Object, Integer>> topNList = new ArrayList<Entry<Object,Integer>>(valueCounts.entrySet());
                Collections.sort(topNList, new TopNValuesComparator());
                
                int sumOfTopNCount = 0;
                for (int i = 0; i < settings.getTopNCount() && i < topNList.size(); i++) {
                    Entry<Object, Integer> entry = topNList.get(i);
                    cpr.addValueCount(entry.getKey(), entry.getValue().intValue());
                    sumOfTopNCount += entry.getValue().intValue();
                }
                
                cpr.addValueCount(ColumnValueCount.OTHER_VALUE_OBJECT, sample.length - sumOfTopNCount);
                
                cpr.setCreateEndTime(System.currentTimeMillis());
    
            }
        }
    }

    /**
     * Compares the two map entries by their value.  This allows us to sort a list
     * of entries by frequency of occurrence.
     */
    class TopNValuesComparator implements Comparator<Map.Entry<Object, Integer>> {

        public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
        
    }
    
    @Override
    public String toString() {
        return "Local Reservoir";
    }
}
