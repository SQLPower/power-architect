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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.swingui.table.DateTableCellRenderer;
import ca.sqlpower.swingui.table.DecimalTableCellRenderer;
import ca.sqlpower.swingui.table.PercentTableCellRenderer;

import com.darwinsys.csv.CSVExport;

public class ProfileCSVFormat implements ProfileFormat {

    /** The desired CSV column list is published in the ProfileColumn enum.
     * @see ca.sqlpower.architect.profile.ProfileFormat#format(java.io.OutputStream, java.util.List, ca.sqlpower.architect.profile.TableProfileManager)
     */
    public void format(OutputStream nout, List<ProfileResult> profileResult) 
                                                                throws Exception {
        PrintWriter out = new PrintWriter(nout);

        // Print a header
        ProfileColumn[] columns = ProfileColumn.values();
        out.println(CSVExport.toString(Arrays.asList(columns)));

        Format dateFormat = new DateTableCellRenderer().getFormat();
        Format decFormat =  new DecimalTableCellRenderer().getFormat();
        Format pctFormat =  new PercentTableCellRenderer().getFormat();
        // Now print column profile
        for ( ProfileResult result : profileResult ) {

            if ( !(result instanceof ColumnProfileResult) )
                continue;

            SQLColumn c = (SQLColumn) result.getProfiledObject();
            SQLTable t = c.getParentTable();
            TableProfileResult tpr = ((ColumnProfileResult)result).getParentResult();
            List<Object> commonData = new ArrayList<Object>();

            for ( ProfileColumn pc : columns ) {
                switch(pc) {
                case DATABASE:
                    commonData.add(t.getParentDatabase().getName());
                    break;
                case CATALOG:
                    commonData.add(t.getCatalog() != null ? t.getCatalog().getName() : "");
                    break;
                case SCHEMA:
                    commonData.add(t.getSchema() != null ? t.getSchema().getName() : "");
                    break;
                case TABLE:
                    commonData.add(t.getName());
                    break;
                case COLUMN:
                    commonData.add(c.getName());
                    break;
                case RUNDATE:
                    Date date = new Date(tpr.getCreateStartTime());
                    commonData.add(dateFormat.format(date));
                    break;
                case RECORD_COUNT:
                    commonData.add(tpr.getRowCount());
                    break;
                case DATA_TYPE:
                    commonData.add(c.getType());
                    break;
                case NULL_COUNT:
                    commonData.add(((ColumnProfileResult) result).getNullCount());
                    break;
                case PERCENT_NULL:
                    if ( tpr.getRowCount() == 0 )
                        commonData.add("n/a");
                    else
                        commonData.add( pctFormat.format(
                            ((ColumnProfileResult) result).getNullCount() / (double)tpr.getRowCount()));
                    break;
                case UNIQUE_COUNT:
                    commonData.add(((ColumnProfileResult) result).getDistinctValueCount());
                    break;
                case PERCENT_UNIQUE:
                    if ( tpr.getRowCount() == 0 )
                        commonData.add("n/a");
                    else
                        commonData.add( pctFormat.format(
                            ((ColumnProfileResult) result).getDistinctValueCount() / (double)tpr.getRowCount()));
                    break;
                case MIN_LENGTH:
                    commonData.add(((ColumnProfileResult) result).getMinLength());
                    break;
                case MAX_LENGTH:
                    commonData.add(((ColumnProfileResult) result).getMaxLength());
                    break;
                case AVERAGE_LENGTH:
                    commonData.add(decFormat.format(((ColumnProfileResult) result).getAvgLength()));
                    break;
                case MIN_VALUE:
                    commonData.add(((ColumnProfileResult) result).getMinValue());
                    break;
                case MAX_VALUE:
                    commonData.add(((ColumnProfileResult) result).getMaxValue());
                    break;
                case AVERAGE_VALUE:

                    String formattedValue;
                    Object value = ((ColumnProfileResult) result).getAvgValue();
                    if (value == null) {
                        formattedValue = "";
                    } else if (value instanceof Number) {
                        formattedValue = decFormat.format(value);
                    } else {
                        formattedValue = value.toString();
                    }
                    commonData.add(formattedValue);
                    break;
                case TOP_VALUE:
                    commonData.add("");
                    break;
                default:
                    throw new IllegalStateException("Need code to handle this column!");

                }
            }
            out.println(CSVExport.toString(commonData));
        }
        out.close();
    }

}
