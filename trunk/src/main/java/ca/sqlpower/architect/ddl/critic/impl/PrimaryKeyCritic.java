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

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticSettings.Severity;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Critic to ensure the primary key is not empty.
 */
public class PrimaryKeyCritic extends AbstractCritic<SQLObject> {
    
    public PrimaryKeyCritic(Severity severity) {
        super(severity);
    }

    public List<Criticism<SQLObject>> criticize(final SQLObject so) {
        if (!(so instanceof SQLTable)) return Collections.emptyList();
        SQLTable t = (SQLTable) so;
        List<Criticism<SQLObject>> criticisms = new ArrayList<Criticism<SQLObject>>();
        if (t.getPkSize() == 0) {
            criticisms.add(new Criticism<SQLObject>(t, "Table has no primary key defined", this));
        }
        return criticisms;
    }

    public String getName() {
        return "Non-empty primary key";
    }
}
