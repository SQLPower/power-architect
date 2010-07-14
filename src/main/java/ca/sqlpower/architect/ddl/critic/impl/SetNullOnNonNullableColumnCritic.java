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

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

/**
 * A critic about relationship update or delete rule that tries to set a
 * non-nullable column to null. Some database platforms consider this an error,
 * and others ignore it. In either case, we treat it as a mistake in the data
 * model that the user should rectify.
 */
public class SetNullOnNonNullableColumnCritic extends CriticAndSettings {

    public SetNullOnNonNullableColumnCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), Messages.getString("SetNullOnNonNullableColumnCritic.name"));
    }

    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof ColumnMapping)) return Collections.emptyList();
        
        List<Criticism> criticisms = new ArrayList<Criticism>();
        ColumnMapping cm = (ColumnMapping) subject;
        UpdateDeleteRule deleteRule = cm.getParent().getDeleteRule();
        final SQLColumn fkcol = cm.getFkColumn();
        if (deleteRule == UpdateDeleteRule.SET_NULL && !fkcol.isDefinitelyNullable()) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("SetNullOnNonNullableColumnCritic.deleteRuleCriticism", 
                            cm.getParent().getName(), fkcol.getName()), this,
                            new CriticFix(Messages.getString("SetNullOnNonNullableColumnCritic.quickFix", fkcol.getName()), FixType.QUICK_FIX) {
                                @Override
                                public void apply() {
                                    fkcol.setNullable(DatabaseMetaData.columnNullable);
                                }
                            }));
        }


        UpdateDeleteRule updateRule = cm.getParent().getUpdateRule();
        if (updateRule == UpdateDeleteRule.SET_NULL && !fkcol.isDefinitelyNullable()) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("SetNullOnNonNullableColumnCritic.updateRuleCriticism", 
                            cm.getParent().getName(), fkcol.getName()), this,
                            new CriticFix(Messages.getString("SetNullOnNonNullableColumnCritic.quickFix", fkcol.getName()), FixType.QUICK_FIX) {
                                @Override
                                public void apply() {
                                    fkcol.setNullable(DatabaseMetaData.columnNullable);
                                }
                            }));
        }
        return criticisms;
    }

}
