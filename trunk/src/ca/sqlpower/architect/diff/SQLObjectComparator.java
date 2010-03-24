/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.architect.diff;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

import ca.sqlpower.sqlobject.SQLObject;

public class SQLObjectComparator implements Comparator<SQLObject>, Serializable {

    /**
     * Compares two SQLObjects case-insensitively by name only. Case comparison
     * is performed with respect to the current default locale. No subtype
     * checking is performed, so (wlog) a table and a relationship with the same
     * name will compare equal to each other.
     * <p>
     * Null values are allowed for either side of the comparison, and are
     * considered to come before non-null values. Nulls are taken as equal to
     * each other.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
            value={"ES_COMPARING_PARAMETER_STRING_WITH_EQ"},
            justification="It's just a pre-check for null==null or reference equality by luck. " +
                          "Falls back to String.compareTo() at the end of the method.") 
	public int compare(SQLObject t1, SQLObject t2) {
		// if t1 and t2 refer to the same object, or are both null, then they're equal		
		if (t1 == t2) return 0;
		else if (t1 == null) return -1;
		else if (t2 == null) return 1;
		else {
            //TODO In version 2.0 we want this to be an option
		    String n1;
		    String n2;
		    if (t1.getPhysicalName() == null || t1.getPhysicalName().trim().equals("")) {
		        n1 = t1.getName();
		    } else {
		        n1 = t1.getPhysicalName();
		    }
		    if (t2.getPhysicalName() == null || t2.getPhysicalName().trim().equals("")) {
                n2 = t2.getName();
            } else {
                n2 = t2.getPhysicalName();
            }
            if (n1 != null) n1 = n1.toLowerCase(Locale.getDefault());
            if (n2 != null) n2 = n2.toLowerCase(Locale.getDefault());
            if (n1 == n2) return 0;
			else if (n1 == null) return -1;
			else if (n2 == null) return 1;
			else return n1.compareTo(n2);
		}
	}
}
