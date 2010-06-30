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

import java.util.regex.Pattern;

/**
 * A generic critic that creates criticisms if the name of an object is not made
 * of alpha-numeric characters.
 */
public class AlphaNumericNameCritic extends PhysicalNameCritic {

    public AlphaNumericNameCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), 
                Pattern.compile("^[a-z_][a-z0-9_]*$", Pattern.CASE_INSENSITIVE), 
                Integer.MAX_VALUE);
        setName(Messages.getString("AlphaNumericNameCritic.name"));
    }
    
}
