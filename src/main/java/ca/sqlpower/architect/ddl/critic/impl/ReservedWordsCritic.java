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

package ca.sqlpower.architect.ddl.critic.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.SQLPowerUtils;

/**
 * Checks the names of tables, columns, indices, and relationships for reserved
 * words being used in the names. If the name is found to be a reserved word a
 * criticism will be created.
 */
public abstract class ReservedWordsCritic extends CriticAndSettings {

    /**
     * The words that if an object's name matches a criticism will be made for
     * the object.
     */
    private final Collection<String> reservedWords;

    public ReservedWordsCritic(String platformType, String name, Collection<String> reservedWords) {
        super(platformType, name);
        this.reservedWords = reservedWords;
    }

    public List<Criticism> criticize(Object subject) {
        if (subject instanceof SQLTable || subject instanceof SQLIndex || 
                subject instanceof SQLRelationship || subject instanceof SQLColumn) {
            final SQLObject sqlObject = (SQLObject) subject;
            String typeName = sqlObject.getClass().getSimpleName().substring(3);
            if (reservedWords.contains(sqlObject.getPhysicalName().toUpperCase())) {
                String name = null;
                SQLDatabase parentDb = SQLPowerUtils.getAncestor(sqlObject, SQLDatabase.class);
                JDBCDataSourceType dsType = parentDb.getDataSource().getParentType();

                if (dsType!=null && (dsType.getSupportsQuotingName()) ) {
                    //quoting is used when user don't want to change physical name (e. name of Column)
                    // if dsType supports quoting then don't change physical name 
                    // SQLDDLGenerating will apply will apply a '[]' for SQLServer and
                    // PostgreSQLDDL Generator will apply a quotes ("") around physical name
                } else {
                    name =  sqlObject.getPhysicalName() + "_1";
                }

                final String newName = name;
                if (newName!= null) {
                    return Collections.singletonList(new Criticism(subject, 
                            Messages.getString("ReservedWordsCritic.criticismDesc", 
                                    Messages.getString(typeName), 
                                    sqlObject.getPhysicalName() + " (" + sqlObject.getName() + ")"), 
                                    this, 
                                    new CriticFix(Messages.getString("ReservedWordsCritic.quickFixDesc", newName), FixType.QUICK_FIX) {
                        @Override
                        public void apply() {
                            sqlObject.setPhysicalName(newName);
                        }
                    }));
                }
            }
        }
        return Collections.emptyList();
    }

}
