package ca.sqlpower.architect.test;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.sql.DBCSSource;
import ca.sqlpower.sql.XMLFileDBCSSource;

import ca.sqlpower.architect.*;

public class TestDB {

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: TestDB xmlFile dbname");
			System.exit(1);
		}
		DBCSSource dbcsSource = new XMLFileDBCSSource(args[0]);
		DBConnectionSpec spec = DBConnectionSpec.searchListForName(dbcsSource.getDBCSList(), args[1]);
		SQLDatabase db = new SQLDatabase(spec);
		System.out.println(db.getTables());
	}
}
