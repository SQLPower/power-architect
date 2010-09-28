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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

/**
 * A critic about relationship update or delete rule that tries to set default
 * on a column with no default value. Some database platforms consider this an error,
 * and others ignore it. In either case, we treat it as a mistake in the data model
 * that the user should rectify.
 * <p>
 * There is no quick fix for this one, because we can't guess what a suitable default
 * value would be.
 */
public class SetDefaultOnColumnWithNoDefaultCritic extends CriticAndSettings {

    public SetDefaultOnColumnWithNoDefaultCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), 
                Messages.getString("SetDefaultOnColumnWithNoDefaultCritic.name"));
    }

    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof ColumnMapping)) return Collections.emptyList();
        
        List<Criticism> criticisms = new ArrayList<Criticism>();
        ColumnMapping cm = (ColumnMapping) subject;
        UpdateDeleteRule deleteRule = cm.getParent().getDeleteRule();
        SQLColumn fkcol = cm.getFkColumn();
        if (deleteRule == UpdateDeleteRule.SET_DEFAULT &&
                (fkcol.getDefaultValue() == null || fkcol.getDefaultValue().length() == 0)) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("SetDefaultOnColumnWithNoDefaultCritic.deleteRuleCriticism", 
                            cm.getParent().getName(), fkcol.getName()), this));
        }
        
        UpdateDeleteRule updateRule = cm.getParent().getUpdateRule();
        if (updateRule == UpdateDeleteRule.SET_DEFAULT &&
                (fkcol.getDefaultValue() == null || fkcol.getDefaultValue().length() == 0)) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("SetDefaultOnColumnWithNoDefaultCritic.updateRuleCriticism",
                            cm.getParent().getName(), fkcol.getName()), this));
        }
        
        return criticisms;
    }

}
