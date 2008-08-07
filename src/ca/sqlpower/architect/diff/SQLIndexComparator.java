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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;

/**
 * A comparator class used for SQLIndex comparisons. It will not distinguish
 * between a rename or drop/add of an index's column. Also, a null value for
 * {@link SQLIndex.Column#getAscendingOrDescending} in a column will be treated
 * to be the same as {@link AscendDescend#UNSPECIFIED}.
 * 
 */
public class SQLIndexComparator implements Comparator<SQLIndex> {

	private static Logger logger = Logger.getLogger(SQLIndexComparator.class);

	SQLObjectComparator comparator = new SQLObjectComparator();

	public int compare(SQLIndex source, SQLIndex target) {
		if (source == target) {
			return 0;
		} else if (source == null) {
			return -1;
		} else if (target == null) {
			return 1;
		}

		int result = compareString(source.getQualifier(), target.getQualifier());
		if (result != 0) return result;
		
        result = compareString(source.getType(), target.getType());
        if (result != 0) return result;
		
        result = compareString(source.getFilterCondition(), target.getFilterCondition());
        if (result != 0) return result;
        
        Boolean sourceVal = Boolean.valueOf(source.isUnique());
        result = sourceVal.compareTo(target.isUnique());
        if (result != 0) return result;
        
        sourceVal = Boolean.valueOf(source.isClustered());
        result = sourceVal.compareTo(target.isClustered());
        if (result != 0) return result;
        
        sourceVal = Boolean.valueOf(source.isPrimaryKeyIndex());
        result = sourceVal.compareTo(target.isPrimaryKeyIndex());
        if (result != 0) return result;

		Set<Column> sourceCol = new TreeSet<Column>(comparator);
		Set<Column> targetCol = new TreeSet<Column>(comparator);

		try {
            sourceCol.addAll(source.getChildren());
        } catch (ArchitectException e) {
            logger.warn("Source index has no columns!");
        }
        try {
            targetCol.addAll(target.getChildren());
        } catch (ArchitectException e) {
            logger.warn("Target index has no columns!");
        }

		result = compareColumns(sourceCol, targetCol);
		if (result != 0)
			return result;

		return 0;
	}

	/**
     * Compares the given sets of Columns on name and ascending/descending. A
     * null ascending/descending value is taken as UNSPECIFIED.
     * 
     * @param source The "left side" for the comparison.
     * @param target The "right side" for the comparison.
     */
	public int compareColumns(Set<Column> source, Set<Column> target) {
		Iterator<Column> sourceIter = source.iterator();
		Iterator<Column> targetIter = target.iterator();
		Column sourceColumn;
		Column targetColumn;
		boolean sourceContinue;
		boolean targetContinue;

		// Checks if both lists of tables contain any columns at all, if they do
		// the iterator is initialized for the list.
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
			
			int result = comparator.compare(sourceColumn, targetColumn);
			if (result != 0) return result;

			if (sourceColumn != null && targetColumn != null) {
			    AscendDescend sourceVal = sourceColumn.getAscendingOrDescending();
			    if (sourceVal == null) {
			        sourceVal = AscendDescend.UNSPECIFIED;
			    }
			    AscendDescend targetVal = targetColumn.getAscendingOrDescending();
			    if (targetVal == null) {
			        targetVal = AscendDescend.UNSPECIFIED;
			    }
			    result = sourceVal.compareTo(targetVal);
			    if (result != 0) return result;
			}

		} while (sourceContinue || targetContinue);

		return 0;
	}

	/**
	 * Performs the String.compareTo() but with null checks as well.
	 * 
	 * @param source The "left side" for the comparison.
	 * @param target The "right side" for the comparison.
	 */
	public int compareString(String source, String target) {
	    if (source == target) {
            return 0;
        } else if (source == null) {
            return -1;
        } else if (target == null) {
            return 1;
        } else {
            return source.compareTo(target);
        }
	}
}
