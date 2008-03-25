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

import java.util.Arrays;

import ca.sqlpower.architect.SQLObject;

/**
 * A warning class to let the user know their data model cannot be faithfully
 * represented in the target database due to feature limitations.  For example,
 * SQL Server doesn't support deferrable constraints and Oracle doesn't support
 * auto-increment columns.
 * <p>
 * Warnings of this type are not quick-fixable, and should be considered non-fatal.
 * To that end, instances of this class will always claim that they are already fixed.
 */
public class UnsupportedFeatureDDLWarning extends AbstractDDLWarning {

    /**
     * Creates a new warning about a feature of the data model that is not supported
     * in the target platform.
     * 
     * @param involvedObjects The object or objects that would need features not available
     * in the target database.
     * @param message The message to show the user which explains the limitation.
     */
    public UnsupportedFeatureDDLWarning(String message, SQLObject ... involvedObjects) {
        super(Arrays.asList(involvedObjects), message, false, null, null, null);
        setFixed(true);
    }
}
