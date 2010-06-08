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

/**
 * A Criticizer uses a collection of critics to analyze objects and come up with
 * criticisms based on the given objects. This object is immutable and can only
 * create a new set of criticisms.
 * 
 * @param <S>
 *            The object type that can be criticized with this criticizer.
 */
public class Criticizer<S> {

    private final List<Critic<S>> critics;

    /**
     * The collection of criticisms of the objects last calculated by this criticizer.
     */
    private final List<Criticism<S>> criticisms;
    
    public Criticizer(List<Critic<S>> critics) {
        this.critics = Collections.unmodifiableList(new ArrayList<Critic<S>>(critics));
        criticisms = new ArrayList<Criticism<S>>();
    }
    
    /**
     * Runs one object through the list of active critics.
     */
    public void criticize(S subject) {
        for (Critic<S> critic : critics) {
            List<Criticism<S>> newCriticisms = critic.criticize(subject);
            criticisms.addAll(newCriticisms);
            // TODO record the critic-subject combination so it can be wiped out later
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
