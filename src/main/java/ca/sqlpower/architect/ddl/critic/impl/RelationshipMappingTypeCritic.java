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
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;

/**
 * Critic that checks for relationships that do not map any columns between
 * tables or an invalid column type is mapped (ie integer to varchar).
 */
public class RelationshipMappingTypeCritic extends CriticAndSettings {
    
    public RelationshipMappingTypeCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), Messages.getString("RelationshipMappingTypeCritic.name"));
    }

    public List<Criticism> criticize(Object so) {
        if (!(so instanceof ColumnMapping)) return Collections.emptyList();
        ColumnMapping cm = (ColumnMapping) so;
        List<Criticism> criticisms = new ArrayList<Criticism>();
        if (ArchitectUtils.columnsDiffer(cm.getFkColumn(), cm.getPkColumn())) {
            final SQLColumn parentColumn = cm.getPkColumn();
            final SQLTable parentTable = parentColumn.getParent();
            final SQLColumn childColumn = cm.getFkColumn();
            final SQLTable childTable = childColumn.getParent();
            
            //The text of the quick fix to update the child to have the same type, precision, and scale as the parent.
            final UserDefinedSQLType parentSQLType = parentColumn.getUserDefinedSQLType();
            String updateToParentQuickFix = "Change type of " + childTable.getName() + "." + 
                childColumn.getName() + " (child column) to " + parentSQLType.getUpstreamType().getName() + 
                (parentSQLType.getPrecisionType(UserDefinedSQLType.GENERIC_PLATFORM).equals(PropertyType.NOT_APPLICABLE)? "" :"(" + 
                        parentSQLType.getPrecision(UserDefinedSQLType.GENERIC_PLATFORM) + 
                            (parentSQLType.getScaleType(UserDefinedSQLType.GENERIC_PLATFORM).equals(PropertyType.NOT_APPLICABLE)? "" : ", " + 
                                    parentSQLType.getScale(UserDefinedSQLType.GENERIC_PLATFORM)) + ")");
            
          //The text of the quick fix to update the parent to have the same type, precision, and scale as the child.
            final UserDefinedSQLType childSQLType = childColumn.getUserDefinedSQLType();
            String updateToChildQuickFix = "Change type of " + parentTable.getName() + "." + 
                parentColumn.getName() + " (parent column) to " + childSQLType.getUpstreamType().getName() + 
                (childSQLType.getPrecisionType(UserDefinedSQLType.GENERIC_PLATFORM).equals(PropertyType.NOT_APPLICABLE)? "" :"(" + 
                        childSQLType.getPrecision(UserDefinedSQLType.GENERIC_PLATFORM) + 
                            (childSQLType.getScaleType(UserDefinedSQLType.GENERIC_PLATFORM).equals(PropertyType.NOT_APPLICABLE)? "" : ", " + 
                                    childSQLType.getScale(UserDefinedSQLType.GENERIC_PLATFORM)) + ")");
            
            criticisms.add(new Criticism(
                    cm.getParent(),
                    "Columns " + cm.getPkColumn().getShortDisplayName() + " and " + 
                        cm.getFkColumn().getShortDisplayName() + " related by FK constraint " +
                    		"have different types, scale, or precision",
                    this,
                    new CriticFix(updateToParentQuickFix, FixType.QUICK_FIX) {
                        @Override
                        public void apply() {
                            UserDefinedSQLType typeToUpdate = childColumn.getUserDefinedSQLType();
                            UserDefinedSQLType typeToMatch = parentSQLType;
                            typeToUpdate.setUpstreamType(typeToMatch.getUpstreamType());
                            childColumn.setType(parentColumn.getType());
                            childColumn.setPrecision(parentColumn.getPrecision());
                            childColumn.setScale(parentColumn.getScale());
                        }
                    },
                    new CriticFix(updateToChildQuickFix, FixType.QUICK_FIX) {
                        @Override
                        public void apply() {
                            UserDefinedSQLType typeToUpdate = parentColumn.getUserDefinedSQLType();
                            UserDefinedSQLType typeToMatch = childColumn.getUserDefinedSQLType();
                            typeToUpdate.setUpstreamType(typeToMatch.getUpstreamType());
                            parentColumn.setType(childColumn.getType());
                            parentColumn.setPrecision(childColumn.getPrecision());
                            parentColumn.setScale(childColumn.getScale());
                        }
                    }
            ));
        }
        return criticisms;
    }
}
