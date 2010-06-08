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

package ca.sqlpower.architect.ddl.critic;

import ca.sqlpower.architect.ddl.critic.CriticSettings.Severity;
import ca.sqlpower.architect.ddl.critic.impl.OraclePhysicalNameCritic;
import ca.sqlpower.architect.ddl.critic.impl.PrimaryKeyCritic;
import ca.sqlpower.architect.ddl.critic.impl.RelationshipMappingTypeCritic;
import ca.sqlpower.sqlobject.SQLObject;

/**
 * Constructs critics. Simple for now but will need to get fancy. 
 */
public class CriticFactory {

    public Critic<SQLObject> createCritic(Class<? extends Critic<SQLObject>> type, Severity severity) {
        if (OraclePhysicalNameCritic.class.equals(type)) {
            return new OraclePhysicalNameCritic(severity);
        } else if (PrimaryKeyCritic.class.equals(type)) {
            return new PrimaryKeyCritic(severity);
        } else if (RelationshipMappingTypeCritic.class.equals(type)) {
            return new RelationshipMappingTypeCritic(severity);
        } else {
            throw new IllegalArgumentException("Unknown critic type " + type);
        }
    }
}
