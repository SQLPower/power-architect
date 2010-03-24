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
import ca.sqlpower.architect.ddl.critic.Criticizer;

public class CriticismTableModel extends AbstractTableModel {

    private final Criticizer<?> criticizer;
    
    public CriticismTableModel(Criticizer<?> criticizer) {
        this.criticizer = criticizer;
    }
    
    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return criticizer.getCriticisms().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Criticism<?> rowVal = criticizer.getCriticisms().get(rowIndex);
        if (columnIndex == 0) {
            return rowVal.getSubject();
        } else if (columnIndex == 1) {
            return rowVal.getDescription();
        } else {
            throw new IllegalArgumentException(
                    "This table has " + getColumnCount() + " columns, and I " +
                    "was asked for column " + columnIndex);
        }
    }

}
