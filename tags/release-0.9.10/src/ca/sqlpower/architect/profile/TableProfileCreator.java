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

import ca.sqlpower.util.Monitorable;

/**
 * Service Provider Interface for creating profile results related to a
 * particular database table.  Different implementations can use different
 * strategies that perform better under certain circumstances, sacrifice
 * accuracy for performance, and so on.
 */
public interface TableProfileCreator {

    /**
     * Populates the given {@link TableProfileResult} (which must not be
     * populated already) and all of its {@link ColumnProfileResult} children.
     * <p>
     * This may be a long-running operation.  Its progress can be monitored and
     * canceled via the TableProfileResult itself, which is {@link Monitorable}.
     * 
     * @param tpr The unpopulated profile result to populate.
     * @return true if the profiling of the table and its columns has completed
     * normally; false if the profiling operation was canceled before it had a
     * chance to complete.
     * @throws RuntimeException If the profiling fails for some reason other than
     * a cancel request, an appropriate exception will be thrown.
     */
    public boolean doProfile(TableProfileResult tpr);
}
