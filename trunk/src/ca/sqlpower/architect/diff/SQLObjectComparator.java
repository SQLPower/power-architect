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
			String n1 = t1.getName();
			String n2 = t2.getName();
			if (n1 == n2) return 0;
			else if (n1 == null) return -1;
			else if (n2 == null) return 1;
			else return n1.compareTo(n2);
		}
	}
}
