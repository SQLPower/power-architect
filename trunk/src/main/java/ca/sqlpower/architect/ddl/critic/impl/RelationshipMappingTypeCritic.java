/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.ddl.critic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.QuickFix;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Critic that checks for relationships that do not map any columns between
 * tables or an invalid column type is mapped (ie integer to varchar).
 */
public class RelationshipMappingTypeCritic extends CriticAndSettings {
    
    public RelationshipMappingTypeCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), "Relationships map columns of different types");
    }

    /**
     * XXX This critic accesses children of the object it is criticizing. This
     * is more difficult to make safe when we move the critics to the background
     * thread.
     */
    public List<Criticism> criticize(Object so) {
        if (!(so instanceof SQLRelationship)) return Collections.emptyList();
        SQLRelationship subject = (SQLRelationship) so;
        List<Criticism> criticisms = new ArrayList<Criticism>();
        for (SQLRelationship.ColumnMapping cm : subject.getChildren(
                SQLRelationship.ColumnMapping.class)) {
            if (ArchitectUtils.columnsDiffer(cm.getFkColumn(), cm.getPkColumn())) {
                final SQLColumn parentColumn = cm.getPkColumn();
                final SQLTable parentTable = parentColumn.getParent();
                final SQLColumn childColumn = cm.getFkColumn();
                final SQLTable childTable = childColumn.getParent();
                criticisms.add(new Criticism(
                        subject,
                        "Columns related by FK constraint have different types",
                        this,
                        new QuickFix("Change type of " + childTable.getName() + "." + childColumn.getName() + " (child column) to parent's type") {
                            @Override
                            public void apply() {
                                childColumn.setType(parentColumn.getType());
                                childColumn.setPrecision(parentColumn.getPrecision());
                                childColumn.setScale(parentColumn.getScale());
                            }
                        },
                        new QuickFix("Change type of " + parentTable.getName() + "." + parentColumn.getName() + " (parent column) to child's type") {
                            @Override
                            public void apply() {
                                parentColumn.setType(childColumn.getType());
                                parentColumn.setPrecision(childColumn.getPrecision());
                                parentColumn.setScale(childColumn.getScale());
                            }
                        }
                ));
            }
        }
        return criticisms;
    }
}
