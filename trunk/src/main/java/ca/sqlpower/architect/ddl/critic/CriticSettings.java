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

import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;

/**
 * The settings of a specific {@link Critic}. Includes if the critic is enabled
 * and any additional settings to decide how to criticize the object model.
 */
public interface CriticSettings extends SPObject {

    /**
     * Returns the severity level of this critic.
     */
    @Accessor
    public Severity getSeverity();

    /**
     * Sets the severity level of this critic. For used to identify how
     * important the criticisms created by this critic is to users.
     */
    @Mutator
    public void setSeverity(Severity severity);
}
