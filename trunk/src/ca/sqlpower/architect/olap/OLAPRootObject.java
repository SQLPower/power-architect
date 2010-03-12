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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.object.SPObject;

/**
 * The top of the OLAP business model. This root object contains OLAPSessions, each
 * of which contains exactly one Schema object.
 */
public class OLAPRootObject extends OLAPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    @SuppressWarnings("unchecked")
    public static List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
                Arrays.asList(OLAPSession.class)));


    /**
     * The session this OLAPRootObject belongs to. Each Architect Session should
     * have one OLAP Root Object, which can in turn have zero or more OLAP Sessions.
     */
    private final ArchitectSession architectSession;

    private final List<OLAPSession> olapSessions = new ArrayList<OLAPSession>();
    
    public OLAPRootObject(ArchitectSession session) {
        this.architectSession = session;
    }

    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof OLAPSession) {
            olapSessions.add(index, (OLAPSession) child);
            child.setParent(this);
            fireChildAdded(OLAPSession.class, child, index);
        }
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof OLAPSession) {
            return removeOLAPSession((OLAPSession) child);
        }
        return false;
    }
    
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean removeOLAPSession(OLAPSession removeChild) {
        int pos = olapSessions.indexOf(removeChild);
        if (pos != -1) {
            removeOLAPSession(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public OLAPSession removeOLAPSession(int pos) {
        OLAPSession removedItem = olapSessions.remove(pos);
        if (removedItem != null) {
            removedItem.setParent(null);
            fireChildRemoved(OLAPSession.class, removedItem, pos);
        }
        return removedItem;
    }
    
    /**
     * Returns the ArchtectSession this OLAP Root Object belongs to.
     */
    public ArchitectSession getArchitectSession() {
        return architectSession;
    }
    
    public boolean allowsChildren() {
        return true;
    }

    public List<OLAPSession> getChildren() {
        return Collections.unmodifiableList(olapSessions);
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (OLAPSession.class.equals(childType)) {
            return 0;
        } else {
            throw new IllegalArgumentException("Child type " + childType + 
                    " is not a valid child type of " + OLAPRootObject.class);        
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        //no-op
    }
}
