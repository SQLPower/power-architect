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
import java.util.List;

public class Criticizer<S> {

    private List<Critic<S>> critics;
    private List<Criticism<S>> criticisms;

    public Criticizer(List<Critic<S>> critics) {
        this.critics = new ArrayList<Critic<S>>(critics);
        criticisms = new ArrayList<Criticism<S>>();
    }
    
    public void criticize(S subject) {
        for (Critic<S> critic : critics) {
            // TODO wipe out the criticisms we're about to replace
            List<Criticism<S>> newCriticisms = critic.criticize(subject);
            criticisms.addAll(newCriticisms);
            // TODO record the critic-subject combination so it can be wiped out later
            // TODO fire event(s) about new criticisms
        }
    }
    
    /**
     * Returns a linear view of the criticisms.
     * <p>
     * XXX decide what should dictate the order
     * 
     * @return an unmodifiable list of criticisms
     */
    public List<Criticism<S>> getCriticisms() {
        return criticisms;
    }
}
