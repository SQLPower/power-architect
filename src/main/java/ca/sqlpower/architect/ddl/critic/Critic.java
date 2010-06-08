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

import java.util.List;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;

/**
 * A critic is used by a {@link Criticizer} to find mistakes in a project.
 * The critic does the actual work of analyzing an object or structure of
 * the project and highlights errors by creating {@link Criticism}s.
 * <p>
 * This type of critic is a basic type. The subject to be criticized is not
 * to be traversable to other objects for safety reasons. If the children or
 * parent of the object needs to be retrieved a different type of critic should
 * be used.
 * <p>
 * Classes of this type must be immutable.
 * 
 * @param <S> The object type that will be analyzed to be criticized.
 */
public interface Critic<S> {
    
    /**
     * Analyzes the subject and returns a set of criticisms if there are
     * problems with the object. This should not change the subject in any way
     * including causing the subject to populate.
     */
    public List<Criticism<S>> criticize(S subject);

    /**
     * The error level this critic was defined to be at when it was created.
     * The severity should not change even if settings in the project change
     * as the object is immutable.
     */
    public Severity getSeverity();
    
    /**
     * Returns the name of the critic.
     */
    public String getName();
}
