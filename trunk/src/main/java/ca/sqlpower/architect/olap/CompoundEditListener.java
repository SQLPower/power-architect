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

package ca.sqlpower.architect.olap;

/**
 * Interface for listeners interested in being notified when a compound
 * edit begins or ends.
 */
public interface CompoundEditListener {

    /**
     * Called when a compound edit has begun on an OLAPObject. All property
     * change and child events from the source of this event and any of its
     * descendants should be considered part of an atomic change that ends when
     * a {@link #compoundEditEnded(OLAPObject, String)} event is received from
     * the same source.
     * 
     * @param source
     *            the object under which the following series of changes should
     *            be considered atomic.
     * @param presentationName
     *            The user-visible name for the sequence of edits.
     */
    void compoundEditStarted(CompoundEditEvent evt);

    /**
     * Called when a compound edit has ended on an OLAPObject. This method will
     * never be called more times than
     * {@link #compoundEditStarted(OLAPObject, String)} has already been called.
     * 
     * @param source
     *            the object under which the compound edit state has ended.
     */
    void compoundEditEnded(CompoundEditEvent evt);
    
}
