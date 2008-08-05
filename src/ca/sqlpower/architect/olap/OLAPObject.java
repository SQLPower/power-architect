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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class OLAPObject {

    /**
     * Helper class for registering and notifying property change listeners.
     */
    protected final PropertyChangeSupport pcs;

    /**
     * The number of compound edits that have started on this object but not yet
     * finished.
     */
    private int compoundEditDepth = 0;

    /**
     * The olap object this object belongs to. This pointer is maintained by the
     * parent's addChild method(s).
     */
    private OLAPObject parent;
    
    protected OLAPObject() {
        pcs = new PropertyChangeSupport(this);
    }
    
    public OLAPObject getParent() {
        return parent;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void startCompoundEdit(String description) {
        compoundEditDepth++;
        // TODO fire compound edit event
    }
    
    public void endCompoundEdit() {
        compoundEditDepth--;
        // TODO fire compound edit event
    }
}
