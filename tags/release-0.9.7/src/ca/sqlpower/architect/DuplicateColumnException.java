/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
