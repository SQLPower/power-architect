/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows objects to be added to it,
 * and checks at each addition if the entire list
 * fits a pattern that repeats at least once.
 * Objects can be continually added, and this object
 * may go back and forth between a valid repeating pattern.
 *
 * @param <T>
 */
public class PatternFinder<T> {

    /**
     * This stores all the items in the list that have been
     * checked and determined to either have a pattern or not
     * (as dictated by the pattern field)
     */
    List<T> checkedSequence = new ArrayList<T>();
    
    /**
     * This is a list of objects that have been recently added,
     * and could potentially match an existing pattern in the list,
     * or be the first repeating term of a pattern.
     */
    List<T> uncheckedSequence = new ArrayList<T>();
    
    /**
     * This stores the largest list of items that the
     * overall list repeats at least once. It is empty
     * if the overall list has no repeating pattern.
     */
    List<T> pattern = new ArrayList<T>();
    
    boolean redundancyCheck = true;
    
    public boolean newItem(T item) {              
        
        /**
         * This list is the shortest non-repeating sequence, which is
         * either the determined repeating pattern sequence, or the entire
         * checkedSequence in the event of no repeating pattern.
         */
        List<T> compareAgainst;
        
        if (pattern.size() > 0) {
            compareAgainst = pattern;
        } else if (checkedSequence.size() > 0) {
            compareAgainst = checkedSequence;
        } else {
            checkedSequence.add(item);
            return false;            
        }

        if (!item.equals(compareAgainst.get(uncheckedSequence.size()))) {
            // There is no pattern. Forfeit this possiblePattern
            // by moving it all to the non-patterned sequence
            checkedSequence.addAll(uncheckedSequence);
            checkedSequence.add(item);
            uncheckedSequence.clear();
            pattern.clear();
            return false;
        } else {
            uncheckedSequence.add(item);
            if (compareAgainst.size() == uncheckedSequence.size()) {
                // This is a double-check, and should always be true at this point.
                if (!redundancyCheck || compareAgainst.equals(uncheckedSequence)) {
                    checkedSequence.addAll(uncheckedSequence);                    
                    if (pattern.size() == 0) {
                        pattern.addAll(uncheckedSequence);
                        System.out.println(pattern);
                    }
                    uncheckedSequence.clear();
                    return true;
                }
            }
            return isRepeatingPattern();
        }        
    }
    
    public boolean isRepeatingPattern() {
        return pattern.size() > 0;
    }

    public List<T> getPattern() {
        return pattern;
    }
    
}
