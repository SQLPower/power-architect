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

package ca.sqlpower.architect.swingui.critic;

import java.util.Comparator;
import java.util.Locale;

/**
 * Compares two objects by name. Normally used by the criticism UI but could
 * really be used anywhere.
 */
public class CriticismObjectComparator implements Comparator<Object> {

    public int compare(Object t1, Object t2) {
     // if t1 and t2 refer to the same object, or are both null, then they're equal      
        if (t1 == t2) {
            return 0;
        } else if (t1 == null) {
            return -1;
        } else if (t2 == null) {
            return 1;
        } else {
            String n1 = CriticismObjectRenderer.getVisibleText(t1);
            String n2 = CriticismObjectRenderer.getVisibleText(t2);
            if (n1 != null) n1 = n1.toLowerCase(Locale.getDefault());
            if (n2 != null) n2 = n2.toLowerCase(Locale.getDefault());
            if (n1 == n2) return 0;
            else if (n1 == null) return -1;
            else if (n2 == null) return 1;
            else return n1.compareTo(n2);
        }
    }

}
