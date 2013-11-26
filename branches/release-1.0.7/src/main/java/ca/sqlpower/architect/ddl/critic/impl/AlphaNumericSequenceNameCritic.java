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
import java.util.regex.Pattern;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.sqlobject.SQLColumn;

/**
 * Same as the {@link AlphaNumericNameCritic} but works on the sequence name
 * property of a column.
 */
public class AlphaNumericSequenceNameCritic extends CriticAndSettings {

    private final Pattern pattern = Pattern.compile("^[a-z_][a-z0-9_]*$", Pattern.CASE_INSENSITIVE); 
    
    public AlphaNumericSequenceNameCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), Messages.getString("AlphaNumericSequenceNameCritic.name"));
    }

    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof SQLColumn)) return Collections.emptyList();
        
        final SQLColumn so = (SQLColumn) subject;
        String physName = so.getAutoIncrementSequenceName();
        
        if (physName == null) return Collections.emptyList();
        
        List<Criticism> criticisms = new ArrayList<Criticism>();
        if (!pattern.matcher(physName).matches()) {
            criticisms.add(new Criticism(
                    so,
                    Messages.getString("AlphaNumericSequenceNameCritic.quickFixMessage", so.getName()),
                    this
                    ));
        }
        return criticisms;
    }

}
