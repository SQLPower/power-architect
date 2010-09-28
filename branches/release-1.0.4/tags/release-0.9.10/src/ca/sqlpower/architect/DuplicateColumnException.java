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
package ca.sqlpower.architect;

/**
 * The DuplicateColumnException represents a failure to add a column
 * to a table because the table already has a column with that name.
 */
public class DuplicateColumnException extends ArchitectException implements java.io.Serializable {

	SQLTable table;
    String dupColName;

	public DuplicateColumnException(SQLTable table, String dupColName) {
		super(table.getName()+" already has a column named "+dupColName);
			this.table = table;
		this.dupColName = dupColName;
	}

	/**
	 * Gets the value of table
	 *
	 * @return the value of table
	 */
	public SQLTable getTable()  {
		return this.table;
	}

	/**
	 * Sets the value of table
	 *
	 * @param argTable Value to assign to this.table
	 */
	public void setTable(SQLTable argTable) {
		this.table = argTable;
	}

	/**
	 * Gets the value of dupColName
	 *
	 * @return the value of dupColName
	 */
	public String getDupColName()  {
		return this.dupColName;
	}

	/**
	 * Sets the value of dupColName
	 *
	 * @param argDupColName Value to assign to this.dupColName
	 */
	public void setDupColName(String argDupColName) {
		this.dupColName = argDupColName;
	}

}
