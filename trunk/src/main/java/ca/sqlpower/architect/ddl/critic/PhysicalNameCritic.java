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

package ca.sqlpower.architect.ddl.critic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ca.sqlpower.sqlobject.SQLObject;

public class PhysicalNameCritic implements Critic<SQLObject> {

    private final Pattern legalNamePattern;
    private final int maxNameLength;
    private final String platformName;

    public PhysicalNameCritic(String platformName, Pattern legalNamePattern, int maxNameLength) {
        this.platformName = platformName;
        this.legalNamePattern = legalNamePattern;
        this.maxNameLength = maxNameLength;
        
    }
    
    public List<Criticism<SQLObject>> criticize(final SQLObject so) {
        String physName = so.getPhysicalName();
        
        if (physName == null) return Collections.emptyList();
        
        List<Criticism<SQLObject>> criticisms = new ArrayList<Criticism<SQLObject>>();
        if (physName.length() > maxNameLength) {
            criticisms.add(new Criticism<SQLObject>(
                    so,
                    "Physical name too long for " + platformName,
                    new QuickFix("Truncate name to " + maxNameLength + " characters") {
                        public void apply() {
                            if (so.getPhysicalName() != null && so.getPhysicalName().length() > maxNameLength) {
                                so.setPhysicalName(so.getPhysicalName().substring(maxNameLength));
                            }
                        }
                    }));
        }
        if (!legalNamePattern.matcher(physName).matches()) {
            criticisms.add(new Criticism<SQLObject>(
                    so,
                    "Physical name not legal for " + platformName
                    // TODO: need replacement pattern to enable quickfix
                    ));
        }
        
        return criticisms;
    }
}
