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
 * This is the physical name critic for Oracle name length restrictions. In the
 * future we will allow users to specify parameters for the critics so they can
 * customize them but parameters of critics is not in the current scope.
 */
public class OraclePhysicalNameCritic extends PhysicalNameCritic {

    public OraclePhysicalNameCritic() {
        super(StarterPlatformTypes.ORACLE.getName(), 
                Pattern.compile("^[a-z_][a-z0-9_]*$", Pattern.CASE_INSENSITIVE), 
                30);
    }
    
}
