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
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

public class DB2UnsupportedFeaturesCritic extends CriticAndSettings {

    public DB2UnsupportedFeaturesCritic() {
        super(StarterPlatformTypes.DB2.getName(), Messages.getString("UnsupportedFeaturesCritic.name", StarterPlatformTypes.DB2.getName()));
    }

    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof SQLRelationship)) return Collections.emptyList();
        
        SQLRelationship r = (SQLRelationship) subject;
        List<Criticism> criticisms = new ArrayList<Criticism>();
        if (r.getDeleteRule() == UpdateDeleteRule.SET_DEFAULT) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("UnsupportedFeaturesCritic.deleteRuleNotSupported", getPlatformType(), r.getName()), this));
        }
        if (r.getUpdateRule() != UpdateDeleteRule.RESTRICT
                && (r.getUpdateRule() != UpdateDeleteRule.NO_ACTION)) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("UnsupportedFeaturesCritic.updateRuleNotSupported", getPlatformType(), r.getName()), this));
        }
        if (r.getDeferrability() != Deferrability.NOT_DEFERRABLE) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("UnsupportedFeaturesCritic.deferrabilityRuleNotSupported", getPlatformType(), r.getName()), this));
        }
        return criticisms;
    }

}
