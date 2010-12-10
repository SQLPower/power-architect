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

public class SQLServerUnsupportedFeaturesCritic extends CriticAndSettings {

    public SQLServerUnsupportedFeaturesCritic() {
        super(StarterPlatformTypes.SQL_SERVER.getName(), 
                Messages.getString("UnsupportedFeaturesCritic.name", StarterPlatformTypes.SQL_SERVER.getName()));
    }

    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof SQLRelationship)) return Collections.emptyList();
        
        List<Criticism> criticisms = new ArrayList<Criticism>();
        SQLRelationship r = (SQLRelationship) subject;
        
        if (r.getDeferrability() != Deferrability.NOT_DEFERRABLE) {
            criticisms.add(new Criticism(subject, 
                    Messages.getString("UnsupportedFeaturesCritic.deferrabilityRuleNotSupported", getPlatformType(), r.getName()), this));
        }
        
        return criticisms;
    }

}
