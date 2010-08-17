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

package ca.sqlpower.architect.enterprise;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLTypeSnapshot;

/**
 * This {@link SPListener} listens for newly added
 * {@link UserDefinedSQLTypeSnapshot}s on an {@link ArchitectProject}. When a
 * {@link UserDefinedSQLTypeSnapshot} is added, all the
 * {@link UserDefinedSQLType}s upstream type previously referencing the
 * snapshot's original UUID needs to be updated to the snapshot's
 * {@link UserDefinedSQLType} UUID.
 */
public class UpstreamTypeUpdaterListener extends AbstractPoolingSPListener {

    /**
     * The {@link ArchitectSession} where its workspace's
     * {@link UserDefinedSQLTypeSnapshot}s and the references to the
     * {@link UserDefinedSQLType} upstream types need to be updated.
     */
    private final ArchitectSession session;

    /**
     * Creates a new {@link UpstreamTypeUpdaterListener}.
     * 
     * @param session
     *            The {@link ArchitectSession} where its workspace's
     *            {@link UserDefinedSQLTypeSnapshot}s and the references to
     *            {@link UserDefinedSQLType} upstream types need to be updated.
     */
    public UpstreamTypeUpdaterListener(ArchitectSession session) {
        this.session = session;
    }
    
    @Override
    protected void childAddedImpl(SPChildEvent evt) {
        if (evt.getChild() instanceof UserDefinedSQLTypeSnapshot) {
            try {
                UserDefinedSQLTypeSnapshot snapshot = (UserDefinedSQLTypeSnapshot) evt.getChild();
                if (!snapshot.isDomainSnapshot()) {
                    String originalUUID = snapshot.getOriginalUUID();
                    List<UserDefinedSQLType> types = new ArrayList<UserDefinedSQLType>();
                    SQLDatabase targetDatabase = session.getWorkspace().getTargetDatabase();
                    SQLObjectUtils.findDescendentsByClass(
                            targetDatabase, 
                            UserDefinedSQLType.class, 
                            types);
                    for (UserDefinedSQLType type : types) {
                        if (type.getUpstreamType() != null && 
                                type.getUpstreamType().getUUID().equals(originalUUID)) {
                            type.setUpstreamType(snapshot.getSPObject());
                        }
                    }
                }
            } catch (SQLObjectException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}
