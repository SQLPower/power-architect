/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui;

import javax.annotation.Nonnull;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLCheckConstraintContainer;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * This {@link TableModelListener} listens to changes in a
 * {@link CheckConstraintTable}'s model. If a row is added, deleted, or changed,
 * the corresponding {@link SQLCheckConstraintContainer} should modify its
 * contained {@link SQLCheckConstraint}s accordingly.
 */
public class CheckConstraintTableModelListener implements TableModelListener {

    /**
     * The {@link SQLCheckConstraintContainer} that contains
     * {@link SQLCheckConstraint}s which the {@link CheckConstraintTable} this
     * listener listens to displays.
     */
    private final SQLCheckConstraintContainer container;

    /**
     * The {@link UserDefinedSQLType} that has {@link SQLTypePhysicalProperties}
     * that contain {@link SQLCheckConstraint}s.
     */
    private final UserDefinedSQLType udt;

    /**
     * The database platform name that the {@link CheckConstraintTable} uses for
     * its {@link TableModel}.
     */
    private final String platform;

    /**
     * Creates a new {@link CheckConstraintTableModelListener} that does not
     * specify a platform.
     * 
     * @param container
     *            The {@link SQLCheckConstraintContainer} that is modelled by
     *            the listened to {@link CheckConstraintTable}.
     */
    public CheckConstraintTableModelListener(@Nonnull SQLCheckConstraintContainer container) {
        this.container = container;
        udt = null;
        platform = null;
    }
    
    public CheckConstraintTableModelListener(@Nonnull UserDefinedSQLType udt, @Nonnull String platform) {
        this.udt = udt;
        this.platform = platform;
        container = null;
    }

    public void tableChanged(TableModelEvent e) {
        DefaultTableModel model = (DefaultTableModel) e.getSource();
        int row = e.getFirstRow();
        SQLCheckConstraint constraint;

        switch (e.getType()) {
        case TableModelEvent.INSERT:
            constraint = new SQLCheckConstraint(
                    (String) model.getValueAt(row, 0), 
                    (String) model.getValueAt(row, 1));
            if (udt == null) {
                container.addCheckConstraint(constraint, row);
            } else {
                udt.addCheckConstraint(platform, constraint, row);
            }
            break;
        case TableModelEvent.DELETE:
            if (udt == null) {
                constraint = container.getCheckConstraints().get(row);
                container.removeCheckConstraint(constraint);
            } else {
                constraint = udt.getCheckConstraints(platform).get(row);
                udt.removeCheckConstraint(platform, constraint);
            }
            break;
        case TableModelEvent.UPDATE:
            final int column = e.getColumn();
            final String newName = (String) model.getValueAt(row, 0);
            final String newConstraint = (String) model.getValueAt(row, 1);
            final int rowCount = model.getRowCount();
            
            if (udt == null) {
                constraint = container.getCheckConstraints().get(row);
            } else {
                constraint = udt.getCheckConstraints(platform).get(row);
            }

            if (column != 1) {
                if (newName.equals("")) {
                    model.setValueAt(constraint.getName(), row, 0);
                } else {
                    int i;
                    for (i = 0; i < rowCount; i++) {
                        if (i != row && model.getValueAt(i, 0).equals(newName)) {
                            model.setValueAt(constraint.getName(), row, 0);
                            break;
                        }
                    }
                    if (i == rowCount) {
                        constraint.setName((String) newName);
                    }
                }
            }
            if (column != 0) {
                if (newConstraint.equals("")) {
                    model.setValueAt(constraint.getConstraint(), row, 1);
                } else {
                    int i;
                    for (i = 0; i < rowCount; i++) {
                        if (i != row && model.getValueAt(i, 1).equals(newConstraint)) {
                            model.setValueAt(constraint.getConstraint(), row, 1);
                            break;
                        }
                    }
                    if (i == rowCount) {
                        constraint.setConstraint((String) newConstraint);
                    }
                }
            }

            break;
        }
    }

    /**
     * Gets the {@link SQLCheckConstraintContainer} that contains
     * {@link SQLCheckConstraint}s which the {@link CheckConstraintTable} this
     * listener listens to displays.
     * 
     * @return The {@link SQLCheckConstraintContainer}.
     */
    public SQLCheckConstraintContainer getContainer() {
        return container;
    }

    /**
     * Gets the database platform name that the {@link CheckConstraintTable}
     * uses for its {@link TableModel}.
     * 
     * @return The database platform name.
     */
    public String getPlatform() {
        return platform;
    }
    
}
