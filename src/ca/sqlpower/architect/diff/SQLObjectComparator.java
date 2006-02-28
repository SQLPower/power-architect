package ca.sqlpower.architect.diff;

import java.util.Comparator;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectComparator implements Comparator<SQLObject> {

	public int compare(SQLObject t1, SQLObject t2) {
		if (t1 instanceof SQLObject) {
			if (t2 instanceof SQLObject) {
				if (t1 != null && t2 != null) {
					return (((SQLObject) t1).getName())
							.compareTo(((SQLObject) t2).getName());
				} else {
					// if t1 is null t2 is greater
					if (t1 == null) {
						return -1;
					} else {
						// if t2 is null t1 is greater
						return 1;
					}
				}

			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}

}
