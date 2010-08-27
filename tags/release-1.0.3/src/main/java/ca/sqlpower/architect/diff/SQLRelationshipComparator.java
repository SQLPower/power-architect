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
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObjectUUIDComparator;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;

public class SQLRelationshipComparator implements Comparator<SQLRelationship> {

	private static Logger logger = Logger.getLogger(SQLRelationshipComparator.class);

	private SQLObjectComparator nameComparator = new SQLObjectComparator();
    private SPObjectUUIDComparator<SQLObject> uuidComparator = 
        new SPObjectUUIDComparator<SQLObject>();
	private boolean useUUID;

	public SQLRelationshipComparator() {
		this(false);
	}

	public SQLRelationshipComparator(boolean compareWithUUID) {
		useUUID = compareWithUUID;
	}
    
	public int compare(SQLRelationship r1, SQLRelationship r2) {
		if (r1 == r2)
			return 0;
		else if (r1 == null)
			return -1;
		else if (r2 == null)
			return 1;

		Comparator<SQLObject> comparatorToUse = (useUUID ? uuidComparator : nameComparator);
		
		//Making sure that the PKTables are the same, else return value
		int result = comparatorToUse.compare(r1.getPkTable(), r2.getPkTable());
		if (result != 0)
			return result;
		
		//Making sure that the FKTables are the same, else return value
		result = comparatorToUse.compare(r1.getFkTable(), r2.getFkTable());
		if (result != 0)
			return result;

		Set<SQLColumn> sourceColPk = new TreeSet<SQLColumn>(comparatorToUse);
		Set<SQLColumn> targetColPk = new TreeSet<SQLColumn>(comparatorToUse);
		Set<SQLColumn> sourceColFk = new TreeSet<SQLColumn>(comparatorToUse);
		Set<SQLColumn> targetColFk = new TreeSet<SQLColumn>(comparatorToUse);

		for (ColumnMapping cm : r1.getChildren(ColumnMapping.class)) {
        	sourceColPk.add(cm.getPkColumn());
        	sourceColFk.add(cm.getFkColumn());
        }

		for (ColumnMapping cm : r2.getChildren(ColumnMapping.class)) {
        	targetColPk.add(cm.getPkColumn());
        	targetColFk.add(cm.getFkColumn());
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

		Iterator<SQLColumn> sourceIter = source.iterator();
		Iterator<SQLColumn> targetIter = target.iterator();
		SQLColumn targetColumn;
		SQLColumn sourceColumn;
		boolean sourceContinue;
		boolean targetContinue;

		//Checks if both lists of tables contain any columns at all, if they do
		//the iterator is initialized for the list
		do {

			if (sourceIter.hasNext()) {
				sourceContinue = true;
				sourceColumn = sourceIter.next();
			} else {
				sourceContinue = false;
				sourceColumn = null;
			}

			if (targetIter.hasNext()) {
				targetContinue = true;
				targetColumn = targetIter.next();
			} else {
				targetContinue = false;
				targetColumn = null;
			}

			int result = nameComparator.compare(sourceColumn, targetColumn);

			if (result != 0)
				return result;

		} while (sourceContinue || targetContinue);

		return 0;
	}

}
