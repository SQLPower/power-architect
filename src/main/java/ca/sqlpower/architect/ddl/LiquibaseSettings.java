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
 
package ca.sqlpower.architect.ddl;

/**
 *
 * @author Thomas Kellerer
 */
public class LiquibaseSettings {

	private boolean separateChangesets;
	private boolean generateID;
	private String author;
	private int idStart = 1;
	private boolean useAddPKTagForSingleColumns = true;

	public LiquibaseSettings() {
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean getGenerateId() {
		return generateID;
	}

	public void setGenerateId(boolean generateID) {
		this.generateID = generateID;
	}

	public int getIdStart() {
		return idStart;
	}

	public void setIdStart(int idStart) {
		this.idStart = idStart;
	}

	public boolean getUseSeparateChangeSets() {
		return separateChangesets;
	}

	public void setUseSeparateChangeSets(boolean separateChangesets) {
		this.separateChangesets = separateChangesets;
	}

	public boolean getUseAddPKTagForSingleColumns() {
		return useAddPKTagForSingleColumns;
	}

	public void setUseAddPKTagForSingleColumns(boolean flag) {
		useAddPKTagForSingleColumns = flag;
	}



}
