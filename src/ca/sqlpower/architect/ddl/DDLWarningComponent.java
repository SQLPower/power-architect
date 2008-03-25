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
package ca.sqlpower.architect.ddl;

import javax.swing.JComponent;

/**
 * A UI component that will display a DDLWarning and provide
 * the user with a GUI of some type to correct the error.
 */
public interface DDLWarningComponent {

    /**
     * Return the Runnable that will apply the changes.
     */
    public Runnable getChangeApplicator();
    /**
     * Return the associated visual component
     */
    public JComponent getComponent();
    /**
     * Return the DDLWarning object
     */
    public DDLWarning getWarning();

    /** Do something - apply the user's changes */
    public void applyChanges();
}
