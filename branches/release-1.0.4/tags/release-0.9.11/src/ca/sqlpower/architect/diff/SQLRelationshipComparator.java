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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;

public class SQLRelationshipComparator implements Comparator<SQLRelationship> {

	private static Logger logger = Logger.getLogger(SQLRelationship.class);

	SQLObjectComparator comparator = new SQLObjectComparator();

	public int compare(SQLRelationship r1, SQLRelationship r2) {
		if (r1 == r2)
			return 0;
		else if (r1 == null)
			return -1;
		else if (r2 == null)
			return 1;

		//Making sure that the PKTables are the same, else return value
		int result = comparator.compare(r1.getPkTable(), r2.getPkTable());
		if (result != 0)
			return result;
		
		//Making sure that the FKTables are the same, else return value
		result = comparator.compare(r1.getFkTable(), r2.getFkTable());
		if (result != 0)
			return result;

		Set<SQLColumn> sourceColPk = new TreeSet<SQLColumn>(comparator);
		Set<SQLColumn> targetColPk = new TreeSet<SQLColumn>(comparator);
		Set<SQLColumn> sourceColFk = new TreeSet<SQLColumn>(comparator);
		Set<SQLColumn> targetColFk = new TreeSet<SQLColumn>(comparator);

		try {
			for (ColumnMapping cm : (List<ColumnMapping>) r1.getChildren()) {
				sourceColPk.add(cm.getPkColumn());
				sourceColFk.add(cm.getFkColumn());
			}
		} catch (ArchitectException e) {
			logger
					.debug("The source columnMapping has no PK Columns!  Shouldn't happen!"
							+ e);
		}

		try {
			for (ColumnMapping cm : (List<ColumnMapping>) r2.getChildren()) {
				targetColPk.add(cm.getPkColumn());
				targetColFk.add(cm.getFkColumn());
			}
		} catch (ArchitectException e) {
			logger
					.debug("The target columnMapping has no PK Columns!  Shouldn't happen!"
							+ e);

		}

		result = compareColumns(sourceColPk, targetColPk);
		if (result != 0)
			return result;

		result = compareColumns(sourceColFk, targetColFk);
		if (result != 0)
			return result;

		return 0;
	}

	public int compareColumns(Set<SQLColumn> source, Set<SQLColumn> target) {

		Iterator sourceIter = source.iterator();
		Iterator targetIter = target.iterator();
		SQLColumn targetColumn;
		SQLColumn sourceColumn;
		boolean sourceContinue;
		boolean targetContinue;

		//Checks if both lists of tables contain any columns at all, if they do
		//the iterator is initialized for the list
		do {

			if (sourceIter.hasNext()) {
				sourceContinue = true;
				sourceColumn = (SQLColumn) sourceIter.next();
			} else {
				sourceContinue = false;
				sourceColumn = null;
			}

			if (targetIter.hasNext()) {
				targetContinue = true;
				targetColumn = (SQLColumn) targetIter.next();
			} else {
				targetContinue = false;
				targetColumn = null;
			}

			int result = comparator.compare(sourceColumn, targetColumn);

			if (result != 0)
				return result;

		} while (sourceContinue || targetContinue);

		return 0;
	}

}
