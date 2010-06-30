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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;

/**
 * A Criticizer uses a collection of critics to analyze objects and come up with
 * criticisms based on the given objects. This object is immutable and can only
 * create a new set of criticisms. The criticizer is used to define how the 
 * object and its descendants will be traversed as well as which critics will be
 * informed of objects to criticize.
 * <p>
 * Package private because classes outside of the critics do not need to know about
 * the implementation.
 */
class Criticizer {

    private final List<Critic> critics;

    public Criticizer(List<Critic> critics) {
        this.critics = Collections.unmodifiableList(new ArrayList<Critic>(critics));
    }
    
    /**
     * Runs an object through the list of active critics. This will also criticize all
     * descendants if it is an {@link SPObject}.
     */
    public List<Criticism> criticize(Object subject) {
        try {
            for (Critic c : critics) {
                c.start();
            }
            return recursivelyCriticize(subject);
        } finally {
            for (Critic c : critics) {
                c.end();
            }
        }
    }
    
    /**
     * 
     * @param root
     *            The SQLObject to criticize
     * @param criticizer
     *            The criticizer that will examine the subtree at root and
     *            accumulate criticisms about it
     * @throws SQLObjectException
     *             if the (sub)tree under root is not already populated, and an
     *             attempt to populate it fails
     */
    @SuppressWarnings("unchecked")
    private List<Criticism> recursivelyCriticize(Object root) {
        List<Criticism> criticisms = new ArrayList<Criticism>();
        
        // skip types that don't warrant criticism
        if ( (!(root instanceof SQLDatabase))) {
            for (Critic critic : critics) {
                List<Criticism> newCriticisms = critic.criticize(root);
                criticisms.addAll(newCriticisms);
                // TODO record the critic-subject combination so it can be wiped out later
            }
        }
        
        if (root instanceof SPObject) {
            for (SPObject child : (List<SPObject>) ((SPObject) root).getChildren()) {
                try {
                    if (child instanceof SQLImportedKey
                            && ((SQLTable) root).getImportedKeys().contains(child)) {
                        // skip contents of every imported keys folder, or else we will visit every relationship twice
                        continue;
                    }
                } catch (SQLObjectException e) {
                    throw new RuntimeException(e);
                }
                criticisms.addAll(recursivelyCriticize(child));
            }
        }
        return criticisms;
    }
    
}
