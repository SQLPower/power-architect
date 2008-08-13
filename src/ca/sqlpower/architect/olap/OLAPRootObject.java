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
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectSession;

/**
 * The top of the OLAP business model. This root object contains OLAPSessions, each
 * of which contains exactly one Schema object.
 */
public class OLAPRootObject extends OLAPObject {

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
    public void addChild(OLAPObject child) {
        if (child instanceof OLAPSession) {
            olapSessions.add((OLAPSession) child);
            child.setParent(this);
            fireChildAdded(OLAPSession.class, olapSessions.size(), child);
        }
    }
    
    /**
     * Returns the ArchtectSession this OLAP Root Object belongs to.
     */
    public ArchitectSession getArchitectSession() {
        return architectSession;
    }
    
    @Override
    public boolean allowsChildren() {
        return true;
    }

    @Override
    public List<OLAPSession> getChildren() {
        return Collections.unmodifiableList(olapSessions);
    }
}
