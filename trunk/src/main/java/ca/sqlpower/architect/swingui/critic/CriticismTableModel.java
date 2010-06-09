/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.critic;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticismBucket;
import ca.sqlpower.architect.ddl.critic.CriticismEvent;
import ca.sqlpower.architect.ddl.critic.CriticismListener;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;

public class CriticismTableModel extends AbstractTableModel {
    
    private final CriticismBucket criticizer;
    
    private final CriticismListener criticListener = new CriticismListener() {
    
        public void criticismRemoved(CriticismEvent e) {
            fireTableRowsDeleted(e.getIndex(), e.getIndex());
        }
    
        public void criticismAdded(CriticismEvent e) {
            fireTableRowsInserted(e.getIndex(), e.getIndex());
        }
    };

    public CriticismTableModel(CriticismBucket criticizer) {
        this.criticizer = criticizer;
        criticizer.addCriticismListener(criticListener);
    }
    
    public int getColumnCount() {
        return 3;
    }
    
    @Override
    public String getColumnName(int column) {
        if (column == 1) {
            return "Object";
        } else if (column == 2) {
            return "Description";
        } else {
            return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Severity.class;
        } else if (columnIndex == 1) {
            return String.class;
        } else if (columnIndex == 2) {
            return String.class;
        } else {
            return null;
        }
    }

    public int getRowCount() {
        return criticizer.getCriticisms().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Criticism rowVal = criticizer.getCriticisms().get(rowIndex);
        if (columnIndex == 0) {
            return rowVal.getCritic().getSeverity();
        } else if (columnIndex == 1) {
            return rowVal.getSubject();
        } else if (columnIndex == 2) {
            return rowVal.getDescription();
        } else {
            throw new IllegalArgumentException(
                    "This table has " + getColumnCount() + " columns, and I " +
                    "was asked for column " + columnIndex);
        }
    }

}
