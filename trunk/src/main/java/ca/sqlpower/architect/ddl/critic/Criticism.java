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

import java.util.Arrays;
import java.util.List;

/**
 * If the enabled {@link Critic}s in the system find problems with the project
 * it will create criticisms. The criticisms can be used by users to help
 * understand errors in their model and do quick fixes for them as well.
 * 
 * @param <S>
 *            The object type this criticism can be found on.
 */
public class Criticism<S> {

    private final S subject;
    private final String description;
    private final QuickFix[] fixes;

    public Criticism(S subject, String description, QuickFix ... fixes) {
        this.subject = subject;
        this.description = description;
        this.fixes = fixes;
    }
    
    public S getSubject() {
        return subject;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<QuickFix> getFixes() {
        return Arrays.asList(fixes);
    }
}
