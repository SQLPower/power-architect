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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This bucket holds all of the current criticisms about the state of the
 * system. The bucket of current criticisms is separate from the
 * {@link Criticizer} which creates them so the creation of the
 * {@link Criticism}s is easier to done on a separate thread. Additionally, the
 * criticism bucket can decide if criticisms being added should be appended to
 * the existing list of criticisms, replace criticisms, or clear the list to
 * start over.
 */
public class CriticismBucket {

    /**
     * The collection of most recent criticisms of the objects last passed to
     * this criticizer.
     */
    private final List<Criticism> criticisms = new ArrayList<Criticism>();
    
    private final List<CriticismListener> listeners = new ArrayList<CriticismListener>();
    
    public void updateCriticismsToMatch(List<Criticism> newCriticisms) {
        clearCriticisms();
        criticisms.addAll(newCriticisms);
        int index = criticisms.size();
        for (Criticism newCriticism : newCriticisms) {
            for (int i = listeners.size() - 1; i >=0; i--) {
                listeners.get(i).criticismAdded(new CriticismEvent(newCriticism, index));
            }
            index++;
        }
    }
    
    /**
     * Removes all current criticisms from the criticizer.
     */
    private void clearCriticisms() {
        ArrayList<Criticism> oldCriticisms = new ArrayList<Criticism>(criticisms);
        criticisms.clear();
        for (int i = oldCriticisms.size() - 1; i >= 0; i--) {
            Criticism oldCriticism = oldCriticisms.get(i);
            for (int j = listeners.size() - 1; j >=0; j--) {
                listeners.get(j).criticismRemoved(new CriticismEvent(oldCriticism, i));
            }
        }
    }
    
    public List<Criticism> getCriticisms() {
        return Collections.unmodifiableList(criticisms);
    }
    
    public List<Criticism> getCriticismsByObject(Object subject) {
        List<Criticism> selectedCriticisms = new ArrayList<Criticism>();
        for (Criticism criticism : criticisms) {
            if (criticism.getSubject().equals(subject)) {
                selectedCriticisms.add(criticism);
            }
        }
        return selectedCriticisms;
    }
    
    public Collection<Object> getCriticismSubjects() {
        Set<Object> subjects = new HashSet<Object>();
        for (Criticism criticism : criticisms) {
            subjects.add(criticism.getSubject());
        }
        return subjects;
    }
    
    public void addCriticismListener(CriticismListener l) {
        listeners.add(l);
    }
    
    public void removeCriticismListener(CriticismListener l) {
        listeners.remove(l);
    }
}
