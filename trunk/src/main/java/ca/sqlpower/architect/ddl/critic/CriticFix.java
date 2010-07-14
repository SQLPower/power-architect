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

/**
 * A quick fix is an automated way to fix a problem with the object model with
 * the click of a button.
 */
public abstract class CriticFix {
    
    public enum FixType {
        /**
         * Fixes of this type can be fixed without user intervention.
         */
        QUICK_FIX,
        /**
         * Fixes of this type display some kind of dialog the user must
         * interact with to fix the criticism.
         */
        PROMPT_FIX;
    }

    /**
     * The description of what will be done if you apply this quick fix.
     */
    private final String description;
    
    private final FixType fixType;

    /**
     * @param description
     *            The description of the quick fix should describe how the
     *            object will change. The data of the object to change can be
     *            used to show the user exactly how the quick fix will make the
     *            change.
     */
    public CriticFix(String description, FixType fixType) {
        this.description = description;
        this.fixType = fixType;
    }

    /**
     * Calling this method will change the object model as described by the
     * description to correct the criticism it is associated with.
     */
    public abstract void apply();
    
    public String getDescription() {
        return description;
    }

    public FixType getFixType() {
        return fixType;
    }
}
