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

public class CompoundEditEvent {

    private final OLAPObject source;
    
    private final String presentationName;

    /**
     * Creates a new compound edit event from the given source with the given
     * presentation name.
     * 
     * @param source
     *            The object that fired the event
     * @param presentationName
     *            The name for this compound edit
     */
    public CompoundEditEvent(OLAPObject source, String presentationName) {
        this.source = source;
        this.presentationName = presentationName;
    }

    /**
     * Creates a new compound edit event from the given source. The presentation
     * name will be null.
     * 
     * @param source
     *            The object that fired the event
     */
    public CompoundEditEvent(OLAPObject source) {
        this.source = source;
        presentationName = null;
    }

    public OLAPObject getSource() {
        return source;
    }
    
    public String getPresentationName() {
        return presentationName;
    }
}
