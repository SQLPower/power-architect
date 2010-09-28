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
package ca.sqlpower.architect.swingui.table;

import java.text.NumberFormat;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.profile.ColumnProfileResult;



public class FreqValueCountTableModel extends AbstractTableModel {

    private ColumnProfileResult profile;    
    
    private static final String COUNT="COUNT";
    private static final String VALUE="VALUE";
    private static final String PERCENT="PERCENT";
    
    public FreqValueCountTableModel(ColumnProfileResult profile) {
        super();
        this.profile = profile;
    }

    @Override
    public String getColumnName(int column) {
        if ( column == 0 ) {
            return COUNT;
        } else if ( column == 1 ) {
            return PERCENT;
        } else if (column == 2) {
            return VALUE;
        } else {
            throw new IllegalStateException("Unknown Column Index:"+column);
        }
    }
    
    public int getRowCount() {
        return profile==null?0:profile.getValueCount().size();
    }

    public int getColumnCount() {
        return 3;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( profile == null )
            return null;
        
        if ( columnIndex == 0 ) {
            return profile.getValueCount().get(rowIndex).getCount();
        } else if ( columnIndex == 1 ) {
            double d = profile.getValueCount().get(rowIndex).getPercent();
            if (d == -1) {
                //this is used to stop old saved files from breaking
                return "n/a";
            } else {
                NumberFormat percentFormat = NumberFormat.getPercentInstance();
                percentFormat.setMaximumFractionDigits(2);
                return percentFormat.format(d);
            }
        } else if (columnIndex == 2) {
            return profile.getValueCount().get(rowIndex).getValue();
        } else {
            throw new IllegalStateException("Unknown Column Index:"+columnIndex);
        }
    }

}
