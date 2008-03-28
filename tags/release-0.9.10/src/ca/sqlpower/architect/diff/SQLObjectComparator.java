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

import java.util.Comparator;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectComparator implements Comparator<SQLObject> {

	public int compare(SQLObject t1, SQLObject t2) {
		// if t1 and t2 refer to the same object, or are both null, then they're equal		
		if (t1 == t2) return 0;
		else if (t1 == null) return -1;
		else if (t2 == null) return 1;
		else {
            //TODO In version 2.0 we want this to be an option
			String n1 = t1.getName();
			String n2 = t2.getName();
            if (n1 != null) n1 = n1.toLowerCase();
            if (n2 != null) n2 = n2.toLowerCase();
            if (n1 == n2) return 0;
			else if (n1 == null) return -1;
			else if (n2 == null) return 1;
			else return n1.compareTo(n2);
		}
	}
}
